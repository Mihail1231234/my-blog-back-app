package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bibikov.myblogbackapp.dto.*;
import ru.bibikov.myblogbackapp.exception.comment.CommentWithIdNotFound;
import ru.bibikov.myblogbackapp.exception.post.PostWithIdNotFound;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.repository.FileRepository;
import ru.bibikov.myblogbackapp.repository.PostRepository;
import ru.bibikov.myblogbackapp.service.CommentService;
import ru.bibikov.myblogbackapp.service.FileService;
import ru.bibikov.myblogbackapp.service.PostService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost")
public class Controller {

    private final PostService postService;
    private final CommentService commentService;
    private final FileService fileService;
    private final FileRepository fileRepository;

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
    public ResponseEntity<?> getPost(@PathVariable(name = "id") Long id){
        Post post=postService.getPost(id);
        return ResponseEntity.ok(PostPreviewDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .tags(post.getTags() != null ? post.getTags() : List.of())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .build());
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
    @PutMapping("/{id}/image")
    public ResponseEntity<Void> addOrUpdateImage(@PathVariable(name = "id") Long id, @RequestParam(name = "image") MultipartFile file){
        String imagePath= fileService.saveImage(file);
        fileService.updatePostService(id,imagePath);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable(name = "id")Long id) throws IOException {
        String fileName=fileRepository.getImage(id);

        Path filePath= Paths.get(fileService.getUploadDir(),fileName);
        Resource resource=new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @ExceptionHandler(PostWithIdNotFound.class)
    public ResponseEntity<String> handlePostNotFound(PostWithIdNotFound e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Пост не найден "+e);
    }
    @ExceptionHandler(CommentWithIdNotFound.class)
    public ResponseEntity<String> handlePostNotFound(CommentWithIdNotFound e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Комментарий не найден "+e);
    }
}
