package com.celements.blog.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogCreatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.blog.created")
public class DocumentCreatedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentCreatedListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentCreatedEvent());
  }

  public String getName() {
    return "celements.blog.created";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = (XWikiDocument) source;
    if ((doc != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + doc.getDocumentReference() + "].");
      notifyIfBlog(doc, BlogCreatedEvent.class);
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

}
