package com.zhj.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.search.common.BaseResponse;
import com.zhj.search.common.ErrorCode;
import com.zhj.search.common.ResultUtils;
import com.zhj.search.exception.BusinessException;
import com.zhj.search.manager.SearchFacade;
import com.zhj.search.model.dto.post.PostQueryRequest;
import com.zhj.search.model.dto.search.SearchRequest;
import com.zhj.search.model.dto.user.UserQueryRequest;
import com.zhj.search.model.entity.Picture;
import com.zhj.search.model.vo.PostVO;
import com.zhj.search.model.vo.SearchVO;
import com.zhj.search.model.vo.UserVO;
import com.zhj.search.service.PictureService;
import com.zhj.search.service.PostService;
import com.zhj.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * 帖子接口
 *
 * @author zhj
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private PictureService pictureService;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @Resource
    private SearchFacade searchFacade;

    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        //门面模式
        return ResultUtils.success(searchFacade.searchAll(searchRequest, request));
    }

    @PostMapping("/all1")
    public BaseResponse<SearchVO> searchAll1(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        //搜索词
        String searchText = searchRequest.getSearchText();

        //搜索用户
        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
            UserQueryRequest userQueryRequest = new UserQueryRequest();
            userQueryRequest.setUserName(searchText);
            Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);
            return userVOPage;
        });

        //搜索文章
        CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
            PostQueryRequest postQueryRequest = new PostQueryRequest();
            postQueryRequest.setSearchText(searchText);
            Page<PostVO> postVOPage = postService.listPostVOByPage(postQueryRequest, request);
            return postVOPage;
        });

        //搜索图片
        CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
            Page<Picture> picturePage = pictureService.searchPicture(searchText, 1, 10);
            return picturePage;
        });

        CompletableFuture.allOf(userTask, postTask, pictureTask).join();

        try {
            Page<UserVO> userVOPage = userTask.get();
            Page<PostVO> postVOPage = postTask.get();
            Page<Picture> picturePage = pictureTask.get();
            //整合
            SearchVO searchVO = new SearchVO();
            searchVO.setUserList(userVOPage.getRecords());
            searchVO.setPostList(postVOPage.getRecords());
            searchVO.setPictureList(picturePage.getRecords());
            return ResultUtils.success(searchVO);
        } catch (Exception e) {
            log.error("查询异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
        }
    }

}
