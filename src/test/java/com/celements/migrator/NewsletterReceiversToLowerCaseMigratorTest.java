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
package com.celements.migrator;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiHibernateStore;

public class NewsletterReceiversToLowerCaseMigratorTest 
    extends AbstractBridgedComponentTestCase {
  NewsletterReceiversToLowerCaseMigrator mig;
  XWikiHibernateStore store;
  XWiki wiki;

  @Before
  public void setUp_NewsletterReceiversToLowerCaseMigratorTest() throws Exception {
    mig = new NewsletterReceiversToLowerCaseMigrator();
    wiki = createMock(XWiki.class);
    getContext().setWiki(wiki);
    store = createMock(XWikiHibernateStore.class);
  }

  @Test
  public void testMigrate_oneCorrectResult() throws XWikiException {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 5, "Regspc.Docname"});
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    mig.migrate(null, getContext());
    verify(store, wiki);
  }

  @Test
  public void testMigrate_oneCorrectOneIncorrectResult() throws XWikiException {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 5, "Regspc.Docname"});
    resultList.add(new Object[]{"ABC@cdef.com", "ISubscribed.Thas", 0, "Regspc.Doc2"});
    DocumentReference ref = new DocumentReference(getContext().getDatabase(), "Regspc",
        "Doc2");
    XWikiDocument upperDoc = new XWikiDocument(ref);
    expect(wiki.getDocument(eq(ref), same(getContext()))).andReturn(upperDoc).once();
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    BaseObject obj = new BaseObject();
    obj.setXClassReference(objRef);
    obj.setNumber(0);
    obj.setStringValue("email", "ABC@cdef.com");
    upperDoc.addXObject(obj);
    wiki.saveDocument(same(upperDoc), same(getContext()));
    expectLastCall();
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    mig.migrate(null, getContext());
    verify(store, wiki);
    assertEquals("abc@cdef.com", obj.getStringValue("email"));
  }

  @Test
  public void testMigrate_sameCorrectAndIncorrectResult() throws XWikiException {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 0, "Regspc.Docname"});
    resultList.add(new Object[]{"ABC@def.com", "ISubscribed.This", 0, "Regspc.Doc2"});
    DocumentReference upperRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Doc2");
    XWikiDocument upperDoc = new XWikiDocument(upperRef);
    expect(wiki.getDocument(eq(upperRef), same(getContext()))).andReturn(upperDoc).once();
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    BaseObject upperObj = new BaseObject();
    upperObj.setXClassReference(objRef);
    upperObj.setNumber(0);
    upperObj.setIntValue("isactive", 1);
    upperObj.setStringValue("email", "ABC@cdef.com");
    upperDoc.addXObject(upperObj);
    DocumentReference lowerRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Docname");
    XWikiDocument lowerDoc = new XWikiDocument(lowerRef);
    expect(wiki.getDocument(eq(lowerRef), same(getContext()))).andReturn(lowerDoc).once();
    BaseObject lowerObj = new BaseObject();
    lowerObj.setXClassReference(objRef);
    lowerObj.setNumber(0);
    lowerObj.setIntValue("isactive", 0);
    lowerObj.setStringValue("email", "abc@def.com");
    lowerDoc.addXObject(lowerObj);
    wiki.deleteDocument(same(upperDoc), same(getContext()));
    expectLastCall();
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    mig.migrate(null, getContext());
    verify(store, wiki);
    assertEquals("abc@def.com", lowerObj.getStringValue("email"));
    assertEquals(0, lowerObj.getIntValue("isactive"));
  }

  @Test
  public void testMigrate_sameCorrectAndIncorrectResult_inactiveUpper() 
      throws XWikiException {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 0, "Regspc.Docname"});
    resultList.add(new Object[]{"ABC@def.com", "ISubscribed.This", 0, "Regspc.Doc2"});
    DocumentReference upperRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Doc2");
    XWikiDocument upperDoc = new XWikiDocument(upperRef);
    expect(wiki.getDocument(eq(upperRef), same(getContext()))).andReturn(upperDoc).once();
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    BaseObject upperObj = new BaseObject();
    upperObj.setXClassReference(objRef);
    upperObj.setNumber(0);
    upperObj.setIntValue("isactive", 0);
    upperObj.setStringValue("email", "ABC@cdef.com");
    upperDoc.addXObject(upperObj);
    DocumentReference lowerRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Docname");
    XWikiDocument lowerDoc = new XWikiDocument(lowerRef);
    expect(wiki.getDocument(eq(lowerRef), same(getContext()))).andReturn(lowerDoc).once();
    BaseObject lowerObj = new BaseObject();
    lowerObj.setXClassReference(objRef);
    lowerObj.setNumber(0);
    lowerObj.setIntValue("isactive", 1);
    lowerObj.setStringValue("email", "abc@def.com");
    lowerDoc.addXObject(lowerObj);
    wiki.saveDocument(same(lowerDoc), same(getContext()));
    expectLastCall();
    wiki.deleteDocument(same(upperDoc), same(getContext()));
    expectLastCall();
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    mig.migrate(null, getContext());
    verify(store, wiki);
    assertEquals("abc@def.com", lowerObj.getStringValue("email"));
    assertEquals(0, lowerObj.getIntValue("isactive"));
  }

  @Test
  public void testMigrate_twoIncorrect() throws XWikiException {
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"ABC@DEF.com", "ISubscribed.This", 0, "Regspc.Docname"});
    resultList.add(new Object[]{"ABC@def.com", "ISubscribed.This", 0, "Regspc.Doc2"});
    DocumentReference lowerRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Docname");
    XWikiDocument lowerDoc = new XWikiDocument(lowerRef);
    expect(wiki.getDocument(eq(lowerRef), same(getContext()))).andReturn(lowerDoc
        ).times(2);
    BaseObject lowerObj = new BaseObject();
    lowerObj.setXClassReference(objRef);
    lowerObj.setNumber(0);
    lowerObj.setIntValue("isactive", 1);
    lowerObj.setStringValue("email", "ABC@DEF.com");
    lowerDoc.addXObject(lowerObj);
    wiki.saveDocument(same(lowerDoc), same(getContext()));
    expectLastCall();
    DocumentReference upperRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Doc2");
    XWikiDocument upperDoc = new XWikiDocument(upperRef);
    expect(wiki.getDocument(eq(upperRef), same(getContext()))).andReturn(upperDoc).once();
    BaseObject upperObj = new BaseObject();
    upperObj.setXClassReference(objRef);
    upperObj.setNumber(0);
    upperObj.setIntValue("isactive", 1);
    upperObj.setStringValue("email", "ABC@def.com");
    upperDoc.addXObject(upperObj);
    wiki.deleteDocument(same(upperDoc), same(getContext()));
    expectLastCall();
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    mig.migrate(null, getContext());
    verify(store, wiki);
    assertEquals("abc@def.com", lowerObj.getStringValue("email"));
    assertEquals(1, lowerObj.getIntValue("isactive"));
  }

  @Test
  public void testMigrate_twoCorrectOnSameDocOneIncorrect() throws XWikiException {
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 0, "Regspc.Doc1"});
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.That", 1, "Regspc.Doc1"});
    resultList.add(new Object[]{"ABC@def.com", "ISubscribed.That", 0, "Regspc.Doc2"});
    DocumentReference lowerRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Doc1");
    XWikiDocument lowerDoc = new XWikiDocument(lowerRef);
    expect(wiki.getDocument(eq(lowerRef), same(getContext()))).andReturn(lowerDoc).once();
    BaseObject lowerObj = new BaseObject();
    lowerObj.setXClassReference(objRef);
    lowerObj.setNumber(0);
    lowerObj.setIntValue("isactive", 1);
    lowerObj.setStringValue("email", "abc@def.com");
    lowerDoc.addXObject(lowerObj);
    BaseObject lowerObj2 = new BaseObject();
    lowerObj2.setXClassReference(objRef);
    lowerObj2.setNumber(1);
    lowerObj2.setIntValue("isactive", 1);
    lowerObj2.setStringValue("email", "abc@def.com");
    lowerDoc.addXObject(lowerObj2);
    wiki.saveDocument(same(lowerDoc), same(getContext()));
    expectLastCall();
    DocumentReference upperRef = new DocumentReference(getContext().getDatabase(), 
        "Regspc", "Doc2");
    XWikiDocument upperDoc = new XWikiDocument(upperRef);
    expect(wiki.getDocument(eq(upperRef), same(getContext()))).andReturn(upperDoc).once();
    BaseObject upperObj = new BaseObject();
    upperObj.setXClassReference(objRef);
    upperObj.setNumber(0);
    upperObj.setIntValue("isactive", 0);
    upperObj.setStringValue("email", "ABC@def.com");
    upperDoc.addXObject(upperObj);
    wiki.deleteDocument(same(upperDoc), same(getContext()));
    expectLastCall();
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    mig.migrate(null, getContext());
    verify(store, wiki);
    assertEquals("abc@def.com", lowerObj.getStringValue("email"));
    assertEquals(1, lowerObj.getIntValue("isactive"));
    assertEquals("abc@def.com", lowerObj2.getStringValue("email"));
    assertEquals(0, lowerObj2.getIntValue("isactive"));
  }
  
  @Test
  public void testBuildMaps() throws XWikiException {
    Map<String, String> lower = new HashMap<String, String>();
    Map<String, String> upper = new HashMap<String, String>();
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 5, "Regspc.Docname"});
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.That", 1, "Regspc.Docname"});
    resultList.add(new Object[]{"ABC@def.com", "ISubscribed.This", 7, "Regspc.Docname"});
    resultList.add(new Object[]{"A@def.com", "ISubscribed.That", 0, "Regspc.Docname"});
    resultList.add(new Object[]{"B@def.com", "ISubscribed.Third", 0, "Regspc.Docname"});
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    mig.buildMaps(lower, upper, getContext());
    verify(store, wiki);
    assertEquals(2, lower.size());
    assertTrue(lower.containsKey("abc@def.com\\isubscribed.this"));
    assertEquals("Regspc.Docname;5", lower.get("abc@def.com\\isubscribed.this"));
    assertTrue(lower.containsKey("abc@def.com\\isubscribed.that"));
    assertEquals("Regspc.Docname;1", lower.get("abc@def.com\\isubscribed.that"));
    assertEquals(3, upper.size());
    assertTrue(upper.containsKey("ABC@def.com\\ISubscribed.This"));
    assertEquals("Regspc.Docname;7", upper.get("ABC@def.com\\ISubscribed.This"));
    assertTrue(upper.containsKey("A@def.com\\ISubscribed.That"));
    assertEquals("Regspc.Docname;0", upper.get("A@def.com\\ISubscribed.That"));
    assertTrue(upper.containsKey("B@def.com\\ISubscribed.Third"));
    assertEquals("Regspc.Docname;0", upper.get("B@def.com\\ISubscribed.Third"));
  }

  @Test
  public void testGetAllReceiversWithCapitals_noClass() throws XWikiException {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 5, "Regspc.Docname"});
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(false);
    replay(wiki);
    assertEquals(0, mig.getAllReceiversWithCapitals(getContext()).size());
    verify(wiki);
  }

  @Test
  public void testGetAllReceiversWithCapitals_noInternalMapping() throws XWikiException {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 5, "Regspc.Docname"});
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    replay(store, wiki);
    assertEquals(0, mig.getAllReceiversWithCapitals(getContext()).size());
    verify(store, wiki);
  }

  @Test
  public void testGetAllReceiversWithCapitals() throws XWikiException {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[]{"abc@def.com", "ISubscribed.This", 5, "Regspc.Docname"});
    expect(wiki.getHibernateStore()).andReturn(store).anyTimes();
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    expect(wiki.exists(eq(classRef), same(getContext()))).andReturn(true);
    XWikiDocument classDoc = new XWikiDocument(classRef);
    classDoc.getXClass().setCustomMapping("internal");
    expect(wiki.getDocument(eq(classRef), same(getContext()))).andReturn(classDoc);
    expect(store.search((String)anyObject(), eq(0), eq(0), same(getContext()))
        ).andReturn(resultList).once();
    replay(store, wiki);
    assertEquals(1, mig.getAllReceiversWithCapitals(getContext()).size());
    verify(store, wiki);
  }
  
  @Test
  public void testGetDocSpace() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("1", "Doc.Name;3");
    assertEquals("Doc", mig.getDocSpace(map, "1"));
  }

  @Test
  public void testGetDocName() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("1", "Doc.Name;3");
    assertEquals("Name", mig.getDocName(map, "1"));
  }

  @Test
  public void testGetObjNr() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("1", "Doc.Name;3");
    assertEquals(new Integer(3), mig.getObjNr(map, "1"));
  }
}
