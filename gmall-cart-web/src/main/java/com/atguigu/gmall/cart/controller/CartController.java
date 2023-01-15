package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    private SkuService skuService;

    @Reference
    private CartService cartService;

    @RequestMapping("checkCart")
    @LoginRequired(mustLogin = false)
    public String checkCart(ModelMap modelMap, String skuId, Short isChecked, HttpServletRequest request, HttpServletResponse response) {
        // 初始化购物车数据
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        // 初始化购物车商品总价
        BigDecimal totalAmount = new BigDecimal(0);

        // 确定用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if (StringUtils.isNotBlank(memberId)) {
            // 用户已登录
            // 修改 DB 中的购物车商品选中状态，并删除 redis 中相关用户的购物车信息
            cartService.checkCart(memberId, skuId, isChecked);
            // 将最新的数据查出，渲染给内嵌页
            omsCartItems = cartService.cartList(memberId);
            // 计算购物车商品总价格
            totalAmount = getTotalAmount(omsCartItems);
        } else {
            // 用户未登录，修改 cookie 中的购物车商品选中状态和更新时间
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if ((StringUtils.isNotBlank(cartListCookie))) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                for (OmsCartItem omsCartItem : omsCartItems) {
                    if (skuId.equals(omsCartItem.getProductSkuId())) {
                        omsCartItem.setIsChecked(isChecked);
                        omsCartItem.setModifyDate(new Date());
                        // 重置修改后的 cookie 中的购物车数据
                        cartListCookie = JSON.toJSONString(omsCartItems);
                        CookieUtil.setCookie(request, response, "cartListCookie", cartListCookie, 60 * 60 * 72, true);
                        // 计算购物车商品总价格
                        totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
                        break;
                    }
                }
            }
        }
        modelMap.put("cartList", omsCartItems);
        modelMap.put("totalAmount", totalAmount);
        return "cartListInner";
    }

    @RequestMapping("cartList")
    @LoginRequired(mustLogin = false)
    public String cartList(ModelMap modelMap, HttpServletRequest request) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if (StringUtils.isNotBlank(memberId)) {
            // 如果用户已登录，从 Redis 或 DB 中查询登录用户的购物车列表
            omsCartItems = cartService.cartList(memberId);
        } else {
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }
        modelMap.put("cartList", omsCartItems);
        // 计算购物车中被选中的商品总价格
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);
        return "cartList";
    }

    @RequestMapping("addToCart")
    @LoginRequired(mustLogin = false)
    public String addToCart(ModelMap modelMap, String skuId, Integer quantity, HttpServletRequest request, HttpServletResponse response) {
        // 调用商品服务查询商品信息
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId, request.getRemoteAddr());

        // 将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(quantity);
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductSubTitle(pmsSkuInfo.getSkuDesc());
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setIsChecked((short) 1);
        omsCartItem.setTotalPrice(pmsSkuInfo.getPrice().multiply(BigDecimal.valueOf(quantity)));

        // 查询销售属性并封装到购物车中
        if (StringUtils.isNotBlank(pmsSkuInfo.getId())) {
            List<PmsSkuSaleAttrValue> pmsSaleAttrValues = skuService.getSaleAttrValueListBySkuId(pmsSkuInfo.getId());
            if (!CollectionUtils.isEmpty(pmsSaleAttrValues)) {
                if (pmsSaleAttrValues.size() == 1) {
                    omsCartItem.setSp1(pmsSaleAttrValues.get(0).getSaleAttrValueName());
                } else if (pmsSaleAttrValues.size() == 2) {
                    omsCartItem.setSp1(pmsSaleAttrValues.get(0).getSaleAttrValueName());
                    omsCartItem.setSp2(pmsSaleAttrValues.get(1).getSaleAttrValueName());
                } else {
                    omsCartItem.setSp1(pmsSaleAttrValues.get(0).getSaleAttrValueName());
                    omsCartItem.setSp2(pmsSaleAttrValues.get(1).getSaleAttrValueName());
                    omsCartItem.setSp3(pmsSaleAttrValues.get(2).getSaleAttrValueName());
                }
            }
        }

        // 判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        List<OmsCartItem> omsCartItems;
        if (StringUtils.isBlank(memberId)) {
            // 用户没有登录
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                // cookie 不为空
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                // 判断添加的购物车数据在 cookie 中是否存在
                boolean exist = isCartExist(omsCartItem, omsCartItems);
                if (exist) {
                    // 之前添加过，更新购物车数据
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            // 更新购物车单条商品数量
                            cartItem.setQuantity(cartItem.getQuantity() + omsCartItem.getQuantity());
                            // 更新购物车单条商品总价
                            cartItem.setTotalPrice(cartItem.getTotalPrice().add(omsCartItem.getTotalPrice()));
                        }
                    }
                } else {
                    // 之前没有添加过，新增商品数据到购物车
                    omsCartItems.add(omsCartItem);
                }
            } else {
                // cooke为空
                omsCartItems = new ArrayList<>();
                omsCartItems.add(omsCartItem);
            }

            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
        } else {
            // 用户已登录，从 DB 中查询
            OmsCartItem omsCartItemFromDB = cartService.ifCartExistByUser(memberId, skuId);
            // 判断 DB 中查询出的购物车数据是否为空
            if (omsCartItemFromDB == null) {
                // DB 中查询为空，该用户未添加过该商品到购物车
                // 添加用户 id
                omsCartItem.setMemberId(memberId);
                cartService.addCart(omsCartItem);
            } else {
                // DB 中查询不为空，该用户添加过该商品到购物车
                // 更新 DB 中单条商品的数量
                omsCartItemFromDB.setQuantity(omsCartItemFromDB.getQuantity() + quantity);
                // 更新 DB 中单条商品的总价
                omsCartItemFromDB.setTotalPrice(omsCartItemFromDB.getTotalPrice().add(omsCartItem.getTotalPrice()));
                cartService.updateCart(omsCartItemFromDB);
            }

            // 同步缓存
            cartService.flushCartCache(memberId);
        }
        return "redirect:/success.html";
    }

    private boolean isCartExist(OmsCartItem omsCartItem, List<OmsCartItem> omsCartItems) {
        boolean result = false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();
            if (productSkuId.equals(omsCartItem.getProductSkuId())) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 计算购物车中被选中的商品总价格
     *
     * @param omsCartItems
     * @return
     */
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal(0);
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (1 == omsCartItem.getIsChecked()){
                totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
            }
        }
        return totalAmount;
    }

}
