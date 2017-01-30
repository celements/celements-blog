package com.celements.blog.article;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class ArticleEngineHQLTest extends AbstractBridgedComponentTestCase {

  private ArticleEngineHQL engine;

  private XWiki xwiki;
  private XWikiContext context;
  private IBlogServiceRole blogServiceMock;

  @Before
  public void setUp_ArticleEngineHQLTest() {
    xwiki = getWikiMock();
    context = getContext();
    engine = (ArticleEngineHQL) Utils.getComponent(IArticleEngineRole.class);
    blogServiceMock = createMockAndAddToDefault(IBlogServiceRole.class);
    engine.injectBlogService(blogServiceMock);
  }

  @Test
  public void testGetArticles() throws Exception {
  }

  @Test
  public void testGetBlogArticles_getArchivedArticles() throws XWikiException {
    String artSpace = "ArtSpace";
    DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace, "Article");
    XWikiDocument xdoc = createMock(XWikiDocument.class);
    Document doc = new Document(xdoc, context);
    List<Object> artName = new ArrayList<>();
    String articleFN = artSpace + ".Article";
    artName.add(articleFN);
    expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))).andReturn(artName).once();
    DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(), artSpace,
        "BlogConfig");
    XWikiDocument confxDoc = createMock(XWikiDocument.class);
    Document confDoc = createMock(Document.class);
    expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(
        confxDoc).atLeastOnce();
    expect(xwiki.getDocument(eq(articleDocRef), same(context))).andReturn(xdoc).atLeastOnce();
    expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
    expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(
        confxDoc).atLeastOnce();
    expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("ArtSpace"))).andReturn(
        blogConfigDocRef).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), same(
        context))).andReturn("de").once();
    expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
    expect(confDoc.hasAccessLevel((String) anyObject())).andReturn(true).atLeastOnce();
    expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
    expect(xdoc.newDocument(same(context))).andReturn(doc).atLeastOnce();
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(articleDocRef);
    obj.setStringValue("lang", "de");
    obj.setDateValue("publishdate", new Date(0, 11, 31));
    obj.setDateValue("archivedate", new Date(10, 0, 1));
    obj.setStringValue("title", "the title");
    obj.setStringValue("content", "the content");
    Vector<BaseObject> objVec = new Vector<>();
    objVec.add(obj);
    expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", "ArticleClass");
    expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
    expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(articleClassRef);
    expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
    expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    replayDefault(confDoc, confxDoc, xdoc);
    List<Article> result = engine.getBlogArticles(artSpace, Collections.<String>emptyList(), "de",
        true, false, false, true, true, true, true, false, true, true);
    assertEquals(1, result.size());
    verifyDefault(confDoc, confxDoc, xdoc);
  }

  @Test
  public void testGetBlogArticles_getArchivedArticles_notYetArchived() throws XWikiException {
    String artSpace = "ArtSpace";
    DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace, "Article");
    XWikiDocument xdoc = createMock(XWikiDocument.class);
    Document doc = new Document(xdoc, context);
    List<Object> artName = new ArrayList<>();
    String articleFN = artSpace + ".Article";
    artName.add(articleFN);
    expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))).andReturn(artName).once();
    XWikiDocument confxDoc = createMock(XWikiDocument.class);
    Document confDoc = createMock(Document.class);
    expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
    expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(
        confxDoc).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), same(
        context))).andReturn("de").once();
    expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
    expect(xdoc.newDocument(same(context))).andReturn(doc);
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(articleDocRef);
    obj.setStringValue("lang", "de");
    obj.setDateValue("publishdate", new Date(8089, 0, 1));
    obj.setDateValue("archivedate", new Date(8099, 11, 31));
    obj.setStringValue("title", "the title");
    obj.setStringValue("content", "the content");
    Vector<BaseObject> objVec = new Vector<>();
    objVec.add(obj);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", "ArticleClass");
    expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
    expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(articleClassRef);
    expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
    expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
    replayDefault(confDoc, confxDoc, xdoc);
    List<Article> result = engine.getBlogArticles(artSpace, Collections.<String>emptyList(), "de",
        true, false, false, true, true, true, true, false, true, true);
    assertEquals("0 Articles expected, but received " + result.size(), 0, result.size());
    verifyDefault(confDoc, confxDoc, xdoc);
  }

  @Test
  public void testGetBlogArticles_getArchivedArticles_archiveBeforePublish() throws XWikiException {
    String artSpace = "ArtSpace";
    DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace, "Article");
    XWikiDocument xdoc = createMock(XWikiDocument.class);
    Document doc = new Document(xdoc, context);
    List<Object> artName = new ArrayList<>();
    String articleFN = artSpace + ".Article";
    artName.add(articleFN);
    expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))).andReturn(artName).once();
    DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(), artSpace,
        "BlogConfig");
    XWikiDocument confxDoc = createMock(XWikiDocument.class);
    Document confDoc = createMock(Document.class);
    expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(
        confxDoc).atLeastOnce();
    expect(xwiki.getDocument(eq(articleDocRef), same(context))).andReturn(xdoc).atLeastOnce();
    expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
    expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(
        confxDoc).atLeastOnce();
    expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("ArtSpace"))).andReturn(
        blogConfigDocRef).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), same(
        context))).andReturn("de").once();
    expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
    expect(confDoc.hasAccessLevel((String) anyObject())).andReturn(true).atLeastOnce();
    expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
    expect(xdoc.newDocument(same(context))).andReturn(doc);
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(articleDocRef);
    obj.setStringValue("lang", "de");
    obj.setDateValue("publishdate", new Date(8099, 11, 31));
    obj.setDateValue("archivedate", new Date(0, 0, 1));
    obj.setStringValue("title", "the title");
    obj.setStringValue("content", "the content");
    Vector<BaseObject> objVec = new Vector<>();
    objVec.add(obj);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", "ArticleClass");
    expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
    expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(articleClassRef);
    expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
    expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
    replayDefault(confDoc, confxDoc, xdoc);
    List<Article> result = engine.getBlogArticles(artSpace, Collections.<String>emptyList(), "de",
        true, false, false, true, true, true, true, false, true, true);
    assertEquals(1, result.size());
    verifyDefault(confDoc, confxDoc, xdoc);
  }

  @Test
  public void testGetBlogArticles_getArchivedArticles_archiveBeforePublishBeforeToday()
      throws XWikiException {
    String artSpace = "ArtSpace";
    DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace, "Article");
    XWikiDocument xdoc = createMock(XWikiDocument.class);
    Document doc = new Document(xdoc, context);
    List<Object> artName = new ArrayList<>();
    String articleFN = artSpace + ".Article";
    artName.add(articleFN);
    expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))).andReturn(artName).once();
    DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(), artSpace,
        "BlogConfig");
    XWikiDocument confxDoc = createMock(XWikiDocument.class);
    Document confDoc = createMock(Document.class);
    expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(
        confxDoc).atLeastOnce();
    expect(xwiki.getDocument(eq(articleDocRef), same(context))).andReturn(xdoc).atLeastOnce();
    expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
    expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(
        confxDoc).atLeastOnce();
    expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("ArtSpace"))).andReturn(
        blogConfigDocRef).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), same(
        context))).andReturn("de").once();
    expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
    expect(confDoc.hasAccessLevel((String) anyObject())).andReturn(true).atLeastOnce();
    expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
    expect(xdoc.newDocument(same(context))).andReturn(doc);
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(articleDocRef);
    obj.setStringValue("lang", "de");
    obj.setDateValue("publishdate", new Date(10, 11, 31));
    obj.setDateValue("archivedate", new Date(0, 0, 1));
    obj.setStringValue("title", "the title");
    obj.setStringValue("content", "the content");
    Vector<BaseObject> objVec = new Vector<>();
    objVec.add(obj);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", "ArticleClass");
    expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
    expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(articleClassRef);
    expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
    expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
    replayDefault(confDoc, confxDoc, xdoc);
    List<Article> result = engine.getBlogArticles(artSpace, Collections.<String>emptyList(), "de",
        true, false, false, true, true, true, true, false, true, true);
    assertEquals(1, result.size());
    verifyDefault(confDoc, confxDoc, xdoc);
  }

  @Test
  public void testGetBlogArticles_getArchivedArticles_noArchiveDateSet() throws XWikiException {
    String artSpace = "ArtSpace";
    DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace, "Article");
    XWikiDocument xdoc = createMock(XWikiDocument.class);
    Document doc = new Document(xdoc, context);
    List<Object> artName = new ArrayList<>();
    String articleFN = artSpace + ".Article";
    artName.add(articleFN);
    expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))).andReturn(artName).once();
    DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(), artSpace,
        "BlogConfig");
    XWikiDocument confxDoc = createMock(XWikiDocument.class);
    Document confDoc = createMock(Document.class);
    expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
    expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(
        confxDoc).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), same(
        context))).andReturn("de").once();
    expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
    expect(xdoc.newDocument(same(context))).andReturn(doc);
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(articleDocRef);
    obj.setStringValue("lang", "de");
    obj.setDateValue("publishdate", new Date(9999, 11, 31));
    obj.setDateValue("archivedate", null);
    obj.setStringValue("title", "the title");
    obj.setStringValue("content", "the content");
    Vector<BaseObject> objVec = new Vector<>();
    objVec.add(obj);
    DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", "ArticleClass");
    expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
    expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(articleClassRef);
    expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
    expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
    expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
    replayDefault(confDoc, confxDoc, xdoc);
    List<Article> result = engine.getBlogArticles(artSpace, Collections.<String>emptyList(), "de",
        true, false, false, true, true, true, true, false, true, true);
    assertEquals(0, result.size());
    verifyDefault(confDoc, confxDoc, xdoc);
  }

  private String getAllArticlesHQL(String artSpace) {
    return "select doc.fullName from XWikiDocument as doc, BaseObject as obj, "
        + "DateProperty as date, StringProperty as lang where obj.name=doc.fullName and "
        + "obj.className='XWiki.ArticleClass' and (doc.space = '" + artSpace + "' ) and "
        + "lang.id.id=obj.id and lang.id.name='lang' and lang.value = 'de' and obj.id = "
        + "date.id.id and date.id.name='publishdate' order by date.value desc, " + "doc.name asc ";
  }

}
