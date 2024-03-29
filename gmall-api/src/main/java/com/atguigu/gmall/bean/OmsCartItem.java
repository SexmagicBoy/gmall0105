package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OmsCartItem implements Serializable {

    @Id
    private String id;
    private String productId;
    private String productSkuId;
    private String memberId;
    private Integer quantity;
    private BigDecimal price;
    private String sp1;
    private String sp2;
    private String sp3;
    private String productPic;
    private String productName;
    private String productSubTitle;
    private String productSkuCode;
    private String memberNickname;
    private Date createDate;
    private Date modifyDate;
    private Integer deleteStatus;
    private String productCategoryId;
    private String productBrand;
    private String productSn;
    private String productAttr;
    private Short isChecked;
    private BigDecimal totalPrice;
}
