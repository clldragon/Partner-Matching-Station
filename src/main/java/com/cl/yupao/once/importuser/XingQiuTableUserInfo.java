package com.cl.yupao.once.importuser;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class XingQiuTableUserInfo {
    /*
    * 用户呢称
    * */
    @ExcelProperty("成员昵称")
    private String username;

    /*
    * 星球编号
    * */
    @ExcelProperty("成员编号")
    private String planetCode;


}
