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
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class BlogCreateListenerTest extends AbstractComponentTest {

  private BlogCreateListener listener;

  @Before
  public void setUp_BlogCreateListenerTest() throws Exception {
    listener = (BlogCreateListener) Utils.getComponent(EventListener.class,
        BlogCreateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("BlogCreateListener", listener.getName());
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
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
