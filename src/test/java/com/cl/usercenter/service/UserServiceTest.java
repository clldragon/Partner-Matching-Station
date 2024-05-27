package com.cl.usercenter.service;
import java.util.Date;

import com.cl.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
/*
* 用户服务测试
* */

@SpringBootTest
 class UserServiceTest {

    @Resource
    private UserService userService;


    /*
    *
    * 测试添加用户
    * */
    @Test
     void testAddUser(){
        User user = new User();
        user.setUsername("dogCl");
        user.setUserAccount("111");
        user.setAvatarUrl("https://profile-avatar.csdnimg.cn/b28c39a1c1c0406fb32cd7d18cd1bd13_qq_68228595.jpg");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        user.setUserStatus(0);
        user.setIsDelete(0);
        user.setUserRole(0);
        user.setPlanetCode("5808");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        //添加断言
        Assertions.assertTrue(result);

    }

   @Test
   void userRegister() {
       String userAccount="cl666";
       String userPassword="xgns8866";
       String checkPassword="xgns8866";
       String planetCode="5678";
       long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
      Assertions.assertEquals(-1,result);
   }
}