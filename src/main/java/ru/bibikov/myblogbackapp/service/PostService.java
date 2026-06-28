package ru.bibikov.myblogbackapp.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bibikov.myblogbackapp.dto.PostPreviewDto;
import ru.bibikov.myblogbackapp.dto.PostResponse;
import ru.bibikov.myblogbackapp.exception.post.PostIdIsNull;
import ru.bibikov.myblogbackapp.exception.post.PostWithIdNotFound;
import ru.bibikov.myblogbackapp.model.Post;
import ru.bibikov.myblogbackapp.model.Tag;
import ru.bibikov.myblogbackapp.repository.PostRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository repository;

    public PostPreviewDto createPost(String title,
                                     String text,
                                     List<String> tags){
        log.info("Начало создания поста: title='{}', text='{}', tags={}",title,text,tags);
        Long postId= repository.createPostReturnId(title,text);
        log.debug("Создан пост с id={}",postId);
        return checkTags(tags,postId);
    }


    public PostResponse getPosts(String search, int pageNumber, int pageSize) {
        log.debug("Получение постов: search='{}', page_number={}, page_size={}",search,pageNumber,pageSize);
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
        log.debug("Получение поста с post_id={}",id);
        validateId(id);
        Post post=repository.getPostWithId(id);
        post.setTags(repository.getTags(id));
        return post;
    }

    public int likesIncrement(Long id){
        log.debug("Инкрементация лайков с post_id={}",id);
        validateId(id);
        return repository.likesIncrement(id);
    }

    public PostPreviewDto updatePost(Long postId, String title,String text, List<String > tags){
        log.info("Обновление поста с post_id={}: title='{}', text='{}', tags={}",postId,title,text,tags);
        validateId(postId);
        repository.updatePost(postId,title,text);
        return checkTags(tags,postId);
    }

    public void deletePost(Long id){
        log.info("Удаление поста с post_id={}",id);
        validateId(id);
        repository.deletePost(id);
        log.info("Пост с post_id={} успешно удален",id);
    }

    private PostPreviewDto checkTags(List<String> tags,Long postId){
        log.debug("Начата проверка тегов {} для поста с post_id={}",tags,postId);
        validateId(postId);
        repository.deletePostTag(postId);
        String placeholders=String.join(",", Collections.nCopies(tags.size(),"?"));
        if (placeholders.isEmpty()) {
            log.debug("Проверка закончена без тегов");
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
        log.debug("Проверка тегов {} в таблице",tags);
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
        log.debug("Созданы все связи постов и тегов");
        Post post=repository.getPostWithId(postId);
        List<String> tag=repository.getTags(postId);
        log.debug("Обработано {} тегов",tag.size());
        return PostPreviewDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .tags(tag)
                .build();
    }
    private void validateId(Long postId){
        if (postId==null){
            throw new PostIdIsNull("ID поста равен null");
        }if (!repository.existId(postId)){
            throw new PostWithIdNotFound("Пост с таким ID не найден");
        }
    }
}
