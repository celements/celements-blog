package com.celements.blog.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadException;
import com.celements.blog.article.ArticleSearchQuery;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IBlogServiceRole {

  public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName);

  public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName);
  
  /**
   * gets articles as list for given {@link ArticleSearchQuery}
   * 
   * @param query may not be null
   * @return
   * @throws ArticleLoadException if there was an error loading articles
   */
  public List<Article> getArticles(ArticleSearchQuery query) throws ArticleLoadException;

}
