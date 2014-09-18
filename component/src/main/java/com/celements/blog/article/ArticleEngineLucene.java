package com.celements.blog.article;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.blog.article.ArticleSearchParameter.SubscriptionMode;
import com.celements.blog.plugin.BlogClasses;
import com.celements.blog.plugin.EmptyArticleException;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.web.service.IWebUtilsService;
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
  private IBlogServiceRole blogService;
  
  @Requirement
  private IWebUtilsService webUtilsService;
  
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
    try {
      List<Article> articles = new ArrayList<Article>();
      LuceneSearchResult result;
      // TODO rights check
      if (param.isSkipChecks()) {
        result = searchService.searchWithoutChecks(convertToLuceneQuery(param), 
            param.getSortFields(), Arrays.asList(param.getLanguage()));
      } else {
        result = searchService.search(convertToLuceneQuery(param), param.getSortFields(), 
            Arrays.asList(param.getLanguage()));
      }
      result.setOffset(param.getOffset()).setLimit(param.getLimit());
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

  LuceneQuery convertToLuceneQuery(ArticleSearchParameter param) throws XWikiException {
    String database = param.getBlogSpaceRef().getParent().getName();
    LuceneQuery query = searchService.createQuery(database);

    DocumentReference articleClassRef = getBlogClasses().getArticleClassRef(database);
    query.add(searchService.createObjectRestriction(articleClassRef));
    if (StringUtils.isNotBlank(param.getLanguage())) {
      query.add(searchService.createFieldRestriction(articleClassRef, "lang", 
          "\"" + param.getLanguage() + "\""));
    }
    
    // TODO DateMode
    
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(
        param.getSubscriptionModes());
    QueryRestrictionGroup blogOrSubsGrp = searchService.createRestrictionGroup(Type.OR);
    if (modes.remove(SubscriptionMode.BLOG)) {
      blogOrSubsGrp.add(searchService.createSpaceRestriction(param.getBlogSpaceRef()));
    }
    if (!modes.isEmpty()) {
      QueryRestrictionGroup subsGrp = searchService.createRestrictionGroup(Type.AND);
      subsGrp.add(searchService.createFieldRestriction(articleClassRef, 
          BlogClasses.PROPERTY_ARTICLE_IS_SUBSCRIBABLE, "\"1\""));
      QueryRestrictionGroup spacesOrGrp = searchService.createRestrictionGroup(Type.OR);
      for (DocumentReference docRef : param.getSubscribedToBlogs()) {
        SpaceReference spaceRef = blogService.getBlogSpaceRef(docRef);
        if (spaceRef != null) {
          QueryRestrictionGroup spaceGrp = searchService.createRestrictionGroup(Type.AND);
          spaceGrp.add(searchService.createSpaceRestriction(spaceRef));
          spaceGrp.add(getArticleSubsRestrictions(modes, docRef));
          spacesOrGrp.add(spaceGrp);
        }
      }
      subsGrp.add(spacesOrGrp);
      blogOrSubsGrp.add(subsGrp);
    }
    query.add(blogOrSubsGrp);
    return query;
  }

  QueryRestrictionGroup getArticleSubsRestrictions(Set<SubscriptionMode> modes, 
      DocumentReference blogConfDocRef) {
    QueryRestrictionGroup ret = searchService.createRestrictionGroup(Type.AND);
    DocumentReference classRef = getBlogClasses().getBlogArticleSubscriptionClassRef(
        blogConfDocRef.getWikiReference().getName());
    if (modes.size() < 3) {
      ret.add(searchService.createObjectRestriction(classRef));
      ret.add(searchService.createFieldRefRestriction(classRef, 
          BlogClasses.PROPERTY_ARTICLE_SUBSCRIPTION_SUBSCRIBER, blogConfDocRef));
      QueryRestrictionGroup doSubsRestrs = searchService.createRestrictionGroup(Type.OR);
      boolean modeUndecided = modes.contains(SubscriptionMode.UNDECIDED);
      if (modes.contains(SubscriptionMode.SUBSCRIBED) != modeUndecided) {
        doSubsRestrs.add(searchService.createFieldRestriction(classRef, 
            BlogClasses.PROPERTY_ARTICLE_SUBSCRIPTION_DO_SUBSCRIBE, "\"1\""));
      }
      if (modes.contains(SubscriptionMode.UNSUBSCRIBED) != modeUndecided) {
        doSubsRestrs.add(searchService.createFieldRestriction(classRef, 
            BlogClasses.PROPERTY_ARTICLE_SUBSCRIPTION_DO_SUBSCRIBE, "\"0\""));
      }
      ret.add(doSubsRestrs);
      ret.setNegate(modeUndecided);
    }    
    return ret;
  }

  private BlogClasses getBlogClasses() {
    return (BlogClasses) blogClasses;
  }

}
