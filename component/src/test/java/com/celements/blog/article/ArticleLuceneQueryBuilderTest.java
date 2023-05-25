package com.celements.blog.article;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.blog.article.ArticleLoadParameter.DateMode;
import com.celements.blog.article.ArticleLoadParameter.SubscriptionMode;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.test.AbstractComponentTest;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class ArticleLuceneQueryBuilderTest extends AbstractComponentTest {

  private static final String RESTR_ARTICLE_ISSUBS = "XWiki.ArticleClass.isSubscribable:"
      + "(+\"1\")";
  private static final String RESTR_ARTSUBS_OBJ = "object:(+Celements2.BlogArticleSubscriptionClass*)";
  private static final String RESTR_ARTSUBS_SPACE = "(Celements2.BlogArticleSubscriptionClass.subscriber:(+\"wiki\\:space.blog\") "
      + "OR Celements2.BlogArticleSubscriptionClass.subscriber:(+\"space.blog\"))";

  private static final String RESTR_ARTSUBS_SUBS = "Celements2.BlogArticleSubscriptionClass.doSubscribe:(+\"1\")";
  private static final String RESTR_ARTSUBS_UNSUBS = "Celements2.BlogArticleSubscriptionClass.doSubscribe:(+\"0\")";

  private WikiReference wikiRef = new WikiReference("wiki");
  private SpaceReference spaceRef = new SpaceReference("space", wikiRef);
  private DocumentReference docRef = new DocumentReference("blog", spaceRef);

  private ArticleLuceneQueryBuilder builder;

  private XWikiRightService rightsServiceMock;

  @Before
  public void prepare() {
    LucenePlugin lucenePlugin = createDefaultMock(LucenePlugin.class);
    expect(getWikiMock().getPlugin(eq("lucene"), same(getContext())))
        .andReturn(lucenePlugin).anyTimes();
    expect(lucenePlugin.getAnalyzer()).andReturn(null).anyTimes();
    builder = (ArticleLuceneQueryBuilder) Utils.getComponent(IArticleLuceneQueryBuilderRole.class);
    rightsServiceMock = createDefaultMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightsServiceMock).anyTimes();
    builder.blogService = createDefaultMock(IBlogServiceRole.class);
    builder.rightsAccess = createDefaultMock(IRightsAccessFacadeRole.class);
  }

  @After
  public void tearDown_ArticleEngineLuceneTest() {
    builder.blogService = Utils.getComponent(IBlogServiceRole.class);
    builder.rightsAccess = Utils.getComponent(IRightsAccessFacadeRole.class);
  }

  @Test
  public void testBuild() throws XWikiException {
    // WikiReference wikiRef = new WikiReference("programmzeitung");
    // SpaceReference spaceRef = new SpaceReference("Content", new WikiReference(
    // "programmzeitung"));
    // ArticleLoadParameter param = new ArticleLoadParameter();
    // param.setBlogDocRef(new DocumentReference("Home", spaceRef));
    // param.setWithBlogArticles(true);
    // param.setSubscribedToBlogs(Arrays.asList(new DocumentReference("Alltag", spaceRef),
    // new DocumentReference("Godefrod", spaceRef), new DocumentReference("Highlights",
    // spaceRef), new DocumentReference("Ungereimtes", spaceRef), new DocumentReference(
    // "Fotos", spaceRef),new DocumentReference("News", spaceRef)));
    // param.setDateModes(ImmutableSet.of(DateMode.FUTURE, DateMode.PUBLISHED));
    // param.setSubscriptionModes(ImmutableSet.of(SubscriptionMode.SUBSCRIBED,
    // SubscriptionMode.UNDECIDED));
    // param.setLanguage("de");
    // param.setOffset(0);
    // param.setLimit(5);
    // param.setSortFields(Arrays.asList("-XWiki.ArticleClass.publishdate", "name"));
    //
    // expect(builder.blogService.getBlogSpaceRef(eq(param.getBlogDocRef()))).andReturn(
    // new SpaceReference("aktuelles", wikiRef)).once();
    // expectSpaceRightsCheck(spaceRef, viewRights, editRights);
    //
    // replayDefault();
    // System.out.println(builder.build(param));
    // verifyDefault();
  }

  @Test
  public void testGetBlogRestriction() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Arrays.asList(DateMode.FUTURE.name()));
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expect(builder.blogService.getBlogSpaceRef(eq(docRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, true, true);

    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();

    assertEquals("(space:(+\"" + spaceRef.getName() + "\") AND " + getFutureQuery(
        param.getExecutionDate()) + ")", ret.getQueryString());
  }

  @Test
  public void testGetBlogRestriction_noEdit() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Arrays.asList(DateMode.ARCHIVED.name()));
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expect(builder.blogService.getBlogSpaceRef(eq(docRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, true, false);

    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();

    assertEquals("(space:(+\"" + spaceRef.getName() + "\") AND " + getArchivedQuery(
        param.getExecutionDate()) + ")", ret.getQueryString());
  }

  @Test
  public void testGetBlogRestriction_noEdit_future() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Arrays.asList(DateMode.FUTURE.name()));
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expect(builder.blogService.getBlogSpaceRef(eq(docRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, true, false);

    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetBlogRestriction_noView() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expect(builder.blogService.getBlogSpaceRef(eq(docRef))).andReturn(spaceRef).once();
    expectSpaceRightsCheck(spaceRef, false, null);

    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetBlogRestriction_withoutBlogArticles() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setWithBlogArticles(false);
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expect(builder.blogService.getBlogSpaceRef(eq(docRef))).andReturn(spaceRef).once();

    replayDefault();
    IQueryRestriction ret = builder.getBlogRestriction(param);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetBlogRestriction_XWE() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Arrays.asList(DateMode.FUTURE.name()));

    expect(builder.blogService.getBlogSpaceRef(eq(docRef))).andThrow(new XWikiException()).once();

    replayDefault();
    try {
      builder.getBlogRestriction(param);
      fail("expecting XWE");
    } catch (XWikiException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetSubsRestrictions_noSub() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);

    replayDefault();
    QueryRestrictionGroup ret = builder.getSubsRestrictions(param);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetSubsRestrictions_oneSub() throws Exception {
    DocumentReference subsBlogDocRef = new DocumentReference("subsBlog", spaceRef);
    SpaceReference subsSpaceRef = new SpaceReference("artSpace", wikiRef);
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setSubscribedToBlogs(Arrays.asList(subsBlogDocRef));
    param.setDateModes(Arrays.asList(DateMode.PUBLISHED.name()));
    param.setSubscriptionModes(Arrays.asList(SubscriptionMode.SUBSCRIBED.name()));

    expect(builder.blogService.getBlogSpaceRef(eq(subsBlogDocRef))).andReturn(subsSpaceRef).once();
    expectSpaceRightsCheck(subsSpaceRef, true, true);

    replayDefault();
    QueryRestrictionGroup ret = builder.getSubsRestrictions(param);
    verifyDefault();

    assertNotNull(ret);
    String expected = "(" + RESTR_ARTICLE_ISSUBS + " AND " + getSubsPublishQuery(subsSpaceRef,
        param.getExecutionDate()) + ")";
    assertEquals(expected, ret.getQueryString());
  }

  @Test
  public void testGetSubsRestrictions_multipleSub() throws Exception {
    DocumentReference subsBlogDocRef1 = new DocumentReference("subsBlog1", spaceRef);
    DocumentReference subsBlogDocRef2 = new DocumentReference("subsBlog2", spaceRef);
    DocumentReference subsBlogDocRef3 = new DocumentReference("subsBlog3", spaceRef);
    DocumentReference subsBlogDocRef4 = new DocumentReference("subsBlog4", spaceRef);
    SpaceReference subsSpaceRef1 = new SpaceReference("artSpace1", wikiRef);
    SpaceReference subsSpaceRef2 = new SpaceReference("artSpace2", wikiRef);
    SpaceReference subsSpaceRef3 = new SpaceReference("artSpace3", wikiRef);
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setSubscribedToBlogs(Arrays.asList(subsBlogDocRef1, subsBlogDocRef2, subsBlogDocRef3,
        subsBlogDocRef4));
    param.setDateModes(Arrays.asList(DateMode.PUBLISHED.name()));
    param.setSubscriptionModes(Arrays.asList(SubscriptionMode.SUBSCRIBED.name()));

    expect(builder.blogService.getBlogSpaceRef(eq(subsBlogDocRef1))).andReturn(
        subsSpaceRef1).once();
    expectSpaceRightsCheck(subsSpaceRef1, true, true);
    expect(builder.blogService.getBlogSpaceRef(eq(subsBlogDocRef2))).andReturn(
        subsSpaceRef2).once();
    expectSpaceRightsCheck(subsSpaceRef2, false, null);
    expect(builder.blogService.getBlogSpaceRef(eq(subsBlogDocRef3))).andReturn(
        subsSpaceRef3).once();
    expectSpaceRightsCheck(subsSpaceRef3, true, true);
    expect(builder.blogService.getBlogSpaceRef(eq(subsBlogDocRef4))).andReturn(null).once();
    expectSpaceRightsCheck(null, false, null);

    replayDefault();
    QueryRestrictionGroup ret = builder.getSubsRestrictions(param);
    verifyDefault();

    assertNotNull(ret);
    String expected = "(" + RESTR_ARTICLE_ISSUBS + " AND (" + getSubsPublishQuery(subsSpaceRef1,
        param.getExecutionDate()) + " OR "
        + getSubsPublishQuery(subsSpaceRef3,
            param.getExecutionDate())
        + "))";
    assertEquals(expected, ret.getQueryString());
  }

  @Test
  public void testGetSubsSpaceRestriction() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Arrays.asList(DateMode.PUBLISHED.name()));
    param.setSubscriptionModes(Arrays.asList(SubscriptionMode.SUBSCRIBED.name()));
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expectSpaceRightsCheck(spaceRef, null, true);

    replayDefault();
    QueryRestrictionGroup ret = builder.getSubsSpaceRestriction(param, spaceRef);
    verifyDefault();

    assertEquals(getSubsPublishQuery(spaceRef, param.getExecutionDate()), ret.getQueryString());
  }

  @Test
  public void testGetSubsSpaceRestriction_noDateQuery() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Collections.<String>emptyList());
    param.setSubscriptionModes(Arrays.asList(SubscriptionMode.SUBSCRIBED.name()));
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expectSpaceRightsCheck(spaceRef, null, true);

    replayDefault();
    QueryRestrictionGroup ret = builder.getSubsSpaceRestriction(param, spaceRef);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetSubsSpaceRestriction_noSubsQuery() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Arrays.asList(DateMode.PUBLISHED.name()));
    param.setSubscriptionModes(Collections.<String>emptyList());
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expectSpaceRightsCheck(spaceRef, null, true);

    replayDefault();
    QueryRestrictionGroup ret = builder.getSubsSpaceRestriction(param, spaceRef);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void testGetSubsSpaceRestriction_noEditRights() throws Exception {
    ArticleLoadParameter param = new ArticleLoadParameter();
    param.setBlogDocRef(docRef);
    param.setDateModes(Arrays.asList(DateMode.PUBLISHED.name(), DateMode.FUTURE.name()));
    param.setSubscriptionModes(Arrays.asList(SubscriptionMode.SUBSCRIBED.name(),
        SubscriptionMode.UNSUBSCRIBED.name()));
    SpaceReference spaceRef = new SpaceReference("artSpace", wikiRef);

    expectSpaceRightsCheck(spaceRef, null, false);

    replayDefault();
    QueryRestrictionGroup ret = builder.getSubsSpaceRestriction(param, spaceRef);
    verifyDefault();

    assertEquals(getSubsPublishQuery(spaceRef, param.getExecutionDate()), ret.getQueryString());
  }

  private String getSubsPublishQuery(SpaceReference spaceRef, Date date) {
    return "(space:(+\"" + spaceRef.getName() + "\") AND " + getPublishedQuery(date) + " AND "
        + getSubsQuery(false) + ")";
  }

  private void expectSpaceRightsCheck(SpaceReference spaceRef, Boolean viewRights,
      Boolean editRights) throws Exception {
    if (viewRights != null) {
      expect(builder.rightsAccess.hasAccessLevel(eq(spaceRef), eq(EAccessLevel.VIEW))).andReturn(
          viewRights).once();
    }
    if (editRights != null) {
      expect(builder.rightsAccess.hasAccessLevel(eq(spaceRef), eq(EAccessLevel.EDIT))).andReturn(
          editRights).once();
    }
  }

  @Test
  public void testGetDateRestrictions_none() {
    Set<DateMode> modes = Collections.emptySet();
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void testGetDateRestrictions_published() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED));
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_published_noEditRights() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED));
    Date date = new Date();
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_archived() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_archived_noEditRights() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_future() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals(getFutureQuery(date), ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_future_noEditRights() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void testGetDateRestrictions_published_archived() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getArchivedQuery(date) + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_published_archived_noEditRights() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.ARCHIVED));
    Date date = new Date();
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getArchivedQuery(date) + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_published_future() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getFutureQuery(date) + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_published_future_noEditRights() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals(getPublishedQuery(date), ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_archived_future() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.ARCHIVED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    String expectedQuery = "(" + getFutureQuery(date) + " OR " + getArchivedQuery(date) + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_archived_future_noEditRights() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.ARCHIVED, DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals(getArchivedQuery(date), ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_all() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.ARCHIVED,
        DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    assertEquals("", ret.getQueryString());
  }

  @Test
  public void testGetDateRestrictions_all_noEditRights() {
    Set<DateMode> modes = new HashSet<>(Arrays.asList(DateMode.PUBLISHED, DateMode.ARCHIVED,
        DateMode.FUTURE));
    Date date = new Date();
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getDateRestrictions(modes, date, hasEditRights);
    verifyDefault();
    String expectedQuery = "(" + getPublishedQuery(date) + " OR " + getArchivedQuery(date) + ")";
    assertEquals(expectedQuery, ret.getQueryString());
  }

  private String getArchivedQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_ARCHIVE + ":(["
        + "0" + " TO " + dateString + "])";
  }

  private String getPublishedQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return "(" + IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_PUBLISH + ":(["
        + "0" + " TO " + dateString + "]) AND "
        + IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_ARCHIVE + ":([" + dateString + " TO "
        + "zzzzzzzzzzzz" + "]))";
  }

  private String getFutureQuery(Date date) {
    String dateString = ILuceneSearchService.SDF.format(date);
    return IArticleLuceneQueryBuilderRole.ARTICLE_FIELD_PUBLISH + ":([" + dateString + " TO "
        + "zzzzzzzzzzzz" + "])";
  }

  @Test
  public void testGetArticleSubsRestrictions_none() {
    Set<SubscriptionMode> modes = Collections.emptySet();
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getSubsQuery(false), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getSubsQuery(false), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getUnsubsQuery(false), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED,
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getSubsUnsubsQuery(false), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_subscribed_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED,
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getSubsQuery(false), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNDECIDED));
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getSubsUnsubsQuery(true), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNDECIDED));
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_unsubscribed() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNDECIDED,
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getSubsQuery(true), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_unsubscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNDECIDED,
        SubscriptionMode.UNSUBSCRIBED));
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_subscribed() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNDECIDED,
        SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getUnsubsQuery(true), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_undecided_subscribed_noEditRights() {
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.UNDECIDED,
        SubscriptionMode.SUBSCRIBED));
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, docRef, hasEditRights);
    verifyDefault();
    assertEquals(getSubsQuery(false), ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_all() {
    DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED,
        SubscriptionMode.UNSUBSCRIBED, SubscriptionMode.UNDECIDED));
    boolean hasEditRights = true;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef,
        hasEditRights);
    verifyDefault();
    assertEquals("", ret.getQueryString());
  }

  @Test
  public void testGetArticleSubsRestrictions_all_noEditRights() {
    DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
    Set<SubscriptionMode> modes = new HashSet<>(Arrays.asList(SubscriptionMode.SUBSCRIBED,
        SubscriptionMode.UNSUBSCRIBED, SubscriptionMode.UNDECIDED));
    boolean hasEditRights = false;
    replayDefault();
    QueryRestrictionGroup ret = builder.getArticleSubsRestrictions(modes, blogConfDocRef,
        hasEditRights);
    verifyDefault();
    assertEquals(getSubsQuery(false), ret.getQueryString());
  }

  private String getSubsQuery(boolean not) {
    String ret = "(" + RESTR_ARTSUBS_OBJ + " AND " + RESTR_ARTSUBS_SUBS + " AND "
        + RESTR_ARTSUBS_SPACE + ")";
    if (not) {
      ret = "NOT " + ret;
    }
    return ret;
  }

  private String getUnsubsQuery(boolean not) {
    String ret = "(" + RESTR_ARTSUBS_OBJ + " AND " + RESTR_ARTSUBS_UNSUBS + " AND "
        + RESTR_ARTSUBS_SPACE + ")";
    if (not) {
      ret = "NOT " + ret;
    }
    return ret;
  }

  private String getSubsUnsubsQuery(boolean not) {
    String ret = "(" + RESTR_ARTSUBS_OBJ + " AND " + "(" + RESTR_ARTSUBS_SUBS + " OR "
        + RESTR_ARTSUBS_UNSUBS + ")" + " AND " + RESTR_ARTSUBS_SPACE + ")";
    if (not) {
      ret = "NOT " + ret;
    }
    return ret;
  }

}
