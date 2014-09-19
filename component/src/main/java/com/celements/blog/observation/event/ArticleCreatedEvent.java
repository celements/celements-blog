package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class ArticleCreatedEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public ArticleCreatedEvent() {
    super();
  }

  public ArticleCreatedEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public ArticleCreatedEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
