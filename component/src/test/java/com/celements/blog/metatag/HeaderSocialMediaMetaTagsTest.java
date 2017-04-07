package com.celements.blog.metatag;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.MetaTagProviderRole;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class HeaderSocialMediaMetaTagsTest extends AbstractComponentTest {

  private HeaderSocialMediaMetaTags headerTags;
  private IBlogServiceRole blogServiceMock;

  @Before
  public void prepareTest() throws Exception {
    blogServiceMock = registerComponentMock(IBlogServiceRole.class);
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
    expect(blogServiceMock.getBlogConfigDocRef(same(
        doc.getDocumentReference().getLastSpaceReference()))).andReturn(docRef);
    replayDefault();
    assertTrue(headerTags.isBlogArticle());
    verifyDefault();
  }

  @Test
  public void testIsBlogArticle_false() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(blogServiceMock.getBlogConfigDocRef(same(
        doc.getDocumentReference().getLastSpaceReference()))).andReturn((DocumentReference) null);
    replayDefault();
    assertFalse(headerTags.isBlogArticle());
    verifyDefault();
  }

  @Test
  public void testIsBlogArticle_false_withException() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(blogServiceMock.getBlogConfigDocRef(same(
        doc.getDocumentReference().getLastSpaceReference()))).andThrow(new XWikiException());
    replayDefault();
    assertFalse(headerTags.isBlogArticle());
    verifyDefault();
  }

}
