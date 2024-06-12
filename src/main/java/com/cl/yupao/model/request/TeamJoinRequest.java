package com.cl.yupao.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/*
* 加入队伍请求封装类
* */
@Data
public class TeamJoinRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1909169786182582291L;
    /*
    * id
    * */
    private Long teamId;
    
    /*
    * 密码
    * */
    private String password;
}
