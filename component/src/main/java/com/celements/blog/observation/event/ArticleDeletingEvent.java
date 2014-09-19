package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class ArticleDeletingEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public ArticleDeletingEvent() {
    super();
  }

  public ArticleDeletingEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public ArticleDeletingEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
