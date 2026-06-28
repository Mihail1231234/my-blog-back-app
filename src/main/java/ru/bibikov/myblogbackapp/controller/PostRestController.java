package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bibikov.myblogbackapp.dto.*;
import ru.bibikov.myblogbackapp.exception.comment.CommentWithIdNotFound;
import ru.bibikov.myblogbackapp.exception.post.PostWithIdNotFound;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.service.PostService;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost")
public class PostRestController {

    private final PostService postService;


    @PostMapping()
    public ResponseEntity<PostPreviewDto> createPost(@RequestBody CreatePostRequest request){
        PostPreviewDto post=postService.createPost(request.getTitle(),request.getText(),request.getTags());
        URI location=URI.create("/api/posts/"+post.getId());
        return ResponseEntity
                .created(location)
                .body(post);
    }

    @GetMapping()
    public PostResponse getPosts(@RequestParam(name = "search",required = false,defaultValue = "") String search,
                                 @RequestParam(name = "pageNumber") int pageNumber,
                                 @RequestParam(name = "pageSize") int pageSize){
        return postService.getPosts(search,pageNumber,pageSize);
    }

    @GetMapping("/{id}")
    public PostPreviewDto getPost(@PathVariable(name = "id") Long id){
        Post post=postService.getPost(id);
        return PostPreviewDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .tags(post.getTags() != null ? post.getTags() : List.of())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .build();
    }

    @PutMapping("/{id}")
    public PostPreviewDto updatePost(@PathVariable(name = "id") Long id,
                                     @RequestBody UpdatePostRequest request){
        return postService.updatePost(id,request.getTitle(),request.getText(),request.getTags());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable(name = "id") Long id){
        int delete= postService.deletePost(id);
        if (delete==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> likesIncrement(@PathVariable(name="id") Long id){
        return ResponseEntity.ok(postService.likesIncrement(id));
    }
}
