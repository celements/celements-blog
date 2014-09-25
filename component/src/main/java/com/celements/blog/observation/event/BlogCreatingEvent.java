package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class BlogCreatingEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public BlogCreatingEvent() {
    super();
  }

  public BlogCreatingEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public BlogCreatingEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
