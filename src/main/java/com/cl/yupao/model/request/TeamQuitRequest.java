package com.cl.yupao.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7387123379627094003L;
    /*
    * 队伍id
    * */
    private Long teamId;
}
