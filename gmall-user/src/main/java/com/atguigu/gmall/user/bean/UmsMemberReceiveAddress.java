package com.atguigu.gmall.user.bean;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
public class UmsMemberReceiveAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private String name;
    private String phoneNumber;
    private Integer defaultStatus;
    private String postCode;
    private String province;
    private String city;
    private String region;
    private String detailAddress;
}
