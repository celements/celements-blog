package com.celements.blog.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.ArticleCreatedEvent;
import com.celements.blog.observation.event.ArticleCreatingEvent;
import com.celements.blog.observation.event.ArticleDeletedEvent;
import com.celements.blog.observation.event.ArticleDeletingEvent;
import com.celements.blog.observation.event.ArticleUpdatedEvent;
import com.celements.blog.observation.event.ArticleUpdatingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class ArticleUpdateListenerTest extends AbstractBridgedComponentTestCase {

  private ArticleUpdateListener listener;

  @Before
  public void setUp_ArticleUpdateListenerTest() throws Exception {
    listener = (ArticleUpdateListener) Utils.getComponent(EventListener.class, 
        ArticleUpdateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("ArticleUpdateListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    String wikiName = "myWiki";
    DocumentReference classRef = new DocumentReference(wikiName, 
        BlogClasses.ARTICLE_CLASS_SPACE, BlogClasses.ARTICLE_CLASS_DOC);
    assertEquals(classRef, listener.getRequiredObjClassRef(new WikiReference(wikiName)));
  }

  @Test
  public void testCreatingEvent() {
    Event event = listener.getCreatingEvent(null);
    assertNotNull(event);
    assertSame(ArticleCreatingEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleCreatingEvent()));
    assertNotSame(listener.getCreatingEvent(null), event);
  }

  @Test
  public void testCreatedEvent() {
    Event event = listener.getCreatedEvent(null);
    assertNotNull(event);
    assertSame(ArticleCreatedEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleCreatedEvent()));
    assertNotSame(listener.getCreatedEvent(null), event);
  }

  @Test
  public void testUpdatingEvent() {
    Event event = listener.getUpdatingEvent(null);
    assertNotNull(event);
    assertSame(ArticleUpdatingEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleUpdatingEvent()));
    assertNotSame(listener.getUpdatingEvent(null), event);
  }

  @Test
  public void testUpdatedEvent() {
    Event event = listener.getUpdatedEvent(null);
    assertNotNull(event);
    assertSame(ArticleUpdatedEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleUpdatedEvent()));
    assertNotSame(listener.getUpdatedEvent(null), event);
  }

  @Test
  public void testDeletingEvent() {
    Event event = listener.getDeletingEvent(null);
    assertNotNull(event);
    assertSame(ArticleDeletingEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleDeletingEvent()));
    assertNotSame(listener.getDeletingEvent(null), event);
  }

  @Test
  public void testDeletedEvent() {
    Event event = listener.getDeletedEvent(null);
    assertNotNull(event);
    assertSame(ArticleDeletedEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleDeletedEvent()));
    assertNotSame(listener.getDeletedEvent(null), event);
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
