package com.celements.blog.observation.listener;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.ArticleCreatingEvent;
import com.celements.blog.observation.event.ArticleUpdatingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.observation.listener.AbstractEventListener;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.blog.articleDatesListener")
public class ArticleDatesListener extends AbstractEventListener {

  private static Logger LOGGER = LoggerFactory.getLogger(ArticleDatesListener.class);

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new ArticleCreatingEvent(), new ArticleUpdatingEvent());
  }

  @Override
  public String getName() {
    return "celements.blog.articleDatesListener";
  }

  private void checkDatesNotNull(XWikiDocument articleDoc) {
    try {
      BaseObject articleObj = articleDoc.getXObject(getBlogClasses().getArticleClassRef(
          webUtilsService.getWikiRef(articleDoc).getName()));
      if (articleObj != null) {
        checkDateNotNull(articleObj, BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE,  
            new Date());
        checkDateNotNull(articleObj, BlogClasses.PROPERTY_ARTICLE_ARCHIVE_DATE, 
            ILuceneSearchService.SDF.parse(ILuceneSearchService.DATE_HIGH));
      } else {
        LOGGER.error("no article class object found on doc '" + articleDoc + "'");
      }
    } catch (ParseException exc) {
      LOGGER.error("Error parsing date string: " + ILuceneSearchService.DATE_HIGH, exc);
    }
  }

  private void checkDateNotNull(BaseObject articleObj, String field, Date setTo) {
    Date date = articleObj.getDateValue(field);
    if (date == null) {
      articleObj.setDateValue(field, setTo);
      LOGGER.info("set empty date field '" + field + "' to '" + setTo + "'");
    }
  }

  BlogClasses getBlogClasses() {
    return (BlogClasses) blogClasses;
  }

  @Override
  protected void onLocalEvent(Event event, Object source, Object data) {
    XWikiDocument doc = (XWikiDocument) source;
    if (doc != null) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + doc.getDocumentReference() + "].");
      checkDatesNotNull(doc);
    } else {
      LOGGER.trace("onEvent: got local event for [" + event.getClass() + "] on source [" 
          + source + "] and data [" + data + "] -> skip.");
    }
  }

  @Override
  protected void onRemoteEvent(Event event, Object source, Object data) {
    LOGGER.trace("onEvent: got remote event for [" + event.getClass() + "] on source [" 
        + source + "] and data [" + data + "] -> skip.");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
