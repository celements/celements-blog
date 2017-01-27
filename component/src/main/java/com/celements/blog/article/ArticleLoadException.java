package com.celements.blog.article;

public class ArticleLoadException extends Exception {

  private static final long serialVersionUID = 20140910181450L;

  public ArticleLoadException(String msg) {
    super(msg);
  }

  public ArticleLoadException(Throwable cause) {
    super(cause);
  }

  public ArticleLoadException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
