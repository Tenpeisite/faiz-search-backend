package com.zhj.search.dataSource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.zhj.search.common.ErrorCode;
import com.zhj.search.constant.CommonConstant;
import com.zhj.search.exception.BusinessException;
import com.zhj.search.exception.ThrowUtils;
import com.zhj.search.mapper.PostFavourMapper;
import com.zhj.search.mapper.PostMapper;
import com.zhj.search.mapper.PostThumbMapper;
import com.zhj.search.model.dto.post.PostEsDTO;
import com.zhj.search.model.dto.post.PostQueryRequest;
import com.zhj.search.model.entity.Post;
import com.zhj.search.model.entity.PostFavour;
import com.zhj.search.model.entity.PostThumb;
import com.zhj.search.model.entity.User;
import com.zhj.search.model.vo.PostVO;
import com.zhj.search.model.vo.UserVO;
import com.zhj.search.service.PostService;
import com.zhj.search.service.UserService;
import com.zhj.search.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 * @author zhj
 */
@Service("post_dataSource")
@Slf4j
public class PostDatasource implements DataSource<PostVO> {

    @Resource
    private PostService postService;

    @Override
    public Page<PostVO> doSearch(String searchText, long pageNum, long pageSize) {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setContent(searchText);
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setCurrent(pageNum);
        postQueryRequest.setPageSize(pageSize);
        //ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //HttpServletRequest request = servletRequestAttributes.getRequest();
        //Page<PostVO> postVOPage = postService.listPostVOByPage(postQueryRequest, request);
        Page<Post> postVOPage = postService.searchFromEs(postQueryRequest);
        return postService.getPostVOPage(postVOPage, null);
    }
}




