package com.example.flowlimiter.controller;

import com.example.flowlimiter.util.RedisCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
public class FlowLimitController {

    private static final String FLOW_LIMIT_KEY = "flow:limit:key";
    @Resource
    RedisCell redisCell;

    @Value("${limit.maxBurst}")
    Integer maxBurst;

    @Value("${limit.tokens}")
    Integer tokens;

    @Value("${limit.seconds}")
    Integer seconds;

    @Value("${limit.requestOneTime}")
    Integer requestOneTime;

    @RequestMapping("/order")
    public void order() throws InterruptedException {
        //每次请求之前，先需要获取一个令牌
        boolean rsp = redisCell.throttle(FLOW_LIMIT_KEY, maxBurst, tokens, seconds, requestOneTime);
        if (rsp) {
            //获取得到令牌，继续执行业务流程
            TimeUnit.SECONDS.sleep(1);

        } else {
            //获取令牌失败，拒绝请求

        }
    }
}
