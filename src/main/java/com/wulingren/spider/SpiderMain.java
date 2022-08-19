package com.wulingren.spider;

import com.wulingren.spider.handler.GeekTimeHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: geek_time_spider
 * @description:
 * @packagename: com.wulingren.spider
 * @author: cil
 * @date: 2022/08/18 22:04
 **/
public class SpiderMain {
    public static void main(String[] args) throws InterruptedException {
        List<Long> products = new ArrayList<>();
        products.add(148L);
        new GeekTimeHandler().spider(products);
    }
}
