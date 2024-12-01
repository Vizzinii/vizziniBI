package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class SparkManagerTest {

    @Resource
    private SparkManager sparkManager;


    private final String userInput =
            "分析需求：\n" +
                    "分析网站用户的增长情况\n" +
                    "请使用：折线图\n" +
                    "原始数据：\n" +
                    "日期，用户数\n" +
                    "1号,10 \n" +
                    "2号,20\n" +
                    "3号,30";

    @Test
    public void testApi() {
        String result = sparkManager.sendHttpTOSpark(userInput);
        System.out.println(result);
    }
}

