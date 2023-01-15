package com.atguigu.gmall.cart.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import constant.RedisConstant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", memberId)
                .andEqualTo("productSkuId", skuId);
        return omsCartItemMapper.selectOneByExample(example);
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())) {
            omsCartItemMapper.insert(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDB) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria()
                .andEqualTo("id", omsCartItemFromDB.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDB, example);
    }

    @Override
    public void flushCartCache(String memberId) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.selectByExample(example);

        // 同步到 redis 缓存中
        String key = RedisConstant.PRE_USER + memberId + RedisConstant.POST_CART;
        Map<String, String> map = new HashMap<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            map.put(omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
        }
        Jedis jedis = redisUtil.getJedis();
        jedis.hmset(key, map);
        // 购物车缓存 48 小时过期
        jedis.expire(key, 60 * 60 * 48);
        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        // 尝试从 redis 中查询该登录用户的购物车信息
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String key = RedisConstant.PRE_USER + userId + RedisConstant.POST_CART;
            List<String> hvals = jedis.hvals(key);
            if (!CollectionUtils.isEmpty(hvals)) {
                for (String hval : hvals) {
                    omsCartItems.add(JSON.parseObject(hval, OmsCartItem.class));
                }
                // 更新 redis 购物车过期时间为 48 小时
                jedis.expire(key, 60 * 60 * 48);
            } else {
                // 如果 redis 中查询不到，到 DB 中查询并更新 redis
                Example example = new Example(OmsCartItem.class);
                example.createCriteria().andEqualTo("memberId", userId);
                // 按照添加日期倒序排序
                example.setOrderByClause("id DESC");
                omsCartItems = omsCartItemMapper.selectByExample(example);
                if (!CollectionUtils.isEmpty(omsCartItems)) {
                    // 添加购物车到 redis
                    Map<String, String> map = new HashMap<>();
                    for (OmsCartItem omsCartItem : omsCartItems) {
                        map.put(omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
                    }
                    jedis.hmset(key, map);
                    // 购物车缓存 48 小时过期
                    jedis.expire(key, 60 * 60 * 48);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return omsCartItems;
    }

    @Override
    public void checkCart(String memberId, String skuId, Short isChecked) {
        // 更新购物车商品的选中状态和更新时间
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setModifyDate(new Date());

        Example example = new Example(OmsCartItem.class);
        example.createCriteria()
                .andEqualTo("memberId", memberId)
                .andEqualTo("productSkuId", skuId);
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);

        // 删除 redis 中的数据
        Jedis jedis = redisUtil.getJedis();
        jedis.del(RedisConstant.PRE_USER + memberId + RedisConstant.POST_CART);
    }
}
