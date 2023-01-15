package com.atguigu.gmall.redisson.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.atguigu.gmall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
@Slf4j
public class RedissonController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedissonClient redissonClient;

    @ResponseBody
    @RequestMapping("testRedisson")
    public String testRedisson() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock();

        try (Jedis jedis = redisUtil.getJedis()) {
            String v = jedis.get("k");
            if (StringUtils.isBlank(v)) {
                v = "1";
            }
            log.info("v：{}", v);
            jedis.set("k", (Integer.parseInt(v) + 1) + "");
        } finally {
            lock.unlock();
        }
        return "success";
    }
}
