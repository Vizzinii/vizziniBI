package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.FileConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.RedissonManager;
import com.yupi.springbootinit.manager.SparkManager;
import com.yupi.springbootinit.model.dto.chart.*;

import com.yupi.springbootinit.model.dto.file.UploadFileRequest;
import com.yupi.springbootinit.model.entity.Chart;

import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.FileUploadBizEnum;
import com.yupi.springbootinit.model.vo.BIResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yupi.springbootinit.utils.ExtelUtils.excelToCsv;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 *  
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private SparkManager sparkManager;

    @Resource
    private RedissonManager redissonManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest postQueryRequest,
                                                       HttpServletRequest request) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(postQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                         HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPost(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String charType = chartQueryRequest.getCharType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id> 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(charType), "charType", charType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
//        queryWrapper.eq(StringUtils.isNotBlank(sortField), "sortField", sortField);
//        queryWrapper.eq(StringUtils.isNotBlank(sortOrder), "sortOrder", sortOrder);


        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }



    /**
     * 图表智能分析（同步）
     *
     * @param multipartFile
     * @param chartAutoAnalysisRequest
     * @param request
     * @return
     */
    @PostMapping("/analysis")
    public BaseResponse<BIResponse> ChartAutoAnalysis(@RequestPart("file") MultipartFile multipartFile,
                                                      ChartAutoAnalysisRequest chartAutoAnalysisRequest, HttpServletRequest request) {

        // 读取用户信息，确保BI平台必须登录使用
        User loginUser = userService.getLoginUser(request);

        // 获取web端输入的信息
        String name = chartAutoAnalysisRequest.getName();
        String goal = chartAutoAnalysisRequest.getGoal();
        String chartType = chartAutoAnalysisRequest.getChartType();

        // 校验
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100 ,ErrorCode.PARAMS_ERROR,"图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标为空");

        // 校验文件大小与名称
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR,"上传文件过大");

        // 校验文件类型（当前安全性仍较低）
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式错误");

        // 限流判断（这里是限制每个用户访问当前方法的次数，不影响用户访问其它方法）
        redissonManager.doRateLimit("ChartAutoAnalysis_" + loginUser.getId());

        // 读取用户输入的分析目标
        StringBuilder userInput = new StringBuilder();
        //userInput.append("你是一位数据分析师。接下来我会给你我的分析目标和原始数据，请你告诉我你的分析结论。").append("\n"); //预设已经在manager里实现

        // 首先填充分析需求
        userInput.append("分析需求：").append("\n");
        userInput.append(goal).append("\n");

        // 其次填充需要生成的表类型，用户没有输入时默认生成折线图

        userInput.append("生成图表类型：").append("\n");
        userInput.append(chartType).append("\n");


        // 再填充原始数据
        userInput.append("原始数据：").append("\n");
        String result = excelToCsv(multipartFile);
        userInput.append(result).append("\n");

        // 读取用户上传的文件
        //String result = excelToCsv(multipartFile);
        //userInput.append("我的数据是：").append(result).append("\n");
        //return ResultUtils.success(userInput.toString());

        // 发送 http 请求
        String juice = sparkManager.sendHttpTOSpark(userInput.toString());

        // 拆分返回结果
        String[] splits = juice.split("【【【【【");
        if(splits.length < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成格式错误，建议重新问一遍");
        }
        // 返回结果已经用 【【【【【 分割成两段，其中第一段是 JSON 代码，第二段是分析结论
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        // 把数据插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setChartData(result); //csv数据
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.OPERATION_ERROR,"图表信息保存失败");
        // TODO 把每一次查询的原始数据表格 单独用一个表来插入，而不是把原始数据作为chart表的字段
        // 解决方案：分库分表
        // （因为单个用户上传的文件过大可能导致所有用户在查询表格时，都需要读取该表项，进而降低整体的查询开销）
        // 分开存储：用每个chart唯一的id来给每个图表取名，降低查询开销
        // 分开查询：之前是直接查询 chart 表取 chartData 字段，分表之后是读取每个chart 单独的 chart_{id} 数据表（用Mybatis的动态Sql实现）


        // 把返回结果封装到vo里，返回给前端
        BIResponse biResponse = new BIResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());

        return ResultUtils.success(biResponse);
//
//        User loginUser = userService.getLoginUser(request);
//        // 文件目录：根据业务、用户来划分
//        String uuid = RandomStringUtils.randomAlphanumeric(8);
//        String filename = uuid + "-" + multipartFile.getOriginalFilename();
//
//        File file = null;
//        try {
//
//            // 返回可访问地址
//            return ResultUtils.success("");
//        } catch (Exception e) {
////            log.error("file upload error, filepath = " + filepath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        } finally {
//            if (file != null) {
//                // 删除临时文件
//                boolean delete = file.delete();
//                if (!delete) {
////                    log.error("file delete error, filepath = {}", filepath);
//                }
//            }
//        }
    }



    /**
     * 图表智能分析（异步）
     *
     * @param multipartFile
     * @param chartAutoAnalysisRequest
     * @param request
     * @return
     */
    @PostMapping("/analysis/async")
    public BaseResponse<BIResponse> ChartAutoAnalysisAsync(@RequestPart("filchartAutoAnalysisAsynce") MultipartFile multipartFile,
                                                  ChartAutoAnalysisRequest chartAutoAnalysisRequest, HttpServletRequest request) {

        // 读取用户信息，确保BI平台必须登录使用
        User loginUser = userService.getLoginUser(request);

        // 获取web端输入的信息
        String name = chartAutoAnalysisRequest.getName();
        String goal = chartAutoAnalysisRequest.getGoal();
        String chartType = chartAutoAnalysisRequest.getChartType();

        // 校验
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100 ,ErrorCode.PARAMS_ERROR,"图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标为空");

        // 校验文件大小与名称
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR,"上传文件过大");

        // 校验文件类型（当前安全性仍较低）
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式错误");

        // 限流判断（这里是限制每个用户访问当前方法的次数，不影响用户访问其它方法）
        redissonManager.doRateLimit("ChartAutoAnalysis_" + loginUser.getId());

        // 读取用户输入的分析目标
        StringBuilder userInput = new StringBuilder();
        //userInput.append("你是一位数据分析师。接下来我会给你我的分析目标和原始数据，请你告诉我你的分析结论。").append("\n"); //预设已经在manager里实现

        // 首先填充分析需求
        userInput.append("分析需求：").append("\n");
        userInput.append(goal).append("\n");

        // 其次填充需要生成的表类型，用户没有输入时默认生成折线图

            userInput.append("生成图表类型：").append("\n");
            userInput.append(chartType).append("\n");


        // 再填充原始数据
        userInput.append("原始数据：").append("\n");
        String result = excelToCsv(multipartFile);
        userInput.append(result).append("\n");

        // 读取用户上传的文件
        //String result = excelToCsv(multipartFile);
        //userInput.append("我的数据是：").append(result).append("\n");
        //return ResultUtils.success(userInput.toString());

        // 把数据插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setChartData(result); //csv数据
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.OPERATION_ERROR,"Fail to 图表信息保存失败");

        // 加入异步化逻辑，用线程工厂和线程来控制提交生成的操作
        CompletableFuture.runAsync(() -> {

            // 修改图表状态为“执行中”，执行结束后更改为“执行成功”或“执行失败”
            // 这一操作的主要目的是为了防止任务的重复提交和执行
            Chart updatechart = new Chart();
            updatechart.setId(chart.getId());
            updatechart.setStatus("running");
            boolean update = chartService.updateById(updatechart);
            if (!update) {
                updatechart.setStatus("failed");
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"Fail to 更改图表状态为running");
            }

            // 发送 http 请求
            String juice = sparkManager.sendHttpTOSpark(userInput.toString());

            // 拆分返回结果
            String[] splits = juice.split("【【【【【");
            if(splits.length < 2){
                updatechart.setStatus("failed");
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成格式错误，建议重新问一遍");
            }
            // 返回结果已经用 【【【【【 分割成两段，其中第一段是 JSON 代码，第二段是分析结论
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();

            // 把最后的结果再写回数据库
            Chart updatechartResult = new Chart();
            updatechartResult.setId(chart.getId());
            updatechartResult.setStatus("succeed");
            updatechartResult.setGenChart(genChart);
            updatechartResult.setGenResult(genResult);
            boolean updateResult = chartService.updateById(updatechartResult);
            if (!updateResult) {
                updatechartResult.setStatus("failed");
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"Fail to 更改图表状态为succeed");
            }

        } , threadPoolExecutor);

        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);






//        chart.setName(name);
//        chart.setGoal(goal);
//        chart.setChartType(chartType);
//        chart.setChartData(result); //csv数据
//        chart.setGenChart(genChart);
//        chart.setGenResult(genResult);
//        chart.setUserId(loginUser.getId());
//        boolean saveResult = chartService.save(chart);
//        ThrowUtils.throwIf(!saveResult,ErrorCode.OPERATION_ERROR,"图表信息保存失败");
        // TODO 把每一次查询的原始数据表格 单独用一个表来插入，而不是把原始数据作为chart表的字段
        // 解决方案：分库分表
        // （因为单个用户上传的文件过大可能导致所有用户在查询表格时，都需要读取该表项，进而降低整体的查询开销）
        // 分开存储：用每个chart唯一的id来给每个图表取名，降低查询开销
        // 分开查询：之前是直接查询 chart 表取 chartData 字段，分表之后是读取每个chart 单独的 chart_{id} 数据表（用Mybatis的动态Sql实现）


        // 把返回结果封装到vo里，返回给前端
//        BIResponse biResponse = new BIResponse();
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
//        biResponse.setChartId(chart.getId());
//
//        return ResultUtils.success(biResponse);
//
//        User loginUser = userService.getLoginUser(request);
//        // 文件目录：根据业务、用户来划分
//        String uuid = RandomStringUtils.randomAlphanumeric(8);
//        String filename = uuid + "-" + multipartFile.getOriginalFilename();
//
//        File file = null;
//        try {
//
//            // 返回可访问地址
//            return ResultUtils.success("");
//        } catch (Exception e) {
////            log.error("file upload error, filepath = " + filepath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        } finally {
//            if (file != null) {
//                // 删除临时文件
//                boolean delete = file.delete();
//                if (!delete) {
////                    log.error("file delete error, filepath = {}", filepath);
//                }
//            }
//        }
    }
}