package com.caspar.eservicemall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.member.entity.MemberEntity;
import com.caspar.eservicemall.member.exception.PhoneException;
import com.caspar.eservicemall.member.exception.UsernameException;
import com.caspar.eservicemall.member.vo.MemberUserLoginVo;
import com.caspar.eservicemall.member.vo.MemberUserRegisterVo;
import com.caspar.eservicemall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:48:26
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberUserRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneException;

    /**
     * 判断用户名是否重复
     * @param userName
     * @return
     */
    void checkUserNameUnique(String userName) throws UsernameException;

    MemberEntity login(MemberUserLoginVo vo);

    /**
     * 社交用户的登录
     * @param socialUser
     * @return
     */
    MemberEntity login(SocialUser socialUser) throws Exception;

    /**
     * 微信登录
     * @param accessTokenInfo
     * @return
     */
    MemberEntity login(String accessTokenInfo);
}

