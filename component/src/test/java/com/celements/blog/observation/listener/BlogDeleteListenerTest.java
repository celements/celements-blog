package com.celements.blog.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.observation.event.BlogDeletingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class BlogDeleteListenerTest extends AbstractBridgedComponentTestCase {

  private BlogDeleteListener listener;

  @Before
  public void setUp_BlogDeleteListenerTest() throws Exception {
    listener = (BlogDeleteListener) Utils.getComponent(EventListener.class, 
        BlogDeleteListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("BlogDeleteListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    String wikiName = "myWiki";
    DocumentReference classRef = new DocumentReference(wikiName, 
        BlogClasses.BLOG_CONFIG_CLASS_SPACE, BlogClasses.BLOG_CONFIG_CLASS_DOC);
    assertEquals(classRef, listener.getRequiredObjClassRef(new WikiReference(wikiName)));
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

}
