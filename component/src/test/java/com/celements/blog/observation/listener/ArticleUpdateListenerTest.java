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
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getCreatingEvent(docRef);
    assertNotNull(event);
    assertSame(ArticleCreatingEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleCreatingEvent(docRef)));
    assertFalse(event.matches(new ArticleCreatingEvent()));
    assertNotSame(listener.getCreatingEvent(docRef), event);
  }

  @Test
  public void testCreatedEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getCreatedEvent(docRef);
    assertNotNull(event);
    assertSame(ArticleCreatedEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleCreatedEvent(docRef)));
    assertFalse(event.matches(new ArticleCreatedEvent()));
    assertNotSame(listener.getCreatedEvent(docRef), event);
  }

  @Test
  public void testUpdatingEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getUpdatingEvent(docRef);
    assertNotNull(event);
    assertSame(ArticleUpdatingEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleUpdatingEvent(docRef)));
    assertFalse(event.matches(new ArticleUpdatingEvent()));
    assertNotSame(listener.getUpdatingEvent(docRef), event);
  }

  @Test
  public void testUpdatedEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getUpdatedEvent(docRef);
    assertNotNull(event);
    assertSame(ArticleUpdatedEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleUpdatedEvent(docRef)));
    assertFalse(event.matches(new ArticleUpdatedEvent()));
    assertNotSame(listener.getUpdatedEvent(docRef), event);
  }

  @Test
  public void testDeletingEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getDeletingEvent(docRef);
    assertNotNull(event);
    assertSame(ArticleDeletingEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleDeletingEvent(docRef)));
    assertFalse(event.matches(new ArticleDeletingEvent()));
    assertNotSame(listener.getDeletingEvent(docRef), event);
  }

  @Test
  public void testDeletedEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getDeletedEvent(docRef);
    assertNotNull(event);
    assertSame(ArticleDeletedEvent.class, event.getClass());
    assertTrue(event.matches(new ArticleDeletedEvent(docRef)));
    assertFalse(event.matches(new ArticleDeletedEvent()));
    assertNotSame(listener.getDeletedEvent(docRef), event);
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
