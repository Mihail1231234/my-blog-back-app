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

    private final PostService service;
    private final PostRepository repository;
    private final FileService fileService;
    private final FileRepository fileRepository;

    @PostMapping()
    public ResponseEntity<PostPreviewDto> createPost(@RequestBody CreatePostRequest request){
        PostPreviewDto post=service.createPost(request.getTitle(),request.getText(),request.getTags());
        URI location=URI.create("/api/posts/"+post.getId());
        return ResponseEntity
                .created(location)
                .body(post);
    }

    @GetMapping()
    public PostResponse getPosts(@RequestParam(name = "search",required = false,defaultValue = "") String search,
                                 @RequestParam(name = "pageNumber") int pageNumber,
                                 @RequestParam(name = "pageSize") int pageSize){
        return service.getPosts(search,pageNumber,pageSize);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable(name = "id") Long id){
        Post post=service.getPost(id);
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
        return service.updatePost(id,request.getTitle(),request.getText(),request.getTags());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable(name = "id") Long id){
        int delete= service.deletePost(id);
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
/*public Long createPostReturnId(String title,        //updated
                                   String text,
                                   List<String> tags){
        String sqlForCreate="insert into posts(title,text) values (?,?) returning id";
        return jdbcTemplate.queryForObject(sqlForCreate,Long.class,title,text);
    }

    public void createPostTagChain(Long postId,Long tagId){ //updated
        jdbcTemplate.update("insert into post_tags(post_id,tag_id) values (?,?)",postId,tagId);
    }

    public List<Tag> tagsExist(String placeholders,List<String> tags){      //updated
        String sqlForTags = "select * from tags where tags.name in(" + placeholders + ")";
        return jdbcTemplate.query(sqlForTags,tagRowMapper,tags.toArray());
    }

    public Long createTag(String tagName){      //updated
        String sqlForCreateTags="insert into tags(name) values(?) returning id";
        return jdbcTemplate.queryForObject(sqlForCreateTags, Long.class, tagName);
    }

    public Post getPostWithId(Long id){         //updated
        String sqlForSelect="select * from posts where id=?";
        return jdbcTemplate.queryForObject(sqlForSelect,postRowMapper,id);
    }

    public int countPosts(String searchPattern){        //updated
        String COUNT_POSTS="select count(*) from posts where lower(title) like lower(?)";
        return jdbcTemplate.queryForObject(
                COUNT_POSTS,
                Integer.class,
                searchPattern
        );
    }

    public List<PostPreviewDto> findPostPaginated(String searchPattern, int pageSize, int offset){ //updated
        String FIND_POSTS_PAGINATED="select *from posts where lower(title) like lower(?)" +
                "order by id desc limit ? offset ?";

        return jdbcTemplate.query(
                FIND_POSTS_PAGINATED,
                (rs, rowNum) -> {

                    String text = rs.getString("text");
                    if (text != null && text.length() > 128) {
                        text = text.substring(0, 128) + "…";
                    }

                    List<String> tagsList=getTags(rs.getLong("id"));

                    return PostPreviewDto.builder()
                            .id(rs.getLong("id"))
                            .title(rs.getString("title"))
                            .text(text)
                            .tags(tagsList)
                            .likesCount(rs.getInt("likes_count"))
                            .commentsCount(rs.getInt("comments_count"))
                            .build();
                },
                searchPattern,
                pageSize,
                offset
        );
    }

    public Post getPost(Long id){ //updated
        String sql="select * from posts where id=?";
        return jdbcTemplate.queryForObject(sql, postRowMapper, id);
    }

    public int deletePost(Long id){ //updated
        String sql="delete from posts where id=?";
        return jdbcTemplate.update(sql,id);
    }

    public int likesIncrement(Long id) {
        String sqlUpdate="update posts set likes_count=likes_count+1 where id=?";
        jdbcTemplate.update(sqlUpdate,id);

        String sqlSelect="select likes_count from posts where id =?";
        return jdbcTemplate.queryForObject(sqlSelect,Integer.class,id);
    }

    public Comment createComment(Long id, String comment){
        String sql="insert into comments(text, post_id) VALUES (?,?) returning id";
        Long newId=jdbcTemplate.queryForObject(sql,Long.class,comment,id);
        String sqlForPosts="update posts set comments_count=comments_count+1 where id=?";
        jdbcTemplate.update(sqlForPosts,id);

        String sqlFromDb="select * from comments where id=?";
        return jdbcTemplate.queryForObject(sqlFromDb,commentRowMapper,newId);
    }

    public List<Comment> getComments(Long id){
        String sql="select * from comments where post_id=?";
        return jdbcTemplate.query(sql,commentRowMapper,id);
    }

    public Comment getComment(Long id){
        String sql="select * from comments where id=?";
        return jdbcTemplate.queryForObject(sql,commentRowMapper,id);
    }

    public Comment updateComment(Long id,String text){
        String sqlUpdate="update comments set text=? where id=?";
        jdbcTemplate.update(sqlUpdate,text,id);

        String sqlSelect="select * from comments where id=?";
        return jdbcTemplate.queryForObject(sqlSelect,commentRowMapper,id);
    }

    public void deleteComment(Long id,Long postId){
        String sql="delete from comments where id=?";
        jdbcTemplate.update(sql,id);
        String sqlForComment="update posts set comments_count=comments_count-1 where id=?";
        jdbcTemplate.update(sqlForComment,postId);
    }

    public List<String> getTags(Long postId){           //updated
        String sqlFromPostTagDb="select tags.id, tags.name from tags " +
                                "join post_tags on tags.id=post_tags.tag_id " +
                                "where post_id=?";
        return jdbcTemplate.query(sqlFromPostTagDb,tagRowMapper,postId).stream()
                .map(Tag::getName)
                .toList();
    }*/