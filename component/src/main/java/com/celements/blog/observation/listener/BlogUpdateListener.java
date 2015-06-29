package com.celements.blog.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogCreatedEvent;
import com.celements.blog.observation.event.BlogCreatingEvent;
import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.observation.event.BlogDeletingEvent;
import com.celements.blog.observation.event.BlogUpdatedEvent;
import com.celements.blog.observation.event.BlogUpdatingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.observation.listener.AbstractDocumentUpdateListener;

@Component(BlogUpdateListener.NAME)
public class BlogUpdateListener extends AbstractDocumentUpdateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlogUpdateListener.class);

  public static final String NAME = "BlogUpdateListener";

  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
    return ((BlogClasses) blogClasses).getBlogConfigClassRef(wikiRef.getName());
  }

  @Override
  protected Event getCreatingEvent(DocumentReference docRef) {
    return new BlogCreatingEvent();
  }

  @Override
  protected Event getCreatedEvent(DocumentReference docRef) {
    return new BlogCreatedEvent();
  }

  @Override
  protected Event getUpdatingEvent(DocumentReference docRef) {
    return new BlogUpdatingEvent();
  }

  @Override
  protected Event getUpdatedEvent(DocumentReference docRef) {
    return new BlogUpdatedEvent();
  }

  @Override
  protected Event getDeletingEvent(DocumentReference docRef) {
    return new BlogDeletingEvent();
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return new BlogDeletedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected boolean includeDocFields() {
    return false;
  }

}
