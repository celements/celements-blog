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
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.observation.listener.AbstractDocumentCreateListener;

@Component(BlogCreateListener.NAME)
public class BlogCreateListener extends AbstractDocumentCreateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlogCreateListener.class);

  public static final String NAME = "BlogCreateListener";

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
  protected Logger getLogger() {
    return LOGGER;
  }

}
