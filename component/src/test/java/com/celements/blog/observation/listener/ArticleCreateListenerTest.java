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
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class ArticleCreateListenerTest extends AbstractComponentTest {

  private ArticleCreateListener listener;

  @Before
  public void setUp_ArticleCreateListenerTest() throws Exception {
    listener = (ArticleCreateListener) Utils.getComponent(EventListener.class,
        ArticleCreateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("ArticleCreateListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    String wikiName = "myWiki";
    DocumentReference classRef = new DocumentReference(wikiName, BlogClasses.ARTICLE_CLASS_SPACE,
        BlogClasses.ARTICLE_CLASS_DOC);
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
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
