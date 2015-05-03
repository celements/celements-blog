package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

import com.celements.common.observation.converter.Remote;

@Remote
public class ArticleUpdatedEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public ArticleUpdatedEvent() {
    super();
  }

  public ArticleUpdatedEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public ArticleUpdatedEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
