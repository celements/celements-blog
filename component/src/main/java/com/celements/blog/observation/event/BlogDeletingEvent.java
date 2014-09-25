package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class BlogDeletingEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public BlogDeletingEvent() {
    super();
  }

  public BlogDeletingEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public BlogDeletingEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
