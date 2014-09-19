package com.celements.blog.article;

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
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.QueryRestrictionGroup;
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

  @Before
  public void setUp_ArticleEngineLuceneTest() {
    engine = (ArticleEngineLucene) Utils.getComponent(IArticleEngineRole.class, "lucene");
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
