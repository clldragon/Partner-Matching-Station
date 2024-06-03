package com.cl.yupao.once.importuser;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/*
* 导入星球数据到数据库
* */

public class ImportXingQiuUser {
    public static void main(String[] args) {
        String fileName = "D:\\gitee\\yupao\\yupao-backed\\src\\main\\resources\\testExcel.xlsx";
        List<XingQiuTableUserInfo> totalDataList =
                EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : totalDataList) {
            System.out.println(xingQiuTableUserInfo);
        }
        System.out.println("总条数：" + totalDataList.size());
        //过滤掉昵称相同的数据
        Map<String, List<XingQiuTableUserInfo>> listMap = totalDataList.stream()
                .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                .collect(Collectors.groupingBy(XingQiuTableUserInfo::getUsername));
        System.out.println("过滤后的总条数："+listMap.keySet().size());

        //todo 功能还未开发完成
    }
}