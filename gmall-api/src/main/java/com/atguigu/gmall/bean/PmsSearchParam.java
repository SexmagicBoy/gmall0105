package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class PmsSearchParam implements Serializable {
    private String keyword;
    private String catalog3Id;
    private String[] valueId;
}
