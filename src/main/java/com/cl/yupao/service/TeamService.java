package com.cl.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cl.yupao.model.domain.Team;
import com.cl.yupao.model.domain.User;
import com.cl.yupao.model.dto.TeamQuery;
import com.cl.yupao.model.request.TeamJoinRequest;
import com.cl.yupao.model.request.TeamQuitRequest;
import com.cl.yupao.model.request.TeamUpdateRequest;
import com.cl.yupao.model.vo.TeamUserVo;

import java.util.List;

/**
 * <p>
 * 队伍 服务类
 * </p>
 *
 * @author cl
 * @since 2024-06-11
 */
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    Long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 修改队伍
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean teamUpdate(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(Long id,User loginUser);
}
