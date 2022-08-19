package com.wulingren.spider.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import com.alibaba.fastjson.JSON;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;


/**
 * @program: geek_time_spider
 * @description:
 * @packagename: com.wulingren.spider.handler
 * @author: cil
 * @date: 2022/08/18 18:34
 **/
@Slf4j
public class GeekTimeHandler {
    private RestTemplate restTemplate = new RestTemplate();
    private final String COOKIE = "your cookie";
    private final String articleUrl = "https://time.geekbang.org/serv/v1/article";
    private final String productInfoUrl = "https://time.geekbang.org/serv/v3/column/info";
    private final String basePath = "/Users/wulingren/Desktop/geektime/";
    private String titleDir = "/";

    private HttpHeaders headers = new HttpHeaders();

    {
        headers.add("Content-Type", "application/json");
        headers.add("Host", "time.geekbang.org");
        headers.add("Cookie", COOKIE);
        headers.add("Connection", "keep-alive");
        headers.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.1 Safari/605.1.15");

    }

    public void spider(List<Long> products) throws InterruptedException {
        for (Long pid : products) {
            spiderAll(getFirstAid(pid));
        }
    }

    /**
     * 用课程id获取文章id
     *
     * @param productId 课程id
     * @return
     */
    private Long getFirstAid(Long productId) {
        log.info("productId: {}", productId);
        //爬！
        headers.set("Referer", "https://time.geekbang.org/column/intro/" + productId + "?tab=catalog");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("product_id", productId);
        jsonObject.put("with_recommend_article", true);

        JSONObject jsonObjectResponseEntity = sendRequestJSON(productInfoUrl, jsonObject);
        JSONObject data = jsonObjectResponseEntity.getJSONObject("data");

        titleDir = basePath + data.getString("title");

        Long aid = data.getJSONObject("article").getLong("id");
        return aid;

    }

    /**
     * 递归爬
     *
     * @param firstAid
     */
    private void spiderAll(Long firstAid) throws InterruptedException {
        headers.set("Referer", "https://time.geekbang.org/column/article/" + firstAid);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", firstAid);
        jsonObject.put("include_neighbors", true);
        jsonObject.put("is_freelyread", true);

        JSONObject jsonObjectResponseEntity = sendRequestJSON(articleUrl, jsonObject);
        JSONObject data = jsonObjectResponseEntity.getJSONObject("data");
        String fileName = data.getString("article_title");
        saveTxt(titleDir, fileName + ".md", data.getString("article_content"));
        try {
            JSONObject right = data.getJSONObject("neighbors").getJSONObject("right");
            if (right.containsKey("id")) {
                Thread.sleep((long) (2000 + Math.random()));
                spiderAll(right.getLong("id"));
            }
        } catch (ClassCastException e) {

        }
    }

    /**
     * 写文件
     * @param filePath
     * @param fileName
     * @param data
     */
    public static void saveTxt(String filePath, String fileName, String data) {
        try {
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(filePath + "/" + fileName);
            FileUtils.writeStringToFile(file, data, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("error");
        }
    }




    /**
     * 发送请求
     *
     * @param url
     * @param body
     * @return
     */
    public JSONObject sendRequestJSON(String url, JSON body) {
        //HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(body.toString(), headers);
        //post
        //需要用string转一下
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        return JSONObject.parseObject(responseEntity.getBody());
    }


}
