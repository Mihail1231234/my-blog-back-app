package ru.bibikov.myblogbackapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    private Long id;
    private String text;
    private Long postId;
}
