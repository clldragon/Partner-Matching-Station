

以下為項目筆記：
#                 cl-伙伴匹配站


#### 需求分析

1. 用户去添加标签，标签的分类

2. 主动搜索，允许用户根据标签去搜索其他用户
3. 组队
4. 允许用户去修改标签
5. 推荐

​		1.相似度计算算法+本地分布式计算

## 伙伴匹配站第一天：
#### 修改数据库

用户有哪些标签？

- 直接在用户表补充tag字段['java','男']存json字符串

​		优点：查询方便，不用新建关联表，标签是用户的固有属性

- 加一个关联表，记录用户和标签的关系

​		关联表的应用场景：查询灵活，可以正查反查



#### 后端初始化

基于已开发的用户中心项目继续开发

1. 复制用户中心项目
2. 替换项目名



​		



#### 开发后端接口

#### 搜索标签

1.允许用户传入多个标签，多个标签都存在才搜索出来 and   //like '%java%' and like '%c++%'

2.允许用户传入多个标签，又任何一个标签存在就能搜索出来 or//like '%java%' or like '%c++%'

两种方式：

​    1.SQL查询（实现简单，可以通过拆分查询进一步优化）

​	2.内存查询（灵活，可以通过并发进一步优化）



- 如果参数可以分析，根据用户的参数去选择查询方式，比如标签数
- 如果参数不可以分析，并且数据库连接足够，内存空间足够，可以并发同时查询，谁先返回用谁。
- 还可以SQL查询与内存计算相结合，比如用SQL过滤掉部分tag



建议通过实际测试来分析那种查询比较快，数据量大的时候验证效果更明显



使用MyBatisX-Generator快速生成表结构的三层架构，实体，以及xml文件

![img](blob:https://b11et3un53m.feishu.cn/338ac9eb-0dc0-4c49-9561-f8397ebdadab)

![image-20240513155448008](C:\Users\陈\AppData\Roaming\Typora\typora-user-images\image-20240513155448008.png)

再自己导入Swagger，1先导入Knife4j的maven坐标2.再配置类中加入Knife4j相关配置

3.设置静态资源映射，否则接口文档页面无法访问



#### 开发“根据标签列表json查询用户”接口

mybatis-plus 开启查询日志

​		mybatis-plus:

​        configuration:

​         log-impl: org.apache.ibatis.logging.stdout.StdOutImpl



直接再用户中心的代码基础上进行开发

1.SQL查询

```java
/*
* 根据标签搜索用户
* */
public List<User> searchUserByTags(List<String> tagNameList){
    if (CollectionUtils.isEmpty(tagNameList)){
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    //拼接 and 查询
    //like '%java%' and like '%Python%'
    for (String tagName : tagNameList) {
        //把每一次模糊查询的结果重新赋给queryWrapper
        queryWrapper=queryWrapper.like("tags",tagName);
    }
    List<User> userList = userMapper.selectList(queryWrapper);
    List<User> users = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    return users;
}
```



2.内存查询：

```java
//2.内存查询
//1.先查询所有的用户
QueryWrapper<User> queryWrapper = new QueryWrapper<>();
List<User> userList = userMapper.selectList(queryWrapper);
Gson gson = new Gson();
//2.再内存中判断是否有包含要求的标签
List<User> users = userList.stream().filter(user -> {
    String tagsStr = user.getTags();
    Set<String> tempTagNameset = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
    }.getType());
    for (String tagName : tagNameList) {
        if (!tempTagNameset.contains(tagName)) {
            return false;
        }
    }
    return true;
}).map(this::getSafetyUser).collect(Collectors.toList());
```

```java
//注意：每一次集合都需要判空
tempTagNameset=Optional.ofNullable(tempTagNameset).orElse(new HashSet<>());
```

这里没有使用if判断，而是使用Optional.ofNullable(tempTagNameset).orElse(new HashSet<>())

eg：

工作中经常会遇到，查询返回空，如果没有判空处理，一不小心就会空指针异常。加上if判断处理也可以，但是jdk1.8有更优雅的处理方式。

public static void main(String[] args) {
        List<String> list = null;
        List<String> newList = Optional.ofNullable(list).orElse(Lists.newArrayList());
        newList.forEach(x -> System.out.println(x));
    }

原文链接：https://blog.csdn.net/lxj_1993/article/details/109451567





解析JSON字符串

序列化：java对象转成json

反序列化：把json对象转成java对象

java json序列化库有很多

1.gson （相对安全）coogle  推荐

2.fastjson  alibaba（快，但是漏洞太多）

3.jackson （相对而言，不丰富）

4.kryo

```java
<!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.8.9</version>
</dependency>
```



这里注意：map(this::getSafetyUser)

注意：应该完善一下数据库中的user表的字段

接着进行单元测试

```java
/*
 * 测试根据标签搜索用户
 * */
@Test
public void testSearchUserByTags(){
    List<String> tagNameList= Arrays.asList("java","python");
    List<User> userList = userService.searchUserByTags(tagNameList);
    Assert.assertNotNull(userList);
}
```



### 用户中心来集中提供用户的检索，操作，注册，登录，鉴权



### 伙伴匹配站第二天：

2024/6/2

1.stream/parallerStream 流失处理

2.Optional 可选类

3.后端整合Swagger+knife4j接口文档

4.存量用户信息导入及同步（爬虫）

Swagger+knife4j

1. 引入依赖
2. 定义SwaggerConfig配置类
3. 定义需要生成接口文档的代码位置（controller）（注意不要再线上暴露接口文档的地址）

如果soringBoot-version>=2.6,需要添加如下

```java
mvc:
  pathmatch:
    matching-strategy: ANT_PATH_MATCHER
```



### 存量用户信息导入及同步

1. 分析原网站是怎么获取数据的以及接口
2. 用程序去调用接口（Java/python）都可以
3. 处理（清洗）一下数据，之后就可以写到数据库里



流程：

1. 从excel中导入全量用户数据，**判重**。easyExcel
2. 抓取写了自我介绍的同学信息，提取出用户呢称，用户唯一id，自我介绍信息
3. 从自我介绍中提取信息，然后写入到数据库中



**EasyExcel**

两种读对象的方式：

1. 确定表头：建立对象，和表头形成映射关系

2. 不确定表头：每一行数据映射为

   ```
   Map<String,Object>
   ```

两种读取模式

1. 监听器：先创建监听器，在读取文件时绑定监听器。单独抽离处理逻辑，代码清晰易于维护；一条一条处理，适用于数据量大的场景。
2. 同步读：无需创建监听器，一次性获取完整数据。简单方便，但是数据量大时会有等待时常，也可能内存溢出



**@RequestParam**

@RequestParam(value = “key”,required = false)String key,
value = “key”，表示前端对传入参数指定为key，如果前端不传key参数名，会报错 。required = false表示该参数可以不传，required在一个请求中默认值是为true。

@RequestParam(value=“username”,required=true,defaultValue=“admin”) defaultValue默认值，如果传输参数没有匹配上则使用默认值，若匹配上则使用传输过来的内容



#### Springboot中参数传递的三个注解的使用

- [Springboot中参数传递的三个注解的使用](https://blog.csdn.net/bbxylqf126com/article/details/109641122#Springboot_2)
- - [@PathVariable](https://blog.csdn.net/bbxylqf126com/article/details/109641122#PathVariable_6)
  - [@RequestParam](https://blog.csdn.net/bbxylqf126com/article/details/109641122#RequestParam_55)
  - - [GET用法](https://blog.csdn.net/bbxylqf126com/article/details/109641122#GET_57)
    - [POST用法](https://blog.csdn.net/bbxylqf126com/article/details/109641122#POST_105)
  - [@RequestBody](https://blog.csdn.net/bbxylqf126com/article/details/109641122#RequestBody_161)

改造用户中心

SESSIONID=D5288C16F698F749C50344EB682F33D5

 D5288C16F698F749C50344EB682F33D5

#### 伙伴匹配站第三天

### Session共享

种 session 的时候注意范围，cookie.domain

如果想要共享cookie，可以种一个更高层的域名，将要共享session的两个域名设为二级域名

### 如何开启同一后端项目，但配置不同的端口号

```
java -jar .\homieMatching-0.0.1-SNAPSHOT.jar --server.port=8081
```



### 为什么在服务器A登录后，服务器B拿不到用户信息

因为用户在A登录，session只存在于A中，而B中没有，所以服务器B获取用户信息时会失败

#### 解决办法

1. Redis（基于内存的 K/V 数据库）

   将Cookie存储在Redis中实现分布式登录，在A中登录的Cookie存在Redis中，那么B要获取登录信息先从Redis中获取对应Cookie再拿到登录信息

2. MySQL

3. 文件服务器ceph

#### 引入redis：

1. 映入redis依赖

2. 配置端口，用户，密码，使用的数据库

3. 引入spring-session-redis的整合使得自动将session存储至redis中

4. 修改spring-session存储配置 spring-session.store-type

   默认是none表示存储在单台服务器

   ```java
   store-type: redis
   ```

**这里我出现了一个问题**

**加入这两个依赖后，项目重启直接报错**

**解决方法：springboot版本冲突**

**删去两个依赖的版本即可**



再配置中添加如下配置，可以将cookie层级从端口提升到域名

**表示该cookie只在localhost域名下有效**。

```jaVA
session:
  cookie:
    domain: localhost
```



#### 伙伴匹配站第四天

#### 导入数据

1. 可视化界面：适合一次导入，数据量可控
2. 写程序：for循环，建议分批，不要一次梭哈

并发要注意执行的先后顺序无所谓，不要用到非并发类的集合

```java
private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
```

这段代码创建了一个ExecutorService对象，使用ThreadPoolExecutor实现，设置了核心线程数为40，最大线程数为1000，空闲线程存活时间为10000分钟，任务队列容量为10000。

1. 创建一个ExecutorService对象，命名为executorService

2. 使用ThreadPoolExecutor类实现线程池

3. 设置核心线程数为40

4. 设置最大线程数为1000

5. 设置空闲线程存活时间为10000分钟

6. 创建一个容量为10000的任务队列(ArrayBlockingQueue)

7. 将任务队列作为参数传入ThreadPoolExecutor构造方法中

8. 将创建的ThreadPoolExecutor对象赋值给executorService变量

   

```
//CPU密集型：分配的核心线程数=CPU-1
//I0密集型：分配的核心线程数可以大于CPU核数
```

数据库慢？预先把数据查出来，放到一个更快读取的地方，不用再
查数据库了。（缓存）
预加载缓存，定时更新缓存。（定时任务）
多个机器都要执行任务么？（分布式锁：控制同一时间只有一台机
器去执行定时任务，其他机器不用重复执行了)



#### 数据查询慢怎么办

用缓存：提前把数据取出来保存好（通常保存在读写更快地介质，比如内存），就可以更快地读写

缓存的实现：

- Redis（分布式缓存，支持多个进程或者多个服务器之间的数据共享）
- memcached（分布式）
- Etcd（主要用于共享配置和服务发现。云原生架构的一个分布式，扩容能力强）
- ehcache（单机）
- 本地缓存（Java的Map集合）
- Caffeine（是一个Java库，但是呢它是本地的，只能在单个JVM进程中使用，不能再多个进程或服务器之间共享数据）
- Google Guava



#### Redis

key-value 存储系统（区别于mysql，它存储的是键值对）

### Java里的实现方式

#### Spring Date Redis（推荐）

Spring Data：通用的数据库访问框架，定义了一组**增删改查**的接口

mysql，redis，jpa

#### Jedis

独立于Spring操作Redis的Java客户端

#### lettuce

高阶操作Redis的Java客户端

#### Redisson

分布式操作Redis的Java客户端，让你像在使用本地的集合一样操作Redis（分布式Redis数据网格）



### 操作方式对比：

1. 如果你用Spring开发，并且没有过多的定制化要求选Spring Data Redis
2. 如果你没有用Spring，并且追求简单，没有过多的性能要求，可以用Jedis + Jedis Pool
3. 如果你的项目不是Spring，并且追求高性能，高定制化，可以用lettuce。支持异步、连接池（技术大牛使用）
4. 如果你的项目是分布式的，需要用到一些分布特性（比如分布式锁，分布式集合），推荐使用Redisson



### 如何如何设计缓存Key

目的：使得不同用户看到的数据不同

systemId:moudleId:func:options(不要和别人冲突)

homie:user:recommend:

redis 内存不能无限增加，k一定要设置过期时间



### 缓存预热

问题：第一个用户访问还是很慢（加入第一个勇士），也能一定程度上保护数据库

缓存预热的优缺点：

1. 解决上述问题，让用户始终访问很快

缺点：

1. 增加开发成本（需要额外的开发和设计）
2. 预热的时机和时间不合适的话，有可能你缓存的数据不对或者太老
3. 空间换时间



### 怎么缓存预热？

1. 定时触发（常用）
2. 手动触发

#### 定时任务实现

1. Spring Scheduler（Spring Boot默认整合的）
2. Quartz（独立于Spring Boot存在的定时任务框架）
3. XXL-Job之类的分布式任务调度平台（界面 + SDK）



**实现**

用·定时任务，每天刷新所有用户的推荐列表

注意点：

1.缓存预热的意义（新增少，用户多）

2缓存的空间不能太大

```java
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //重点用户
    List<Long> mainUserList= Arrays.asList(3L);
    @Scheduled(cron = "0 0 12 * * ?")
    public void doCacheRecommendUser(){
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
    }

}
```



### 伙伴匹配站第四天

### 控制定时任务的发布

#### why？

1. 浪费资源，想象10000条服务器同时“打鸣”
2. 脏数据，比如重复插入

要控制定时任务在同一时间只有一个服务器执行

#### 实现方式：

1. 分离定时任务，只安排一个服务器执行定时任务。成本太大
2. 写死配置，每个服务器都执行定时任务，但是只有IP地址符合配置的服务器才会执行。适合于并发量不大的场景，成本低。问题：IP可能是不固定的。
3. 动态配置：配置是可以轻松的，很方便的·更新的（代码无需重启），但是只有ip符合配置的服务器才真实执行业务逻辑

- 数据库
- Redis
- 配置中心

1. 分布式锁，只有抢到锁的服务器才能执行对应的业务逻辑。

   - 坏处：增加成本
   - 好处：不用手动配置，不管有多少个服务器在抢锁

​	

**单机就会存在故障**

### 锁

在资源有限的情况下，控制同一时间（段）只有某些线程（用户 / 服务器）能够访问资源

Java实现锁：synchronized关键字，并发包的类

问题：只对单个JVM有效

#### 分布式锁

为啥需要分布式锁？

1. 在资源有限的情况下，控制同一时间（段）只有某些线程（用户 / 服务器）能够访问资源
2. 单个锁只对单个JVM有效

### 分布式锁实现的关键

#### 抢锁机制

怎么保证同一时间只有一个服务器能抢到锁？

核心思想：先来的人先把数据改为自己独有的标识（比如服务器IP），后来的人发现标识存在，则抢锁失败，继续等到。等先来的人的执行方法结束，把标识清空，其他人继续抢锁。

实现方式：

1. MySQL数据库：select for update行级锁（最简单）

2. 乐观锁

3. **Redis实现**：内存数据库，**速度快**。支持setnx，lua脚本支持原子性操作

   setnx: set if not exists如果不存在，则设置；只有设置成功才会返回true

4. Zookeeper实现（不推荐）

##### 注意事项：

1. 锁用完要释放

2. 锁一定要加过期时间

3. 如果方法执行时期过长，锁提前过期了？

   问题：

4. 连锁效应：释放别人的锁

5. 这样还是会存在多个方法同时执行的情况

解决方案：

- 续期

1. - 如何判断方法为执行完？

     Aop实现：提前定义flag为false，如果执行完毕呢flag则设为true，未执行完成依旧是false。通过循环线程，判断flag是否为true，false表示未执行完，true表示执行完了。

     ![image-20231227215621095](C:\Users\17653\AppData\Roaming\Typora\typora-user-images\image-20231227215621095.png)

2. 释放锁的时候，有可能先判断出来是自己的锁，但key提前过期，最后还是释放了别人的锁。同时B抢到资源执行业务方法，并释放了锁，这时呢C又进来了，执行了业务方法并释放了锁。![image-20231227215649910](C:\Users\17653\AppData\Roaming\Typora\typora-user-images\image-20231227215649910.png)

   解决方案：Redis + lua脚本实现，可理解为一个事务

   

#### Redisson实现分布式锁

Java客户端，数据网格

实现了很多Java里支持的接口和数据结构



Redisson是一个Java操作Redis的客户端，**提供了大量的分布式数据集来简化对Redis的操作和使用，可以让开发者像使用本地集合一样使用Redis，完全感受不到Redis的存在



##### 2种引入方式

1. spring boot starter引入（不推荐，版本迭代太快，容易冲突）htps:业github.com/redisson/redisson/tree/master/redisson-spring-boot-starter
2. 直接引入：https://github.com/redisson/redisson#quick-start

示例代码：

```
// 数据存在 redis 的内存中
        RList<Object> rList = redissonClient.getList("test-list");
        System.out.println("rList:" + rList.get(0));
        rList.remove(0);
        // map
        Map<String, Integer> map = new HashMap();
        map.put("yupi", 10);
        map.get("yupi");

        RMap<String, Integer> rMap = redissonClient.getMap("test-map");
        rMap.put("yupi", 10);
        rMap.get("yupi");
```



##### 引入Redisson依赖：

```
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.17.5</version>
        </dependency>
```

##### 编写Redisson配置类

```
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private String host;
    private String port;

    @Bean
    public RedissonClient redissonClient(){
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);//设置单个服务器，设置地址，选择数据库
        // 2. 创建势力
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
```

##### 定时任务 + 锁

1. waitTime设置为0，只抢一次，抢不到就放弃
2. 注意释放锁要写在finally中

##### 实现代码

```
    public void watchDogTest(){
        String doCacheLockId = String.format("%s:precachejob:docache:lock", RedisConstant.SYSTEM_ID);
        RLock lock = redissonClient.getLock(doCacheLockId);
        try {
            // 只有一个线程能够获取锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                // to do
                doSomething // 业务代码
                System.out.println(Thread.currentThread().getId() + "我拿到锁了");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally { // 不管所是否会失效都会执行下段保证释放锁
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) { // 判断当前的锁是不是当前这个线程加的锁，每次抢锁时都会有一个线程Id，
                // 这个Id会存在redis中，验证线程的id就好了
                System.out.println(Thread.currentThread().getId() + "锁已经释放了");
                lock.unlock(); // 执行业务逻辑后，要释放锁
            }
        }
    }
```



##### 看门狗机制

> Redisson帮我们实现了续期机制

可以理解为给你要执行的方法添加监听器，当你方法未执行完会进行续期，重置Redis锁的过期时间

原理：

1. 监听当前线程，每十秒续期一次，过期时长为30s（补到30s）
2. 如果线程挂掉（**注意**：debug模式也会被它当成服务器宕机），则不会续期

##### 为什么看门狗能够自动续期还得手动释放呢，另外续期时间为30s，而不是永久？

怕宕机，防止Redis宕机导致锁未释放



Redisson 分布式锁的watch dog自动续期机制：https://blog.csdn.net/qq_26222859/article/details/79645203

##### 看门狗失效原因：

程序debug导致watch dog认为redis宕机，从而失效了



**分布式锁导致其他服务器数据不一致**

使用红锁（redlock）

缓存，缓存预热，分布式锁（redisson）



### 伙伴匹配站第五天

#### 为什么需要请求包装类

1.请求参数名称和实体类不一样

2.有一些参数用不到，如果生成接口文档，会增加理解成本



#### 为什么要包装类

可能有些字段需要隐藏，不能返回给前端

或者有些字段某些方法是不关系的



#### 系统接口设计

1. 请求参数不能为空

2. 是否登录，未登录不能创建

3. 检验信息

   1. 队伍人数>1且<=20
   2. 队伍标题<=20
   3. 描述<=512
   4. status是否公开（int）不传默认为0（公开）
   5. 如果status是加密状态，一定要有密码，且密码存在<=32
   6. 超时时间必须大于当前时间
   7. 校验用户最多创建5个队伍

   

   4.插入队伍信息到队伍表

   5.插入用户=>队伍关系到关系表

   

   根据队伍的三种状态，建立一个枚举

   同时写了一个根据值获取枚举的·静态方法

   ```java
   public enum TeamStatusEnum {
   
       PUBLIC(0,"公开"),
       PRIVATE(1,"私有"),
       SECRET(2,"加密");
   
       private int value;
       private String text;
   
       public static TeamStatusEnum getEnumByValue(Integer value){
           if (value==null){
               return null;
           }
           TeamStatusEnum[] values = TeamStatusEnum.values();
           for (TeamStatusEnum teamStatusEnum : values) {
               if (teamStatusEnum.getValue() == value){
                   return teamStatusEnum;
               }
           }
           return null;
       }
   
       TeamStatusEnum(int value, String text) {
           this.value = value;
           this.text = text;
       }
   
       public int getValue() {
           return value;
       }
   
       public void setValue(int value) {
           this.value = value;
       }
   
       public String getText() {
           return text;
       }
   
       public void setText(String text) {
           this.text = text;
       }
   }
   ```

   在进行接口测试时，创建队伍个数不能超过5个出现错误

   判断条件应为

   ```java
   if (hasTeamNum>=5){
       throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍数量超过限制");
   }
   ```

   

   #### 事务注解

   ```java
   @Transactional(rollbackFor = Exception.class)
   ```

   要么数据操作都成功，要么都失败

   

   ### 伙伴伙伴匹配站第六天

   #### 2.查询队伍列表

   **分页展示队伍列表，根据名称搜索队伍，在信息流中不展示已过期的队伍**

4. 从请求参数中取出队伍名称，如果存在则作为查询条件

5. 不展示已过期的队伍（根据过期时间筛选）

6. 可以通过某个**关键词**同时对名称和描述查询

7. **只有管理员才能查看加密还有非公开的房间**

8. 关联查询已加入队伍的用户信息



### 3修改用户信息

1. 判断请求参数是否为空

2. 查询队伍是否存在

3. 中有管理员或者队伍创建者可以修改

4. 如果用户传入的新值和老值一致，就不用update了（可自行实现，降低数据库的使用次数）

5. **如果队伍状态改为加密，必须要有密码**

6. 更新成功

   

### 4.用户可以加入队伍

其他人  ，未满  ， 未过期  ， 允许加入多个队伍 但是要有一个上限

1. 用户最多加入5个队伍
2. 只能加入未满，未过期
3. 不能重复加入已加入的队伍（幂等性）
4. 禁止加入私有的队伍
5. 如果加入的队伍是加密的，必须 密码匹配才可以
6. 新增队伍，关联用户信息



### 伙伴匹配站第七天

#### 5.用户可以退出队伍

​		如果队长退出,权限转移给第二早加入的用户---先来后到

请求参数: 队伍id

1. 校验请求参数

2. 校验队伍是否存在

3. 校验我是否已加入队伍

4. 如果队伍

   1. 只剩一人,队伍解散

   2. 如果还有其他人

      1. 如果是队长退出队伍,权限转移给第二早加入的用户

         -----先来后到(可以根据id判断)

         > 只用取id最小的2条

      2. 非队长,自己退出队伍

BUG:

```java
if(team.getUserId()==userId)//这样写是判false
if (team.getUserId().equals(userId))//必须写成equals判断
```



#### 注意涉及到多张表的增删改查，一定要添加事务回滚

```Java
@Transactional(rollbackFor = Exception.class)
```

#### 6.队长可以解散队伍

请求参数：队伍id

业务流程：

1. 校验请求参数
2. 校验队伍是否存在
3. 校验是不是队伍的队长
4. 移除所有加入队伍的关联信息
5. 删除队伍



#### 7.分享队伍=》邀请其他用户加入队伍

业务流程：

1. 生成分享链接（分享二维码）
2. 用户访问链接，点击可以加入


















#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
