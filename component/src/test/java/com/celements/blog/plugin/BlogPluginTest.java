/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.blog.plugin;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.web.XWikiMessageTool;

public class BlogPluginTest extends AbstractComponentTest {

  BlogPlugin plugin;
  private IBlogServiceRole blogServiceMock;

  @Before
  public void setUp_BlogPluginTest() throws Exception {
    plugin = new BlogPlugin("", "", getContext());
    blogServiceMock = createDefaultMock(IBlogServiceRole.class);
    plugin.injected_BlogService = blogServiceMock;
  }

  @Test
  public void testBatchImportReceivers_Exception() throws Exception {
    Map<String, String> result = new TreeMap<>();
    result.put("myname@email.com", "invalid");
    String importData = "\r\n,My Name <myName@email.com>,,,,";
    String nl = "My.Newsletter";
    XWikiMessageTool messageTool = createDefaultMock(XWikiMessageTool.class);
    expect(messageTool.get(eq("cel_newsletter_subscriber_invalid"))).andReturn("invalid").once();
    XWikiContext context = mockContext(messageTool, null);
    expect(getWikiMock().search((String) anyObject(), same(context))).andReturn(null).anyTimes();
    expect(getWikiMock().generateRandomString(eq(16))).andReturn("abc");
    expect(getWikiMock().exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
    expect(getWikiMock().getDocument(eq("NewsletterReceivers.abc"), same(context))).andThrow(
        new XWikiException());
    mockDictionaryLoad(context);
    mockHibernate(Collections.emptyList(), null, context);
    replayDefault();
    assertEquals(result, plugin.batchImportReceivers(false, importData, nl, context));
    verifyDefault();
  }

  @Test
  public void testBatchImportReceivers_activeAdd() throws Exception {
    Map<String, String> result = new TreeMap<>();
    result.put("myname@email.com", "active");
    String importData = "\r\n,My Name <myName@email.com>,,,,";
    String nl = "My.Newsletter";
    DocumentReference nlDocRef = new DocumentReference(getContext().getDatabase(), "My",
        "Newsletter");
    XWikiDocument doc1 = createDefaultMock(XWikiDocument.class);
    XWikiMessageTool messageTool = createDefaultMock(XWikiMessageTool.class);
    expect(messageTool.get(eq("cel_newsletter_subscriber_active"))).andReturn("active").once();
    XWikiContext context = mockContext(messageTool, doc1);
    expect(getWikiMock().search((String) anyObject(), same(context))).andReturn(null).anyTimes();
    expect(getWikiMock().generateRandomString(eq(16))).andReturn("abc");
    expect(getWikiMock().exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
    expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
    expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
    expect(getWikiMock().getDocument(eq("NewsletterReceivers.abc"), same(context))).andReturn(doc1)
        .times(2);
    XWikiDocument nlDoc = new XWikiDocument(nlDocRef);
    expect(getWikiMock().exists(eq(nl), same(context))).andReturn(true);
    expect(getWikiMock().getDocument(eq(nl), same(context))).andReturn(nlDoc);
    expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), eq(nl), eq(
        false))).andReturn(null);
    BaseObject obj = new BaseObject();
    expect(doc1.newObject(eq("Celements.NewsletterReceiverClass"), same(context))).andReturn(obj);
    expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), eq(nl), eq(
        false))).andReturn(obj);
    getWikiMock().saveDocument(same(doc1), same(context));
    expectLastCall();
    mockDictionaryLoad(context);
    mockHibernate(Collections.emptyList(), nlDoc, context);
    replayDefault();
    assertEquals(result, plugin.batchImportReceivers(false, importData, nl, context));
    verifyDefault();
  }

  @Test
  public void testBatchImportReceivers_activeAddInactive() throws Exception {
    Map<String, String> result = new TreeMap<>();
    result.put("myname@email.com", "inactive");
    String importData = "\r\n,My Name <myName@email.com>,,,,";
    String nl = "My.Newsletter";
    DocumentReference nlDocRef = new DocumentReference(getContext().getDatabase(), "My",
        "Newsletter");
    XWikiDocument doc1 = createDefaultMock(XWikiDocument.class);
    XWikiMessageTool messageTool = createDefaultMock(XWikiMessageTool.class);
    expect(messageTool.get(eq("cel_newsletter_subscriber_inactive"))).andReturn("inactive").once();
    XWikiContext context = mockContext(messageTool, doc1);
    expect(getWikiMock().exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(
        false).times(2);
    expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
    expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
    expect(getWikiMock().getDocument(eq("NewsletterReceivers.abc"), same(context)))
        .andReturn(doc1).times(2);
    XWikiDocument nlDoc = new XWikiDocument(nlDocRef);
    expect(getWikiMock().exists(eq(nl), same(context))).andReturn(true);
    expect(getWikiMock().getDocument(eq(nl), same(context))).andReturn(nlDoc);
    List<Object> list = new ArrayList<>();
    list.add("NewsletterReceivers.abc");
    BaseObject obj = new BaseObject();
    obj.setIntValue("isactive", 1);
    expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), eq(nl), eq(
        false))).andReturn(obj).times(2);
    mockDictionaryLoad(context);
    mockHibernate(list, nlDoc, context);
    getWikiMock().saveDocument(same(doc1), same(context));
    expectLastCall();
    replayDefault();
    assertEquals(result, plugin.batchImportReceivers(true, importData, nl, context));
    verifyDefault();
  }

  @Test
  public void testBatchImportReceivers_inactiveAddActive() throws Exception {
    Map<String, String> result = new TreeMap<>();
    result.put("myname@email.com", "inactive");
    String importData = "\r\n,My Name <myName@email.com>,,,,";
    String nl = "My.Newsletter";
    DocumentReference nlDocRef = new DocumentReference(getContext().getDatabase(), "My",
        "Newsletter");
    XWikiDocument doc1 = createDefaultMock(XWikiDocument.class);
    XWikiMessageTool messageTool = createDefaultMock(XWikiMessageTool.class);
    expect(messageTool.get(eq("cel_newsletter_subscriber_inactive"))).andReturn("inactive").once();
    XWikiContext context = mockContext(messageTool, doc1);
    expect(getWikiMock().exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false)
        .atLeastOnce();
    expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
    expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
    expect(getWikiMock().getDocument(eq("NewsletterReceivers.abc"), same(context))).andReturn(doc1)
        .times(2);
    XWikiDocument nlDoc = new XWikiDocument(nlDocRef);
    expect(getWikiMock().exists(eq(nl), same(context))).andReturn(true);
    expect(getWikiMock().getDocument(eq(nl), same(context))).andReturn(nlDoc);
    List<Object> list = new ArrayList<>();
    list.add("NewsletterReceivers.abc");
    BaseObject obj = new BaseObject();
    obj.setIntValue("isactive", 0);
    expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), eq(nl), eq(
        false))).andReturn(obj).times(2);
    mockDictionaryLoad(context);
    mockHibernate(list, nlDoc, context);
    replayDefault();
    assertEquals(result, plugin.batchImportReceivers(false, importData, nl, context));
    assertEquals(0, obj.getIntValue("isactive"));
    verifyDefault();
  }

  @Test
  public void testBatchImportReceivers_inactiveAdd() throws Exception {
    Map<String, String> result = new TreeMap<>();
    result.put("myname@email.com", "inactive");
    result.put("myName2@email", "invalid");
    result.put("myName @email.com", "invalid");
    String importData = "\r\n,myName @email.com,,,myName2@email\nmyName@email.com";
    String nl = "My.Newsletter";
    DocumentReference nlDocRef = new DocumentReference(getContext().getDatabase(), "My",
        "Newsletter");
    XWikiDocument doc1 = createDefaultMock(XWikiDocument.class);
    XWikiMessageTool messageTool = createDefaultMock(XWikiMessageTool.class);
    expect(messageTool.get(eq("cel_newsletter_subscriber_invalid"))).andReturn("invalid").times(2);
    expect(messageTool.get(eq("cel_newsletter_subscriber_inactive"))).andReturn("inactive").once();
    XWikiContext context = mockContext(messageTool, doc1);
    expect(getWikiMock().search((String) anyObject(), same(context))).andReturn(null).anyTimes();
    expect(getWikiMock().generateRandomString(eq(16))).andReturn("abc");
    expect(getWikiMock().exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
    expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
    expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
    expect(getWikiMock().getDocument(eq("NewsletterReceivers.abc"), same(context))).andReturn(doc1)
        .times(2);
    XWikiDocument nlDoc = new XWikiDocument(nlDocRef);
    expect(getWikiMock().exists(eq(nl), same(context))).andReturn(true);
    expect(getWikiMock().getDocument(eq(nl), same(context))).andReturn(nlDoc);
    expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), eq(nl), eq(
        false))).andReturn(null);
    BaseObject obj = new BaseObject();
    expect(doc1.newObject(eq("Celements.NewsletterReceiverClass"), same(context))).andReturn(obj);
    expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), eq(nl), eq(
        false))).andReturn(obj);
    getWikiMock().saveDocument(same(doc1), same(context));
    expectLastCall();
    mockDictionaryLoad(context);
    mockHibernate(Collections.emptyList(), nlDoc, context);
    replayDefault();
    assertEquals(result, plugin.batchImportReceivers(true, importData, nl, context));
    verifyDefault();
  }

  @Test
  public void testExtractEmailFromString() {
    assertEquals("bla@da.ch", plugin.extractEmailFromString("gla@bla@da.ch"));
    assertEquals("xpo+bedo@credo.ch", plugin.extractEmailFromString("xpo+bedo@credo.ch"));
    assertEquals("aco@bla.tra", plugin.extractEmailFromString("H. Meier <aco@bla.tra>"));
    assertEquals("aco@bla.tra", plugin.extractEmailFromString("wucht aco@bla.tra trala"));
    assertEquals("myName.Meier-Mueller@mail.com", plugin.extractEmailFromString(
        "myName.Meier-Mueller@mail.com"));
    assertEquals("Meier_Mueller@mail.com", plugin.extractEmailFromString("Meier_Mueller@mail.com"));
    assertEquals("Meier.Mueller@mail.com", plugin.extractEmailFromString("Meier.Mueller@mail.com"));
  }

  @Test
  public void testContainsEmailAddress_true() {
    assertTrue(plugin.containsEmailAddress("gla@bla@da.ch"));
    assertTrue(plugin.containsEmailAddress("a@b.c"));
    assertTrue(plugin.containsEmailAddress("xpo+bedo@credo.ch"));
    assertTrue(plugin.containsEmailAddress("hans_wurscht@plumpl.com"));
    assertTrue(plugin.containsEmailAddress("friz.meier@geier.xy"));
    assertTrue(plugin.containsEmailAddress("plimm@blubb.co.uk"));
    assertTrue(plugin.containsEmailAddress("baem@da-ga_ba.na_za.bla-ta.ch"));
    assertTrue(plugin.containsEmailAddress("Heiri Meier <aco@bla.tra>"));
    assertTrue(plugin.containsEmailAddress("wurscht aco@bla.tra tralla"));
    assertTrue(plugin.containsEmailAddress("trullalla@is.icc.u-tokai.ac.jp"));
  }

  @Test
  public void testContainsEmailAddress_false() {
    assertFalse(plugin.containsEmailAddress(""));
    assertFalse(plugin.containsEmailAddress("quatsch"));
    assertFalse(plugin.containsEmailAddress("agar@agar"));
    assertFalse(plugin.containsEmailAddress("@blab.com"));
  }

  @Test
  public void testGetContainsEmailRegex() {
    assertTrue("myName@mail.com".matches(plugin.getContainsEmailRegex()));
    assertTrue("myName.Meier@mail.com".matches(plugin.getContainsEmailRegex()));
    assertTrue("myName.Meier-Mueller@mail.com".matches(plugin.getContainsEmailRegex()));
    assertTrue("myName+test@mail.com".matches(plugin.getContainsEmailRegex()));
    assertFalse("Nice meaningless text.".matches(plugin.getContainsEmailRegex()));
  }

  @Test
  public void testSplitImportDataToEmailCandidates_one() {
    String importData = "\r\n,My Name <myName@email.com>,,,,";
    String[] result = plugin.splitImportDataToEmailCandidates(importData);
    List<String> resultList = new ArrayList();
    for (String string : result) {
      resultList.add(string);
    }
    assertTrue(resultList.contains("My Name <myName@email.com>"));
  }

  @Test
  public void testSplitImportDataToEmailCandidates_several() {
    String importData = "\r\n,myName @email.com,,,myName2@email\nmyName@email.com";
    String[] result = plugin.splitImportDataToEmailCandidates(importData);
    List<String> resultList = new ArrayList();
    for (String string : result) {
      resultList.add(string);
    }
    assertTrue(resultList.contains("myName @email.com"));
    assertTrue(resultList.contains("myName2@email"));
    assertTrue(resultList.contains("myName@email.com"));
  }

  // *************************************************************************************
  // HELPER
  // *************************************************************************************

  private DocumentReference getDocRef(String spaceName, String pageName) {
    return new DocumentReference(getContext().getDatabase(), spaceName, pageName);
  }

  private Vector<BaseObject> getArticleObjVect(int i) {
    Vector<BaseObject> objVec = new Vector<>();
    BaseObject obj = new BaseObject();
    obj.setName("Space.Article" + i);
    obj.setStringValue("lang", "de");
    obj.setDateValue("publishdate", new Date(i, 11, 31));
    obj.setDateValue("archivedate", new Date(9999, 0, 1));
    obj.setStringValue("title", "the title");
    obj.setStringValue("content", "the content");
    objVec.add(obj);
    return objVec;
  }

  private XWikiContext mockContext(XWikiMessageTool messageTool, XWikiDocument doc) {
    XWikiContext context = createDefaultMock(XWikiContext.class);
    expect(context.getWiki()).andReturn(getWikiMock()).anyTimes();
    expect(context.getUser()).andReturn("XWiki.Admin").anyTimes();
    expect(context.getDoc()).andReturn(doc).anyTimes();
    if (messageTool != null) {
      expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
    }
    return context;
  }

  private void mockDictionaryLoad(XWikiContext context) throws Exception {
    String nlrClassName = "Celements.NewsletterReceiverClass";
    expect(getWikiMock().hasCustomMappings()).andReturn(true).atLeastOnce();
    expect(getWikiMock().getClass(eq(nlrClassName), (XWikiContext) anyObject())).andReturn(
        new BaseClass()).atLeastOnce();
    expect(getWikiMock().getPropertyClassFromName(eq(nlrClassName + "_email"),
        (XWikiContext) anyObject())).andReturn(new PropertyClass()).atLeastOnce();
  }

  private void mockHibernate(List<Object> list, XWikiDocument nlDoc, XWikiContext context)
      throws Exception {
    XWikiHibernateStore hibStore = createDefaultMock(XWikiHibernateStore.class);
    expect(getWikiMock().getHibernateStore()).andReturn(hibStore).atLeastOnce();
    expect(getWikiMock().getStore()).andReturn(hibStore).anyTimes();
    expect(hibStore.loadXWikiDoc((XWikiDocument) anyObject(), same(context))).andReturn(nlDoc)
        .anyTimes();
    expect(hibStore.executeRead((XWikiContext) anyObject(), eq(true),
        (HibernateCallback) anyObject())).andReturn(list).atLeastOnce();
  }

}
