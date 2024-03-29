package com.celements.blog.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.ArticleDeletedEvent;
import com.celements.blog.observation.event.ArticleDeletingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class ArticleDeleteListenerTest extends AbstractComponentTest {

  private ArticleDeleteListener listener;

  @Before
  public void setUp_ArticleDeleteListenerTest() throws Exception {
    listener = (ArticleDeleteListener) Utils.getComponent(EventListener.class,
        ArticleDeleteListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("ArticleDeleteListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    String wikiName = "myWiki";
    DocumentReference classRef = new DocumentReference(wikiName, BlogClasses.ARTICLE_CLASS_SPACE,
        BlogClasses.ARTICLE_CLASS_DOC);
    assertEquals(classRef, listener.getRequiredObjClassRef(new WikiReference(wikiName)));
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
