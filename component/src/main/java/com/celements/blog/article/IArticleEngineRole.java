package com.celements.blog.article;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.blog.plugin.BlogClasses;

@ComponentRole
public interface IArticleEngineRole {
  
  static final String ARTICLE_FIELD_PUBLISH = BlogClasses.ARTICLE_CLASS + "." 
      + BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE;
  static final String ARTICLE_FIELD_ARCHIVE = BlogClasses.ARTICLE_CLASS + "." 
      + BlogClasses.PROPERTY_ARTICLE_ARCHIVE_DATE;
  
  public List<Article> getArticles(ArticleSearchParameter param
      ) throws ArticleLoadException;

}
