package com.celements.blog.article;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.blog.plugin.EmptyArticleException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.plugin.lucene.SearchResult;
import com.xpn.xwiki.plugin.lucene.SearchResults;

@Component("lucene")
public class ArticleEngineLucene implements IArticleEngineRole {

  private LucenePlugin lucenePlugin;

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      ArticleEngineLucene.class);

  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public List<Article> getArticles(ArticleSearchQuery query) throws ArticleLoadException {
    try {
      String queryString = query.getAsLuceneQuery().getQueryString();
      String[] sortFieldsArray = query.getSortFields().toArray(
          new String[query.getSortFields().size()]);
      String language = query.getLanguage();
      SearchResults results;
      if (query.isSkipChecks()) {
        results = getLucenePlugin().getSearchResultsWithoutChecks(queryString, 
            sortFieldsArray, null, language, getContext());
      } else {
        results = getLucenePlugin().getSearchResults(queryString, sortFieldsArray, null, 
            language, getContext());
      }
      return getArticlesFromSearchResult(query, results);
    } catch (XWikiException xwe) {
      throw new ArticleLoadException(xwe);
    } catch (IOException ioe) {
      throw new ArticleLoadException(ioe);
    } catch (ParseException exc) {
      throw new ArticleLoadException(exc);
    }
  }
  
  private List<Article> getArticlesFromSearchResult(ArticleSearchQuery query, 
      SearchResults results) throws XWikiException {
    List<Article> articles = new ArrayList<Article>();
    int offset = query.getOffset() + 1;
    int limit = query.getLimit();
    if (limit <= 0) {
      limit = results.getHitcount();
    }
    for (SearchResult result : results.getResults(offset, limit)) {
      XWikiDocument articleDoc = getContext().getWiki().getDocument(
          result.getDocumentReference(), getContext());
      try {
        articles.add(new Article(articleDoc, getContext()));
      } catch (EmptyArticleException exc) {
        LOGGER.warn("empty article: " + articleDoc, exc);
      }
    }
    return articles;
  }

  private LucenePlugin getLucenePlugin() {
    if (lucenePlugin == null) {
      lucenePlugin = (LucenePlugin) getContext().getWiki().getPlugin("lucene", 
          getContext());
    }
    return lucenePlugin;
  }

  void injectLucenePlugin(LucenePlugin lucenePlugin) {
    this.lucenePlugin = lucenePlugin;
  }

}
