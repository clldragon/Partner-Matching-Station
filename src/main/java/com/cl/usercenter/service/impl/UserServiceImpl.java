package com.cl.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cl.usercenter.model.domain.User;
import com.cl.usercenter.service.UserService;
import com.cl.usercenter.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author 陈
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-05-20 15:21:08
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    //不存在特殊字符的正则表达式
    private final static  String VALIDPATTER = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        //非空
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return -1;
        }
        //账号长度不小于4位
        if (userAccount.length()<4){
            return -1;
        }
        //密码与校验密码不小于8位
        if (userPassword.length()<8||checkPassword.length()<8){
            return -1;
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
            return -1;
        }
        //2.加密
        final String SALT="cl";
        String encrptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println("加密后的密码："+encrptPassword);

        //3.插入数据

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encrptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult){
            return -1;
        }

        return user.getId();
    }
}




