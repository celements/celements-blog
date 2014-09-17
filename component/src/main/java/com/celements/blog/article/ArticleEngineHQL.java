package com.celements.blog.article;

import java.util.List;

import org.xwiki.component.annotation.Component;

@Component
public class ArticleEngineHQL implements IArticleEngineRole {

  @Override
  public List<Article> getArticles(ArticleSearchParameter param
      ) throws ArticleLoadException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("TODO");
  } 

}
