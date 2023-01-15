package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMemberReceiveAddress> getMemberReceiveAddressByMemberId(String memberId);
}
