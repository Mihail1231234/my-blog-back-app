package ru.bibikov.myblogbackapp.repository;


import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.bibikov.myblogbackapp.dto.CommentResponse;
import ru.bibikov.myblogbackapp.dto.CreateCommentRequest;
import ru.bibikov.myblogbackapp.dto.PostPreviewDto;
import ru.bibikov.myblogbackapp.dto.PostResponse;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.model.Tag;

import java.util.*;

@Repository
@AllArgsConstructor
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Post> postRowMapper=(rs,rowNum)->{
      Post post=new Post();
      post.setId(rs.getLong("id"));
      post.setTitle(rs.getString("title"));
      post.setText(rs.getString("text"));
      post.setLikesCount(rs.getInt("likes_count"));
      post.setCommentsCount(rs.getInt("comments_count"));
      post.setImage(rs.getString("image"));
      return post;
    };
    private final RowMapper<Comment> commentRowMapper=(rs,rowNum)->{
        Comment comment=new Comment();
        comment.setId(rs.getLong("id"));
        comment.setText(rs.getString("text"));
        comment.setPostId(rs.getLong("post_id"));
        return comment;
    };
    private final RowMapper<Tag> tagRowMapper=(rs,rowNum)->{
        Tag tag=new Tag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        return tag;
    };

    public PostPreviewDto createPost(String title,
                                   String text,
                                   List<String> tags){
        String sqlForCreate="insert into posts(title,text) values (?,?) returning id";
        Long postId=jdbcTemplate.queryForObject(sqlForCreate,Long.class,title,text);

        String placeholders=String.join(",", Collections.nCopies(tags.size(),"?"));
        if (placeholders.isEmpty()) {
            String sqlForSelect="select * from posts where id=?";
            Post post=jdbcTemplate.queryForObject(sqlForSelect,postRowMapper,postId);

            return PostPreviewDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .text(post.getText())
                    .likesCount(post.getLikesCount())
                    .commentsCount(post.getCommentsCount())
                    .tags(List.of())
                    .build();
        }
        String sqlForTags = "select * from tags where tags.name in(" + placeholders + ")";
        List<Tag> tagsExist=jdbcTemplate.query(sqlForTags,tagRowMapper,tags.toArray());

        Map<String,Long> tagNameToId=new HashMap<>();
        for (Tag tag:tagsExist){
            tagNameToId.put(tag.getName(),tag.getId());
        }
        String sqlForCreateTags="insert into tags(name) values(?) returning id";
        for (String tagName:tags){
            if (!tagNameToId.containsKey(tagName)) {
                Long newId = jdbcTemplate.queryForObject(sqlForCreateTags, Long.class, tagName);
                tagNameToId.put(tagName, newId);
            }
        }
        for (String tagName:tags){
            Long tagId=tagNameToId.get(tagName);
            jdbcTemplate.update("insert into post_tags(post_id,tag_id) values (?,?)",postId,tagId);
        }
        String sqlForSelect="select * from posts where id=?";
        Post post=jdbcTemplate.queryForObject(sqlForSelect,postRowMapper,postId);
        List<String> tag=getTags(postId);
        return PostPreviewDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .tags(tag)
                .build();
    }

    public PostResponse getPosts(String search, int pageNumber, int pageSize) {

        String COUNT_POSTS="select count(*) from posts where lower(title) like lower(?)";
        String FIND_POSTS_PAGINATED="select *from posts where lower(title) like lower(?)" +
                "order by id desc limit ? offset ?";

        String searchPattern = "%" + search + "%";

        Integer totalPosts = jdbcTemplate.queryForObject(
                COUNT_POSTS,
                Integer.class,
                searchPattern
        );

        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
        lastPage = Math.max(lastPage, 1);

        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        int offset = (pageNumber - 1) * pageSize;

        List<PostPreviewDto> posts = jdbcTemplate.query(
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

        return PostResponse.builder()
                .posts(posts)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .lastPage(lastPage)
                .build();
    }

    public Post getPost(Long id){
        String sql="select * from posts where id=?";
        Post post=jdbcTemplate.queryForObject(sql, postRowMapper, id);
        post.setTags(getTags(id));
        return post;
    }

    public int deletePost(Long id){
        String sql="delete from posts where id=?";
        return jdbcTemplate.update(sql,id);
    }

    public int likesIncrement(Long id) {
        String sqlUpdate="update posts set likes_count=likes_count+1 where id=?";
        jdbcTemplate.update(sqlUpdate,id);

        String sqlSelect="select likes_count from posts where id =?";
        return jdbcTemplate.queryForObject(sqlSelect,Integer.class,id);
    }

    public Comment createComment(Long id, String comment){
        String sql="insert into comments(text, post_id) VALUES (?,?) returning id";
        Long newId=jdbcTemplate.queryForObject(sql,Long.class,comment,id);
        String sqlForPosts="update posts set comments_count=comments_count+1 where id=?";
        jdbcTemplate.update(sqlForPosts,id);

        String sqlFromDb="select * from comments where id=?";
        return jdbcTemplate.queryForObject(sqlFromDb,commentRowMapper,newId);
    }

    public List<Comment> getComments(Long id){
        String sql="select * from comments where post_id=?";
        return jdbcTemplate.query(sql,commentRowMapper,id);
    }

    public Comment getComment(Long id){
        String sql="select * from comments where id=?";
        return jdbcTemplate.queryForObject(sql,commentRowMapper,id);
    }

    public Comment updateComment(Long id,String text){
        String sqlUpdate="update comments set text=? where id=?";
        jdbcTemplate.update(sqlUpdate,text,id);

        String sqlSelect="select * from comments where id=?";
        return jdbcTemplate.queryForObject(sqlSelect,commentRowMapper,id);
    }

    public void deleteComment(Long id,Long postId){
        String sql="delete from comments where id=?";
        jdbcTemplate.update(sql,id);
        String sqlForComment="update posts set comments_count=comments_count-1 where id=?";
        jdbcTemplate.update(sqlForComment,postId);
    }

    public List<String> getTags(Long postId){
        String sqlFromPostTagDb="select tags.id, tags.name from tags " +
                                "join post_tags on tags.id=post_tags.tag_id " +
                                "where post_id=?";
        List<Tag>tags=jdbcTemplate.query(sqlFromPostTagDb,tagRowMapper,postId);
        return tags.stream()
                .map(Tag::getName)
                .toList();
    }


}


