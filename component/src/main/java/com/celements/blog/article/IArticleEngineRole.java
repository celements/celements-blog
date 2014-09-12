package com.celements.blog.article;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IArticleEngineRole {
  
  public List<Article> getArticles(ArticleSearchParameter param
      ) throws ArticleLoadException;

}
