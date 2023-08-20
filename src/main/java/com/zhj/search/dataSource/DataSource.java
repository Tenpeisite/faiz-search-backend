package com.zhj.search.dataSource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 数据源接口（新接入的数据源必须实现）
 * @author zhj
 * @version 1.0
 * @date 2023/5/20 11:20
 */
public interface DataSource<T> {

    Page<T> doSearch(String searchText, long pageNum, long pageSize);
}
