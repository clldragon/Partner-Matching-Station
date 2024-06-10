package com.cl.yupao.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest

public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;
    @Test
    void test(){
        //list.数据存在本地JVM内存中
        List<String> list = new ArrayList<>();
        list.add("yupi");
        System.out.println("list"+list.get(0));

        //数据存储在redis的内存中
        RList<String> rlist = redissonClient.getList("test-list");
      //  rlist.add("yupi");
        System.out.println("rlist:"+rlist.get(0));
        rlist.remove(0);
    }


}
