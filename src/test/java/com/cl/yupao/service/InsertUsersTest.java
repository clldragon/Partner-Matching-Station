package com.cl.yupao.service;

import com.cl.yupao.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
* 导入用户测试
* */
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /*
     * 批量插入用户
     * */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> userList = new ArrayList<>();
        final int INSERT_NUM = 1000;
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
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /*
     * 并发批量插入用户
     * */
    @Test
    public void doConcurrentInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //分10组
        int batchSize=1000;
        int j=0;
        List<CompletableFuture<Void>> futureList=new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList=new ArrayList<>();
            while (true){
                j++;
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
                userList.add(user);
                if (j % batchSize==0){
                    break;
                }
            }
        //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
