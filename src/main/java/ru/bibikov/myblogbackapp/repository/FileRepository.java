package ru.bibikov.myblogbackapp.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.bibikov.myblogbackapp.exception.post.PostIdIsNull;
import ru.bibikov.myblogbackapp.exception.post.PostWithIdNotFound;

@Repository
@AllArgsConstructor
public class FileRepository {

    private final JdbcTemplate template;

    private static final String UPDATE_POST_IMAGE_BY_ID="update posts set image =? where id=?";
    private static final String GET_IMAGE_BY_POST_ID="select image from posts where id =?";
    private static final String POST_ID_EXIST="select exists(select 1 from posts where id=?)";

    public void updateImage(Long postId, String imagePath){
        validateId(postId);
        template.update(UPDATE_POST_IMAGE_BY_ID,imagePath,postId);
    }
    public String getImage(Long postId){
        validateId(postId);
        return template.queryForObject(GET_IMAGE_BY_POST_ID,String.class,postId);
    }
    private void validateId(Long id){
        if (!template.queryForObject(POST_ID_EXIST,Boolean.class,id)){
            throw new PostWithIdNotFound("Post with this id was not found");
        }if (id==null){
            throw new PostIdIsNull("ID поста равен null");
        }
    }
}
