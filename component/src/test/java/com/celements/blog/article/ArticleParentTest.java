package com.celements.blog.article;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.parents.IDocParentProviderRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class ArticleParentTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private IBlogServiceRole blogServiceMock;
  private ArticleParent articleParentProvider;

  @Before
  public void setUp_ArticleParentTest() throws Exception {
    context = getContext();
    blogServiceMock = createMockAndAddToDefault(IBlogServiceRole.class);
    articleParentProvider = (ArticleParent) Utils.getComponent(IDocParentProviderRole.class,
        ArticleParent.DOC_PROVIDER_NAME);
    articleParentProvider.injectBlogService(blogServiceMock);
  }

  @Test
  public void getDocumentParentsList_default() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MyBlog",
        "ArticleChild");
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "content",
        "BlogParent");
    ArrayList<DocumentReference> expectedDocParents = new ArrayList<DocumentReference>();
    expectedDocParents.add(parentDocRef);
    expect(blogServiceMock.getBlogConfigDocRef(eq(docRef.getLastSpaceReference()))).andReturn(
        parentDocRef).once();
    replayDefault();
    List<DocumentReference> docParents = articleParentProvider.getDocumentParentsList(docRef);
    assertEquals(1, docParents.size());
    assertEquals(expectedDocParents, docParents);
    verifyDefault();
  }

  @Test
  public void getDocumentParentsList_empty() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyBlog",
        "Article1");
    ArrayList<DocumentReference> expectedDocParents = new ArrayList<DocumentReference>();
    expect(blogServiceMock.getBlogConfigDocRef(eq(docRef.getLastSpaceReference()))).andReturn(
        null).anyTimes();
    replayDefault();
    List<DocumentReference> docParents = articleParentProvider.getDocumentParentsList(docRef);
    assertEquals(0, docParents.size());
    assertEquals(expectedDocParents, docParents);
    verifyDefault();
  }

}
