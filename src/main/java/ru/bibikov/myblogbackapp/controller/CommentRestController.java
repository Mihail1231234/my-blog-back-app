package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bibikov.myblogbackapp.dto.CommentResponse;
import ru.bibikov.myblogbackapp.dto.CreateCommentRequest;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @PostMapping("/{id}/comments")
    public CommentResponse createComment(@RequestBody CreateCommentRequest request,
                                         @PathVariable(name = "id")Long postId){
        log.info("Создание нового комментария для поста с id={}: text='{}'",postId,request.getText());
        Comment newComment=commentService.createComment(postId, request.getText());
        log.info("Комментарий успешно создан с id={}",newComment.getId());
        return CommentResponse.builder()
                .id(newComment.getId())
                .text(newComment.getText())
                .postId(newComment.getPostId())
                .build();
    }
    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable(name = "id")Long postId){
        log.debug("Получение всех комментарий для поста с id={}",postId);
        List<Comment> comments=commentService.getComments(postId);
        log.debug("Получено {} комментариев для поста с id={}",comments.size(),postId);
        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .postId(comment.getPostId())
                        .build())
                .toList();
    }
    @GetMapping("/{id}/comments/{commentId}")
    public CommentResponse getComment(@PathVariable(name = "commentId") Long commentId,
                                      @PathVariable(name = "id")Long postId){
        log.debug("Получение комментария с comment_id={} и post_id={}",commentId,postId);
        Comment comment=commentService.getComment(commentId,postId);
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
    @PutMapping("/{id}/comments/{commentId}")
    public CommentResponse updateComment(@RequestBody CommentResponse request,
                                         @PathVariable(name = "commentId")Long commentId,
                                         @PathVariable(name = "id")Long postId){
        log.info("Обновление комментария с comment_id={} и post_id={}: text='{}'",commentId,postId,request.getText());
        Comment comment=commentService.updateComment(commentId,postId, request.getText());
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable(name = "commentId")Long commentId,
                                              @PathVariable(name = "id")Long postId){
        log.info("Удаление комментария по comment_id={} и post_id={}",commentId,postId);
        commentService.deleteComment(commentId,postId);
        log.info("Комментраий с id={} успешно удален",commentId);
        return ResponseEntity.ok().build();
    }
}
