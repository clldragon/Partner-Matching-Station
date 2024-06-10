package com.cl.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cl.yupao.model.domain.User;
import com.cl.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    //重点用户
    List<Long> mainUserList= Arrays.asList(3L);
    @Scheduled(cron = "0 0 12 * * ?")
    public void doCacheRecommendUser(){
        RLock lock=redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getLock:"+Thread.currentThread().getId());
            }
            for (Long userId : mainUserList) {
                //读取数据库中数据
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> userPage = userService.page(new Page<>(1, 8), queryWrapper);
                String redisKey = String.format("yupao:user:recommend:%s",userId);
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                //写缓存
                try {
                    valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    log.info("redis set key error",e);
                }
            }
        } catch (Exception e) {
           log.info("doCacheRecommendUser error",e);
        }
        finally {
            //只能释放自己锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlok:"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
