package com.celements.blog.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadParameter;
import com.celements.blog.article.ArticleLoadParameter.DateMode;
import com.celements.blog.article.ArticleLoadParameter.SubscriptionMode;
import com.celements.blog.dto.BlogConfig;
import com.celements.blog.plugin.EmailAddressDate;
import com.celements.blog.plugin.EmptyArticleException;
import com.celements.blog.plugin.NewsletterReceivers;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.xpn.xwiki.XWikiException;

@Component("celblog")
public class BlogScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlogScriptService.class);

  private final IBlogServiceRole blogService;
  private final ModelContext mContext;
  private final IRightsAccessFacadeRole rightsAccess;
  private final IModelAccessFacade modelAccess;

  @Inject
  public BlogScriptService(IBlogServiceRole blogService, ModelContext mContext,
      IRightsAccessFacadeRole rightsAccess, IModelAccessFacade modelAccess) {
    this.blogService = blogService;
    this.mContext = mContext;
    this.rightsAccess = rightsAccess;
    this.modelAccess = modelAccess;
  }

  public Optional<BlogConfig> getBlogConfig(DocumentReference blogConfDocRef) {
    return blogService.getBlogConfig(blogConfDocRef);
  }

  public List<String> getAddresses(DocumentReference blogDocRef) {
    return getNewsletterReceiversDetails(blogDocRef, nR -> nR.getAllAddresses());
  }

  public List<EmailAddressDate> getAddressesOrderedByDate(DocumentReference blogDocRef) {
    return getNewsletterReceiversDetails(blogDocRef, nR -> nR.getAddressesOrderByDate());
  }

  private <T extends Comparable<? super T>> List<T> getNewsletterReceiversDetails(
      DocumentReference blogDocRef,
      Function<NewsletterReceivers, List<T>> dataAccessor) {
    if (rightsAccess.isAdmin()) {
      try {
        NewsletterReceivers newsletterReceivers = new NewsletterReceivers(
            modelAccess.getDocument(blogDocRef));
        List<T> data = dataAccessor.apply(newsletterReceivers);
        Collections.sort(data);
        return data;
      } catch (XWikiException | DocumentNotExistsException exp) {
        LOGGER.error("Failed to get Blog document for [{}].", blogDocRef, exp);
      }
    } else {
      LOGGER.info("getAddresses failed because user [{}] has no admin rights.",
          mContext.getUserName());
    }
    return Collections.emptyList();
  }

  public DocumentReference getBlogDocRefForSpaceRef(SpaceReference spaceRef) {
    DocumentReference ret = null;
    try {
      if (spaceRef != null) {
        ret = blogService.getBlogConfigDocRef(spaceRef);
      }
    } catch (Exception exc) {
      LOGGER.error("Error getting blog config for '{}'", spaceRef, exc);
    }
    return ret;
  }

  public SpaceReference getBlogSpaceRef(DocumentReference blogConfDocRef) {
    SpaceReference ret = null;
    try {
      ret = blogService.getBlogSpaceRef(blogConfDocRef);
    } catch (Exception exc) {
      LOGGER.error("Error getting blog space ref for '{}'", blogConfDocRef, exc);
    }
    return ret;
  }

  public List<SpaceReference> getSubribedToBlogs(DocumentReference blogConfDocRef) {
    List<SpaceReference> ret;
    try {
      ret = blogService.getSubribedToBlogsSpaceRefs(blogConfDocRef);
    } catch (Exception exc) {
      LOGGER.error("Error getting blog space ref for '{}'", blogConfDocRef, exc);
      ret = Collections.emptyList();
    }
    return ret;
  }

  public ArticleLoadParameter newArticleLoadParameter() {
    return new ArticleLoadParameter();
  }

  public ArticleLoadParameter getDefaultArticleLoadParameter() {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setDateModes(new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.FUTURE)));
    param.setSubscriptionModes(new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED,
        SubscriptionMode.UNDECIDED)));
    return param;
  }

  public ArticleLoadParameter getArchiveArticleLoadParameter() {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setDateModes(new HashSet<>(Arrays.asList(DateMode.ARCHIVED)));
    param.setSubscriptionModes(new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED,
        SubscriptionMode.UNDECIDED)));
    return param;
  }

  public ArticleLoadParameter getAllArticleLoadParameter() {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setDateModes(new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.FUTURE,
        DateMode.ARCHIVED)));
    param.setSubscriptionModes(new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED,
        SubscriptionMode.UNSUBSCRIBED, SubscriptionMode.UNDECIDED)));
    return param;
  }

  public ArticleLoadParameter getAllSubsribedArticleLoadParameter() {
    ArticleLoadParameter param = getAllArticleLoadParameter();
    param.setWithBlogArticles(false);
    return param;
  }

  public ArticleLoadParameter getUndecidedArticleLoadParameter() {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setDateModes(new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.FUTURE,
        DateMode.ARCHIVED)));
    param.setSubscriptionModes(new HashSet<>(Arrays.asList(SubscriptionMode.UNDECIDED)));
    param.setWithBlogArticles(false);
    return param;
  }

  public List<Article> getArticles(DocumentReference blogConfDocRef) {
    return getArticles(blogConfDocRef, null);
  }

  public List<Article> getArticles(DocumentReference blogConfDocRef, ArticleLoadParameter param) {
    List<Article> ret = Collections.emptyList();
    try {
      if (blogConfDocRef != null) {
        ret = blogService.getArticles(blogConfDocRef, param);
      }
    } catch (Exception exc) {
      LOGGER.error("Error getting articles for '{}' and param '{}'", blogConfDocRef, param, exc);
    }
    return ret;
  }

  public Article getArticle(DocumentReference articleDocRef) throws XWikiException {
    Article article = null;
    try {
      article = new Article(modelAccess.getDocument(articleDocRef),
          mContext.getXWikiContext());
    } catch (EmptyArticleException | DocumentNotExistsException exc) {
      LOGGER.info("Empty article {}", articleDocRef, exc);
    }
    return article;
  }

}
