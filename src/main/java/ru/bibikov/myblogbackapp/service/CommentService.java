package ru.bibikov.myblogbackapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bibikov.myblogbackapp.exception.comment.CommentIdIsNull;
import ru.bibikov.myblogbackapp.exception.comment.CommentWithIdNotFound;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.repository.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository repository;

    public Comment createComment(Long postId,String comment){
        log.info("Создание комментария с post_id={}: comment='{}'",postId,comment);
        Long commentId= repository.createComment(postId,comment);
        log.info("Создание комментария успешно совершено, присвоено comment_id={}",commentId);
        repository.updateCommentCountIncrementInPost(postId);
        return repository.getComment(commentId,postId);
    }

    public List<Comment> getComments(Long id){
        log.debug("Получение комментариев с post_id={}",id);
        validateId(id);
        return repository.getComments(id);
    }

    public Comment getComment(Long id,Long postId){
        log.debug("Получение комментария с comment_id={} и post_id={}",id,postId);
        validateId(id);
        return repository.getComment(id,postId);
    }

    public Comment updateComment(Long commentId,Long postId,String text){
        log.info("Обновление комментария с comment_id={} и post_id={}: text='{}'",commentId,postId,text);
        validateId(commentId);
        repository.updateComment(commentId,postId,text);
        log.info("Комментарий с comment_id={} успешно обновлен",commentId);
        return repository.getComment(commentId,postId);
    }

    public void deleteComment(Long commentId,Long postId){
        log.info("Удаление комментария с comment_id={} и post_id={}",commentId,postId);
        validateId(commentId);
        repository.deleteComment(commentId);
        repository.updateCommentCountDecrementInPost(postId);
        log.info("Комментарий с comment_id={} и post_id={} успешно удален",commentId,postId);
    }
    private void validateId(Long commentId){
        if (commentId==null){
            throw new CommentIdIsNull("ID комментария равен нулю");
        }if (!repository.existId(commentId)){
            throw new CommentWithIdNotFound("Комментарий с таким ID не найден");
        }
    }
}
