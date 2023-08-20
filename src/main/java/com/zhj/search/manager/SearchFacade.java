package com.zhj.search.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.search.common.ErrorCode;
import com.zhj.search.dataSource.*;
import com.zhj.search.exception.BusinessException;
import com.zhj.search.exception.ThrowUtils;
import com.zhj.search.model.dto.post.PostQueryRequest;
import com.zhj.search.model.dto.search.SearchRequest;
import com.zhj.search.model.dto.user.UserQueryRequest;
import com.zhj.search.model.entity.Picture;
import com.zhj.search.model.enums.SearchTypeEnum;
import com.zhj.search.model.vo.PostVO;
import com.zhj.search.model.vo.SearchVO;
import com.zhj.search.model.vo.UserVO;
import com.zhj.search.service.PictureService;
import com.zhj.search.service.PostService;
import com.zhj.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 搜索门面
 *
 * @author zhj
 * @version 1.0
 * @date 2023/8/20 10:53
 */
@Component
@Slf4j
public class SearchFacade {

    @Resource
    private PictureDatasource pictureDatasource;

    @Resource
    private PostDatasource postDatasource;

    @Resource
    private UserDatasource userDatasource;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchVO searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.FORBIDDEN_ERROR);
        //搜索词
        String searchText = searchRequest.getSearchText();
        //页码
        long current = searchRequest.getCurrent();
        //条数
        long pageSize = searchRequest.getPageSize();
        //如果没有这个type对应的枚举类则查询所有
        if (searchTypeEnum == null) {

            //搜索用户
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                Page<UserVO> userVOPage = userDatasource.doSearch(searchText, current, pageSize);
                return userVOPage;
            });

            //搜索文章
            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                Page<PostVO> postVOPage = postDatasource.doSearch(searchText, current, pageSize);
                return postVOPage;
            });

            //搜索图片
            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
                Page<Picture> picturePage = pictureDatasource.doSearch(searchText, current, pageSize);
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
                return searchVO;
            } catch (Exception e) {
                log.error("查询异常", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
            }
        } else {
            //注册器模式
            //DataSource dataSource = dataSourceRegistry.getDataSourceByType(type);

            //或者是工厂模式
            DataSource dataSource = applicationContext.getBean(type + "_dataSource", DataSource.class);

            //整合
            SearchVO searchVO = new SearchVO();
            Page<T> page = dataSource.doSearch(searchText, current, pageSize);
            searchVO.setDataList(page.getRecords());
            return searchVO;
        }


    }
}
