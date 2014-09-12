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

import com.celements.blog.plugin.EmptyArticleException;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("lucene")
public class ArticleEngineLucene implements IArticleEngineRole {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      ArticleEngineLucene.class);
  
  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public List<Article> getArticles(ArticleSearchQuery query) throws ArticleLoadException {
    List<Article> articles = new ArrayList<Article>();
    LuceneSearchResult result;
    if (query.isSkipChecks()) {
      result = searchService.searchWithoutChecks(query.getAsLuceneQuery(), 
          query.getSortFields(), Arrays.asList(query.getLanguage()));
    } else {
      result = searchService.search(query.getAsLuceneQuery(), query.getSortFields(), 
          Arrays.asList(query.getLanguage()));
    }
    result.setOffset(query.getOffset()).setLimit(query.getLimit());
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

}
