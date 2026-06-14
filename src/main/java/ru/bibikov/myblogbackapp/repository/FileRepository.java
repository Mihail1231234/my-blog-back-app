package ru.bibikov.myblogbackapp.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.bibikov.myblogbackapp.exception.post.PostWithIdNotFound;

@Repository
@AllArgsConstructor
public class FileRepository {

    private final JdbcTemplate template;

    public void updateImage(Long postId, String imagePath){
        validateId(postId);
        template.update("update posts set image =? where id=?",imagePath,postId);
    }
    public String getImage(Long postId){
        validateId(postId);
        return template.queryForObject("select image from posts where id =?",String.class,postId);
    }
    private void validateId(Long id){
        String sql="select exists(select 1 from posts where id=?)";
        if (!template.queryForObject(sql,Boolean.class,id)||id==0){
            throw new PostWithIdNotFound("Post with this id was not found");
        }
    }
}
