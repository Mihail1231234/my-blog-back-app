package ru.bibikov.myblogbackapp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.bibikov.myblogbackapp.model.Comment;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_COMMENT="insert into comments(text, post_id) VALUES (?,?) returning id";
    private static final String GET_ALL_COMMENTS="select * from comments where post_id=?";
    private static final String GET_COMMENT_BY_ID="select * from comments where id=? and post_id=?";
    private static final String UPDATE_COMMENT_COUNT_INCREMENT_IN_POST="update posts set comments_count=comments_count+1 where id=?";
    private static final String UPDATE_COMMENT_BY_ID="update comments set text=? where id=? and post_id=?";
    private static final String DELETE_COMMENT_BY_ID="delete from comments where id=?";
    private static final String UPDATE_COMMENT_COUNT_DECREMENT_IN_POST="update posts set comments_count=comments_count-1 where id=?";
    private static final String COMMENT_ID_IS_EXIST="select exists(select 1 from comments where id=?)";

    private final RowMapper<Comment> commentRowMapper=(rs, rowNum)->{
        Comment comment=new Comment();
        comment.setId(rs.getLong("id"));
        comment.setText(rs.getString("text"));
        comment.setPostId(rs.getLong("post_id"));
        return comment;
    };

    @Transactional
    public Long createComment(Long id, String comment){ //updated
        return jdbcTemplate.queryForObject(CREATE_COMMENT,Long.class,comment,id);
    }
    public List<Comment> getComments(Long id){  //updated
        return jdbcTemplate.query(GET_ALL_COMMENTS,commentRowMapper,id);
    }

    public Comment getComment(Long id,Long postId){     //updated
        return jdbcTemplate.queryForObject(GET_COMMENT_BY_ID,commentRowMapper,id,postId);
    }
    @Transactional
    public void updateCommentCountIncrementInPost(Long postId){  //updated
        jdbcTemplate.update(UPDATE_COMMENT_COUNT_INCREMENT_IN_POST,postId);
    }
    @Transactional
    public void updateComment(Long commentId,Long postId,String text){
        jdbcTemplate.update(UPDATE_COMMENT_BY_ID,text,commentId,postId);
    }
    @Transactional
    public void deleteComment(Long id){
        jdbcTemplate.update(DELETE_COMMENT_BY_ID,id);
    }
    @Transactional
    public void updateCommentCountDecrementInPost(Long postId){
        jdbcTemplate.update(UPDATE_COMMENT_COUNT_DECREMENT_IN_POST,postId);
    }

    public boolean existId(Long id){
        return jdbcTemplate.queryForObject(COMMENT_ID_IS_EXIST,Boolean.class,id);
    }

}
