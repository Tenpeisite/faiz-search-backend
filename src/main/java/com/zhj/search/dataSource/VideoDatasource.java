package com.zhj.search.dataSource;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.search.model.entity.Video;
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
@Service("video_Service")
public class VideoDatasource implements DataSource {


    @Override
    public Page<Video> doSearch(String searchText, long pageNum, long pageSize) {
        String url1 = "https://www.bilibili.com/";
        String url2 = String.format("https://api.bilibili.com/x/web-interface/search/type?search_type=video&keyword=%s&page=%s&page_size=%s", searchText, pageNum, pageSize);
        HttpCookie cookie = HttpRequest.get(url1).execute().getCookie("buvid3");

        //String body = null;
        //try {
        //    body = retryer.call(() -> HttpRequest.get(url2)
        //            .cookie(cookie)
        //            .execute().body());
        //} catch (Exception e) {
        //    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重试失败");
        //}
        String body = HttpRequest.get(url2)
                .cookie(cookie)
                .execute().body();

        Map map = JSONUtil.toBean(body, Map.class);
        Map data = (Map) map.get("data");
        JSONArray videoList = (JSONArray) data.get("result");
        Page<Video> page = new Page<>(pageNum, pageSize);
        List<Video> VideoList = new ArrayList<>();
        for (Object video : videoList) {
            JSONObject tempVideo = (JSONObject) video;
            Video Video = new Video();
            Video.setUpic(tempVideo.getStr("upic"));
            Video.setAuthor(tempVideo.getStr("author"));
            Video.setPubdate(tempVideo.getInt("pubdate"));
            Video.setArcurl(tempVideo.getStr("arcurl"));
            Video.setPic("http:" + tempVideo.getStr("pic"));
            Video.setTitle(tempVideo.getStr("title"));
            Video.setDescription(tempVideo.getStr("description"));
            VideoList.add(Video);
            if (VideoList.size() >= pageSize) {
                break;
            }
        }
        page.setRecords(VideoList);
        return page;
    }
}
