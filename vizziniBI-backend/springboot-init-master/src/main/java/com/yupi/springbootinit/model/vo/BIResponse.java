package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * 返回给前端的封装数据类型
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 *
 */
/**
 * 返回结果
 */
@Data
public class BIResponse {
    private String genChart ;
    private String genResult;
    private Long chartId;
}
