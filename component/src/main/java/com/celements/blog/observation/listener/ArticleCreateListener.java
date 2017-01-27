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
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.observation.listener.AbstractDocumentCreateListener;

@Component(ArticleCreateListener.NAME)
public class ArticleCreateListener extends AbstractDocumentCreateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleCreateListener.class);

  public static final String NAME = "ArticleCreateListener";

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
    return new ArticleCreatingEvent();
  }

  @Override
  protected Event getCreatedEvent(DocumentReference docRef) {
    return new ArticleCreatedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
