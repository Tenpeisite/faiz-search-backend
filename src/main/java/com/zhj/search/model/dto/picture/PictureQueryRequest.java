package com.zhj.search.model.dto.picture;

import com.zhj.search.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/5/19 19:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    private static final long seriaVersionUID = 1L;

}
