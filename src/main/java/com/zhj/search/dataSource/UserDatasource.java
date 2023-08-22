package com.zhj.search.dataSource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.search.common.ErrorCode;
import com.zhj.search.constant.CommonConstant;
import com.zhj.search.constant.UserConstant;
import com.zhj.search.exception.BusinessException;
import com.zhj.search.mapper.UserMapper;
import com.zhj.search.model.dto.user.UserQueryRequest;
import com.zhj.search.model.entity.User;
import com.zhj.search.model.enums.UserRoleEnum;
import com.zhj.search.model.vo.LoginUserVO;
import com.zhj.search.model.vo.UserVO;
import com.zhj.search.service.UserService;
import com.zhj.search.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author zhj
 * 
 */
@Service("user_dataSource")
@Slf4j
public class UserDatasource implements DataSource<UserVO> {

    @Resource
    private UserService userService;

    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long pageSize){
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setCurrent(pageNum);
        userQueryRequest.setPageSize(pageSize);
        userQueryRequest.setUserName(searchText);
        Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);
        return userVOPage;
    }
    //public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
    //    long current = userQueryRequest.getCurrent();
    //    long size = userQueryRequest.getPageSize();
    //    Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
    //    Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
    //    List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
    //    userVOPage.setRecords(userVO);
    //    return userVOPage;
    //}
}
