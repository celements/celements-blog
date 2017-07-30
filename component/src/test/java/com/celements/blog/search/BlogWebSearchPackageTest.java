package com.celements.blog.search;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;

import com.celements.blog.plugin.BlogClasses;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.LuceneUtils;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.base.Joiner;
import com.xpn.xwiki.web.Utils;

public class BlogWebSearchPackageTest extends AbstractComponentTest {

  BlogWebSearchPackage webSearchPackage;

  @Before
  public void prepareTest() throws Exception {
    webSearchPackage = (BlogWebSearchPackage) Utils.getComponent(WebSearchPackage.class,
        BlogWebSearchPackage.NAME);
  }

  @Test
  public void test_getName() {
    assertEquals(BlogWebSearchPackage.NAME, webSearchPackage.getName());
  }

  @Test
  public void test_isDefault() {
    assertTrue(webSearchPackage.isDefault());
  }

  @Test
  public void test_isRequired() {
    assertFalse(webSearchPackage.isRequired(null));
  }

  @Test
  public void test_getDocType() {
    assertSame(LuceneDocType.DOC, webSearchPackage.getDocType());
  }

  @Test
  public void test_getQueryRestriction_empty() {
    String searchTerm = "";
    IQueryRestriction restriction = webSearchPackage.getQueryRestriction(null, searchTerm);
    assertNotNull(restriction);
    assertEquals(String.format("XWiki.ArticleClass.lang:(+%s*)", getContext().getLanguage()),
        restriction.getQueryString());
  }

  @Test
  public void test_getQueryRestriction_date() {
    String searchTerm = "201708010830";
    IQueryRestriction restriction = webSearchPackage.getQueryRestriction(null, searchTerm);
    assertNotNull(restriction);
    assertEquals(String.format("(XWiki.ArticleClass.lang:(+%s*) AND "
        + "XWiki.ArticleClass.publishdate:(%s))", getContext().getLanguage(), searchTerm),
        restriction.getQueryString());
  }

  @Test
  public void test_getQueryRestriction_text_exact() {
    String searchTerm = LuceneUtils.exactify("find me");
    IQueryRestriction restriction = webSearchPackage.getQueryRestriction(null, searchTerm);
    assertNotNull(restriction);
    assertEquals(String.format("(XWiki.ArticleClass.lang:(+%s*) AND "
        + "(XWiki.ArticleClass.title:(+%s) OR XWiki.ArticleClass.extract:(+%s) OR "
        + "XWiki.ArticleClass.content:(+%s)))", getContext().getLanguage(), searchTerm, searchTerm,
        searchTerm), restriction.getQueryString());
  }

  @Test
  public void test_getQueryRestriction_text_tokenized() {
    String searchTerm1 = "find";
    String searchTerm2 = "us";
    IQueryRestriction restriction = webSearchPackage.getQueryRestriction(null, Joiner.on(' ').join(
        searchTerm1, searchTerm2));
    assertNotNull(restriction);
    assertEquals(String.format("(XWiki.ArticleClass.lang:(+%s*) AND "
        + "(XWiki.ArticleClass.title:(+%s* +%s*) OR XWiki.ArticleClass.extract:(+%s* +%s*) OR "
        + "XWiki.ArticleClass.content:(+%s* +%s*)))", getContext().getLanguage(), searchTerm1,
        searchTerm2, searchTerm1, searchTerm2, searchTerm1, searchTerm2),
        restriction.getQueryString());
  }

  @Test
  public void test_getLinkedClassRef() {
    assertEquals(new ClassReference(BlogClasses.ARTICLE_CLASS_SPACE, BlogClasses.ARTICLE_CLASS_DOC),
        webSearchPackage.getLinkedClassRef().get());
  }

}
