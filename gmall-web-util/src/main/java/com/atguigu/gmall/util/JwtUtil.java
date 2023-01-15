package com.atguigu.gmall.util;

import io.jsonwebtoken.*;

import java.util.Date;
import java.util.Map;

public class JwtUtil {
    // key 为服务器密钥
    // salt 为盐值，一般是由浏览器 ip + 时间戳 + 机器码构成（切换浏览器或机器会导致校验失败，满足安全性）
    // param 中可以保存用户的一些信息

    public static String encode(String key, Map<String, Object> param, String salt) {
        if (salt != null) {
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        // 两小时过期
        Date expiresDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2);
        jwtBuilder = jwtBuilder.setClaims(param).setExpiration(expiresDate);

        return jwtBuilder.compact();

    }

    public static Map<String, Object> decode(String token, String key, String salt) {
        Claims claims = null;
        if (salt != null) {
            key += salt;
        }
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            return null;
        }
        return claims;
    }
}
