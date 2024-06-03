package com.cl.yupao.service;

import com.cl.yupao.mapper.UserMapper;
import com.cl.yupao.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
/*
* 用户服务测试
* */

@SpringBootTest
 class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;


    /*
    *
    * 测试添加用户
    * */
    @Test
     void testAddUser(){
        User user = new User();
        user.setUsername("cl");
        user.setUserAccount("catCl");
        user.setAvatarUrl("https://profile-avatar.csdnimg.cn/b28c39a1c1c0406fb32cd7d18cd1bd13_qq_68228595.jpg");
        user.setGender(0);
        user.setUserPassword("xgns8866");
        user.setPhone("123");
        user.setEmail("456");
        user.setUserStatus(0);
        user.setIsDelete(0);
        user.setUserRole(0);
        user.setPlanetCode("5");
        user.setTags("java");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        //添加断言
        Assertions.assertTrue(result);

    }

   @Test
   void userRegister() {
       String userAccount="cl888";
       String userPassword="12345678";
       String checkPassword="12345678";
       String planetCode="4";
       long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
      Assertions.assertEquals(4,result);
   }

   @Test
    void tsetStatus(){
    int status=1;
    Long id= 11L;
       Integer i = userService.startOrStop(status, id);
       Assertions.assertEquals(1,i);
   }

   /*
   * 测试根据标签查询用户
   * */
    @Test
    void testTags(){
        List<String> tagNameList= Arrays.asList("java","python");
        List<User> users = userService.searchUserByTags(tagNameList);
        Assertions.assertNotNull(users);
    }
}