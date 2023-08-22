package com.zhj.search;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.search.common.ErrorCode;
import com.zhj.search.dataSource.DataSource;
import com.zhj.search.dataSource.DataSourceRegistry;
import com.zhj.search.esdao.PostEsDao;
import com.zhj.search.exception.BusinessException;
import com.zhj.search.model.dto.post.PostEsDTO;
import com.zhj.search.model.entity.Music;
import com.zhj.search.model.entity.Picture;
import com.zhj.search.model.entity.Post;
import com.zhj.search.model.entity.Video;
import com.zhj.search.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/19 16:52
 */
@SpringBootTest
public class CrawlerTest {
    @Resource
    private PostService postService;

    @Resource
    private PostEsDao postEsDao;

    @Test
    void testFetchPicture() throws IOException {
        int current = 1;
        String text = "假面骑士";
        String url = String.format("https://cn.bing.com/images/search?q=%s&first=%s", text, current);
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".iuscp.isv");
        ArrayList<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            //取图片地址(murl)
            String m = element.select(".iusc").get(0).attr("m");
            //System.out.println(m);
            Map<String, String> map = JSONUtil.toBean(m, Map.class);
            String murl = map.get("murl");
            System.out.println(murl);
            //取到标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            //System.out.println(title);
            //System.out.println(element);
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
        }

    }

//    @Test
//    void testFetchPicture() throws IOException {
//        int current = 1;
//        String url = "https://cn.bing.com/images/search?q=小黑子&first=" + current;
//        Document doc = Jsoup.connect(url).get();
//        Elements elements = doc.select(".iuscp.isv");
//        List<Picture> pictures = new ArrayList<>();
//        for (Element element : elements) {
//            // 取图片地址（murl）
//            String m = element.select(".iusc").get(0).attr("m");
//            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
//            String murl = (String) map.get("murl");
////            System.out.println(murl);
//            // 取标题
//            String title = element.select(".inflnk").get(0).attr("aria-label");
////            System.out.println(title);
//            Picture picture = new Picture();
//            picture.setTitle(title);
//            picture.setUrl(murl);
//            pictures.add(picture);
//        }
//        System.out.println(pictures);
//    }

    @Test
    void testFetchPassage() {
        // 1. 获取数据
        String json = "{\"current\":1,\"pageSize\":1,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"资源\",\"reviewStatus\":1}";

        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result = HttpRequest
                .post(url)
                .body(json)
                .execute()
                .body();
//        System.out.println(result);
        // 2. json 转对象
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        List<Post> postList = new ArrayList<>();
        for (Object record : records) {
            JSONObject tempRecord = (JSONObject) record;
            Post post = new Post();
            post.setTitle(tempRecord.getStr("title"));
            post.setContent(tempRecord.getStr("content"));
            post.setId(Long.valueOf(tempRecord.getStr("id")));
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            List<String> tagList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1L);
            postList.add(post);
        }
        // 3. 数据入库
        boolean b = postService.saveBatch(postList);
        Assertions.assertTrue(b);
    }


    @Test
    void testFetchPost() {
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
        System.out.println(postList);
        // 3. 数据入库
        boolean b = postService.saveBatch(postList);
        postEsDao.saveAll(PostEsDTO.objToDtoList(postList));
    }

    @Test
    public void testFetchMusic() {
        String searchText = "唯一";
        int current = 1;
        int pageSize = 10;

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
        List<Music> records = new ArrayList<>();
        for (Object song : songs) {
            JSONObject tempRecord = (JSONObject) song;
            Music music = new Music();
            music.setName(tempRecord.getStr("name"));
            //Map<String, String> artists = tempRecord.getBean("artists", Map.class);
            //tempRecord.getBeanList("artists",List.class)
            String json = tempRecord.getBeanList("artists", String.class).get(0);
            Map<String, String> artists = JSONUtil.toBean(json, Map.class);
            music.setAuthor(artists.get("name"));
            Map<String, String> album = tempRecord.getBean("album", Map.class);
            music.setPicUrl(album.get("blurPicUrl"));
            music.setMp3Url(tempRecord.getStr("mp3Url"));
            System.out.println(music);
            records.add(music);
            if (records.size() >= 10) {
                break;
            }
        }
    }


    @Test
    public void testFetchVideo() {
        int pageNum = 1;
        int pageSize = 50;
        String searchText = "假面骑士";
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
    }

    @Test
    public void test() {
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
    }

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    //测试工厂模式和注册器模式的性能
    //各自创建100000次耗时
    @Test
    public void test1() {
        //先测试工厂模式
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            DataSource post_service = applicationContext.getBean("post_dataSource", DataSource.class);
        }
        long mid = System.currentTimeMillis();
        System.out.println("工厂模式耗时：" + (mid - start) + "ms");
        for (int i = 0; i < 1000000; i++) {
            DataSource post = dataSourceRegistry.getDataSourceByType("post");
        }
        long end = System.currentTimeMillis();
        System.out.println("注册器模式耗时：" + (end - mid) + "ms");

    }


}
