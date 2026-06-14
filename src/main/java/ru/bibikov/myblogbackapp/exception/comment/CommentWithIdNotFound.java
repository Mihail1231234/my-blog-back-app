package ru.bibikov.myblogbackapp.exception.comment;

public class CommentWithIdNotFound extends RuntimeException {
  public CommentWithIdNotFound(String message) {
    super(message);
  }
}
