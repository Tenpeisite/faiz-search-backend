package com.zhj.search.esdao;

import com.zhj.search.model.dto.post.PostEsDTO;
import com.zhj.search.model.dto.post.PostQueryRequest;
import com.zhj.search.model.entity.Post;
import com.zhj.search.service.PostService;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * 帖子 ES 操作测试
 *
 * @author zhj
 * 
 */
@SpringBootTest
public class PostEsDaoTest {

    @Resource
    private PostEsDao postEsDao;

    @Resource
    private PostService postService;

    @Test
    void test() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> page =
                postService.searchFromEs(postQueryRequest);
        System.out.println(page);
    }

    @Test
    void testSelect() {
        System.out.println(postEsDao.count());
        Page<PostEsDTO> PostPage = postEsDao.findAll(
                PageRequest.of(0, 5, Sort.by("createTime")));
        List<PostEsDTO> postList = PostPage.getContent();
        System.out.println(postList);
    }

    @Test
    void testAdd() {
        PostEsDTO postEsDTO = new PostEsDTO();
        postEsDTO.setId(3L);
        postEsDTO.setTitle("Faiz");
        postEsDTO.setContent("我最喜欢的假面骑士就是Faiz");
        postEsDTO.setTags(Arrays.asList("java", "python"));
        //postEsDTO.setThumbNum(1);
        //postEsDTO.setFavourNum(1);
        postEsDTO.setUserId(1L);
        postEsDTO.setCreateTime(new Date());
        postEsDTO.setUpdateTime(new Date());
        postEsDTO.setIsDelete(0);
        postEsDao.save(postEsDTO);
        System.out.println(postEsDTO.getId());
    }

    @Test
    void testFindById() {
        Optional<PostEsDTO> postEsDTO = postEsDao.findById(1L);
        System.out.println(postEsDTO);
    }

    @Test
    void testCount() {
        System.out.println(postEsDao.count());
    }

    @Test
    void testFindByCategory() {
        List<PostEsDTO> postEsDaoTestList = postEsDao.findByUserId(1L);
        System.out.println(postEsDaoTestList);
    }

    @Test
    void testFindByTitle(){
        List<PostEsDTO> postEsDTOS = postEsDao.findByTitle("test");
        System.out.println(postEsDTOS);
    }

    @Test
    void testDeleteById(){
        postEsDao.deleteById(1L);
    }
}
