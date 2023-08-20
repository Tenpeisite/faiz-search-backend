package com.zhj.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.zhj.search.common.BaseResponse;
import com.zhj.search.common.ErrorCode;
import com.zhj.search.common.ResultUtils;
import com.zhj.search.exception.ThrowUtils;
import com.zhj.search.model.dto.picture.PictureQueryRequest;
import com.zhj.search.model.entity.Picture;
import com.zhj.search.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子接口
 *
 * @author zhj
 * 
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

  @Resource
  private PictureService pictureService;

    private final static Gson GSON = new Gson();

    /**
     * 分页获取列表（封装类）
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Picture>> listPostVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                        HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        String searchText = pictureQueryRequest.getSearchText();
        Page<Picture> picturePage = pictureService.searchPicture(searchText, current, size);
        return ResultUtils.success(picturePage);
    }

}
