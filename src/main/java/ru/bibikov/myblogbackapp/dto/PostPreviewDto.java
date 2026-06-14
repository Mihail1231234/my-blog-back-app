package ru.bibikov.myblogbackapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostPreviewDto {

    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private Integer likesCount;
    private Integer commentsCount;

}
