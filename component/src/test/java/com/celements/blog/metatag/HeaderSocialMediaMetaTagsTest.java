package com.celements.blog.metatag;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.MetaTagProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class HeaderSocialMediaMetaTagsTest extends AbstractComponentTest {

  private HeaderSocialMediaMetaTags headerTags;
  private IPageTypeResolverRole ptResolverMock;

  @Before
  public void prepareTest() throws Exception {
    ptResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    headerTags = (HeaderSocialMediaMetaTags) Utils.getComponent(MetaTagProviderRole.class,
        HeaderSocialMediaMetaTags.COMPONENT_NAME);
  }

  @Test
  public void testGetBodyMetaTags() {
    assertEquals(0, headerTags.getBodyMetaTags().size());
  }

  @Test
  public void testIsBlogArticle_true() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(ptResolverMock.getPageTypeRefForDoc(same(doc))).andReturn(new PageTypeReference(
        "Article", "provider", Arrays.asList("cat")));
    replayDefault();
    assertTrue(headerTags.isBlogArticle());
    verifyDefault();
  }

  @Test
  public void testIsBlogArticle_false_noPt() {
    getContext().setDoc(new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc")));
    assertFalse(headerTags.isBlogArticle());
  }

  @Test
  public void testIsBlogArticle_false_withPt() {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(ptResolverMock.getPageTypeRefForDoc(same(doc))).andReturn(new PageTypeReference(
        "RichText", "provider", Arrays.asList("cat")));
    replayDefault();
    assertFalse(headerTags.isBlogArticle());
    verifyDefault();
  }

}
