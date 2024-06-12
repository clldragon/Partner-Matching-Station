package com.cl.yupao.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/*
* 通用请求分页参数
* */
@Data
public class PageRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = -2346615806931885980L;
    /*
    * 页面大小
    * */
    protected  int pageSize=10;

    /*
    * 当前是第几页
    * */
    protected int pageNum=1;
}
