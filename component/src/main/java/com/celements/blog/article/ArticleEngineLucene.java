package com.celements.blog.article;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.plugin.BlogClasses;
import com.celements.blog.plugin.EmptyArticleException;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("lucene")
public class ArticleEngineLucene implements IArticleEngineRole {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      ArticleEngineLucene.class);
  
  @Requirement
  private ILuceneSearchService searchService;
  
  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;

  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public List<Article> getArticles(ArticleSearchParameter param
      ) throws ArticleLoadException {
    List<Article> articles = new ArrayList<Article>();
    LuceneSearchResult result;
    if (param.isSkipChecks()) {
      result = searchService.searchWithoutChecks(convertToLuceneQuery(param), 
          param.getSortFields(), Arrays.asList(param.getLanguage()));
    } else {
      result = searchService.search(convertToLuceneQuery(param), param.getSortFields(), 
          Arrays.asList(param.getLanguage()));
    }
    result.setOffset(param.getOffset()).setLimit(param.getLimit());
    try {
      for (DocumentReference docRef : result.getResults()) {
        XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
        try {
          articles.add(new Article(doc, getContext()));
        } catch (EmptyArticleException exc) {
          LOGGER.warn("empty article: " + doc, exc);
        }
      }
      return articles;
    } catch (LuceneSearchException lse) {
      throw new ArticleLoadException(lse);
    } catch (XWikiException xwe) {
      throw new ArticleLoadException(xwe);
    }
  }

  // TODO
  public LuceneQueryApi convertToLuceneQuery(ArticleSearchParameter param) {
    LuceneQueryApi query = searchService.createQuery(param.getDatabase());
    return query;
  }
  
  private BlogClasses getBlogClasses() {
    return (BlogClasses) blogClasses;
  }

}
