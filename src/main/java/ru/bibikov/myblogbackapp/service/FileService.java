package ru.bibikov.myblogbackapp.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.bibikov.myblogbackapp.exception.file.FileStorageException;
import ru.bibikov.myblogbackapp.repository.FileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Data
public class FileService {

    @Value("${app.upload-dir}")
    String uploadDir;

    @Value("#{'${app.upload-dir.allowed-image-extensions}'.split(',')}")
    Set<String> allowedImageExtensions=new HashSet<>();

    private final FileRepository repository;

    /*public String saveImage(MultipartFile multipartFile) {
        try {
            String fileName = multipartFile.getOriginalFilename();
            String nameWithoutPrefix = extract(fileName);
            String newNameFile = UUID.randomUUID() + nameWithoutPrefix;
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path filePath=uploadPath.resolve(newNameFile);
            try(InputStream inputStream=multipartFile.getInputStream()){
                Files.copy(inputStream,filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return newNameFile;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }*/
    public String saveImage(MultipartFile file) {
        log.info("=== START saveImage ===");
        log.info("File original name: {}", file.getOriginalFilename());
        log.info("File size: {} bytes", file.getSize());
        log.info("File content type: {}", file.getContentType());
        log.info("File is empty: {}", file.isEmpty());
        try {
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            String extension = extract(originalFilename);

            String fileName = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir);

            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);

            log.info("Saving file to {}", filePath.toAbsolutePath());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Image saved successfully {}", fileName);
            return fileName;

        } catch (IOException e) {

            log.error("Failed to save image {}", file.getOriginalFilename(), e);
            throw new FileStorageException("Failed to save image "+e);
        }
    }



    private String extract(String name){
        int index = name.lastIndexOf(".");
        return name.substring(index).toLowerCase();
    }

    public String getImage(Long postId){
        log.debug("Получение изображения поста с post_Id={}",postId);
        return repository.getImage(postId);
    }

    public void updatePostService(Long id, String file) {
        log.info("Редактирование изображения поста с post_id={}: file='{}'",id,file);
        repository.updateImage(id,file);
    }
}
