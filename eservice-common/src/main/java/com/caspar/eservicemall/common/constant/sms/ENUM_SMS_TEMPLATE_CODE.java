package com.caspar.eservicemall.common.constant.sms;

public enum ENUM_SMS_TEMPLATE_CODE {
    /**
     * 注册短信模板
     * reg
     * */
    REG("SMS_276516612","注册短信验证码","reg"),
    LOGIN("SMS_276516612","登录短信验证码","login");
    private final String code;
    private final String msg;

    private final String smsType;
    ENUM_SMS_TEMPLATE_CODE(String code, String msg, String smsType) {
        this.code = code;
        this.msg = msg;
        this.smsType = smsType;
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public String getSmsType() {
        return smsType;
    }
    public static String getTemplateCodeByType(String smsType)
    {
        for(ENUM_SMS_TEMPLATE_CODE branchBankType:ENUM_SMS_TEMPLATE_CODE.values())
        {
            if(smsType.equals(branchBankType.getSmsType()))
            {
                return branchBankType.getCode();
            }
        }
        return null;
    }
}
