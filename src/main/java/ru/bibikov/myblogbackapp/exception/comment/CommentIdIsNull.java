package ru.bibikov.myblogbackapp.exception.comment;

public class CommentIdIsNull extends RuntimeException {
    public CommentIdIsNull(String message) {
        super(message);
    }
}
