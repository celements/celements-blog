package com.celements.blog.article;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.blog.article.ArticleSearchParameter.DateMode;
import com.celements.blog.article.ArticleSearchParameter.SubscriptionMode;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class ArticleEngineLuceneTest extends AbstractBridgedComponentTestCase {

  private static final String BASE = "object:(+Celements2.BlogArticleSubscriptionClass*) "
      + "AND (Celements2.BlogArticleSubscriptionClass.subscriber:(+\"space.blog\") "
      + "OR Celements2.BlogArticleSubscriptionClass.subscriber:(+\"wiki\\:space.blog\"))";
  private static final String SUBS = 
      "Celements2.BlogArticleSubscriptionClass.doSubscribe:(+\"1\")";
  private static final String UNSUBS = 
      "Celements2.BlogArticleSubscriptionClass.doSubscribe:(+\"0\")";
  private static final String SUBS_UNSUBS = "(" + SUBS + " OR " + UNSUBS + ")";
  
  private DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
  
  private ArticleEngineLucene engine;

  private XWiki xwiki;
  private XWikiContext context;
  private XWikiRightService rightsServiceMock;
  private INextFreeDocRole nextFreeDocServiceMock;

  @Before
  public void setUp_ArticleEngineLuceneTest() {
    xwiki = getWikiMock();
    context = getContext();
    engine = (ArticleEngineLucene) Utils.getComponent(IArticleEngineRole.class, "lucene");
    rightsServiceMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightsServiceMock).anyTimes();
    nextFreeDocServiceMock = createMockAndAddToDefault(INextFreeDocRole.class);
    engine.injectNextFreeDocService(nextFreeDocServiceMock);
  }
  
  @Test
  public void testCheckRights() throws Exception {
    ArticleSearchParameter param = new ArticleSearchParameter();
    param.setBlogSpaceRef(new SpaceReference("blogSpace", new WikiReference("wiki")));
    Set<SubscriptionMode> subsModes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.values())); 
    param.setSubscriptionModes(subsModes);
    Set<DateMode> dateModes = new HashSet<DateMode>(Arrays.asList(DateMode.values()));
    param.setDateModes(dateModes);
    
    expect(nextFreeDocServiceMock.getNextUntitledPageDocRef(eq(param.getBlogSpaceRef()))
        ).andReturn(blogConfDocRef).once();
    expect(rightsServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(true).once();
    expect(rightsServiceMock.hasAccessLevel(eq("edit"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(true).once();
    
    replayDefault();
    boolean ret = engine.checkRights(param);
    verifyDefault();
    
    assertTrue(ret);
    assertEquals(subsModes, param.getSubscriptionModes());
    assertEquals(dateModes, param.getDateModes());
  }
  
  @Test
  public void testCheckRights_noView() throws Exception {
    ArticleSearchParameter param = new ArticleSearchParameter();
    param.setBlogSpaceRef(new SpaceReference("blogSpace", new WikiReference("wiki")));
    Set<SubscriptionMode> subsModes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.values())); 
    param.setSubscriptionModes(subsModes);
    Set<DateMode> dateModes = new HashSet<DateMode>(Arrays.asList(DateMode.values()));
    param.setDateModes(dateModes);
    
    expect(nextFreeDocServiceMock.getNextUntitledPageDocRef(eq(param.getBlogSpaceRef()))
        ).andReturn(blogConfDocRef).once();
    expect(rightsServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(false).once();
    
    replayDefault();
    boolean ret = engine.checkRights(param);
    verifyDefault();
    
    assertFalse(ret);
    assertEquals(subsModes, param.getSubscriptionModes());
    assertEquals(dateModes, param.getDateModes());
  }
  
  @Test
  public void testCheckRights_noEdit() throws Exception {
    ArticleSearchParameter param = new ArticleSearchParameter();
    param.setBlogSpaceRef(new SpaceReference("blogSpace", new WikiReference("wiki")));
    Set<SubscriptionMode> subsModes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.values())); 
    param.setSubscriptionModes(subsModes);
    Set<DateMode> dateModes = new HashSet<DateMode>(Arrays.asList(DateMode.values()));
    param.setDateModes(dateModes);
    
    expect(nextFreeDocServiceMock.getNextUntitledPageDocRef(eq(param.getBlogSpaceRef()))
        ).andReturn(blogConfDocRef).once();
    expect(rightsServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(true).once();
    expect(rightsServiceMock.hasAccessLevel(eq("edit"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(false).once();
    
    replayDefault();
    boolean ret = engine.checkRights(param);
    verifyDefault();
    
    assertTrue(ret);
    assertEquals(new HashSet<SubscriptionMode>(Arrays.asList(SubscriptionMode.BLOG, 
        SubscriptionMode.SUBSCRIBED)), param.getSubscriptionModes());
    assertEquals(new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED)), param.getDateModes());
  }
  
  @Test
  public void testCheckRights_noSubsModes() throws Exception {
    ArticleSearchParameter param = new ArticleSearchParameter();
    param.setBlogSpaceRef(new SpaceReference("blogSpace", new WikiReference("wiki")));
    Set<SubscriptionMode> subsModes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNSUBSCRIBED)); 
    param.setSubscriptionModes(subsModes);
    Set<DateMode> dateModes = new HashSet<DateMode>(Arrays.asList(DateMode.values()));
    param.setDateModes(dateModes);
    
    expect(nextFreeDocServiceMock.getNextUntitledPageDocRef(eq(param.getBlogSpaceRef()))
        ).andReturn(blogConfDocRef).once();
    expect(rightsServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(true).once();
    expect(rightsServiceMock.hasAccessLevel(eq("edit"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(false).once();
    
    replayDefault();
    boolean ret = engine.checkRights(param);
    verifyDefault();
    
    assertFalse(ret);
    assertEquals(param.getSubscriptionModes(), param.getSubscriptionModes());
    assertEquals(new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED)), param.getDateModes());
  }
  
  @Test
  public void testCheckRights_XWE() throws Exception {
    ArticleSearchParameter param = new ArticleSearchParameter();
    param.setBlogSpaceRef(new SpaceReference("blogSpace", new WikiReference("wiki")));
    XWikiException cause = new XWikiException();
    
    expect(nextFreeDocServiceMock.getNextUntitledPageDocRef(eq(param.getBlogSpaceRef()))
        ).andReturn(blogConfDocRef).once();
    expect(rightsServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andThrow(cause).once();
    
    replayDefault();
    try {
      engine.checkRights(param);
      fail("Expecting ALE");
    } catch (ArticleLoadException ale) {
      assertSame(cause, ale.getCause());
    }
    verifyDefault();
  }
  
  @Test
  public void testCheckRights_noDateModes() throws Exception {
    ArticleSearchParameter param = new ArticleSearchParameter();
    param.setBlogSpaceRef(new SpaceReference("blogSpace", new WikiReference("wiki")));
    Set<SubscriptionMode> subsModes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.values())); 
    param.setSubscriptionModes(subsModes);
    Set<DateMode> dateModes = new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE));
    param.setDateModes(dateModes);
    
    expect(nextFreeDocServiceMock.getNextUntitledPageDocRef(eq(param.getBlogSpaceRef()))
        ).andReturn(blogConfDocRef).once();
    expect(rightsServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(true).once();
    expect(rightsServiceMock.hasAccessLevel(eq("edit"), eq("XWiki.XWikiGuest"), 
        eq("wiki:space.blog"), same(context))).andReturn(false).once();
    
    replayDefault();
    boolean ret = engine.checkRights(param);
    verifyDefault();
    
    assertFalse(ret);
    assertEquals(new HashSet<SubscriptionMode>(Arrays.asList(SubscriptionMode.BLOG, 
        SubscriptionMode.SUBSCRIBED)), param.getSubscriptionModes());
    assertEquals(param.getDateModes(), param.getDateModes());
  }
  
  @Test
  public void testGetDateRestrictions_published() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED));
    Date date = new Date();
    String dateString = ILuceneSearchService.SDF.format(date);
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date);
    String expectedQuery = "(" + IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "]) AND " 
        + IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" + dateString + " TO " 
        + ILuceneSearchService.DATE_HIGH + "]))";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED));
    Date date = new Date();
    String dateString = ILuceneSearchService.SDF.format(date);
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date);
    String expectedQuery = IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "])";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE));
    Date date = new Date();
    String dateString = ILuceneSearchService.SDF.format(date);
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date);
    String expectedQuery = IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" + dateString 
        + " TO " + ILuceneSearchService.DATE_HIGH + "])";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_published_archived() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED));
    Date date = new Date();
    String dateString = ILuceneSearchService.SDF.format(date);
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date);
    String expectedQuery = "((" + IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "]) AND " 
        + IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" + dateString + " TO " 
        + ILuceneSearchService.DATE_HIGH + "])) OR " 
        + IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" + ILuceneSearchService.DATE_LOW 
        + " TO " + dateString + "]))";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_published_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.FUTURE));
    Date date = new Date();
    String dateString = ILuceneSearchService.SDF.format(date);
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date);
    String expectedQuery = "((" + IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "]) AND " 
        + IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" + dateString + " TO " 
        + ILuceneSearchService.DATE_HIGH + "])) OR " 
        + IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" + dateString + " TO " 
        + ILuceneSearchService.DATE_HIGH + "]))";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED, 
        DateMode.FUTURE));
    Date date = new Date();
    String dateString = ILuceneSearchService.SDF.format(date);
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date);
    String expectedQuery = "(" + IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" 
        + dateString + " TO " + ILuceneSearchService.DATE_HIGH + "]) OR " 
        + IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" + ILuceneSearchService.DATE_LOW 
        + " TO " + dateString + "]))";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_all() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED, DateMode.FUTURE));
    Date date = new Date();
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date);
    assertEquals("", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED));
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNSUBSCRIBED));
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef);
    assertEquals("(" + BASE + " AND " + UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED));
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef);
    assertEquals("(" + BASE + " AND " + SUBS_UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED));
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef);
    assertEquals("NOT (" + BASE + " AND " + SUBS_UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.UNSUBSCRIBED));
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef);
    assertEquals("NOT (" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.SUBSCRIBED));
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef);
    assertEquals("NOT (" + BASE + " AND " + UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_all() {
    DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED, 
        SubscriptionMode.UNDECIDED));
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef);
    assertEquals("", ret.getQueryString());
  }

}
