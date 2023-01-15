package com.atguigu.gmall.user.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.util.RedisUtil;
import constant.RedisConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(Long memberId) {
        Example example = new Example(UmsMemberReceiveAddress.class);
        example.createCriteria().andEqualTo("memberId", memberId);
        return umsMemberReceiveAddressMapper.selectByExample(example);
    }

    /**
     * 登录方法，判断传入的 username 和 password 是否有效
     *
     * @param umsMember 包含 username 和 password 用于验证的信息
     * @return 如果有效，则返回用户信息，如果无效，则返回 null
     */
    @Override
    public UmsMember login(UmsMember umsMember) {
        UmsMember umsMemberLogin = null;

        if (StringUtils.isNotBlank(umsMember.getUsername()) && StringUtils.isNotBlank(umsMember.getPassword())) {
            Jedis jedis = null;
            try {
                jedis = redisUtil.getJedis();
                String password = jedis.get(RedisConstant.PRE_USER + umsMember.getUsername() + RedisConstant.POST_PASSWORD);
                if (StringUtils.isNotBlank(password)) {
                    if (password.equals(umsMember.getPassword())) {
                        String umsInfo = jedis.get(RedisConstant.PRE_USER + umsMember.getUsername() + RedisConstant.POST_INFO);
                        if (StringUtils.isNotBlank(umsInfo)) {
                            umsMemberLogin = JSON.parseObject(umsInfo, UmsMember.class);
                        }
                    } else {
                        return null;
                    }
                }

                if (umsMemberLogin == null) {
                    umsMemberLogin = getUmsMemberByUsernameAndPassword(umsMember);

                    if (umsMemberLogin != null) {
                        jedis.setex(RedisConstant.PRE_USER + umsMember.getUsername() + RedisConstant.POST_PASSWORD,
                                60 * 60 * 2,
                                umsMemberLogin.getPassword());
                        jedis.setex(RedisConstant.PRE_USER + umsMember.getUsername() + RedisConstant.POST_INFO,
                                60 * 60 * 2,
                                JSON.toJSONString(umsMemberLogin));
                    }
                }
            } catch (Exception e) {
                // 如果 redis 连接不上，直接查数据库
                umsMemberLogin = getUmsMemberByUsernameAndPassword(umsMember);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return umsMemberLogin;
    }

    @Override
    public UmsMember save(UmsMember umsMember) {
        if (umsMemberMapper.insert(umsMember) == 0) {
            return null;
        }
        return umsMember;
    }

    @Override
    public UmsMember getBySourceUid(String sourceUid) {
        Example example = new Example(UmsMember.class);
        example.createCriteria().andEqualTo("sourceUid", sourceUid);
        return umsMemberMapper.selectOneByExample(example);
    }

    @Override
    public boolean update(UmsMember umsMember) {
        return umsMemberMapper.updateByPrimaryKey(umsMember) != 0;
    }

    private UmsMember getUmsMemberByUsernameAndPassword(UmsMember umsMember) {
        UmsMember umsMemberLogin;
        Example example = new Example(UmsMember.class);
        example.createCriteria().andEqualTo("username", umsMember.getUsername())
                .andEqualTo("password", umsMember.getPassword());
        umsMemberLogin = umsMemberMapper.selectOneByExample(example);
        return umsMemberLogin;
    }

}
