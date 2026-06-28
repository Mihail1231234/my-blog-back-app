package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bibikov.myblogbackapp.dto.CommentResponse;
import ru.bibikov.myblogbackapp.dto.CreateCommentRequest;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.service.CommentService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/post")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost")
public class CommentRestController {

    private final CommentService commentService;

    @PostMapping("/{id}/comments")
    @CrossOrigin(origins = "http://localhost")
    public CommentResponse createComment(@RequestBody CreateCommentRequest request){
        Comment newComment=commentService.createComment(request.getPostId(), request.getText());
        return CommentResponse.builder()
                .id(newComment.getId())
                .text(newComment.getText())
                .postId(newComment.getPostId())
                .build();
    }
    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable(name = "id")Long postId){
        List<Comment> comments=commentService.getComments(postId);
        List<CommentResponse> responses = new ArrayList<>();
        for (Comment comment:comments){
            CommentResponse commentResponse=CommentResponse.builder()
                    .id(comment.getId())
                    .text(comment.getText())
                    .postId(comment.getPostId())
                    .build();
            responses.add(commentResponse);
        }
        return responses;
    }
    @GetMapping("/{id}/comments/{commentId}")
    public CommentResponse getComment(@PathVariable(name = "commentId") Long id){
        Comment comment=commentService.getComment(id);
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
    @PutMapping("/{id}/comments/{commentId}")
    public CommentResponse updateComment(@RequestBody CommentResponse response,
                                         @PathVariable(name = "commentId")Long id){
        Comment comment=commentService.updateComment(id, response.getText());
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable(name = "commentId")Long id,
                                              @PathVariable(name = "id")Long postId){
        commentService.deleteComment(id,postId);
        return ResponseEntity.ok().build();
    }
}
