package com.yupi.springbootinit.manager;

import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置api操作
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 *
 */
@Component
@Slf4j
public class SparkManager {
    @Resource
    private SparkClient sparkClient;

    /**
     * AI生成问题的预设条件
     */
    public static final String PRECONDITION = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "分析需求：\n" +
            "{数据分析的需求或者目标}\n" +
            "生成图表类型： \n" +
            "{需要你生成 JSON 代码的图表类型} \n" +
            "原始数据：\n" +
            "{csv格式的原始数据，用,作为分隔符}\n" +
            "请根据以上两部分内容，按照以下指定格式生成内容：\n" +
            "【【【【【\n" +
            "{ 前端 Echarts v5 的 option 配置对象的 JSON 代码，不要出现任何英文双引号，合理地将数据进行可视化，不要生成任何多余的内容}\n" +
            "【【【【【\n" +
            "{数据分析结论}\n" +
            "以下是我的分析目标和原始数据：\n";

    /**
     * 向 Spark AI 发送请求
     *
     * @param content
     * @return
     */
    public String sendHttpTOSpark(final String content) {
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.systemContent(PRECONDITION));
        messages.add(SparkMessage.userContent(content));
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度，非必传，默认为2048
                //.maxTokens(2048)
                // 结果随机性，取值越高随机性越强，即相同的问题得到的不同答案的可能性越高，非必传，取值为[0,1]，默认为0.5
                //.temperature(0.2)
                // 指定请求版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();
        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        String responseContent = chatResponse.getContent();
        //log.info("Spark AI 返回的结果{}", responseContent);
        return responseContent;
    }
}

