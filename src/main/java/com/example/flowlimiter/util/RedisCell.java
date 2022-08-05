package com.example.flowlimiter.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Component
public class RedisCell {
    @Resource
    RedisTemplate redisTemplate;

    /**
     * 利用redis-cell模块实现限流，该模块实现了令牌桶算法，利用lua脚本保证算法的原子性，由CL.THROTTLE指令实现
     * 令牌桶算法的原理是定义一个按一定速率产生token的桶，每次去桶中申请token，若桶中没有足够的token则申请失败，否则成功。
     *
     * @param key            限流的key
     * @param maxBurst       max_burst 最大的突发请求
     * @param tokens         每seconds秒添加tokens个令牌到桶中
     * @param seconds        每seconds秒添加tokens个令牌到桶中
     * @param requestOneTime 每次获取的令牌数目
     * @return
     */
    public boolean throttle(String key, Integer maxBurst, Integer tokens, Integer seconds, Integer requestOneTime) {
        try {
            DefaultRedisScript script = new DefaultRedisScript();
            script.setResultType(Long.class);
            script.setScriptText("return redis.call('cl.throttle',KEYS[1], ARGV[1], ARGV[2], ARGV[3], ARGV[4])[1]");
            Long rsp = (Long) redisTemplate.execute(script, Arrays.asList(key), maxBurst, tokens, seconds, requestOneTime);
            return rsp == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
