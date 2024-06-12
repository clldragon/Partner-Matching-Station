package com.cl.yupao.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/*
* 队伍更新请求封装类
* */
@Data
public class TeamUpdateRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = -4872486777656685342L;
    /*
    * id
    * */

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
     * 过期时间
     */

    private Date expireTime;


    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */

    private Integer status;

    /**
     * 密码
     */

    private String password;



}
