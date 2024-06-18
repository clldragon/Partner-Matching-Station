package com.cl.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cl.yupao.common.ErrorCode;
import com.cl.yupao.exception.BusinessException;
import com.cl.yupao.mapper.UserMapper;
import com.cl.yupao.model.domain.User;
import com.cl.yupao.service.UserService;
import com.cl.yupao.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cl.yupao.contant.UserContant.ADMIN_ROLE;
import static com.cl.yupao.contant.UserContant.USER_LOGIN_STATE;

/**
* @author 陈
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-05-20 15:21:08
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    //不存在特殊字符的正则表达式
    private static final String VALIDPATTER = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    //盐值：混淆密码
    public static final String SALT = "cl";
    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param planetCode
     * @return
     */

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1.校验
        //非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        //账号长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        //密码与校验密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //星球编号不大于5
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        //账户不能包含特殊字符
        Matcher matcher = Pattern.compile(VALIDPATTER).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }
        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        Long count1 = userMapper.selectCount(queryWrapper);
        if (count1 > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
        }
        //2.加密
        String encrptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println("加密后的密码：" + encrptPassword);

        //3.插入数据

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encrptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }

        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        //非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账号长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //密码与校验密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //账户不能包含特殊字符
        Matcher matcher = Pattern.compile(VALIDPATTER).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.加密
        String encrptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println("加密后的密码：" + encrptPassword);
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encrptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //3.用户脱敏
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     *  用户脱敏
     * @param originUser
     * @return
     */
    public User getSafetyUser(User originUser) {
        //一定要判空
        if (originUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 启用和禁用用户
     * @param userStatus
     * @param id
     * @return
     */
    @Override
    public Integer startOrStop(Integer userStatus, Long id) {
        //判断用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("Id", id);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("userStatus", userStatus);
        int result = userMapper.update(updateWrapper);

        return result;
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签查询用户（内存过滤）
     *
     * @param tagsNameList
     * @return
     */
    public List<User> searchUserByTags(List<String> tagsNameList) {
        if (CollectionUtils.isEmpty(tagsNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //内存查询
        QueryWrapper<User> queryWrapper= new QueryWrapper<>();
        //1.查询所有用户
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2。在内存中判断是否有符合要求的标签
        List<User> safeUser = userList.stream().filter(user -> {
            String tagStr = user.getTags();
            if (StringUtils.isBlank(tagStr)){
                return false;
            }
            Set<String> TempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
            }.getType());
            //1. 使用Gson库中的fromJson方法将tagStr字符串解析为Set<String>类型的对象。
            //2. 使用TypeToken类的匿名子类来指定要解析的数据类型为Set<String>。
            //3. 调用getType()方法获取TypeToken对象的类型。
            //4. 将tagStr字符串转换为Set<String>类型的TempTagNameSet对象。

          TempTagNameSet = Optional.ofNullable(TempTagNameSet).orElse(new HashSet<>());
            for (String tagsName : tagsNameList) {
                if (!TempTagNameSet.contains(tagsName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());

        return safeUser;
    }

    /**
     * 更新用户信息表
     *
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userMapper.updateById(user);
        return result;
    }

    /**
     * 获取当前登录信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request==null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
       if (userObj==null){
           throw new BusinessException(ErrorCode.NO_AUTH);
       }
        return (User) userObj;
    }

    /**
     * 是否为管理员
      * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可以查询
        Object userobj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User) userobj;
        if (user==null||user.getUserRole()!= ADMIN_ROLE){
            return false;
        }
        return true;
    }

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser!=null && loginUser.getUserRole()== ADMIN_ROLE;
    }

    /**
     * 根据标签（相似度）匹配用户
     * @param number
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(Long number, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        Long userId = loginUser.getId();
        String tags = loginUser.getTags();
        Gson gson = new Gson();
       List<String> tagsList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
       //创建用户列表下标=》相似值
        List<Pair<User, Long>> list = new ArrayList<>();
        //将当前用户与其他用户计算
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags)||user.getId().equals(userId)){
                continue;
            }
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance =AlgorithmUtils.minDistance(tagsList, userTagsList);
            list.add(new Pair<>(user,distance));
        }
        //按编辑距离从小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream().sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(number)
                .collect(Collectors.toList());
        //按顺序的UserIdList
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        //根据id查询用户
        QueryWrapper<User> querywrapper = new QueryWrapper<>();
        querywrapper.in("id",userIdList);
        List<User> oldUserList = this.list(querywrapper);
        //in查询是没有顺序的，所以将其存储在Map中
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = oldUserList.stream().map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User>finalUserList=new ArrayList<>();
        for (Long id : userIdList) {
            finalUserList.add(userIdUserListMap.get(id).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户（SQl版）
     * @param tagsNameList
     * @return
     */
    @Deprecated//表示某个方法、类或接口已经过时，不推荐使用
    private List<User> searchUserByTagsBySQl(List<String> tagsNameList) {
        //SQl查询
        if (CollectionUtils.isEmpty(tagsNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagsName : tagsNameList) {
            queryWrapper.like("tags", tagsName);
        }
        List<User> users = userMapper.selectList(queryWrapper);
        List<User> safeUserList = users.stream().map(this::getSafetyUser).collect(Collectors.toList());
        return safeUserList;
    }

}




