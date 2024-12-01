package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 *  
 */
@Data
public class ChartAutoAnalysisRequest implements Serializable {

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 业务
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}