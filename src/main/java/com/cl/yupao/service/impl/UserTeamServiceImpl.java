package com.cl.yupao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cl.yupao.model.domain.UserTeam;
import com.cl.yupao.service.UserTeamService;
import com.cl.yupao.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 陈
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-06-11 13:46:30
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




