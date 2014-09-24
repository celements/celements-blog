package com.celements.blog.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadParameter;
import com.celements.blog.plugin.EmailAddressDate;
import com.celements.blog.plugin.NewsletterReceivers;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("celblog")
public class BlogScriptService implements ScriptService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      BlogScriptService.class);

  @Requirement
  IBlogServiceRole blogService;

  @Requirement
  Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
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
  
  public ArticleLoadParameter newArticleLoadParameter() {
    return new ArticleLoadParameter();
  }
  
  public List<Article> getArticles(DocumentReference blogConfDocRef) {
    return getArticles(blogConfDocRef, null);
  }
  
  public List<Article> getArticles(DocumentReference blogConfDocRef, 
      ArticleLoadParameter param) {
    List<Article> ret = Collections.emptyList();
    try {
      if (blogConfDocRef != null) {
        ret = blogService.getArticles(blogConfDocRef, param);
      }
    } catch (Exception exc) {
      LOGGER.error("Error getting articles for '" + blogConfDocRef + "' and param '" 
          + param + "'", exc);
    }
    return ret;
  }

}
