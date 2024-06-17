package com.cl.yupao.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/*
* 通用删除请求参数
* */
@Data
public class DeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5786147840753183904L;
    /*
    * id
    * */

    private Long id;

}
