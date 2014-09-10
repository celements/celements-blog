package com.celements.blog.article;

public class ArticleLoadException extends Exception {

  private static final long serialVersionUID = 20140910181450L;
  
  ArticleLoadException(Throwable cause) {
    super(cause);
  }

}
