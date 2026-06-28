package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bibikov.myblogbackapp.service.FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@Slf4j
public class ImageRestController {
    private final FileService service;

    @PutMapping("/{id}/image")
    public ResponseEntity<Void> addOrUpdateImage(@PathVariable(name = "id") Long id,
                                                 @RequestParam(name = "image") MultipartFile file){
        log.info("Загрузка изображения для поста с post_id={}: image={}",id,file);
        String imagePath= service.saveImage(file);
        service.updatePostService(id,imagePath);
        log.info("Добавление в базу данных прошло успешно");
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable(name = "id")Long postId) throws IOException {
        log.debug("Получение изображения для поста с post_id={}",postId);
        String fileName=service.getImage(postId);
        log.debug("Изображение получено");
        Path filePath= Paths.get(service.getUploadDir(),fileName);
        Resource resource=new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
