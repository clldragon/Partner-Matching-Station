package com.cl.yupao.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
@Data
/*
* 队伍用户信息封装类（脱敏）
* */
public class TeamUserVo {

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

   /*
   * 更新时间
   * */
    private LocalDateTime updateTime;

    /*
    * 创建人用户信息
    * */
   private UserVo CreateUser;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;

}
