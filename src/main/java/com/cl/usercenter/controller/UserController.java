package com.cl.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cl.usercenter.common.BaseResponse;
import com.cl.usercenter.common.ErrorCode;
import com.cl.usercenter.common.ResultUtils;
import com.cl.usercenter.contant.UserContant;
import com.cl.usercenter.exception.BusinessException;
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
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode=userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /*
    * 用户登录
    * */
    @PostMapping("/login")
    public BaseResponse <User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest==null){
          return ResultUtils.error(ErrorCode.PARAMS_ERROR) ;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
           return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /*
    * 注销用户
    * */
    @PostMapping("/logout")
    public BaseResponse<Integer>  userLogout(HttpServletRequest request){
        if (request==null){
         throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /*
    * 获取当前登录用户
    * */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser=(User) userObj;
        if (currentUser==null){
           throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        //todo 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /*
    * 用户查询
    * */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
       if (!isAdmin(request)){
          throw new BusinessException(ErrorCode.NO_AUTH);
       }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /*
    * 删除用户
    * */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
      if (!isAdmin(request)){
          throw new BusinessException(ErrorCode.NO_AUTH);
      }
        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
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
