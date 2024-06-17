package com.cl.yupao.service;

import com.cl.yupao.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 陈
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-05-20 15:21:08
*/
public interface UserService extends IService<User> {


    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param checkPassword 星球编号
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     * @return
     */

    int userLogout(HttpServletRequest request);

    /**
     * 启用和禁用用户
     * @param userStatus
     * @param id
     * @return
     */
    Integer startOrStop(Integer userStatus, Long id);

    /**
     * 根据标签搜索用户
     * @param tagsNameList
     * @return
     */
    List<User> searchUserByTags(List<String> tagsNameList);

    /**
     * 更新用户信息表
     *
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
     boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
     boolean isAdmin(User loginUser);

    /**
     * 根据标签（相似度）匹配用户
     * @param number
     * @param loginUser
     * @return
     */
    List<User> matchUsers(Long number, User loginUser);
}
