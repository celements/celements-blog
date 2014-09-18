package com.celements.blog.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogDeletedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.blog.deleted")
public class DocumentDeletedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentDeletedListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentDeletedEvent());
  }

  public String getName() {
    return "celements.blog.deleted";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = getOrginialDocument(source);
    if ((doc != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + doc.getDocumentReference() + "].");
      notifyIfBlog(doc, BlogDeletedEvent.class);
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }
  
  private XWikiDocument getOrginialDocument(Object source) {
    if (source != null) {
      return ((XWikiDocument) source).getOriginalDocument();
    }
    return null;
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

}
