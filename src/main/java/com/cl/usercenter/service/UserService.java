package com.cl.usercenter.service;

import com.cl.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 陈
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-05-20 15:21:08
*/
public interface UserService extends IService<User> {

    /**
     * 用户注释
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);




}
