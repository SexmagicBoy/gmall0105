package com.atguigu.gmall.passport.config;

import com.atguigu.gmall.passport.properties.WeiboProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class WeiboConfig {

    @Value("${weibo.appKey}")
    private String appKey;

    @Value("${weibo.appSecret}")
    private String appSecret;

    @Value("${weibo.redirectUri}")
    private String redirectUri;

    @Value("${weibo.errorUri}")
    private String errorUri;

    @Bean
    public WeiboProperties weiboProperties() {
        WeiboProperties weiboProperties = new WeiboProperties();
        weiboProperties.setAppKey(appKey);
        weiboProperties.setAppSecret(appSecret);
        weiboProperties.setRedirectUri(redirectUri);
        weiboProperties.setErrorUri(errorUri);
        return weiboProperties;
    }
}
