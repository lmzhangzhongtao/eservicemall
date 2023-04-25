package com.caspar.eservicemall.member.exception;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: caspar
 **/
public class UsernameException extends RuntimeException {


    public UsernameException() {
        super("存在相同的用户名");
    }
}
