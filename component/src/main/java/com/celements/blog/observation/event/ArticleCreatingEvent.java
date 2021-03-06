package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class ArticleCreatingEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public ArticleCreatingEvent() {
    super();
  }

  public ArticleCreatingEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public ArticleCreatingEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
