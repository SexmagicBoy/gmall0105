package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OmsOrderItem implements Serializable {

    private String id;
    private String orderId;
    private String orderSn;
    private String productId;
    private String productPic;
    private String productName;
    private String productBrand;
    private String productSn;
    private BigDecimal productPrice;
    private Integer productQuantity;
    private String productSkuId;
    private String productSkuCode;
    private String productCategoryId;
    private String sp1;
    private String sp2;
    private String sp3;
    private String promotionName;
    private BigDecimal promotionAmount;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
    private String realAmount;
    private Integer giftIntegration;
    private Integer giftGrowth;
    private String productAttr;

}
