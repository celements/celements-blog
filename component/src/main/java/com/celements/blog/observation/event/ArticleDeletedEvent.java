package com.celements.blog.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

import com.celements.common.observation.converter.Remote;

@Remote
public class ArticleDeletedEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  public ArticleDeletedEvent() {
    super();
  }

  public ArticleDeletedEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  public ArticleDeletedEvent(EventFilter eventFilter) {
    super(eventFilter);
  }

}
