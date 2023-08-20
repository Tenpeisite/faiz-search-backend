package com.zhj.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.search.model.entity.Picture;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/19 19:16
 */
public interface PictureService {

    Page<Picture> searchPicture(String searchText, long pageNum, long pageSize);
}
