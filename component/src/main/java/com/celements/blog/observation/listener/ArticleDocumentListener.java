package com.celements.blog.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.ArticleCreatedEvent;
import com.celements.blog.observation.event.ArticleCreatingEvent;
import com.celements.blog.observation.event.ArticleDeletedEvent;
import com.celements.blog.observation.event.ArticleDeletingEvent;
import com.celements.blog.observation.event.ArticleUpdatedEvent;
import com.celements.blog.observation.event.ArticleUpdatingEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.blog.articleDocListener")
public class ArticleDocumentListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      ArticleDocumentListener.class);

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentCreatingEvent(), 
        new DocumentUpdatingEvent(), new DocumentDeletingEvent(), 
        new DocumentCreatedEvent(), new DocumentUpdatedEvent(), 
        new DocumentDeletedEvent());
  }

  @Override
  public String getName() {
    return "celements.blog.articleDocListener";
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = getDocument(source, event);
    if ((doc != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + doc.getDocumentReference() + "].");
      notifyIfArticle(doc, event, source, data);
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }
  
  private void notifyIfArticle(XWikiDocument doc, Event event, Object source, 
      Object data) {
    DocumentReference classRef = getBlogClasses().getArticleClassRef(
        doc.getDocumentReference().getWikiReference().getName());
    Event notifyEvent = getArticleEvent(event);
    if ((doc.getXObject(classRef) != null) && (notifyEvent != null)) {
      getObservationManager().notify(notifyEvent, source, data);
    } else {
      LOGGER.trace("no article object found on doc '" + doc + "', not notifiying event '" 
          + notifyEvent);
    }
  }

  private Event getArticleEvent(Event event) {
    if (event != null) {
      if (event instanceof DocumentCreatingEvent) {
        return new ArticleCreatingEvent();
      } else if (event instanceof DocumentUpdatingEvent) {
        return new ArticleUpdatingEvent();
      } else if (event instanceof DocumentDeletingEvent) {
        return new ArticleDeletingEvent();
      } else if (event instanceof DocumentCreatedEvent) {
        return new ArticleCreatedEvent();
      } else if (event instanceof DocumentUpdatedEvent) {
        return new ArticleUpdatedEvent();
      } else if (event instanceof DocumentDeletedEvent) {
        return new ArticleDeletedEvent();
      }
    }
    return null;
  }

}
