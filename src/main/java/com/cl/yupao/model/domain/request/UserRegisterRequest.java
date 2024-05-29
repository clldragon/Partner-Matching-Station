package com.cl.yupao.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/*
* 用户注册请求体
* @author chen
* */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 8264973080960712858L;
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;

    /*
    * 星球编号
    * */
    private String planetCode;

}

