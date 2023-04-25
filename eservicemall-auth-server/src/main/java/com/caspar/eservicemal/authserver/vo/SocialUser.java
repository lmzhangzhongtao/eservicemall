package com.caspar.eservicemal.authserver.vo;

import lombok.Data;

/**
 * @Description: 社交用户信息
 * @Created: with IntelliJ IDEA.
 * @author: caspar
 * @createTime: 2023-04-25 11:04
 **/

@Data
public class SocialUser {

    private String access_token;

    private String remind_in;

    private long expires_in;

    private String uid;

    private String isRealName;

}
