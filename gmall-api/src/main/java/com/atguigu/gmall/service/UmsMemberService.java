package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UmsMemberService {

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(Long memberId);

    UmsMember login(UmsMember umsMember);

    UmsMember save(UmsMember umsMember);

    UmsMember getBySourceUid(String sourceUid);

    boolean update(UmsMember umsMember);

}
