package com.zhj.search;

import com.alibaba.fastjson.JSON;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import org.aspectj.weaver.ast.Var;

import java.util.List;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/21 18:50
 */
public class Test {

    public static void main(String[] args) {
        Gson gson = new Gson();
        //String tags="[前端,面试,性能优化]";
        String tags="[后端,IntelliJ IDEA,Java]";
        Object o = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        System.out.println(o);
    }
}
