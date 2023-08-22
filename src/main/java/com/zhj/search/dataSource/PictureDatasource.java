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

import javax.annotation.Resource;
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

    @Resource
    private PictureService pictureService;

    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
        Page<Picture> picturePage = pictureService.searchPicture(searchText, pageNum, pageSize);
        picturePage.setRecords(picturePage.getRecords());
        return picturePage;
    }
}
