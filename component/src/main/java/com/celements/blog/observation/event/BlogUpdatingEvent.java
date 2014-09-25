package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class BlogUpdatingEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public BlogUpdatingEvent() {
    super();
  }

  public BlogUpdatingEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public BlogUpdatingEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
