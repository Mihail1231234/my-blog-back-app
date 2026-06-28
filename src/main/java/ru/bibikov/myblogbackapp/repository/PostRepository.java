package ru.bibikov.myblogbackapp.repository;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.bibikov.myblogbackapp.dto.PostPreviewDto;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.model.Tag;

import java.util.*;

@Slf4j
@Repository
@AllArgsConstructor
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_POST_AND_RETURN_POST_ID="insert into posts(title,text) values (?,?) returning id";
    private static final String CREATE_POST_TAG_CHAIN="insert into post_tags(post_id,tag_id) values (?,?)";
    private static final String TAGS_EXIST="select * from tags where tags.name in(%s)";
    private String buildSelectTags(String placeholder){
        return String.format(TAGS_EXIST,placeholder);
    }
    private static final String CREATE_TAG_AND_RETURN_TAG_ID="insert into tags(name) values(?) returning id";
    private static final String GET_COUNT_POST="select count(*) from posts where lower(title) like lower(?)";
    private static final String FIND_POSTS_PAGINATED="select *from posts where lower(title) like lower(?) order by id desc limit ? offset ?";
    private static final String GET_POST_BY_ID="select * from posts where id=?";
    private static final String UPDATE_POST_BY_ID="update posts set title=?,text=? where id=?";
    private static final String DELETE_POST_BY_ID="delete from posts where id=?";
    private static final String INCREMENT_LIKE_BY_POST_ID="update posts set likes_count=likes_count+1 where id=? returning likes_count";
    private static final String GET_TAGS_BY_POST_ID="select tags.id, tags.name from tags join post_tags on tags.id=post_tags.tag_id where post_id=?";
    private static final String DELETE_POST_TAG_CHAIN_BY_POST_ID="delete from post_tags where post_id=?";
    private static final String POST_ID_IS_EXIST="select exists(select 1 from posts where id=?)";

    private final RowMapper<Post> postRowMapper=(rs, rowNum)->{
        Post post=new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setText(rs.getString("text"));
        post.setLikesCount(rs.getInt("likes_count"));
        post.setCommentsCount(rs.getInt("comments_count"));
        post.setImage(rs.getString("image"));
        return post;
    };

    private final RowMapper<Tag> tagRowMapper=(rs, rowNum)->{
        Tag tag=new Tag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        return tag;
    };

    @Transactional
    public Long createPostReturnId(String title,        //updated
                                   String text){
        return jdbcTemplate.queryForObject(CREATE_POST_AND_RETURN_POST_ID,Long.class,title,text);
    }

    @Transactional
    public void createPostTagChain(Long postId,Long tagId){ //updated
        jdbcTemplate.update(CREATE_POST_TAG_CHAIN,postId,tagId);
    }

    public List<Tag> tagsExist(String placeholders,List<String> tags){//updated
        String sql=buildSelectTags(placeholders);
        return jdbcTemplate.query(sql,tagRowMapper,tags.toArray());
    }

    @Transactional
    public Long createTag(String tagName){      //updated
        return jdbcTemplate.queryForObject(CREATE_TAG_AND_RETURN_TAG_ID, Long.class, tagName);
    }

    public int countPosts(String searchPattern){        //updated
        return jdbcTemplate.queryForObject(
                GET_COUNT_POST,
                Integer.class,
                searchPattern
        );
    }

    public List<PostPreviewDto> findPostPaginated(String searchPattern, int pageSize, int offset){ //updated
        return jdbcTemplate.query(
                FIND_POSTS_PAGINATED,
                (rs, rowNum) -> {

                    String text = rs.getString("text");
                    if (text != null && text.length() > 128) {
                        text = text.substring(0, 128) + "…";
                    }

                    List<String> tagsList=getTags(rs.getLong("id"));

                    return PostPreviewDto.builder()
                            .id(rs.getLong("id"))
                            .title(rs.getString("title"))
                            .text(text)
                            .tags(tagsList)
                            .likesCount(rs.getInt("likes_count"))
                            .commentsCount(rs.getInt("comments_count"))
                            .build();
                },
                searchPattern,
                pageSize,
                offset
        );
    }

    public Post getPostWithId(Long id){         //updated
        return jdbcTemplate.queryForObject(GET_POST_BY_ID,postRowMapper,id);
    }

    @Transactional
    public void updatePost(Long postId, String title, String text){ //updated
        jdbcTemplate.update(UPDATE_POST_BY_ID, title, text, postId);
    }

    @Transactional
    public int deletePost(Long id){ //updated
        return jdbcTemplate.update(DELETE_POST_BY_ID,id);
    }

    @Transactional
    public int likesIncrement(Long id) { //updated
        return jdbcTemplate.queryForObject(INCREMENT_LIKE_BY_POST_ID,Integer.class,id);
    }

    public List<String> getTags(Long postId){           //updated
        return jdbcTemplate.query(GET_TAGS_BY_POST_ID,tagRowMapper,postId).stream()
                .map(Tag::getName)
                .toList();
    }

    @Transactional
    public void deletePostTag(Long postId){
        jdbcTemplate.update(DELETE_POST_TAG_CHAIN_BY_POST_ID,postId);
    }
    public boolean existId(Long id){
        return jdbcTemplate.queryForObject(POST_ID_IS_EXIST,Boolean.class,id);
    }
}

