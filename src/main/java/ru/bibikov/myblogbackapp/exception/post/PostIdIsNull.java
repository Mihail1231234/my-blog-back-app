package ru.bibikov.myblogbackapp.exception.post;

public class PostIdIsNull extends RuntimeException {
    public PostIdIsNull(String message) {
        super(message);
    }
}
