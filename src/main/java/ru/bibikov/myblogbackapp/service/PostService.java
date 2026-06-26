package ru.bibikov.myblogbackapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.bibikov.myblogbackapp.dto.PostPreviewDto;
import ru.bibikov.myblogbackapp.dto.PostResponse;
import ru.bibikov.myblogbackapp.model.Comment;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.model.Tag;
import ru.bibikov.myblogbackapp.repository.PostRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository repository;

    public PostPreviewDto createPost(String title,
                                     String text,
                                     List<String> tags){

        Long postId= repository.createPostReturnId(title,text,tags);

        return checkTags(tags,postId);
    }


    public PostResponse getPosts(String search, int pageNumber, int pageSize) {

        String searchPattern = "%" + search + "%";

        int totalPosts = repository.countPosts(searchPattern);

        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
        lastPage = Math.max(lastPage, 1);

        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        int offset = (pageNumber - 1) * pageSize;

        List<PostPreviewDto> posts = repository.findPostPaginated(searchPattern,pageSize,offset);

        return PostResponse.builder()
                .posts(posts)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .lastPage(lastPage)
                .build();
    }

    public Post getPost(Long id) {
        Post post=repository.getPost(id);
        post.setTags(repository.getTags(id));
        return post;
    }

    public PostPreviewDto updatePost(Long postId, String title,String text, List<String > tags){
        repository.updatePost(postId,title,text);
        return checkTags(tags,postId);
    }

    public int deletePost(Long id){
        return repository.deletePost(id);
    }

    private PostPreviewDto checkTags(List<String> tags,Long postId){
        repository.deletePostTag(postId);
        String placeholders=String.join(",", Collections.nCopies(tags.size(),"?"));
        if (placeholders.isEmpty()) {
            Post post=repository.getPostWithId(postId);

            return PostPreviewDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .text(post.getText())
                    .likesCount(post.getLikesCount())
                    .commentsCount(post.getCommentsCount())
                    .tags(List.of())
                    .build();
        }
        List<Tag> tagsExist=repository.tagsExist(placeholders,tags);

        Map<String,Long> tagNameToId=new HashMap<>();
        for (Tag tag:tagsExist){
            tagNameToId.put(tag.getName(),tag.getId());
        }

        for (String tagName:tags){
            if (!tagNameToId.containsKey(tagName)) {
                Long newId = repository.createTag(tagName);
                tagNameToId.put(tagName, newId);
            }
        }
        for (String tagName:tags){
            Long tagId=tagNameToId.get(tagName);
            repository.createPostTagChain(postId,tagId);
        }
        Post post=repository.getPostWithId(postId);
        List<String> tag=repository.getTags(postId);
        return PostPreviewDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .tags(tag)
                .build();
    }
}
