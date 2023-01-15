package com.atguigu.gmall.passport.properties;

import lombok.Data;

@Data
public class WeiboProperties {
    private String appKey;
    private String appSecret;
    private String redirectUri;
    private String errorUri;
}
