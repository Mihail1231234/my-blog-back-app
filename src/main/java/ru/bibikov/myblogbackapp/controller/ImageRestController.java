package ru.bibikov.myblogbackapp.controller;

import lombok.AllArgsConstructor;
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
@RequestMapping("/api/post")
@AllArgsConstructor
@CrossOrigin("http://localhost")
public class ImageRestController {
    private final FileService service;

    @PutMapping("/{id}/image")
    public ResponseEntity<Void> addOrUpdateImage(@PathVariable(name = "id") Long id, @RequestParam(name = "image") MultipartFile file){
        String imagePath= service.saveImage(file);
        service.updatePostService(id,imagePath);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable(name = "id")Long id) throws IOException {
        String fileName=service.getImage(id);

        Path filePath= Paths.get(service.getUploadDir(),fileName);
        Resource resource=new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
