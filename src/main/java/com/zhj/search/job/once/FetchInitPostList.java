package com.zhj.search.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhj.search.model.entity.Post;
import com.zhj.search.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全量同步帖子到 es
 *
 * @author zhj
 * 
 */
@Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;


    @Override
    public void run(String... args) {
        // 1. 抓取数据
        String json = "{\"id_type\":2,\"client_type\":2608,\"sort_type\":200,\"cursor\":\"0\",\"limit\":20}";
        String result = HttpRequest.post("https://api.juejin.cn/recommend_api/v1/article/recommend_all_feed?aid=2608&uuid=7123262301927228965&spider=0")
                .body(json)
                .execute().body();
        System.out.println(result);
        // 2. json 转对象
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        JSONArray data = (JSONArray) map.get("data");
        System.out.println(data);
        List<Post> postList = new ArrayList<>();
        for (Object item : data) {
            JSONObject tempItem = (JSONObject) item;
            // 判空
            if (tempItem.isNull("item_info") ||
                    tempItem.getJSONObject("item_info").isNull("article_info") ||
                    tempItem.getJSONObject("item_info").isNull("tags")) {
                continue;
            }
            JSONObject item_info = tempItem.getJSONObject("item_info");
            JSONObject article_info = item_info.getJSONObject("article_info");
            // 获取tags
            List<String> tagList = new ArrayList<>();
            if (!item_info.get("tags").equals(null)) {
                JSONArray tags = item_info.getJSONArray("tags");
                for (Object tag : tags) {
                    JSONObject tempTag = (JSONObject) tag;
                    tagList.add(tempTag.getStr("tag_name"));
                }
            }
            // 新增post
            Post post = new Post();
            post.setTitle(article_info.getStr("title"));
            post.setContent(article_info.getStr("brief_content"));
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1682051319042527234L);
            postList.add(post);
        }
//        System.out.println(postList);
        // 3. 数据入库
        boolean b = postService.saveBatch(postList);
        if(b){
            log.info("获取初始化帖子成功，条数={}",data.size());
        }else{
            log.error("获取帖子失败");
        }
    }
}
