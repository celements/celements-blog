package com.celements.blog.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadParameter;
import com.celements.blog.article.ArticleLoadParameter.DateMode;
import com.celements.blog.article.ArticleLoadParameter.SubscriptionMode;
import com.celements.blog.plugin.EmailAddressDate;
import com.celements.blog.plugin.EmptyArticleException;
import com.celements.blog.plugin.NewsletterReceivers;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("celblog")
public class BlogScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlogScriptService.class);

  @Requirement
  private IBlogServiceRole blogService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public List<String> getAddresses(DocumentReference blogDocRef) {
    if (getContext().getWiki().getRightService().hasAdminRights(getContext())) {
      try {
        NewsletterReceivers newsletterReceivers = new NewsletterReceivers(
            getContext().getWiki().getDocument(blogDocRef, getContext()), getContext());
        List<String> addresses = newsletterReceivers.getAllAddresses();
        Collections.sort(addresses);
        return addresses;
      } catch (XWikiException exp) {
        LOGGER.error("Failed to get Blog document for [" + blogDocRef + "].", exp);
      }
    } else {
      LOGGER.info("getAddresses failed because user [" + getContext().getUser()
          + "] has no admin rights.");
    }
    return Collections.emptyList();
  }

  public List<EmailAddressDate> getAddressesOrderedByDate(DocumentReference blogDocRef) {
    if (getContext().getWiki().getRightService().hasAdminRights(getContext())) {
      try {
        NewsletterReceivers newsletterReceivers = new NewsletterReceivers(
            getContext().getWiki().getDocument(blogDocRef, getContext()), getContext());
        List<EmailAddressDate> addresses = newsletterReceivers.getAddressesOrderByDate();
        Collections.sort(addresses);
        return addresses;
      } catch (XWikiException exp) {
        LOGGER.error("Failed to get Blog document for [" + blogDocRef + "].", exp);
      }
    } else {
      LOGGER.info("getAddresses failed because user [" + getContext().getUser()
          + "] has no admin rights.");
    }
    return Collections.emptyList();
  }

  /**
   * @deprecated since 1.32 instead use {@link #getBlogDocRefForSpaceRef(SpaceReference)}
   * @param blogSpaceName
   * @return
   */
  @Deprecated
  public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName) {
    return getBlogDocRefForSpaceRef(new SpaceReference(blogSpaceName, new WikiReference(
        getContext().getDatabase())));
  }

  public DocumentReference getBlogDocRefForSpaceRef(SpaceReference spaceRef) {
    DocumentReference ret = null;
    try {
      if (spaceRef != null) {
        ret = blogService.getBlogConfigDocRef(spaceRef);
      }
    } catch (Exception exc) {
      LOGGER.error("Error getting blog config for '" + spaceRef + "'", exc);
    }
    return ret;
  }

  public SpaceReference getBlogSpaceRef(DocumentReference blogConfDocRef) {
    SpaceReference ret = null;
    try {
      ret = blogService.getBlogSpaceRef(blogConfDocRef);
    } catch (Exception exc) {
      LOGGER.error("Error getting blog space ref for '" + blogConfDocRef + "'", exc);
    }
    return ret;
  }

  public List<SpaceReference> getSubribedToBlogs(DocumentReference blogConfDocRef) {
    List<SpaceReference> ret;
    try {
      ret = blogService.getSubribedToBlogsSpaceRefs(blogConfDocRef);
    } catch (Exception exc) {
      LOGGER.error("Error getting blog space ref for '" + blogConfDocRef + "'", exc);
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
      LOGGER.error("Error getting articles for '" + blogConfDocRef + "' and param '" + param + "'",
          exc);
    }
    return ret;
  }

  public Article getArticle(DocumentReference articleDocRef) throws XWikiException {
    Article article = null;
    try {
      article = new Article(getContext().getWiki().getDocument(articleDocRef, getContext()),
          getContext());
    } catch (EmptyArticleException exc) {
      LOGGER.info("Empty article {}", articleDocRef, exc);
    }
    return article;
  }

  public void addArticleSocialMediaTagsToCollector(DocumentReference articleDocRef,
      String language) {
    Article article = null;
    try {
      article = getArticle(articleDocRef);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception getting article for doc ref {}", articleDocRef, xwe);
    }
    if (article != null) {
      article.addArticleSocialMediaTagsToCollector(language);
    }
  }

}
