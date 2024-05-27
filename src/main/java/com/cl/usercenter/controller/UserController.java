package com.cl.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cl.usercenter.contant.UserContant;
import com.cl.usercenter.model.domain.User;
import com.cl.usercenter.model.domain.request.UserLoginRequest;
import com.cl.usercenter.model.domain.request.UserRegisterRequest;
import com.cl.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.cl.usercenter.contant.UserContant.ADMIN_ROLE;
import static com.cl.usercenter.contant.UserContant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author 陈
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /*
    * 用户注册
    * */
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest==null){
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }
      return   userService.userRegister(userAccount,userPassword,checkPassword);
    }

    /*
    * 用户登录
    * */
    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest==null){
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        return  userService.userLogin(userAccount,userPassword,request);
    }

    /*
    * 注销用户
    * */
    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request){
        if (request==null){
            return -1;
        }
        return userService.userLogout(request);
    }

    /*
    * 获取当前登录用户
    * */
    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser=(User) userObj;
        if (currentUser==null){
            return null;
        }
        Long userId = currentUser.getId();
        //todo 校验用户是否合法
        User user = userService.getById(userId);
        return userService.getSafetyUser(user);
    }

    /*
    * 用户查询
    * */
    @GetMapping("/search")
    public List<User> searchUsers(String username,HttpServletRequest request){
       if (!isAdmin(request)){
           return new ArrayList<>();
       }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return list;
    }

    /*
    * 删除用户
    * */
    @PostMapping("/delete")
    public boolean deleteUser(@RequestBody long id,HttpServletRequest request){
      if (!isAdmin(request)){
          return false;
      }
        if (id<=0){
            return false;
        }
      return userService.removeById(id);
    }

    /*
    * 是否为管理员
    * */
    private static boolean isAdmin(HttpServletRequest request) {
        //仅管理员可以查询
        Object userobj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User) userobj;
        if (user==null||user.getUserRole()!= ADMIN_ROLE){
            return false;
        }
        return true;
    }

}
