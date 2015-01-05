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

import com.celements.blog.observation.event.BlogCreatedEvent;
import com.celements.blog.observation.event.BlogCreatingEvent;
import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.observation.event.BlogDeletingEvent;
import com.celements.blog.observation.event.BlogUpdatedEvent;
import com.celements.blog.observation.event.BlogUpdatingEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.blog.blogDocListener")
public class BlogDocumentListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      BlogDocumentListener.class);

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentCreatingEvent(), 
        new DocumentUpdatingEvent(), new DocumentDeletingEvent(), 
        new DocumentCreatedEvent(), new DocumentUpdatedEvent(), 
        new DocumentDeletedEvent());
  }

  @Override
  public String getName() {
    return "celements.blog.blogDocListener";
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = getDocument(source, event);
    if ((doc != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + doc.getDocumentReference() + "].");
      notifyIfBlog(doc, event, source, data);
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }
  
  private void notifyIfBlog(XWikiDocument doc, Event event, Object source, 
      Object data) {
    DocumentReference classRef = getBlogClasses().getBlogConfigClassRef(
        doc.getDocumentReference().getWikiReference().getName());
    Event notifyEvent = getBlogEvent(event);
    if ((doc.getXObject(classRef) != null) && (notifyEvent != null)) {
      getObservationManager().notify(notifyEvent, source, data);
    } else {
      //FIXME check if originalDocument has an BlogConfig object THUS a BlogDeleted Event
      //FIXME must be fired
      LOGGER.trace("no blog config object found on doc '" + doc + "', not notifiying event '" 
          + notifyEvent);
    }
  }

  private Event getBlogEvent(Event event) {
    if (event != null) {
      if (event instanceof DocumentCreatingEvent) {
        return new BlogCreatingEvent();
      } else if (event instanceof DocumentUpdatingEvent) {
        return new BlogUpdatingEvent();
      } else if (event instanceof DocumentDeletingEvent) {
        return new BlogDeletingEvent();
      } else if (event instanceof DocumentCreatedEvent) {
        return new BlogCreatedEvent();
      } else if (event instanceof DocumentUpdatedEvent) {
        return new BlogUpdatedEvent();
      } else if (event instanceof DocumentDeletedEvent) {
        return new BlogDeletedEvent();
      }
    }
    return null;
  }

}
