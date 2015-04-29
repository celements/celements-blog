package com.celements.blog.article;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleEngineLucene.class);
  
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
  public List<Article> getArticles(ArticleLoadParameter param
      ) throws ArticleLoadException {
    try {
      List<Article> articles = new ArrayList<Article>();
      LuceneQuery query = queryBuilder.build(param);
      if (query != null) {
        LuceneSearchResult result = searchService.searchWithoutChecks(query, 
            param.getSortFields(), Arrays.asList("default", param.getLanguage()));
        result.setOffset(param.getOffset()).setLimit(param.getLimit());
        for (EntityReference ref : result.getResults()) {
          if (ref instanceof DocumentReference) {
            XWikiDocument doc = getContext().getWiki().getDocument((DocumentReference) ref, 
                getContext());
            try {
              articles.add(new Article(doc, getContext()));
            } catch (EmptyArticleException exc) {
              LOGGER.warn("getArticles: empty article '{}'", exc, doc);
            }
          } else {
            LOGGER.warn("getArticles: not expecting Attachment as search result '{}' "
                + "for search '{}'", ref, param);
          }
        }
      } else {
        LOGGER.warn("got null from query builder for '" + param + "'");
      }
      return articles;
    } catch (LuceneSearchException lse) {
      throw new ArticleLoadException(lse);
    } catch (XWikiException xwe) {
      throw new ArticleLoadException(xwe);
    }
  }

  void injectSearchService(ILuceneSearchService searchService) {
    this.searchService = searchService;
  }

  void injectQueryBuilder(IArticleLuceneQueryBuilderRole queryBuilder) {
    this.queryBuilder = queryBuilder;
  }

}
