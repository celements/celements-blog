package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class BlogUpdatedEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public BlogUpdatedEvent() {
    super();
  }

  public BlogUpdatedEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public BlogUpdatedEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
