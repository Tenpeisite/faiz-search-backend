package com.zhj.search.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/5/22 12:03
 */
@Data
public class Music implements Serializable {

    private String name;

    private String author;

    private String picUrl;

    private String mp3Url;

    private static final long serialVersionUID = 1L;
}
