package com.celements.blog.article;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.blog.plugin.BlogClasses;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface IArticleLuceneQueryBuilderRole {
  
  static final String ARTICLE_FIELD_PUBLISH = BlogClasses.ARTICLE_CLASS + "." 
      + BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE;
  static final String ARTICLE_FIELD_ARCHIVE = BlogClasses.ARTICLE_CLASS + "." 
      + BlogClasses.PROPERTY_ARTICLE_ARCHIVE_DATE;
  
  public LuceneQuery build(ArticleSearchParameter param) throws XWikiException;

}
