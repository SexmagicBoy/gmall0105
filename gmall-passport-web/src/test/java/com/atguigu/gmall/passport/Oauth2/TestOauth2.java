package com.atguigu.gmall.passport.Oauth2;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestOauth2 {

    public static void main(String[] args) {
        // part1: 尝试获取授权码
        // HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=1488720794&response_type=code&redirect_uri=http://passport.gmall.com/vlogin");

        // part2: 拿到授权码 http://passport.gmall.com/vlogin?code=f98a1ba9ba163cce254cffcde1d8fba3

        // part3: 用授权码交换 access_token
        /*Map<String, String> map = new HashMap<>();
        map.put("client_id", "1488720794");
        map.put("client_secret", "71fcc900002c4c281dce93db869d306e");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://passport.gmall.com/vlogin");
        map.put("code", "f98a1ba9ba163cce254cffcde1d8fba3");
        String access_token = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token", map);

        System.out.println(access_token); */
        // {"access_token":"2.00PcdWEIiRWkcB770d7c9a16PRlWDC","remind_in":"157679999","expires_in":157679999,"uid":"7395946783","isRealName":"true"}

        // part4: 用 access_token 换取用户信息
        /*String user_info = HttpclientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token=2.00PcdWEIiRWkcB770d7c9a16PRlWDC&uid=7395946783");
        Map<String, Object> map = JSON.parseObject(user_info, Map.class);
        assert map != null;
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        System.out.println("用户名=" + map.get("name"));*/
    }
}
