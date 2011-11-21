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

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class ArticleTest extends AbstractBridgedComponentTestCase{

  private XWikiContext context;
  private XWikiDocument articleDoc;
  private Article article;
  private XWiki xwiki;

  @Before
  public void setUp_ArticleTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }
  
  @Test
  public void testGetTitleDetailed() throws XWikiException, EmptyArticleException {
    BaseObject bObj = new BaseObject();
    bObj.setStringValue("title", "Article Title");
    bObj.setStringValue("lang", "de");
    Object obj = new Object(bObj, context);
    List<Object> list = new ArrayList<Object>();
    list.add(obj);
    article = new Article(list, "space", context);
    String[] result = article.getTitleDetailed("de");
    assertEquals("de", result[0]);
    assertEquals("Article Title", result[1]);
  }
  
  @Test
  public void testGetTitleDetailed_noTranslation() throws XWikiException, 
      EmptyArticleException {
    BaseObject bObj = new BaseObject();
    bObj.setStringValue("title", "Article Title");
    bObj.setStringValue("lang", "de");
    Object obj = new Object(bObj, context);
    BaseObject frbObj = new BaseObject();
    frbObj.setStringValue("title", "");
    frbObj.setStringValue("lang", "fr");
    Object frObj = new Object(frbObj, context);
    List<Object> list = new ArrayList<Object>();
    list.add(obj);
    list.add(frObj);
    expect(xwiki.getWebPreference(eq("default_language"), eq("space"), eq(""), 
        same(context))).andReturn("de");
    replay(xwiki);
    article = new Article(list, "space", context);
    String[] result = article.getTitleDetailed("fr");
    assertEquals("de", result[0]);
    assertEquals("Article Title", result[1]);
    verify(xwiki);
  }
  
  @Test
  public void getTitle() throws XWikiException, EmptyArticleException {
    BaseObject bObj = new BaseObject();
    bObj.setStringValue("title", "Article Title");
    bObj.setStringValue("lang", "de");
    Object obj = new Object(bObj, context);
    List<Object> list = new ArrayList<Object>();
    list.add(obj);
    article = new Article(list, "space", context);
    assertEquals("Article Title", article.getTitle("de"));
  }
  
  @Test
  public void getTitle_noTranslation() throws XWikiException, EmptyArticleException {
    BaseObject bObj = new BaseObject();
    bObj.setStringValue("title", "Article Title");
    bObj.setStringValue("lang", "de");
    Object obj = new Object(bObj, context);
    BaseObject frbObj = new BaseObject();
    frbObj.setStringValue("title", "");
    frbObj.setStringValue("lang", "fr");
    Object frObj = new Object(frbObj, context);
    List<Object> list = new ArrayList<Object>();
    list.add(obj);
    list.add(frObj);
    expect(xwiki.getWebPreference(eq("default_language"), eq("space"), eq(""), 
        same(context))).andReturn("de");
    replay(xwiki);
    article = new Article(list, "space", context);
    assertEquals("Article Title", article.getTitle("fr"));
    verify(xwiki);
  }

  @Test 
  public void getStringProperty() throws XWikiException, EmptyArticleException {
    BaseObject bObj = new BaseObject();
    bObj.setStringValue("field", "value");
    Object obj = new Object(bObj, context);
    List<Object> list = new ArrayList<Object>();
    list.add(obj);
    article = new Article(list, "space", context);
    assertEquals("value", article.getStringProperty(obj, "field"));
  }
  
  @Test
  public void testHasMoreLink_in_translation_translation_empty(
      ) throws XWikiException, EmptyArticleException {
    articleDoc = createMock(XWikiDocument.class);
    Document articleApiDoc = new Document(articleDoc, context);
    expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
    Vector<BaseObject> articleObjs = new Vector<BaseObject>();
    BaseObject articleDe = new BaseObject();
    articleDe.setLargeStringValue("extract", "deutscher extract");
    articleDe.setLargeStringValue("content", "deutscher extract");
    articleDe.setStringValue("lang", "de");
    articleObjs.add(articleDe);
    BaseObject articleFr = new BaseObject();
    articleFr.setStringValue("lang", "fr");
    articleObjs.add(articleFr);
    BaseObject articleIt = new BaseObject();
    articleIt.setStringValue("lang", "it");
    articleObjs.add(articleIt);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
        "ArticleClass");
    expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
    expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
        articleClassRef);
    expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
    DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
    expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xwiki.getWebPreference(eq("default_language"), eq("News"), eq(""),
        same(context))).andReturn("de");
    replay(articleDoc, xwiki);
    article = new Article(articleDoc , context);
    assertTrue("No translation in it but details in de.", article.hasMoreLink("it",
        false));
    verify(articleDoc, xwiki);
  }

  @Test
  public void testHasMoreLink_in_translation_translation_not_empty(
      ) throws XWikiException, EmptyArticleException {
    articleDoc = createMock(XWikiDocument.class);
    Document articleApiDoc = new Document(articleDoc, context);
    expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
    Vector<BaseObject> articleObjs = new Vector<BaseObject>();
    BaseObject articleDe = new BaseObject();
    articleDe.setLargeStringValue("extract", "deutscher extract");
    articleDe.setLargeStringValue("content", "deutscher content");
    articleDe.setStringValue("lang", "de");
    articleObjs.add(articleDe);
    BaseObject articleFr = new BaseObject();
    articleFr.setStringValue("lang", "fr");
    articleObjs.add(articleFr);
    BaseObject articleIt = new BaseObject();
    articleIt.setStringValue("lang", "it");
    articleIt.setLargeStringValue("extract", "Ital extract");
    articleIt.setLargeStringValue("content", "Ital content");
    articleObjs.add(articleIt);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
        "ArticleClass");
    expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
    expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
        articleClassRef);
    expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
    DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
    expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xwiki.getWebPreference(eq("default_language"), eq("News"), eq(""),
        same(context))).andReturn("de");
    replay(articleDoc, xwiki);
    article = new Article(articleDoc , context);
    assertTrue("Has translation in it.", article.hasMoreLink("it",
        false));
    verify(articleDoc, xwiki);
  }

  @Test
  public void testHasMoreLink_in_default_lang(
      ) throws XWikiException, EmptyArticleException {
    articleDoc = createMock(XWikiDocument.class);
    Document articleApiDoc = new Document(articleDoc, context);
    expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
    Vector<BaseObject> articleObjs = new Vector<BaseObject>();
    BaseObject articleDe = new BaseObject();
    articleDe.setLargeStringValue("extract", "deutscher extract");
    articleDe.setLargeStringValue("content", "deutscher content");
    articleDe.setStringValue("lang", "de");
    articleObjs.add(articleDe);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
        "ArticleClass");
    expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
    expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
        articleClassRef);
    expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
    DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
    expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xwiki.getWebPreference(eq("default_language"), eq("News"), eq(""),
        same(context))).andReturn("de");
    replay(articleDoc, xwiki);
    article = new Article(articleDoc , context);
    assertTrue("Details in de.", article.hasMoreLink("de",
        false));
    verify(articleDoc, xwiki);
  }

  @Test
  public void testHasMoreLink_in_default_lang_no_extract(
      ) throws XWikiException, EmptyArticleException {
    articleDoc = createMock(XWikiDocument.class);
    Document articleApiDoc = new Document(articleDoc, context);
    expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
    Vector<BaseObject> articleObjs = new Vector<BaseObject>();
    BaseObject articleDe = new BaseObject();
    articleDe.setLargeStringValue("content", "deutscher content");
    articleDe.setStringValue("lang", "de");
    articleObjs.add(articleDe);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
        "ArticleClass");
    expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
    expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
        articleClassRef);
    expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
    DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
    expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xwiki.getWebPreference(eq("default_language"), eq("News"), eq(""),
        same(context))).andReturn("de");
    replay(articleDoc, xwiki);
    article = new Article(articleDoc , context);
    assertFalse("Only details (short) but no extract in de.", article.hasMoreLink("de",
        false));
    verify(articleDoc, xwiki);
  }

  @Test
  public void testHasMoreLink_in_default_lang_no_extract_long_content(
      ) throws XWikiException, EmptyArticleException {
    articleDoc = createMock(XWikiDocument.class);
    Document articleApiDoc = new Document(articleDoc, context);
    expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
    Vector<BaseObject> articleObjs = new Vector<BaseObject>();
    BaseObject articleDe = new BaseObject();
    articleDe.setLargeStringValue("content", getLoremIpsum());
    articleDe.setStringValue("lang", "de");
    articleObjs.add(articleDe);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
        "ArticleClass");
    expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
    expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
        articleClassRef);
    expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
    DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
    expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xwiki.getWebPreference(eq("default_language"), eq("News"), eq(""),
        same(context))).andReturn("de");
    replay(articleDoc, xwiki);
    article = new Article(articleDoc , context);
    assertTrue("Only details (to long for extract) but no extract in de.", 
        article.hasMoreLink("de", false));
    verify(articleDoc, xwiki);
  }
  
  @Test
  public void testHasMoreLink_in_translation_no_extract(
      ) throws XWikiException, EmptyArticleException {
    articleDoc = createMock(XWikiDocument.class);
//    expect(articleDoc.getSpace()).andReturn("News");
    Document articleApiDoc = new Document(articleDoc, context);
    expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
    Vector<BaseObject> articleObjs = new Vector<BaseObject>();
    BaseObject articleDe = new BaseObject();
    articleDe.setLargeStringValue("content", "deutscher content");
    articleDe.setStringValue("lang", "de");
    articleObjs.add(articleDe);
    BaseObject articleIt = new BaseObject();
    articleIt.setStringValue("lang", "it");
    articleObjs.add(articleIt);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
        "ArticleClass");
    expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
    expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
        articleClassRef);
    expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
    DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
    expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xwiki.getWebPreference(eq("default_language"), eq("News"), eq(""),
        same(context))).andReturn("de");
    replay(articleDoc, xwiki);
    article = new Article(articleDoc , context);
    assertFalse("Only details (short) but no extract in de.", 
        article.hasMoreLink("it", false));
    verify(articleDoc, xwiki);
  }
  
  @Test
  public void testHasMoreLink_in_translation_no_extract_long_content(
      ) throws XWikiException, EmptyArticleException {
    articleDoc = createMock(XWikiDocument.class);
    Document articleApiDoc = new Document(articleDoc, context);
    expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
    Vector<BaseObject> articleObjs = new Vector<BaseObject>();
    BaseObject articleDe = new BaseObject();
    articleDe.setLargeStringValue("content", getLoremIpsum());
    articleDe.setStringValue("lang", "de");
    articleObjs.add(articleDe);
    BaseObject articleIt = new BaseObject();
    articleIt.setStringValue("lang", "it");
    articleObjs.add(articleIt);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
        "ArticleClass");
    expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
    expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
        articleClassRef);
    expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
    DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
    expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xwiki.getWebPreference(eq("default_language"), eq("News"), eq(""),
        same(context))).andReturn("de");
    replay(articleDoc, xwiki);
    article = new Article(articleDoc , context);
    assertTrue("Only details (to long for extract) but no extract in de.", 
        article.hasMoreLink("it", false));
    verify(articleDoc, xwiki);
  }

  // Helper
  
  String getLoremIpsum() {
    return "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo " +
    		"ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis " +
    		"parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, " +
    		"pellentesque eu, pretiumsquis, sem. Nulla consequat massa quis enim. Donec " +
    		"pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, " +
    		"rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede " +
    		"mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper " +
    		"nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, " +
    		"consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra " +
    		"quis, feugiat a, tellus.";
  }
}
