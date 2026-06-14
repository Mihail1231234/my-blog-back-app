package ru.bibikov.myblogbackapp.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "posts",
        "hasPrev",
        "hasNext",
        "lastPage"
})
public class PostResponse {

    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;
    private List<PostPreviewDto> posts;
}
