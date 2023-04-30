package com.caspar.eservicemall.cart.interceptor;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.caspar.eservicemall.common.constant.auth.AuthConstant;
import com.caspar.eservicemall.common.constant.cart.CartConstant;
import com.caspar.eservicemall.common.to.cart.UserInfoTO;
import com.caspar.eservicemall.common.vo.MemberResponseVo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.UUID;

/**
 * 判断用户登录状态，并封装用户信息传递给controller
 *
 * @Author: caspar
 * @Date: 2023/4/28 17:33
 */
public class CartInterceptor implements HandlerInterceptor{
    public static ThreadLocal<UserInfoTO> threadLocal = new ThreadLocal<>();
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取会话信息，获取登录用户信息
        HttpSession session = request.getSession();
        MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthConstant.LOGIN_USER);
        // 判断是否登录，并封装User对象给controller使用
        UserInfoTO user = new UserInfoTO();
        if (attribute != null) {
            // 登录状态，封装用户ID，供controller使用
            user.setUserId(attribute.getId());
        }
        // 获取当前请求游客用户标识user-key
        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isNotEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    // 获取user-key值封装到user，供controller使用
                    user.setUserKey(cookie.getValue());
                    user.setTempUser(true);// 不需要重新分配
                    break;
                }
            }
        }

        // 判断当前是否存在游客用户标识
        if (StringUtils.isBlank(user.getUserKey())) {
            // 无游客标识，分配游客标识
            user.setUserKey(UUID.randomUUID().toString());
        }

        // 封装用户信息（登录状态userId非空，游客状态userId空）
        threadLocal.set(user);
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        UserInfoTO user = threadLocal.get();
        if (user != null && !user.isTempUser()) {
            // 需要为客户端分配游客信息
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, user.getUserKey());
            cookie.setDomain("eservicemall.com");// 作用域
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);// 过期时间
            response.addCookie(cookie);
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}
