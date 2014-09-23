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
import com.celements.search.lucene.query.LuceneQuery;
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
  private IArticleLuceneQueryBuilderRole queryBuilder;

  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public List<Article> getArticles(ArticleSearchParameter param
      ) throws ArticleLoadException {
    try {
      List<Article> articles = new ArrayList<Article>();
      LuceneQuery query = queryBuilder.build(param);
      if (query != null) {
        LuceneSearchResult result = searchService.searchWithoutChecks(query, 
            param.getSortFields(), Arrays.asList(param.getLanguage()));
        result.setOffset(param.getOffset()).setLimit(param.getLimit());
        for (DocumentReference docRef : result.getResults()) {
          XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
          try {
            articles.add(new Article(doc, getContext()));
          } catch (EmptyArticleException exc) {
            LOGGER.warn("empty article: " + doc, exc);
          }
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
