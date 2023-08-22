package com.zhj.search.job.cycle;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.zhj.search.esdao.PostEsDao;
import com.zhj.search.mapper.PostMapper;
import com.zhj.search.model.dto.post.PostEsDTO;
import com.zhj.search.model.entity.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.zhj.search.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 增量同步帖子到 es
 *
 * @author zhj
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class IncSyncPostToEs {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostEsDao postEsDao;

    @Resource
    private PostService postService;


    /**
     * 每天晚上三点拉取数据
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void task() {
        // 1. 抓取数据
        String json = "{\"id_type\":2,\"client_type\":2608,\"sort_type\":200,\"cursor\":\"0\",\"limit\":20}";
        String result = HttpRequest.post("https://api.juejin.cn/recommend_api/v1/article/recommend_all_feed?aid=2608&uuid=7204751966281729575&spider=0")
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
            post.setId(Long.valueOf(article_info.getStr("article_id")));
            post.setTitle(article_info.getStr("title"));
            post.setContent(article_info.getStr("brief_content"));
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1682051319042527234L);
            postList.add(post);
        }
        log.info("postList:{}", postList);
        try {
            // 3. 数据入库
            boolean b = postService.saveBatch(postList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 每分钟执行一次
     */
    //@Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<Post> postList = postMapper.listPostWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(postList)) {
            log.info("no inc post");
            return;
        }
        List<PostEsDTO> postEsDTOList = postList.stream()
                .map(PostEsDTO::objToDto)
                .collect(Collectors.toList());
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("IncSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            postEsDao.saveAll(postEsDTOList.subList(i, end));
        }
        log.info("IncSyncPostToEs end, total {}", total);
    }
}
