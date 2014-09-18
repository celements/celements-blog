package com.celements.blog.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogUpdatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.blog.updated")
public class DocumentUpdatedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentUpdatedListener.class);

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentUpdatedEvent());
  }

  @Override
  public String getName() {
    return "celements.blog.updated";
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = (XWikiDocument) source;
    if ((doc != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + doc.getDocumentReference() + "].");
      notifyIfBlog(doc, BlogUpdatedEvent.class);
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
