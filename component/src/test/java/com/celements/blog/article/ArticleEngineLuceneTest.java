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

import com.celements.blog.article.ArticleSearchParameter.DateMode;
import com.celements.blog.article.ArticleSearchParameter.SubscriptionMode;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
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
  public void testGetDateRestrictions_published() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_published_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getFutureQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_future_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertNull(ret);
  }
  
  @Test
  public void testGetDateRestrictions_published_archived() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
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
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
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
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
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
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_archived_future() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.ARCHIVED, 
        DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
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
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_all() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    assertEquals("", ret.getQueryString());
  }
  
  @Test
  public void testGetDateRestrictions_all_noEditRights() {
    Set<DateMode> modes = new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED, 
        DateMode.ARCHIVED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getDateRestrictions(modes, date, hasEditRights);
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getArchivedQuery(date) 
        + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }
  
  private String getArchivedQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "])";
  }
  
  private String getPublishedQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return "(" + IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" 
        + ILuceneSearchService.DATE_LOW + " TO " + dateString + "]) AND " 
        + IArticleEngineRole.ARTICLE_FIELD_ARCHIVE + ":([" + dateString + " TO " 
        + ILuceneSearchService.DATE_HIGH + "]))";
  }
  
  private String getFutureQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return IArticleEngineRole.ARTICLE_FIELD_PUBLISH + ":([" + dateString 
        + " TO " + ILuceneSearchService.DATE_HIGH + "])";
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS_UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.SUBSCRIBED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("NOT (" + BASE + " AND " + SUBS_UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("NOT (" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = true;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("NOT (" + BASE + " AND " + UNSUBS + ")", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_subscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<SubscriptionMode>(Arrays.asList(
        SubscriptionMode.UNDECIDED, SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = false;
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
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
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
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
    QueryRestrictionGroup ret = engine.getArticleSubsRestrictions(modes, blogConfDocRef, 
        hasEditRights);
    assertEquals("(" + BASE + " AND " + SUBS + ")", ret.getQueryString());
  }

}
