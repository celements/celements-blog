package com.celements.blog.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.ArticleCreatedEvent;
import com.celements.blog.observation.event.ArticleCreatingEvent;
import com.celements.blog.observation.event.ArticleDeletedEvent;
import com.celements.blog.observation.event.ArticleDeletingEvent;
import com.celements.blog.observation.event.ArticleUpdatedEvent;
import com.celements.blog.observation.event.ArticleUpdatingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.observation.listener.AbstractDocumentUpdateListener;

@Component(ArticleUpdateListener.NAME)
public class ArticleUpdateListener extends AbstractDocumentUpdateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleUpdateListener.class);

  public static final String NAME = "ArticleUpdateListener";

  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
    return ((BlogClasses) blogClasses).getArticleClassRef(wikiRef.getName());
  }

  @Override
  protected Event getCreatingEvent(DocumentReference docRef) {
    return new ArticleCreatingEvent(docRef);
  }

  @Override
  protected Event getCreatedEvent(DocumentReference docRef) {
    return new ArticleCreatedEvent(docRef);
  }

  @Override
  protected Event getUpdatingEvent(DocumentReference docRef) {
    return new ArticleUpdatingEvent(docRef);
  }

  @Override
  protected Event getUpdatedEvent(DocumentReference docRef) {
    return new ArticleUpdatedEvent(docRef);
  }

  @Override
  protected Event getDeletingEvent(DocumentReference docRef) {
    return new ArticleDeletingEvent(docRef);
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return new ArticleDeletedEvent(docRef);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
