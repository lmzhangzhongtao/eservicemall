package com.caspar.eservicemal.authserver.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.TypeReference;
import com.caspar.eservicemal.authserver.feign.MemberFeignService;
import com.caspar.eservicemal.authserver.feign.ThirdPartFeignService;
import com.caspar.eservicemal.authserver.vo.UserLoginVo;
import com.caspar.eservicemal.authserver.vo.UserRegisterVo;
import com.caspar.eservicemall.common.constant.auth.AuthConstant;
import com.caspar.eservicemall.common.constant.sms.ENUM_SMS_TEMPLATE_CODE;
import com.caspar.eservicemall.common.exception.BizCodeEnum;
import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.common.vo.MemberResponseVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.caspar.eservicemall.common.constant.auth.AuthConstant.LOGIN_USER;

/**
 * 登录
 *
 * @Author: wanzenghui
 * @Date: 2021/11/26 22:26
 */
@Controller
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 访问登录页面
     * 登录状态自动跳转首页
     */
    @GetMapping(value = "/login.html")
    public String loginPage(HttpSession session) {
        // 判断是否登录状态
        Object attribute = session.getAttribute(LOGIN_USER);
        if (attribute == null) {
            // 未登录，返回登录页资源
            return "login";
        } else {
            // 已登录
            return "redirect:http://eservicemall.com";
        }
    }

//    /**
//     * 访问注册页面（该方法已省略，使用视图控制器简化代码）
//     */
//    @GetMapping(value = "/reg.html")
//    public String regPage(HttpSession session) {
//        return "reg";
//    }
    /**
     * 发送短信验证码
     *
     * @param phone 号码
     * @param businessType  业务类型
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendRegCode(@RequestParam(name = "phone", required = true) String phone,@RequestParam(name = "businessType", required = true) String businessType) {
        // 1.判断60秒间隔发送，防刷
        String _code = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(_code) && System.currentTimeMillis() - Long.parseLong(_code.split("_")[1]) < 60000) {
            // 调用接口小于60秒间隔不允许重新发送新的验证码
            return R.error(String.valueOf(BizCodeEnum.SMS_CODE_EXCEPTION));
        }
        // 2.验证码存入缓存
//        String code = UUID.randomUUID().toString().substring(0, 5);
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);
        // 验证码缓存到redis中（并且记录当前时间戳）
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, codeNum + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);
        // 3.发送验证码
        String templateCode= ENUM_SMS_TEMPLATE_CODE.getTemplateCodeByType(businessType);
        thirdPartFeignService.sendCode(phone, codeNum,templateCode);
        return R.ok();
    }

    /**
     *
     * TODO: 重定向携带数据：利用session原理，将数据放在session中。
     * TODO:只要跳转到下一个页面取出这个数据以后，session里面的数据就会删掉
     * TODO：分布下session问题
     * RedirectAttributes：重定向也可以保留数据，不会丢失
     * 用户注册
     * @return
     */
    @PostMapping(value = "/register")
    public String register(@Valid UserRegisterVo vos, BindingResult result,
                           RedirectAttributes attributes){
        //如果有错误回到注册页面
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors",errors);
            //效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //1、校验验证码
        String code = vos.getCode();
        //获取存入Redis里的验证码
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            //截取字符串
            if (code.equals(redisCode.split("_")[0])) {
                //删除验证码;令牌机制
                redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX+vos.getPhone());
                //验证码通过，真正注册，调用远程服务进行注册
                R register = memberFeignService.register(vos);
                if (register.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.eservicemall.com/login.html";
                } else {
                    //失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", register.getData("msg",new TypeReference<String>(){}));
                    attributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.eservicemall.com/reg.html";
                }


            } else {
                //效验出错回到注册页面
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                attributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.eservicemall.com/reg.html";
            }
        } else {
            //效验出错回到注册页面
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.eservicemall.com/reg.html";
        }
    }

    @PostMapping(value = "/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) {

        //远程登录
        R login = memberFeignService.login(vo);

        if (login.getCode() == 0) {
            MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {});
            session.setAttribute(LOGIN_USER,data);
            return "redirect:http://eservicemall.com";
        } else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.eservicemall.com/login.html";
        }
    }


    @GetMapping(value = "/loguot.html")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute(LOGIN_USER);
        request.getSession().invalidate();
        return "redirect:http://eservicemall.com";
    }


    public static void main(String[] args) {
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);
        System.out.println(codeNum);
    }
}