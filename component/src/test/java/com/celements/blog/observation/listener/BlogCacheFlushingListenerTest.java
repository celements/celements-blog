package com.celements.blog.observation.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;

import com.celements.blog.cache.BlogCache;
import com.celements.blog.observation.event.BlogCreatedEvent;
import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.observation.event.BlogUpdatedEvent;
import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class BlogCacheFlushingListenerTest extends AbstractBridgedComponentTestCase {

  private BlogCacheFlushingListener listener;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp_BlogCacheFlushingListenerTest() throws Exception {
    listener = (BlogCacheFlushingListener) Utils.getComponent(EventListener.class, 
        BlogCache.NAME);
    listener.navCache = createMockAndAddToDefault(IDocumentReferenceCache.class);
  }

  @After
  @SuppressWarnings("unchecked")
  public void tearDown_BlogCacheFlushingListenerTest() throws Exception {
    listener.navCache = Utils.getComponent(IDocumentReferenceCache.class, 
        BlogCache.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals(BlogCache.NAME, listener.getName());
  }

  @Test
  public void testGetEvents() {
    assertEquals(3, listener.getEvents().size());
    assertEquals(BlogCreatedEvent.class, listener.getEvents().get(0).getClass());
    assertEquals(BlogUpdatedEvent.class, listener.getEvents().get(1).getClass());
    assertEquals(BlogDeletedEvent.class, listener.getEvents().get(2).getClass());
  }

  @Test
  public void testOnEvent() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "nav");
    listener.navCache.flush(eq(docRef.getWikiReference()));
    expectLastCall().once();
    replayDefault();
    listener.onEvent(null, new XWikiDocument(docRef), null);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noDoc() throws Exception {
    replayDefault();
    listener.onEvent(null, new Object(), null);
    verifyDefault();
  }

  @Test
  public void testOnEvent_null() throws Exception {
    replayDefault();
    listener.onEvent(null, null, null);
    verifyDefault();
  }

}
