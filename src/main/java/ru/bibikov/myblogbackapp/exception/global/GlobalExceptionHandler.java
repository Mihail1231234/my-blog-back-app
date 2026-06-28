package ru.bibikov.myblogbackapp.exception.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.bibikov.myblogbackapp.exception.comment.CommentWithIdNotFound;
import ru.bibikov.myblogbackapp.exception.post.PostWithIdNotFound;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public ResponseEntity handlePostNotFound(PostWithIdNotFound e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пост с таким ID не найден "+e);
    }
    public ResponseEntity handleCommentNotFound(CommentWithIdNotFound e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Комментарий с таким ID не найден "+e);
    }
}
