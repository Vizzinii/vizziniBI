package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;
import java.io.Serializable;

/**
 * 创建请求
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 *  
 */
@Data
public class ChartAddRequest implements Serializable {

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartDate;

    /**
     * 图表类型
     */
    private String charType;

    private static final long serialVersionUID = 1L;
}