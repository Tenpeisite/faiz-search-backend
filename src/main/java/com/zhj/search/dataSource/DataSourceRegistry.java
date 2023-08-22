package com.zhj.search.dataSource;

import com.zhj.search.model.enums.SearchTypeEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册器模式
 *
 * @author zhj
 * @version 1.0
 * @date 2023/5/20 13:07
 */
@Component
public class DataSourceRegistry {

    @Resource
    private PostDatasource postDatasource;

    @Resource
    private UserDatasource userDatasource;

    @Resource
    private PictureDatasource pictureDatasource;

    @Resource
    private VideoDatasource videoDatasource;

    @Resource
    private MusicDatasource musicDatasource;

    private Map<String, DataSource> typeDataSourceMap;


    @PostConstruct
    public void doInit() {
        typeDataSourceMap=new HashMap(){{
            put(SearchTypeEnum.POST.getValue(), postDatasource);
            put(SearchTypeEnum.USER.getValue(), userDatasource);
            put(SearchTypeEnum.PICTURE.getValue(), pictureDatasource);
            put(SearchTypeEnum.VIDEO.getValue(), videoDatasource);
            put(SearchTypeEnum.MUSIC.getValue(), musicDatasource);
        }};

    }


    public DataSource getDataSourceByType(String type) {
        return typeDataSourceMap.get(type);
    }

}
