package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bibikov.myblogbackapp.dto.*;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.service.PostService;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@Slf4j
public class PostRestController {

    private final PostService postService;


    @PostMapping()
    public ResponseEntity<PostPreviewDto> createPost(@RequestBody CreatePostRequest request){
        log.info("Создание поста: title='{}',text='{}',tags={}",request.getTitle(),request.getText(),request.getTags());
        PostPreviewDto post=postService.createPost(request.getTitle(),request.getText(),request.getTags());
        log.info("Пост успешно создан с id={}",post.getId());
        URI location=URI.create("/api/posts/"+post.getId());
        return ResponseEntity
                .created(location)
                .body(post);
    }

    @GetMapping()
    public PostResponse getPosts(@RequestParam(name = "search",required = false,defaultValue = "") String search,
                                 @RequestParam(name = "pageNumber") int pageNumber,
                                 @RequestParam(name = "pageSize") int pageSize){
        log.debug("Запрос постов: search='{}', page={}, size={}",search,pageNumber,pageSize);
        return postService.getPosts(search,pageNumber,pageSize);
    }

    @GetMapping("/{id}")
    public PostPreviewDto getPost(@PathVariable(name = "id") Long id){
        log.debug("Запрос поста по id={}",id);
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
        log.info("Обновление поста с id='{}': title='{}', text='{}', tags={}",id,request.getTitle(),request.getText(),request.getTags());
        return postService.updatePost(id,request.getTitle(),request.getText(),request.getTags());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable(name = "id") Long id){
        log.info("Удаление поста с id={}",id);
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> likesIncrement(@PathVariable(name="id") Long id){
        log.info("Инкрементация лайков по id={}",id);
        return ResponseEntity.ok(postService.likesIncrement(id));
    }
}
