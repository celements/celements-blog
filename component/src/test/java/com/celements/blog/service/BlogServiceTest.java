package com.celements.blog.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadException;
import com.celements.blog.article.ArticleLoadParameter;
import com.celements.blog.article.IArticleEngineRole;
import com.celements.blog.plugin.BlogClasses;
import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.common.test.AbstractComponentTest;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class BlogServiceTest extends AbstractComponentTest {

  private XWiki xwiki;
  private XWikiContext context;
  private IDocumentReferenceCache<SpaceReference> blogCacheMock;
  private IArticleEngineRole articleEngineMock;

  private BlogService blogService;

  private final WikiReference wikiRef = new WikiReference("wiki");
  private final String testEngineHint = "test";

  @Before
  @SuppressWarnings("unchecked")
  public void prepare() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    articleEngineMock = registerComponentMock(IArticleEngineRole.class, testEngineHint);
    blogService = (BlogService) Utils.getComponent(IBlogServiceRole.class);
    blogCacheMock = createDefaultMock(IDocumentReferenceCache.class);
    blogService.blogCache = blogCacheMock;
  }

  @Test
  public void testGetBlogConfigDocRef() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);

    expect(blogCacheMock.getCachedDocRefs(eq(spaceRef))).andReturn(ImmutableSet.of(docRef)).once();

    replayDefault();
    DocumentReference ret = blogService.getBlogConfigDocRef(spaceRef);
    verifyDefault();

    assertEquals(docRef, ret);
  }

  @Test
  public void testGetBlogConfigDocRef_none() throws Exception {
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);

    expect(blogCacheMock.getCachedDocRefs(eq(spaceRef))).andReturn(
        Collections.<DocumentReference>emptySet()).once();

    replayDefault();
    DocumentReference ret = blogService.getBlogConfigDocRef(spaceRef);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetBlogConfigDocRef_multiple() throws Exception {
    DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "blog1");
    DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "blog2");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);

    expect(blogCacheMock.getCachedDocRefs(eq(spaceRef))).andReturn(ImmutableSet.of(docRef1,
        docRef2)).once();

    replayDefault();
    DocumentReference ret = blogService.getBlogConfigDocRef(spaceRef);
    verifyDefault();

    assertTrue(ret.equals(docRef1) || ret.equals(docRef2));
  }

  @Test
  public void testGetBlogSpaceRef() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
    XWikiDocument doc = getBlogDoc(docRef, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE,
        spaceRef.getName());

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    replayDefault();
    SpaceReference ret = blogService.getBlogSpaceRef(docRef);
    verifyDefault();

    assertEquals(spaceRef, ret);
  }

  @Test
  public void testGetBlogSpaceRef_notSet() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    SpaceReference spaceRef = new SpaceReference(docRef.getName(), wikiRef);
    XWikiDocument doc = getBlogDoc(docRef, null, null);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    replayDefault();
    SpaceReference ret = blogService.getBlogSpaceRef(docRef);
    verifyDefault();

    assertEquals(spaceRef, ret);
  }

  @Test
  public void testGetBlogSpaceRef_noBObj() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    XWikiDocument doc = new XWikiDocument(docRef);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    replayDefault();
    SpaceReference ret = blogService.getBlogSpaceRef(docRef);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetBlogSpaceRef_XWikiException() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");

    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(new XWikiException()).once();

    replayDefault();
    try {
      blogService.getBlogSpaceRef(docRef);
      fail("expecting XWE");
    } catch (XWikiException xwe) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetBlogSpaceRef_null() throws Exception {
    replayDefault();
    SpaceReference ret = blogService.getBlogSpaceRef(null);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testIsSubscribable() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    XWikiDocument doc = getBlogDoc(docRef, "", "");
    doc.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    replayDefault();
    boolean ret = blogService.isSubscribable(docRef);
    verifyDefault();

    assertTrue(ret);
  }

  @Test
  public void testIsSubscribable_wrongVal() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    XWikiDocument doc = getBlogDoc(docRef, "", "");
    doc.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 0);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    replayDefault();
    boolean ret = blogService.isSubscribable(docRef);
    verifyDefault();

    assertFalse(ret);
  }

  @Test
  public void testIsSubscribable_notSet() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    XWikiDocument doc = getBlogDoc(docRef, null, null);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    replayDefault();
    boolean ret = blogService.isSubscribable(docRef);
    verifyDefault();

    assertFalse(ret);
  }

  @Test
  public void testIsSubscribable_noObj() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    XWikiDocument doc = new XWikiDocument(docRef);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    replayDefault();
    boolean ret = blogService.isSubscribable(docRef);
    verifyDefault();

    assertFalse(ret);
  }

  @Test
  public void testIsSubscribable_XWikiException() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");

    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(new XWikiException()).once();

    replayDefault();
    try {
      blogService.isSubscribable(docRef);
      fail("expecting XWE");
    } catch (XWikiException xwe) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetSubribedToBlogs() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    String space1 = "space1";
    String space2 = "space2";
    String space3 = "space3";
    String space4 = "space4";
    XWikiDocument doc = getBlogDoc(docRef, BlogClasses.PROPERTY_BLOG_CONFIG_SUBSCRIBE_TO, space1
        + "," + space2 + "," + space3 + "," + space4);
    DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "blog1");
    XWikiDocument doc1 = getBlogDoc(docRef1, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, space1);
    doc1.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);
    DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "blog2");
    XWikiDocument doc2 = getBlogDoc(docRef2, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, space2);
    doc2.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 0);
    DocumentReference docRef3 = new DocumentReference(wikiRef.getName(), "space", "blog3");
    XWikiDocument doc3 = getBlogDoc(docRef3, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, space3);
    doc3.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);

    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space1, wikiRef)))).andReturn(
        ImmutableSet.of(docRef1)).once();
    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space2, wikiRef)))).andReturn(
        ImmutableSet.of(docRef2)).once();
    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space3, wikiRef)))).andReturn(
        ImmutableSet.of(docRef3)).once();
    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space4, wikiRef)))).andReturn(
        ImmutableSet.<DocumentReference>of()).once();

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.getDocument(eq(docRef1), same(context))).andReturn(doc1).once();
    expect(xwiki.getDocument(eq(docRef2), same(context))).andReturn(doc2).once();
    expect(xwiki.getDocument(eq(docRef3), same(context))).andReturn(doc3).once();

    replayDefault();
    List<DocumentReference> ret = blogService.getSubribedToBlogs(docRef);
    verifyDefault();

    assertEquals(Arrays.asList(docRef1, docRef3), ret);
  }

  @Test
  public void testGetSubribedToBlogsSpaceRefs() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    String space1 = "space1";
    String space2 = "space2";
    String space3 = "space3";
    String space4 = "space4";
    XWikiDocument doc = getBlogDoc(docRef, BlogClasses.PROPERTY_BLOG_CONFIG_SUBSCRIBE_TO, space1
        + "," + space2 + "," + space3 + "," + space4);
    DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "blog1");
    XWikiDocument doc1 = getBlogDoc(docRef1, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, space1);
    doc1.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);
    DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "blog2");
    XWikiDocument doc2 = getBlogDoc(docRef2, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, space2);
    doc2.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 0);
    DocumentReference docRef3 = new DocumentReference(wikiRef.getName(), "space", "blog3");
    XWikiDocument doc3 = getBlogDoc(docRef3, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, space3);
    doc3.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
        BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);

    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space1, wikiRef)))).andReturn(
        ImmutableSet.of(docRef1)).once();
    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space2, wikiRef)))).andReturn(
        ImmutableSet.of(docRef2)).once();
    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space3, wikiRef)))).andReturn(
        ImmutableSet.of(docRef3)).once();
    expect(blogCacheMock.getCachedDocRefs(eq(new SpaceReference(space4, wikiRef)))).andReturn(
        ImmutableSet.<DocumentReference>of()).once();

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.getDocument(eq(docRef1), same(context))).andReturn(doc1).times(2);
    expect(xwiki.getDocument(eq(docRef2), same(context))).andReturn(doc2).once();
    expect(xwiki.getDocument(eq(docRef3), same(context))).andReturn(doc3).times(2);

    replayDefault();
    List<SpaceReference> ret = blogService.getSubribedToBlogsSpaceRefs(docRef);
    verifyDefault();

    assertEquals(Arrays.asList(new SpaceReference(space1, wikiRef), new SpaceReference(space3,
        wikiRef)), ret);
  }

  @Test
  public void testGetArticles() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setExecutionDate(new Date(0));
    param.setSubscribedToBlogs(Arrays.asList(docRef));
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(new XWikiDocument(
        docRef)).once();
    expect(xwiki.getXWikiPreference(eq("blog_article_engine"), eq("blog.article.engine"), isNull(
        String.class), same(getContext()))).andReturn(testEngineHint).once();
    expect(articleEngineMock.getArticles(same(param))).andReturn(
        Collections.<Article>emptyList()).once();

    replayDefault();
    List<Article> ret = blogService.getArticles(docRef, param);
    verifyDefault();

    assertEquals(0, ret.size());
    assertTrue(new Date(0).before(param.getExecutionDate()));
    Date dateNow = new Date();
    assertTrue(param.getExecutionDate().before(dateNow) || dateNow.equals(
        param.getExecutionDate()));
    assertEquals(docRef, param.getBlogDocRef());
    assertEquals(0, param.getSubscribedToBlogs().size());
  }

  @Test
  public void testGetArticles_nullparam() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    Capture<ArticleLoadParameter> paramCapture = newCapture();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(new XWikiDocument(
        docRef)).once();
    expect(xwiki.getXWikiPreference(eq("blog_article_engine"), eq("blog.article.engine"), isNull(
        String.class), same(getContext()))).andReturn(testEngineHint).once();
    expect(articleEngineMock.getArticles(capture(paramCapture))).andReturn(
        Collections.<Article>emptyList()).once();

    replayDefault();
    List<Article> ret = blogService.getArticles(docRef, null);
    verifyDefault();

    assertEquals(0, ret.size());
    ArticleLoadParameter param = paramCapture.getValue();
    assertEquals(docRef, param.getBlogDocRef());
  }

  @Test
  public void testGetArticles_ALE() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    ArticleLoadParameter param = new ArticleLoadParameter();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(new XWikiDocument(
        docRef)).once();
    expect(xwiki.getXWikiPreference(eq("blog_article_engine"), eq("blog.article.engine"), isNull(
        String.class), same(getContext()))).andReturn(testEngineHint).once();
    ArticleLoadException cause = new ArticleLoadException("");
    expect(articleEngineMock.getArticles(same(param))).andThrow(cause).once();

    replayDefault();
    try {
      blogService.getArticles(docRef, param);
      fail("expecting ALE");
    } catch (ArticleLoadException ale) {
      assertSame(cause, ale);
    }
    verifyDefault();

    assertEquals(docRef, param.getBlogDocRef());
  }

  @Test
  public void testGetArticles_ALEfromXWE() throws Exception {
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    XWikiException cause = new XWikiException();
    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(cause).once();

    replayDefault();
    try {
      blogService.getArticles(docRef, null);
      fail("expecting ALE");
    } catch (ArticleLoadException ale) {
      assertSame(cause, ale.getCause());
    }
    verifyDefault();
  }

  private XWikiDocument getBlogDoc(DocumentReference docRef, String field, String val) {
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject blogObj = new BaseObject();
    blogObj.setXClassReference(blogService.getBlogConfigClassRef(wikiRef));
    if (field != null) {
      blogObj.setStringValue(field, val);
    }
    doc.addXObject(blogObj);
    return doc;
  }

}
