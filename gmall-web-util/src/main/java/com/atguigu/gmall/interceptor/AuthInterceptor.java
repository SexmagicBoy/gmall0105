package com.atguigu.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import util.HttpclientUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    /**
     * This implementation always returns {@code true}.
     *
     * @param request
     * @param response
     * @param handler
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        StringBuffer requestURL = request.getRequestURL();
        System.out.println(requestURL);

        // 判断被拦截的请求的访问方法的注解，确定该方法是否是需要拦截的
        // handler 中有请求中所携带的要访问的方法信息，强转成 handlerMethod，可以反射得到注解信息
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (methodAnnotation == null) {
            // 如果该方法没有 @LoginRequired 这个注解，不用拦截
            return true;
        }

        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        String status = null;
        String successJson;
        Map<String, String> successMap = null;
        // token有值，调用认证中心进行验证
        if (StringUtils.isNotBlank(token)) {
            // 获取通过 nginx 转发的原始客户端 ip
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }

            successJson = HttpclientUtil.doGet("http://passport.gmall.com/verify?token=" + token + "&currentIp=" + ip);
            if (StringUtils.isNotBlank(successJson)) {
                successMap = JSON.parseObject(successJson, Map.class);
                status = successMap.get("status");
            }
        }

        // 如果该方法有 @LoginRequired 注解，判断 mustLogin 属性
        boolean mustLogin = methodAnnotation.mustLogin();
        if (mustLogin) {
            // 必须登录成功才能使用
            if (!"success".equals(status)) {
                // 登录不成功则，重定向到 passport 登录页面，保留原始页面链接
                response.sendRedirect("http://passport.gmall.com/index?ReturnUrl=" + request.getRequestURL());
                return false;
            }
            // 登录成功，将 token 中携带的用户信息写入，覆盖 cookie 中的 oldToken
            afterLoginSuccessSetUserinfoAndToken(request, response, token, successMap);
        } else {
            // 没有登录也能使用，功能有一些限制
            if ("success".equals(status)) {
                // 登录成功，将 token 中携带的用户信息写入，覆盖 cookie 中的 oldToken
                afterLoginSuccessSetUserinfoAndToken(request, response, token, successMap);
            }
        }
        return true;
    }

    private void afterLoginSuccessSetUserinfoAndToken(HttpServletRequest request, HttpServletResponse response, String token, Map<String, String> successMap) {
        // 登录成功，将 token 中携带的用户信息写入
        String memberId = successMap.get("memberId");
        if (StringUtils.isNotBlank(memberId)) {
            request.setAttribute("memberId", memberId);
        }
        String nickname = successMap.get("nickname");
        if (StringUtils.isNotBlank(nickname)) {
            request.setAttribute("nickname", nickname);
        }

        // 登录成功，覆盖 cookie
        setOldToken(request, response, token);
    }

    private void setOldToken(HttpServletRequest request, HttpServletResponse response, String token) {
        CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
    }

}
