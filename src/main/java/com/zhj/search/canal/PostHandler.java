package com.zhj.search.canal;

import com.zhj.search.esdao.PostEsDao;
import com.zhj.search.model.dto.post.PostEsDTO;
import com.zhj.search.model.entity.Post;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import javax.annotation.Resource;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/5/21 15:20
 */
@CanalTable("post")
@Component
public class PostHandler implements EntryHandler<Post> {

    @Resource
    private PostEsDao postEsDao;

    @Override
    public void insert(Post post) {
        PostEsDTO postEsDTO = PostEsDTO.objToDto(post);
        postEsDao.save(postEsDTO);
    }

    @Override
    public void update(Post before, Post after) {
        PostEsDTO postEsDTO = PostEsDTO.objToDto(after);
        postEsDao.save(postEsDTO);
    }

    //@Override
    //public void delete(Post post) {
    //    EntryHandler.super.delete(post);
    //}
}
