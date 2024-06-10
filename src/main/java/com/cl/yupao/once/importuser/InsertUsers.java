package com.cl.yupao.once.importuser;

import com.cl.yupao.mapper.UserMapper;
import com.cl.yupao.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/*
* 引导用户任务
* */
@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;


    /*
    * 批量插入用户
    * */
   // @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        System.out.println("gooddoodgood");
        stopWatch.start();
        final  int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假cl");
            user.setUserAccount("fakeCl");
            user.setAvatarUrl("https://profile-avatar.csdnimg.cn/b28c39a1c1c0406fb32cd7d18cd1bd13_qq_68228595.jpg");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("11111111");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
