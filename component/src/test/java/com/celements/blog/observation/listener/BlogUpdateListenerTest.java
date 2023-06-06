package com.celements.blog.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogCreatedEvent;
import com.celements.blog.observation.event.BlogCreatingEvent;
import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.observation.event.BlogDeletingEvent;
import com.celements.blog.observation.event.BlogUpdatedEvent;
import com.celements.blog.observation.event.BlogUpdatingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class BlogUpdateListenerTest extends AbstractComponentTest {

  private BlogUpdateListener listener;

  @Before
  public void setUp_BlogUpdateListenerTest() throws Exception {
    listener = (BlogUpdateListener) Utils.getComponent(EventListener.class,
        BlogUpdateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("BlogUpdateListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    String wikiName = "myWiki";
    DocumentReference classRef = new DocumentReference(wikiName,
        BlogClasses.BLOG_CONFIG_CLASS_SPACE, BlogClasses.BLOG_CONFIG_CLASS_DOC);
    assertEquals(classRef, listener.getRequiredObjClassRef(new WikiReference(wikiName)));
  }

  @Test
  public void testCreatingEvent() {
    Event event = listener.getCreatingEvent(null);
    assertNotNull(event);
    assertSame(BlogCreatingEvent.class, event.getClass());
    assertTrue(event.matches(new BlogCreatingEvent()));
    assertNotSame(listener.getCreatingEvent(null), event);
  }

  @Test
  public void testCreatedEvent() {
    Event event = listener.getCreatedEvent(null);
    assertNotNull(event);
    assertSame(BlogCreatedEvent.class, event.getClass());
    assertTrue(event.matches(new BlogCreatedEvent()));
    assertNotSame(listener.getCreatedEvent(null), event);
  }

  @Test
  public void testUpdatingEvent() {
    Event event = listener.getUpdatingEvent(null);
    assertNotNull(event);
    assertSame(BlogUpdatingEvent.class, event.getClass());
    assertTrue(event.matches(new BlogUpdatingEvent()));
    assertNotSame(listener.getUpdatingEvent(null), event);
  }

  @Test
  public void testUpdatedEvent() {
    Event event = listener.getUpdatedEvent(null);
    assertNotNull(event);
    assertSame(BlogUpdatedEvent.class, event.getClass());
    assertTrue(event.matches(new BlogUpdatedEvent()));
    assertNotSame(listener.getUpdatedEvent(null), event);
  }

  @Test
  public void testDeletingEvent() {
    Event event = listener.getDeletingEvent(null);
    assertNotNull(event);
    assertSame(BlogDeletingEvent.class, event.getClass());
    assertTrue(event.matches(new BlogDeletingEvent()));
    assertNotSame(listener.getDeletingEvent(null), event);
  }

  @Test
  public void testDeletedEvent() {
    Event event = listener.getDeletedEvent(null);
    assertNotNull(event);
    assertSame(BlogDeletedEvent.class, event.getClass());
    assertTrue(event.matches(new BlogDeletedEvent()));
    assertNotSame(listener.getDeletedEvent(null), event);
  }

  @Test
  public void testIncludeDocFields() {
    assertFalse(listener.includeDocFields());
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
