package com.liu.picturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户访问请求
 */
@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -4851195202507179574L;
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

}