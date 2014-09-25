package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class ArticleUpdatingEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public ArticleUpdatingEvent() {
    super();
  }

  public ArticleUpdatingEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public ArticleUpdatingEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
