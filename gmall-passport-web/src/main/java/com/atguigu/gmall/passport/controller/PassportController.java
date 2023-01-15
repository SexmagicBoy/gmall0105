package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.passport.properties.WeiboProperties;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.util.JwtUtil;
import constant.JwtConstant;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import util.HttpclientUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UmsMemberService umsMemberService;

    @Autowired
    private WeiboProperties weiboProperties;

    /**
     * 微博登录
     *
     * @param code
     */
    @RequestMapping("vlogin")
    public String vlogin(String code, HttpServletRequest request) {
        String redirectUrl = "redirect:http://search.gmall.com/index";

        if (StringUtils.isNotBlank(code)) {
            // 授权码换取 access_token
            Map<String, String> map = new HashMap<>();
            map.put("client_id", weiboProperties.getAppKey());
            map.put("client_secret", weiboProperties.getAppSecret());
            map.put("grant_type", "authorization_code");
            map.put("redirect_uri", weiboProperties.getRedirectUri());
            map.put("code", code);
            String accessJsonStr = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token", map);
            if (accessJsonStr == null) {
                return redirectUrl;
            }
            Map<String, String> accessMap = JSON.parseObject(accessJsonStr, Map.class);
            String access_token = accessMap.get("access_token");
            String uid = accessMap.get("uid");

            // access_token 换取用户信息
            String userInfoJsonStr = HttpclientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid);
            Map<String, Object> userInfoMap = JSON.parseObject(userInfoJsonStr, Map.class);
            String name = (String) userInfoMap.get("name");
            String profileImageUrl = (String) userInfoMap.get("profile_image_url");

            // 检查用户是否存在
            UmsMember umsMember = umsMemberService.getBySourceUid(uid);
            if (umsMember == null) {
                // 将用户信息保存在数据库，用户类型设置为微博用户
                umsMember = new UmsMember();
                umsMember.setMemberLevelId("4");
                umsMember.setNickname(name);
                umsMember.setStatus(1);
                umsMember.setCreateTime(new Date());
                umsMember.setIcon(profileImageUrl);
                umsMember.setSourceType(1);
                umsMember.setSourceUid(uid);
                umsMember.setAccessToken(access_token);
                umsMember.setAccessCode(code);
                umsMember = umsMemberService.save(umsMember);
            } else {
                umsMember.setAccessCode(code);
                umsMember.setAccessCode(access_token);
                umsMemberService.update(umsMember);
            }

            // 生成 jwt 的 token，并且重定向到首页，携带 token
            // 颁发 token
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("memberId", umsMember.getId());
            tokenMap.put("nickname", umsMember.getNickname());

            String ip = getIp(request);
            String hexIp = DigestUtils.md5Hex(ip);

            // 按照设计的算法对参数进行加密后，生成 token
            String token = JwtUtil.encode(JwtConstant.key, tokenMap, hexIp);
            redirectUrl += "?token=" + token;
        }

        return redirectUrl;
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = null;

        // 调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = umsMemberService.login(umsMember);
        if (umsMemberLogin != null) {
            // 验证成功颁发 token
            Map<String, Object> map = new HashMap<>();
            map.put("memberId", umsMemberLogin.getId());
            map.put("nickname", umsMemberLogin.getNickname());

            String ip = getIp(request);
            String hexIp = DigestUtils.md5Hex(ip);

            // 按照设计的算法对参数进行加密后，生成 token
            token = JwtUtil.encode(JwtConstant.key, map, hexIp);
        }
        return token;
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp) {
        String successJson = null;
        String hexIp = DigestUtils.md5Hex(currentIp);

        // 通过 jwt 核验 token 真假
        Map<String, String> map = new HashMap<>();
        Map<String, Object> decodeMap = JwtUtil.decode(token, JwtConstant.key, hexIp);
        if (decodeMap != null) {
            map.put("status", "success");
            map.put("memberId", (String) decodeMap.get("memberId"));
            map.put("nickname", (String) decodeMap.get("nickname"));
            successJson = JSON.toJSONString(map);
        }

        return successJson;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap) {
        modelMap.put("ReturnUrl", ReturnUrl);
        modelMap.put("weiboAppKey", weiboProperties.getAppKey());
        return "index";
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for"); // 通过 nginx 转发的原始客户端 ip
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();
            if (StringUtils.isBlank(ip)) {
                ip = "127.0.0.1";
            }
        }
        return ip;
    }

}
