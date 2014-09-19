package com.celements.blog.observation.listener;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.ArticleCreatingEvent;
import com.celements.blog.observation.event.ArticleUpdatingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.search.lucene.ILuceneSearchService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

// TODO tests

@Component("celements.blog.articleDatesListener")
public class ArticleDatesListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      ArticleDatesListener.class);

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new ArticleCreatingEvent(), new ArticleUpdatingEvent());
  }

  @Override
  public String getName() {
    return "celements.blog.articleDatesListener";
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = getDocument(source, event);
    if ((doc != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + doc.getDocumentReference() + "].");
      checkDatesNotNull(doc);
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }
  
  private void checkDatesNotNull(XWikiDocument articleDoc) {
    try {
      BaseObject articleObj = articleDoc.getXObject(getBlogClasses().getArticleClassRef(
          articleDoc.getDocumentReference().getWikiReference().getName()));
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

}
