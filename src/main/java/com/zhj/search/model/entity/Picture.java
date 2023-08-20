package com.zhj.search.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/19 19:04
 */
@Data
public class Picture implements Serializable {

    private String title;

    private String url;

    private static final long serialVersionUID = 1L;
}
