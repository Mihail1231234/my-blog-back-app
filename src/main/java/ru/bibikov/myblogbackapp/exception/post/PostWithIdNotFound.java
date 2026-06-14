package ru.bibikov.myblogbackapp.exception.post;

public class PostWithIdNotFound extends RuntimeException {
    public PostWithIdNotFound(String message) {
        super(message);
    }
}
