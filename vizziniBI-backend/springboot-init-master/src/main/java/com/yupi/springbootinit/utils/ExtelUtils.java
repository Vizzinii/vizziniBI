package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel相关工具类
 */
@Slf4j
public class ExtelUtils {

    /**
     * excel 转 csv 的功能
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile) {
//        File file = null;
//        try {
//            file= ResourceUtils.getFile("classpath:test.xlsx");
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        List<Map<Integer,String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格转换为csv发生错误",e);
        }
        if(CollUtil.isEmpty(list)){
            return "";
        }
        // 转换为csv
        StringBuilder stringBuilder = new StringBuilder();
        // 读取表头
        LinkedHashMap <Integer,String> headMap = (LinkedHashMap) list.get(0);// 读取第一行
        // 过滤为空的数据格
        List<String> headerList = headMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        // 把一行里的数据拼接起来，其间用逗号分割
        stringBuilder.append(StringUtils.join(headerList,",")).append("\n");
        System.out.println(StringUtils.join(headerList,","));
        // 读取每一行数据
        for(int i = 1; i < list.size(); i ++){
            LinkedHashMap <Integer,String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList,",")).append("\n");
            System.out.println(StringUtils.join(dataList,","));
        }
        System.out.println(list);
        return stringBuilder.toString();
    }


    public static void main(String[] args) {
        excelToCsv(null);
    }
}
