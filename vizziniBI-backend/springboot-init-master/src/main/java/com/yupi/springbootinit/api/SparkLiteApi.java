package com.yupi.springbootinit.api;


import cn.hutool.http.HttpRequest;
import okhttp3.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

public class SparkLiteApi {
    public static void main(String[] args) {
//        OkHttpClient client = new OkHttpClient();
//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//        String url = "https://spark-api-open.xf-yun.com/v1/chat/completions";
//        RequestBody body = RequestBody.create(JSON, "{\"model\":\"generalv3.5\",\"messages\":[{\"role\":\"user\",\"content\":\"来一个只有程序员能听懂的笑话\"}],\"stream\":true}");
//        Request request = new Request.Builder()
//                .url("https://spark-api-open.xf-yun.com/v1/chat/completions")
//                        .post((okhttp3.RequestBody) body)
//                                .addHeader("Authorization","uiEhigRPVfEizOCjJQZX:SomyiTcUeMlurgJFcvqD")
//                                        .addHeader("Content-Type","application/json")
//                                                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                e.printStackTrace();
//            }
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                System.out.println(response.body().string());
//            }
//        });

//        HttpRequest.post(url)
//                .header("Authorization","54849561cefcd910fd375df1686b66e4")

//
//        try (CloseableHttpClient client = HttpClients.createDefault()) {
//            HttpPost httpPost = new HttpPost("https://spark-api-open.xf-yun.com/v1/chat/completions");
//            httpPost.setHeader("Content-Type", "application/json");
//            httpPost.setHeader("Authorization", "Bearer uiEhigRPVfEizOCjJQZX:SomyiTcUeMlurgJFcvqD");
//
//            // 构建请求体
//            String jsonBody = "{\\\"model\\\":\\\"generalv1.1\\\",\\\"user\\\":\\\"fba7d293\\\",\\\"messages\\\":[{\\\"role\\\":\\\"user\\\",\\\"content\\\":\\\"来一个只有程序员能听懂的笑话\\\"}],\\\"stream\\\":true}";
//            StringEntity entity = new StringEntity(jsonBody);
//            httpPost.setEntity(entity);
//
//            // 发送请求并接收响应
//            HttpResponse response = client.execute(httpPost);
//            String result = EntityUtils.toString(response.getEntity());
//            System.out.println(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    }
}
