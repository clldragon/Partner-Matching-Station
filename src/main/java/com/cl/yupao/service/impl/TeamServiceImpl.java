package com.cl.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cl.yupao.common.ErrorCode;
import com.cl.yupao.exception.BusinessException;
import com.cl.yupao.mapper.TeamMapper;
import com.cl.yupao.model.domain.Team;
import com.cl.yupao.model.domain.User;
import com.cl.yupao.model.domain.UserTeam;
import com.cl.yupao.model.dto.TeamQuery;
import com.cl.yupao.model.enums.TeamStatusEnum;
import com.cl.yupao.model.request.TeamJoinRequest;
import com.cl.yupao.model.request.TeamQuitRequest;
import com.cl.yupao.model.request.TeamUpdateRequest;
import com.cl.yupao.model.vo.TeamUserVo;
import com.cl.yupao.model.vo.UserVo;
import com.cl.yupao.service.TeamService;
import com.cl.yupao.service.UserService;
import com.cl.yupao.service.UserTeamService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 队伍 服务实现类
 * </p>
 *
 * @author cl
 * @since 2024-06-11
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addTeam(Team team, User loginUser) {
        //1. 请求参数不能为空
        if (team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不能创建
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final Long userId = loginUser.getId();
        //3. 检验信息
        //   1. 队伍人数>1且<=20
        int  maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum<1||maxNum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不符合要求");
        }
        //   2. 队伍标题<=20
        String name=team.getName();
        if (StringUtils.isBlank(name) || name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不符合要求");
        }
        //   3. 描述<=512
        String desc=team.getDescription();
        if (StringUtils.isNotBlank(desc) && desc.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //   4. status是否公开（int）不传默认为0（公开）
        int status=Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //   5. 如果status是加密状态，一定要有密码，且密码存在<=32
        String password=team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)){
            if (StringUtils.isBlank(password) || password.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
        //   6. 超时时间必须大于当前时间
       Date expireTime=team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"当前时间>超时时间");
        }
        //   7. 校验用户最多创建5个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = count(queryWrapper);
        if (hasTeamNum>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍数量超过限制");
        }
        //   4.插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        //   5.插入用户=>队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        if (!save){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        return teamId;
    }

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id!=null&&id>0){
            queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)){
            queryWrapper.in("id",idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw->qw.like("name",searchText)).or().like("description",searchText);
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum!=null&&maxNum>0){
                queryWrapper.eq("maxNum",maxNum);
            }
            //根据创建人id查询
            Long userId = teamQuery.getUserId();
            if (userId!=null&&userId>0){
                queryWrapper.eq("userId",userId);
            }
            //根据队伍状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum==null){
                statusEnum=TeamStatusEnum.PUBLIC;
            }
            //todo 这有一个BUG，如果用户自己查询自己加入和创建的队伍时，调用这个接口时，除公开的队伍，其余都被隐藏。
            if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status",statusEnum.getValue());
        }
        //不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw->qw.gt("expireTime",new Date())).or().isNull("expireTime");
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVoList =new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);

            //脱敏用户信息
            if (user!=null){
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user,userVo);
                teamUserVo.setCreateUser(userVo);

            }
            teamUserVoList.add(teamUserVo);
        }

        return teamUserVoList;

    }

    /**
     * 修改队伍
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean teamUpdate(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id==null || id<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //只有管理员和队伍创建者可以修改
        if (oldTeam.getUserId()!=loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //队伍状态如果为加密，则密码必须存在
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)){
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须要有密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        boolean result = this.updateById(updateTeam);
        return result;
    }

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //队伍未过期
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime!=null && team.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        //禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }
        //如果队伍是加密的，则密码需匹配
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if (StringUtils.isBlank(password) || !team.getPassword().equals(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不匹配");
            }
        }
        //接下来的业务场景都需要查询数据库，建议写在一起
        //最多创建和加入5个队伍
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = userTeamService.count(queryWrapper);
        if (hasTeamNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多加入5个队伍");
        }
        //用户不能加入相同队伍
        queryWrapper=new QueryWrapper<UserTeam>();
        queryWrapper.eq("userId",userId);
        queryWrapper.eq("teamId",teamId);
        long count = userTeamService.count(queryWrapper);
        if (count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入相同的队伍");
        }
        //队伍人数不能超过最大人数
        Integer maxNum = team.getMaxNum();
        long hasUserNum = getHasUserNum(teamId);
        if (hasUserNum>=maxNum){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        return result;
    }



    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
   // Java

/**
 * 退出队伍操作，校验用户是否加入队伍并处理队伍解散或转让队长权限
 */
 public Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest==null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = loginUser.getId();
        Long teamId = teamQuitRequest.getTeamId();
        //校验队伍是否存在
        Team team = getTeamById(teamId);
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        //校验用户是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count==0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户未加入队伍");
        }
        //用户已加入队伍
        //判断队伍人数
        long hasUserNum = getHasUserNum(teamId);
        if (hasUserNum==1){
            //如果队伍只剩下1人，队伍解散
            this.removeById(teamId);
        }else {
            //队伍人数大于1
            //判断用户是否为队长
            if (team.getUserId().equals(userId)){
                //如果是队长
                QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId",teamId);
                //再sql语句最后插入"order by id asc"按id升序排序，并取前两个
                queryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextLeaderUserId = nextUserTeam.getUserId();
               //更新队伍队长

                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextLeaderUserId);
                boolean result = this.updateById(updateTeam);
                if (!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队长失败");
                }
            }
            //非队长，自己退出队伍
        }
        //移除关系
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        return  remove;
    }

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteTeam(Long id, User loginUser) {
        //校验队伍是否存在
        Team team = getTeamById(id);
        Long teamId = team.getId();
        //校验队伍的队长
        Long userId = loginUser.getId();
        if(!team.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无操作权限");
        }
        //移除当前队伍所有的关联关系
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //删除当前队伍
        boolean remove = this.removeById(teamId);
        return remove;
    }

    /**
     * 根据id获取队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId ==null || teamId <=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        return team;
    }

    /**
     * 获取队伍人数
     * @param teamId
     * @return
     */
    private long getHasUserNum(Long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long hasUserNum = userTeamService.count(userTeamQueryWrapper);
        return hasUserNum;
    }

}
