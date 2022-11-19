package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UmsMemberService {

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(Long memberId);
}
