package com.cl.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cl.usercenter.common.ErrorCode;
import com.cl.usercenter.exception.BusinessException;
import com.cl.usercenter.model.domain.User;
import com.cl.usercenter.service.UserService;
import com.cl.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cl.usercenter.contant.UserContant.USER_LOGIN_STATE;

/**
* @author 陈
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-05-20 15:21:08
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    //不存在特殊字符的正则表达式
    private  static final  String VALIDPATTER = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    //盐值：混淆密码
    public   static final String SALT="cl";
    @Resource
    private UserMapper userMapper;

    /*
    * 用户注册
    * */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        //1.校验
        //非空
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //账号长度不小于4位
        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        //密码与校验密码不小于8位
        if (userPassword.length()<8||checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        //星球编号不大于5
        if (planetCode.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }
        //账户不能包含特殊字符
        Matcher matcher = Pattern.compile(VALIDPATTER).matcher(userAccount);
        if (matcher.find()){
           return -1;
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            return -1;
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.count(queryWrapper);
        if (count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户重复");
        }
        //星球编号不能重复
       queryWrapper= new QueryWrapper<>();
       queryWrapper.eq("planetCode",planetCode);
        Long count1 = userMapper.selectCount(queryWrapper);
        if (count1>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号重复");
        }
        //2.加密
        String encrptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println("加密后的密码："+encrptPassword);

        //3.插入数据

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encrptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult){
            return -1;
        }

        return user.getId();
    }

    /*
    * 用户登录
    * */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        //非空
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账号长度不小于4位
        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //密码与校验密码不小于8位
        if (userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //账户不能包含特殊字符
        Matcher matcher = Pattern.compile(VALIDPATTER).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.加密
        String encrptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println("加密后的密码："+encrptPassword);
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encrptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user==null){
            log.info("user login failed,userAccount cannot match userPassword");
           throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //3.用户脱敏
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);

        return safetyUser;
    }
    /*
    * 用户脱敏
    * */
    public User getSafetyUser(User originUser){
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
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




