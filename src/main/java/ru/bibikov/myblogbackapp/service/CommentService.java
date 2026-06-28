package ru.bibikov.myblogbackapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bibikov.myblogbackapp.exception.post.PostWithIdNotFound;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.repository.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository repository;

    public Comment createComment(Long postId,String comment){
        Long commentId= repository.createComment(postId,comment);
        repository.updateCommentCountIncrementInPost(postId);
        return repository.getComment(commentId);
    }

    public List<Comment> getComments(Long id){
        validateId(id);
        return repository.getComments(id);
    }

    public Comment getComment(Long id){
        validateId(id);
        return repository.getComment(id);
    }

    public Comment updateComment(Long id,String text){
        validateId(id);
        repository.updateComment(id,text);
        return repository.getComment(id);
    }

    public void deleteComment(Long commentId,Long postId){
        validateId(commentId);
        repository.deleteComment(commentId);
        repository.updateCommentCountDecrementInPost(postId);
    }
    private void validateId(Long commentId){
        if (commentId==null||!repository.existId(commentId)){
            throw new PostWithIdNotFound("Пост с таким ID не найден");
        }
    }
}
