package com.cl.yupao.service;

import com.alibaba.fastjson.JSON;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

/*
* 测试用算法类根据标签计算相似用户
* */
@SpringBootTest
public class MatchUserTest {

    @Test
        //编辑距离算法（不完美）
    void matchTest(){
        String tagstr1="java,大一,男";
        String tagstr2="java,大二,男";
        String tagstr3="java,大一,女";
        int i = minDistance(tagstr2, tagstr3);
        System.out.println(i);
    }

    public static int minDistance(String word1, String word2) {
        if (word1 == null || word2 == null) {
            throw new RuntimeException("参数不能为空");
        }
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];
        //初始化DP数组
        for (int i = 0; i <= word1.length(); i++) {
            dp[i][0] = i;
        }
        for (int i = 0; i <= word2.length(); i++) {
            dp[0][i] = i;
        }
        int cost;
        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    cost = 0;
                } else {
                    cost = 1;
                }
                dp[i][j] = min(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost);
            }
        }
        return dp[word1.length()][word2.length()];
    }

    private static int min(int x, int y, int z) {
        return Math.min(x, Math.min(y, z));
    }

    @Test
    void matchByCos(){
        String str1="java,大一,男";
        String str3="java,大二,男";
        String str2="java,大二,女";
        String str4="python,大一,男";
        CosAlgorithm(str1, str2);

    }

    //余弦相似度算法（可以完美）
    public static void CosAlgorithm(String str1, String str2) {
        StopRecognition filter = new StopRecognition();
        //过滤掉标点
        filter.insertStopNatures("w");
        //分词-统计词频
        Map<String,Integer> map1= new HashMap<>();
        ToAnalysis.parse(str1).recognition(filter).forEach(item -> {
            //没有则赋初始值，有则+1
            if (map1.get(item.getName()) == null){
                map1.put(item.getName(),1);
            }else {
                map1.put(item.getName(),map1.get(item.getName())+1);
            }
        });
        Map<String,Integer> map2 = new HashMap<>();
        ToAnalysis.parse(str2).recognition(filter).forEach(item -> {
            //没有则赋初始值，有则+1
            if (map2.get(item.getName()) == null){
                map2.put(item.getName(),1);
            }else {
                map2.put(item.getName(),map2.get(item.getName())+1);
            }
        });
        System.out.println("map1="+ JSON.toJSONString(map1));
        System.out.println("map2="+ JSON.toJSONString(map2));
        Set<String> set1 = map1.keySet();
        Set<String> set2 = map2.keySet();
        Set<String> setAll = new HashSet<>();
        setAll.addAll(set1);
        setAll.addAll(set2);
        System.out.println("all="+JSON.toJSONString(setAll));
        List<Integer> list1 = new ArrayList<>(setAll.size());
        List<Integer> list2 = new ArrayList<>(setAll.size());
        //构建向量
        setAll.forEach(item ->{
            if (set1.contains(item)){
                list1.add(map1.get(item));
            }else {
                list1.add(0);
            }

            if (set2.contains(item)){
                list2.add(map2.get(item));
            }else {
                list2.add(0);
            }
        });
        //计算余弦相似度
        int sum =0;
        long sq1 = 0;
        long sq2 = 0;
        double result = 0;
        for (int i =0;i<setAll.size();i++){
            sum +=list1.get(i)*list2.get(i);
            sq1 += list1.get(i)*list1.get(i);
            sq2 += list2.get(i)*list2.get(i);
        }
        result = sum/(Math.sqrt(sq1)*Math.sqrt(sq2));
        System.out.println("余弦相似度="+result);
    }


}
