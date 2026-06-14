package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bibikov.myblogbackapp.dto.*;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.repository.PostRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost")
public class Controller {

    private final PostRepository repository;

    @PostMapping()
    public ResponseEntity<PostPreviewDto> createPost(@RequestBody CreatePostRequest request){
        PostPreviewDto post=repository.createPost(request.getTitle(),request.getText(),request.getTags());
        URI location=URI.create("/api/posts/"+post.getId());
        return ResponseEntity
                .created(location)
                .body(post);
    }

    @GetMapping()
    public PostResponse getPosts(@RequestParam(name = "search",required = false,defaultValue = "") String search,
                                 @RequestParam(name = "pageNumber") int pageNumber,
                                 @RequestParam(name = "pageSize") int pageSize){
        return repository.getPosts(search,pageNumber,pageSize);
    }

    @GetMapping("/{id}")
    public PostPreviewDto getPost(@PathVariable(name = "id") Long id){
        Post post=repository.getPost(id);
        return PostPreviewDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .tags(post.getTags() != null ? post.getTags() : List.of())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable(name = "id") Long id){
        int delete= repository.deletePost(id);
        if (delete==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> likesIncrement(@PathVariable(name="id") Long id){
        int newLikesCount=repository.likesIncrement(id);
        return ResponseEntity.ok(newLikesCount);
    }
    @PostMapping("/{id}/comments")
    @CrossOrigin(origins = "http://localhost")
    public CommentResponse createComment(@RequestBody CreateCommentRequest request){
        Comment newComment=repository.createComment(request.getPostId(), request.getText());
        return CommentResponse.builder()
                .id(newComment.getId())
                .text(newComment.getText())
                .postId(newComment.getPostId())
                .build();
    }
    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable(name = "id")Long postId){
        List<Comment> comments=repository.getComments(postId);
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
        Comment comment=repository.getComment(id);
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
    @PutMapping("/{id}/comments/{commentId}")
    public CommentResponse updateComment(@RequestBody CommentResponse response,
                                         @PathVariable(name = "commentId")Long id){
        Comment comment=repository.updateComment(id, response.getText());
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable(name = "commentId")Long id,
                                              @PathVariable(name = "id")Long postId){
        repository.deleteComment(id,postId);
        return ResponseEntity.ok().build();
    }

}
