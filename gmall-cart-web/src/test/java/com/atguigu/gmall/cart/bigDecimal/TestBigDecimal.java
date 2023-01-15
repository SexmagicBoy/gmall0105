package com.atguigu.gmall.cart.bigDecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TestBigDecimal {
    public static void main(String[] args) {
        // 初始化
        BigDecimal b1 = new BigDecimal(0.01f);
        BigDecimal b2 = new BigDecimal(0.01d);
        BigDecimal b3 = new BigDecimal("0.01");
        System.out.println(b1); // 0.00999999977648258209228515625
        System.out.println(b2); // 0.01000000000000000020816681711721685132943093776702880859375
        System.out.println(b3); // 0.01
        // 初始化方法用字符串，因为 java 默认使用分数的幂函数表示小数，用字符串避免幂函数造成的精度损失

        // 比较
        int i = b1.compareTo(b2); // 1 0 -1
        System.out.println(i); // -1 代表 b1 比 b2 小

        // 运算
        BigDecimal b4 = new BigDecimal("1");
        System.out.println(b1.add(b4).setScale(3, RoundingMode.HALF_UP)); // 1.010
        System.out.println(b1.subtract(b4).setScale(3, RoundingMode.HALF_UP)); // -0.990
        System.out.println(b1.multiply(b4).setScale(3, RoundingMode.HALF_UP)); // 0.010
        System.out.println(b1.divide(b4, 2, RoundingMode.HALF_UP)); // 0.01
    }
}
