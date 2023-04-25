package com.caspar.eservicemall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.caspar.eservicemall.common.exception.BizCodeEnum;
import com.caspar.eservicemall.member.exception.PhoneException;
import com.caspar.eservicemall.member.exception.UsernameException;
import com.caspar.eservicemall.member.feign.CouponFeignService;
import com.caspar.eservicemall.member.vo.MemberUserLoginVo;
import com.caspar.eservicemall.member.vo.MemberUserRegisterVo;
import com.caspar.eservicemall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.caspar.eservicemall.member.entity.MemberEntity;
import com.caspar.eservicemall.member.service.MemberService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.R;



/**
 * 会员
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:48:26
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;
    @PostMapping(value = "/register")
    public R register(@RequestBody MemberUserRegisterVo vo) {

        try {
            memberService.register(vo);
        } catch (PhoneException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }


    @PostMapping(value = "/login")
    public R login(@RequestBody MemberUserLoginVo vo) {

        MemberEntity memberEntity = memberService.login(vo);

        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }
    @PostMapping(value = "/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws Exception {

        MemberEntity memberEntity = memberService.login(socialUser);

        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }



    @RequestMapping("/coupons")
    public R test() {
        //当前服务的用户
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("天路");
        //远程调用获取优惠券
        R membercoupons = couponFeignService.memberCoupons();
        //响应用户与优惠券信息
        return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
 //   @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
  //  @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
 //   @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
  //  @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
