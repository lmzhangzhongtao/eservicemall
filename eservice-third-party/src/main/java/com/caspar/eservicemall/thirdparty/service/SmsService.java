package com.caspar.eservicemall.thirdparty.service;

import java.util.concurrent.ExecutionException;

public interface SmsService {
    /**
     * 发送短信验证码
     * @param phone 电话号码
     * @param code  验证码
     * @param templateCode 短信模板代码
     */
    public Boolean sendCode(String phone, String code,String templateCode) throws ExecutionException, InterruptedException;

}
