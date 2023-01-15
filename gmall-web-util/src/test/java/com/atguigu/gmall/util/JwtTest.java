package com.atguigu.gmall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    public static void main(String[] args) {
        String key = "2023gmall0105";

        Map<String, Object> map = new HashMap<>();
        map.put("memberId", "1");
        map.put("nickname", "张三");

        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        String salt = ip + time;

        String encode = JwtUtil.encode(key, map, salt);
        System.out.println(encode);
        // eyJhbGciOiJIUzI1NiJ9.eyJuaWNrbmFtZSI6IuW8oOS4iSIsIm1lbWJlcklkIjoiMSJ9.T-RJMvED6zRF2n4ePACaOS3JGakhENsy0xLM9Ru_Nxs


    }
}
