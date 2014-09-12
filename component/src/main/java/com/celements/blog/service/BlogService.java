package com.celements.blog.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadException;
import com.celements.blog.article.ArticleSearchParameter;
import com.celements.blog.article.IArticleEngineRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class BlogService implements IBlogServiceRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(BlogService.class);
  
  @Requirement
  private ComponentManager componentManager;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName) {
    try {
      String xwql = "from doc.object(Celements2.BlogConfigClass) as bconfig";
      xwql += " where bconfig.blogspace = :blogSpaceName";
      Query query = queryManager.createQuery(xwql, Query.XWQL);
      query.bindValue("blogSpaceName", blogSpaceName);
      List<String> blogList = query.setLimit(1).execute();
      if (blogList.size() > 0) {
        DocumentReference blogDocRef = webUtilsService.resolveDocumentReference(
            blogList.get(0));
        if (getContext().getWiki().exists(blogDocRef, getContext())) {
          return blogDocRef;
        }
      }
    } catch (QueryException queryExp) {
      LOGGER.error("Failed to parse xwql query to get BlogDocRef for blog space ["
          + blogSpaceName + "].", queryExp);
    }
    return null;
  }

  public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName) {
    DocumentReference blogDocRef = getBlogDocRefByBlogSpace(blogSpaceName);
    if (blogDocRef != null) {
      try {
        return getContext().getWiki().getDocument(blogDocRef, getContext());
      } catch (XWikiException exp) {
        LOGGER.error("Failed to get blog document for blog space [" + blogSpaceName
            + "].", exp);
      }
    }
    return null;
  }

  @Override
  public List<Article> getArticles(ArticleSearchParameter param
      ) throws ArticleLoadException {
    List<Article> articles;
    if (param == null) {
      // TODO create new param
    }
    IArticleEngineRole engine = getArticleEngine();
    if (engine != null) {
      articles = Collections.unmodifiableList(engine.getArticles(param));
    } else {
      articles = Collections.emptyList();
    }
    LOGGER.info("getBlogArticles: for " + param + " got " + articles.size() 
        + " articles");
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("getBlogArticles: for " + param + " got: " + articles);
    }
    return articles;
  }

  private IArticleEngineRole getArticleEngine() {
    IArticleEngineRole engine = null;
    String engineHint = getContext().getWiki().getXWikiPreference("blog_article_engine",
        "blog.article.engine", null, getContext());
    try {
      Map<String, IArticleEngineRole> engineMap = componentManager.lookupMap(
          IArticleEngineRole.class);
      engine = engineMap.get(engineHint);
      if (engine == null) {
        engine = engineMap.get("default");
      }
    } catch (ComponentLookupException exc) {
      LOGGER.error("Error looking up engine components", exc);
    }
    LOGGER.info("getBlogArticleEngine: got engine '" + engine + "' for hint '" 
        + engineHint + "'");
    return engine;
  }

}
