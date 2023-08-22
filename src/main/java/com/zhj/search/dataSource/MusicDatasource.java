package com.zhj.search.dataSource;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.search.model.entity.Music;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/22 13:00
 */
@Service("music_Service")
public class MusicDatasource implements DataSource {


    @Override
    public Page<Music> doSearch(String searchText, long pageNum, long pageSize) {
        HttpCookie cookie = HttpRequest.get("https://music.163.com/").execute().getCookies().get(0);
        // 1. 抓取数据
        String result = HttpRequest.post("http://music.163.com/api/search/pc")
                .cookie(cookie)
                .form("s", searchText)
                .form("offset", 0)
                .form("type", 1)
                .form("limit", pageSize)
                .execute().body();
        System.out.println(result);
        // 2. json 转对象
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        System.out.println(map);
        JSONObject data = (JSONObject) map.get("result");
        JSONArray songs = (JSONArray) data.get("songs");
        Page<Music> page = new Page<>(pageNum, pageSize);
        List<Music> records = new ArrayList<>();
        for (Object song : songs) {
            JSONObject tempRecord = (JSONObject) song;
            Music music = new Music();
            music.setName(tempRecord.getStr("name"));
            String json = tempRecord.getBeanList("artists", String.class).get(0);
            Map<String, String> artists = JSONUtil.toBean(json, Map.class);
            music.setAuthor(artists.get("name"));
            Map<String,String> album = tempRecord.getBean("album", Map.class);
            music.setPicUrl(album.get("blurPicUrl"));
            music.setMp3Url(tempRecord.getStr("mp3Url"));
            //System.out.println(music);
            records.add(music);
            if (records.size() >= pageSize) {
                break;
            }
        }
        page.setRecords(records);
        return page;
    }
}
