package com.celements.blog.article;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.blog.article.ArticleLoadParameter.DateMode;
import com.celements.blog.article.ArticleLoadParameter.SubscriptionMode;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class ArticleLuceneQueryBuilderTest extends AbstractBridgedComponentTestCase {

  private static final String BASE = "object:(+Celements2.BlogArticleSubscriptionClass*) "
      + "AND (Celements2.BlogArticleSubscriptionClass.subscriber:(+\"space.blog\") "
      + "OR Celements2.BlogArticleSubscriptionClass.subscriber:(+\"wiki\\:space.blog\"))";
  private static final String SUBS = 
      "Celements2.BlogArticleSubscriptionClass.doSubscribe:(+\"1\")";
  private static final String UNSUBS = 
      "Celements2.BlogArticleSubscriptionClass.doSubscribe:(+\"0\")";
  private static final String SUBS_UNSUBS = "(" + SUBS + " OR " + UNSUBS + ")";
  
  private DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
  
  private ArticleLuceneQueryBuilder builder;

  private XWiki xwiki;
  private XWikiContext context;
  private XWikiRightService rightsServiceMock;
  private IBlogServiceRole blogServiceMock;
  private INextFreeDocRole nextFreeDocServiceMock;

  @Before
  public void setUp_ArticleEngineLuceneTest() {
    xwiki = getWikiMock();
    context = getContext();
    builder = (ArticleLuceneQueryBuilder) Utils.getComponent(
        IArticleLuceneQueryBuilderRole.class);
    rightsServiceMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightsServiceMock).anyTimes();
    blogServiceMock = createMockAndAddToDefault(IBlogServiceRole.class);
    builder.injectBlogService(blogServiceMock);
    nextFreeDocServiceMock = createMockAndAddToDefault(INextFreeDocRole.class);
    builder.injectNextFreeDocService(nextFreeDocServiceMock);
  }
  
  @Test
  public void testGetBlogRestriction() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(blogConfDocRef);
    param.setDateModes(new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE)));
    SpaceReference spaceRef = new SpaceReference("artSpace", new WikiReference("wiki"));
    
    expect(blogServiceMock.getBlogSpaceRef(eq(blogConfDocRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, true, true);
    
    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();
    
    assertEquals("(space:(+\"artSpace\") AND " + getFutureQuery(param.getExecutionDate()) 
        + ")", ret.getQueryString());
  }
  
  @Test
  public void testGetBlogRestriction_noEdit() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(blogConfDocRef);
    param.setDateModes(new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED)));
    SpaceReference spaceRef = new SpaceReference("artSpace", new WikiReference("wiki"));
    
    expect(blogServiceMock.getBlogSpaceRef(eq(blogConfDocRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, true, false);
    
    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();
    
    assertEquals("(space:(+\"artSpace\") AND " + getArchivedQuery(param.getExecutionDate()) 
        + ")", ret.getQueryString());
  }
  
  @Test
  public void testGetBlogRestriction_noEdit_future() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(blogConfDocRef);
    param.setDateModes(new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE)));
    SpaceReference spaceRef = new SpaceReference("artSpace", new WikiReference("wiki"));
    
    expect(blogServiceMock.getBlogSpaceRef(eq(blogConfDocRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, true, false);
    
    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();
    
    assertNull(ret);
  }
  
  @Test
  public void testGetBlogRestriction_noView() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(blogConfDocRef);
    SpaceReference spaceRef = new SpaceReference("artSpace", new WikiReference("wiki"));
    
    expect(blogServiceMock.getBlogSpaceRef(eq(blogConfDocRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, false, false);
    
    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();
    
    assertNull(ret);
  }
  
  @Test
  public void testGetBlogRestriction_withoutBlogArticles() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(blogConfDocRef);
    param.setWithBlogArticles(false);
    SpaceReference spaceRef = new SpaceReference("artSpace", new WikiReference("wiki"));
    
    expect(blogServiceMock.getBlogSpaceRef(eq(blogConfDocRef))).andReturn(spaceRef).once();
    
    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();
    
    assertNull(ret);
  }
  
  @Test
  public void testGetBlogRestriction_XWE() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(blogConfDocRef);
    param.setDateModes(new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE)));
    
    expect(blogServiceMock.getBlogSpaceRef(eq(blogConfDocRef))).andThrow(
        new XWikiException()).once();
    
    replayDefault();
    try {
      builder.getBlogRestriction(param);
      fail("expecting XWE");
    } catch (XWikiException exc) {
      //expected
    }
    verifyDefault();
  }
  
  @Test
  public void testGetSubsRestrictions() {
    fail("TODO"); // TODO
  }
  
  private void expectSpaceRightsCheck(SpaceReference spaceRef, boolean viewRights, 
      boolean editRights) throws Exception {;
      DocumentReference untitledDocRef = new DocumentReference("untitled1", spaceRef);
      expect(nextFreeDocServiceMock.getNextUntitledPageDocRef(eq(spaceRef))).andReturn(
          untitledDocRef).atLeastOnce();
    expect(rightsServiceMock.hasAccessLevel(eq("view"), eq(context.getUser()), 
        eq(serialize(untitledDocRef)), same(context))).andReturn(viewRights).once();
    if (viewRights) {
      expect(rightsServiceMock.hasAccessLevel(eq("edit"), eq(context.getUser()), 
          eq(serialize(untitledDocRef)), same(context))).andReturn(editRights).once();
    }
  }
  
  @Test
  public void testGetDateRestrictions_none() {
    Set<DateMode> modes = Collections.emptySet();
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertNull(ret);
  }
  
  @Test
  public void testGetDateRestrictions_published() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_published_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getFutureQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_future_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertNull(ret);
  }
  
  @Test
  public void testGetDateRestrictions_published_archived() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getArchivedQuery(date) 
        + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_published_archived_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getArchivedQuery(date) 
        + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_published_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getFutureQuery(date) 
        + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_published_future_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED, 
        DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    String expectedQuery = "(" + getFutureQuery(date) + " OR " + getArchivedQuery(date) 
        + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived_future_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED, 
        DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_all() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    assertEquals("", ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_all_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getArchivedQuery(date) 
        + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  private String getArchivedQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_ARCHIVE + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "])";
  }
  
  private String getPublishedQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return "(" + IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_PUBLISH + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "]) AND " 
        + IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_ARCHIVE + ":([" + dateString 
        + " TO " + ILuceneSearchService.DATE_HIGH + "]))";
  }
  
  private String getFutureQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_PUBLISH + ":([" + dateString 
        + " TO " + ILuceneSearchService.DATE_HIGH + "])";
  }

  @Test
  public void testGetArticleSubsRestrictions_none() {
    Set<SubscriptionMode> modes = Collections.emptySet();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS_UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("NOT (" + BASE + " AND " + SUBS_UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("NOT (" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("NOT (" + BASE + " AND " + UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_subscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_all() {
    DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED, 
        SubscriptionMode.UNDECIDED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_all_noEditRights() {
    DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED, 
        SubscriptionMode.UNDECIDED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }
  
  private String serialize(DocumentReference docRef) {
    return Utils.getComponent(IWebUtilsService.class).getRefDefaultSerializer().serialize(
        docRef);
  }

}