package com.atguigu.gmall.manage.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;
import util.RandomUtil;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        // 处理默认图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        if (CollectionUtils.isEmpty(skuImageList)) {
            return "请设置默认图片后再提交";
        }

        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isEmpty(skuDefaultImg)) {
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
            pmsSkuInfo.getSkuImageList().get(0).setIsDefault("1");
        }

        // 将 spuId 封装给 productId
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());


        // 插入sku信息
        int count = pmsSkuInfoMapper.insert(pmsSkuInfo);
        String pmsSkuInfoId = pmsSkuInfo.getId();

        // 插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                pmsSkuAttrValue.setSkuId(pmsSkuInfoId);
                pmsSkuAttrValueMapper.insert(pmsSkuAttrValue);
            }
        }

        // 插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                pmsSkuSaleAttrValue.setSkuId(pmsSkuInfoId);
                pmsSkuSaleAttrValueMapper.insert(pmsSkuSaleAttrValue);
            }
        }

        // 插入图片信息
        if (!CollectionUtils.isEmpty(skuImageList)) {
            for (PmsSkuImage pmsSkuImage : skuImageList) {
                pmsSkuImage.setProductImgId(pmsSkuImage.getSpuImgId());
                pmsSkuImage.setSkuId(pmsSkuInfoId);
                pmsSkuImageMapper.insert(pmsSkuImage);
            }
        }
        return "success";
    }

    /*@Override
    public PmsSkuInfo getSkuById(String skuId) {
        // 查询 sku 信息
        Example example = new Example(PmsSkuInfo.class);
        example.createCriteria().andEqualTo("id",skuId);
        PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectOneByExample(example);

        // 查询图片列表并封装
        Example imgExample = new Example(PmsSkuImage.class);
        imgExample.createCriteria().andEqualTo("skuId",skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.selectByExample(imgExample);
        pmsSkuInfo.setSkuImageList(pmsSkuImages);
        return pmsSkuInfo;
    }*/

    @Override
    public PmsSkuInfo getSkuById(String skuId, String ip) {
        log.info("getSkuById by ip：{} 线程：{} 进入商品详情的请求", ip, Thread.currentThread().getName());

        PmsSkuInfo pmsSkuInfo;

        // 查询 redis
        Jedis jedis = redisUtil.getJedis();
        String skuKey = RedisConstant.PRE_SKU + skuId + RedisConstant.POST_INFO;
        String skuInfoJson = jedis.get(skuKey);

        // redis 未命中，则查询 mysql
        if (StringUtils.isEmpty(skuInfoJson)) {

            log.info("getSkuById by ip：{} 线程：{} 发现缓冲中没有，从数据库中查询数据，申请分布式锁id：{}",
                    ip, Thread.currentThread().getName(), RedisConstant.PRE_SKU + skuId + RedisConstant.POST_LOCK);

            // 设置分布式锁，防止缓存击穿，一次只允许一个请求查数据库，锁的过期时间为 10 秒
            // 将锁的值存储为 uuid，防止因线程过期误删除其他人的锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set(RedisConstant.PRE_SKU + skuId + RedisConstant.POST_LOCK, token, "nx", "px", 10 * 1000L);
            if (StringUtils.isNotBlank(OK) && "OK".equals(OK)) {

                log.info("getSkuById by ip：{} 线程：{} 成功拿到锁，可在 10 秒内访问数据库：{} value：{}",
                        ip, Thread.currentThread().getName(), RedisConstant.PRE_SKU + skuId + RedisConstant.POST_LOCK, token);

                // 睡眠做高并发测试
                /*try {
                    Thread.sleep(5 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                pmsSkuInfo = getPmsSkuInfoFromDB(skuId, jedis);

                log.info("getSkuById by ip：{} 线程：{} 数据库成功访问，将锁归还：{} value：{}",
                        ip, Thread.currentThread().getName(), RedisConstant.PRE_SKU + skuId + RedisConstant.POST_LOCK, token);

                // 在访问 mysql 后，将分布式锁释放
                // 用 token 确认删除的是自己的 sku 的锁
                /*if (token.equals(jedis.get(RedisConstant.SKU + skuId + RedisConstant.LOCK))){
                    jedis.del(RedisConstant.SKU + skuId + RedisConstant.LOCK);
                }*/
                // 改为 lua 表达式，将判断和删除一次性传递组 redis 客户端处理
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList(RedisConstant.PRE_SKU + skuId + RedisConstant.POST_LOCK), Collections.singletonList(token));

                return pmsSkuInfo;
            } else {
                log.info("getSkuById by ip：{} 线程：{} 没有拿到锁，开始自旋：{}",
                        ip, Thread.currentThread().getName(), RedisConstant.PRE_SKU + skuId + RedisConstant.POST_LOCK);

                // 设置分布式锁失败，自旋（在该线程睡眠1秒后，重新尝试访问该方法）
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 释放连接
                jedis.close();

                return getSkuById(skuId, ip);
            }
        }

        log.info("getSkuById by ip：{} 线程：{} 从缓存中获取商品详情",
                ip, Thread.currentThread().getName());

        if (StringUtils.isBlank(skuInfoJson)) {
            return new PmsSkuInfo();
        }
        pmsSkuInfo = JSON.parseObject(skuInfoJson, PmsSkuInfo.class);
        return pmsSkuInfo;
    }

    /**
     * 通过数据库查询 pmsSkuInfo
     *
     * @param skuId
     * @param jedis
     * @return
     */
    private PmsSkuInfo getPmsSkuInfoFromDB(String skuId, Jedis jedis) {
        PmsSkuInfo pmsSkuInfo;
        // 查询 sku 信息
        Example example = new Example(PmsSkuInfo.class);
        example.createCriteria().andEqualTo("id", skuId);
        pmsSkuInfo = pmsSkuInfoMapper.selectOneByExample(example);

        if (pmsSkuInfo == null) {
            // 如果数据库中数据不存在，则依然将一个空格添加到缓存中,防止缓存穿透问题
            // 过期时间设置为 5-6 分钟，防止缓存雪崩问题
            Random random = RandomUtil.getRandom();
            int anInt = random.nextInt(61) + 300;
            jedis.setex(RedisConstant.PRE_SKU + skuId + RedisConstant.POST_INFO, anInt, " ");
            return new PmsSkuInfo();
        }

        // 查询图片列表并封装
        Example imgExample = new Example(PmsSkuImage.class);
        imgExample.createCriteria().andEqualTo("skuId", skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.selectByExample(imgExample);
        pmsSkuInfo.setSkuImageList(pmsSkuImages);

        // mysql 查询结果存入 redis
        // 随机过期时间为 5-6 分钟，防止缓存雪崩问题
        Random random = RandomUtil.getRandom();
        int anInt = random.nextInt(61) + 300;
        jedis.setex(RedisConstant.PRE_SKU + skuId + RedisConstant.POST_INFO, anInt, JSON.toJSONString(pmsSkuInfo));
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        return pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
    }

    /**
     * 查询所有 sku 属性
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            Example pmsSkuAttrValueExample = new Example(PmsSkuAttrValue.class);
            pmsSkuAttrValueExample.createCriteria().andEqualTo("skuId", skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.selectByExample(pmsSkuAttrValueExample);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuSaleAttrValue> getSaleAttrValueListBySkuId(String skuId) {
        Example example = new Example(PmsSkuSaleAttrValue.class);
        example.createCriteria().andEqualTo("skuId",skuId);
        return pmsSkuSaleAttrValueMapper.selectByExample(example);
    }
}
