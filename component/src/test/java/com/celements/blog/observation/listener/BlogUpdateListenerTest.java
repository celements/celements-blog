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
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class BlogUpdateListenerTest extends AbstractBridgedComponentTestCase {

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
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getCreatingEvent(docRef);
    assertNotNull(event);
    assertSame(BlogCreatingEvent.class, event.getClass());
    assertTrue(event.matches(new BlogCreatingEvent(docRef)));
    assertFalse(event.matches(new BlogCreatingEvent()));
    assertNotSame(listener.getCreatingEvent(docRef), event);
  }

  @Test
  public void testCreatedEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getCreatedEvent(docRef);
    assertNotNull(event);
    assertSame(BlogCreatedEvent.class, event.getClass());
    assertTrue(event.matches(new BlogCreatedEvent(docRef)));
    assertFalse(event.matches(new BlogCreatedEvent()));
    assertNotSame(listener.getCreatedEvent(docRef), event);
  }

  @Test
  public void testUpdatingEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getUpdatingEvent(docRef);
    assertNotNull(event);
    assertSame(BlogUpdatingEvent.class, event.getClass());
    assertTrue(event.matches(new BlogUpdatingEvent(docRef)));
    assertFalse(event.matches(new BlogUpdatingEvent()));
    assertNotSame(listener.getUpdatingEvent(docRef), event);
  }

  @Test
  public void testUpdatedEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getUpdatedEvent(docRef);
    assertNotNull(event);
    assertSame(BlogUpdatedEvent.class, event.getClass());
    assertTrue(event.matches(new BlogUpdatedEvent(docRef)));
    assertFalse(event.matches(new BlogUpdatedEvent()));
    assertNotSame(listener.getUpdatedEvent(docRef), event);
  }

  @Test
  public void testDeletingEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getDeletingEvent(docRef);
    assertNotNull(event);
    assertSame(BlogDeletingEvent.class, event.getClass());
    assertTrue(event.matches(new BlogDeletingEvent(docRef)));
    assertFalse(event.matches(new BlogDeletingEvent()));
    assertNotSame(listener.getDeletingEvent(docRef), event);
  }

  @Test
  public void testDeletedEvent() {
    DocumentReference docRef = new DocumentReference("myWiki", "mySpace", "myDoc");
    Event event = listener.getDeletedEvent(docRef);
    assertNotNull(event);
    assertSame(BlogDeletedEvent.class, event.getClass());
    assertTrue(event.matches(new BlogDeletedEvent(docRef)));
    assertFalse(event.matches(new BlogDeletedEvent()));
    assertNotSame(listener.getDeletedEvent(docRef), event);
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
