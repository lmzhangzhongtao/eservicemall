package com.caspar.eservicemal.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.caspar.eservicemal.authserver.feign.MemberFeignService;
import com.caspar.eservicemal.authserver.vo.SocialUser;
import com.caspar.eservicemall.common.utils.HttpUtils;
import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.common.vo.MemberResponseVo;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.apache.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static com.caspar.eservicemall.common.constant.auth.AuthConstant.LOGIN_USER;

@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    private MemberFeignService memberFeignService;


    /**
     * @Description: 处理社交登录请求
     **/
    @GetMapping(value = "/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("client_id","1538139000");
        map.put("client_secret","5b3848ea98cc39d7874ac0131323136b");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.eservicemall.com/oauth2.0/weibo/success");
        map.put("code",code);

        //1、根据用户授权返回的code换取access_token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());

        //2、处理
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取到了access_token,转为通用社交登录对象
            String json = EntityUtils.toString(response.getEntity());
            //String json = JSON.toJSONString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //知道了哪个社交用户
            //1）、当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息，以后这个社交账号就对应指定的会员）
            //登录或者注册这个社交用户
            System.out.println(socialUser.getAccess_token());
            //调用远程服务
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberResponseVo data = oauthLogin.getData("data", new TypeReference<MemberResponseVo>() {});
                log.info("登录成功：用户信息：{}",data.toString());

                //1、第一次使用session，命令浏览器保存卡号，JSESSIONID这个cookie
                //以后浏览器访问哪个网站就会带上这个网站的cookie
                session.setAttribute(LOGIN_USER,data);
                // 首次使用session时，spring会自动颁发cookie设置domain，所以这里手动设置cookie很麻烦，采用springsession的方式颁发父级域名的domain权限
//                Cookie cookie = new Cookie("JSESSIONID", loginUser.getId().toString());
//                cookie.setDomain("gulimall.com");
//                servletResponse.addCookie(cookie);
                // 跳回首页
                //2、登录成功跳回首页
                return "redirect:http://eservicemall.com";
            } else {

                return "redirect:http://auth.eservicemall.com/login.html";
            }

        } else {
            return "redirect:http://auth.eservicemall.com/login.html";
        }

    }

}
