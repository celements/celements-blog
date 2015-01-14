package com.celements.blog.observation.listener;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.ArticleCreatingEvent;
import com.celements.blog.observation.event.ArticleUpdatingEvent;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.ILuceneSearchService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class ArticleDatesListenerTest extends AbstractBridgedComponentTestCase {
  
  private ArticleDatesListener listener;

  @Before
  public void setUp_EventChangedListenerTest() throws Exception {
    listener = (ArticleDatesListener) Utils.getComponent(EventListener.class, 
        "celements.blog.articleDatesListener");
  }
  
  @Test
  public void testGetEvents() {
    replayDefault();
    HashSet<Class<? extends Event>> eventClasses = new HashSet<Class<? extends Event>>();
    for (Event theEvent : listener.getEvents()) {
      eventClasses.add(theEvent.getClass());
    }
    assertEquals(2, eventClasses.size());
    assertTrue("Expecting registration for SubscribedEventCreated events",
        eventClasses.contains(ArticleCreatingEvent.class));
    assertTrue("Expecting registration for SubscribedEventUpdated events",
        eventClasses.contains(ArticleUpdatingEvent.class));
    verifyDefault();
  }

  @Test
  public void testGetName() {
    replayDefault();
    assertEquals("celements.blog.articleDatesListener", listener.getName());
    verifyDefault();
  }

  @Test
  public void testOnEvent() throws Exception {
    DocumentReference articleDocRef = new DocumentReference("wiki", "space", "articleDoc");
    XWikiDocument articleDoc = new XWikiDocument(articleDocRef);
    BaseObject articleObj = new BaseObject();
    articleObj.setXClassReference(listener.getBlogClasses().getArticleClassRef("wiki"));
    articleDoc.addXObject(articleObj);

    Date beforeDate = new Date();
    replayDefault();
    listener.onEvent(new ArticleCreatingEvent(), articleDoc, null);
    verifyDefault();
    Date afterDate = new Date();
    
    Date publishDate = articleObj.getDateValue(BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE);
    assertNotNull(publishDate);
    assertTrue(beforeDate.equals(publishDate) || beforeDate.before(publishDate));
    assertTrue(afterDate.equals(publishDate) || afterDate.after(publishDate));
    Date archiveDate = articleObj.getDateValue(BlogClasses.PROPERTY_ARTICLE_ARCHIVE_DATE);
    assertNotNull(archiveDate);
    assertEquals(ILuceneSearchService.SDF.parse(ILuceneSearchService.DATE_HIGH), 
        archiveDate);
  }

}
