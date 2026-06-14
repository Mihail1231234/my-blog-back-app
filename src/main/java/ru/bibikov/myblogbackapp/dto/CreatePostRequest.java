package ru.bibikov.myblogbackapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {

    private String title;
    private String text;
    private List<String> tags;
}