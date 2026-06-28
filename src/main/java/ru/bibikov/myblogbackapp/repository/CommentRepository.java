package ru.bibikov.myblogbackapp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.bibikov.myblogbackapp.model.Comment;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Comment> commentRowMapper=(rs, rowNum)->{
        Comment comment=new Comment();
        comment.setId(rs.getLong("id"));
        comment.setText(rs.getString("text"));
        comment.setPostId(rs.getLong("post_id"));
        return comment;
    };

    public Long createComment(Long id, String comment){ //updated
        String sql="insert into comments(text, post_id) VALUES (?,?) returning id";
        return jdbcTemplate.queryForObject(sql,Long.class,comment,id);
    }
    public void updateCommentCountIncrementInPost(Long postId){  //updated
        String sqlForPosts="update posts set comments_count=comments_count+1 where id=?";
        jdbcTemplate.update(sqlForPosts,postId);
    }

    public List<Comment> getComments(Long id){  //updated
        String sql="select * from comments where post_id=?";
        return jdbcTemplate.query(sql,commentRowMapper,id);
    }

    public Comment getComment(Long id){     //updated
        String sql="select * from comments where id=?";
        return jdbcTemplate.queryForObject(sql,commentRowMapper,id);
    }

    public void updateComment(Long id,String text){
        String sqlUpdate="update comments set text=? where id=?";
        jdbcTemplate.update(sqlUpdate,text,id);
    }

    public void deleteComment(Long id){
        String sql="delete from comments where id=?";
        jdbcTemplate.update(sql,id);
    }

    public void updateCommentCountDecrementInPost(Long postId){
        String sqlForComment="update posts set comments_count=comments_count-1 where id=?";
        jdbcTemplate.update(sqlForComment,postId);
    }

}
