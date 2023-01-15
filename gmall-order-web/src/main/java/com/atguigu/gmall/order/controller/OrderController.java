package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderController {
    @Reference
    private CartService cartService;

    @Reference
    private UserService userService;

    @RequestMapping("toTrade")
    @LoginRequired(mustLogin = true)
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        // 将购物车集合转化为页面计算结算清单结合
        // 同时计算商品的总价格
        BigDecimal totalAmount = new BigDecimal(0);
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            if (1 == omsCartItem.getIsChecked()) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                BeanUtils.copyProperties(omsCartItem, omsOrderItem, "id");
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItem.setProductPrice(omsCartItem.getPrice());
                omsOrderItems.add(omsOrderItem);

                totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
            }
        }

        // 查询用户收获地址列表
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getMemberReceiveAddressByMemberId(memberId);

        // 在页面中添加 model
        modelMap.put("nickName", nickname);
        modelMap.put("userAddressList", umsMemberReceiveAddresses);
        modelMap.put("orderDetailList", omsOrderItems);
        modelMap.put("totalAmount", totalAmount);
        return "trade";
    }

}
