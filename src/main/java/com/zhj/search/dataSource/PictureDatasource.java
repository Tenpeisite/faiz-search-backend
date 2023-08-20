package com.zhj.search.dataSource;

import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.search.common.ErrorCode;
import com.zhj.search.exception.BusinessException;
import com.zhj.search.model.entity.Picture;
import com.zhj.search.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/19 19:17
 */
@Service("picture_dataSource")
@Slf4j
public class PictureDatasource implements DataSource<Picture> {

    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
        long current = (pageNum - 1) * pageSize;
        String url = String.format("https://cn.bing.com/images/search?q=%s&first=%s", searchText, current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
        }
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            //取图片地址(murl)
            String m = element.select(".iusc").get(0).attr("m");
            //System.out.println(m);
            Map<String, String> map = JSONUtil.toBean(m, Map.class);
            String murl = map.get("murl");
            log.info(murl);
            //取到标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            //System.out.println(title);
            //System.out.println(element);
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
            if (pictures.size() >= pageSize) {
                break;
            }
        }
        Page<Picture> picturePage = new Page<>(pageNum, pageSize);
        picturePage.setRecords(pictures);
        return picturePage;
    }
}
