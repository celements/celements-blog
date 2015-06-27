package com.celements.blog.cache;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.easymock.IExpectationSetters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.cache.CacheLoadingException;
import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.query.IQueryExecutionServiceRole;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class BlogCacheTest extends AbstractBridgedComponentTestCase {

  private BlogCache cache;
  private QueryManager queryManagerMock;
  private IQueryExecutionServiceRole queryExecServiceMock;

  @Before
  public void setUp_BlogCacheTest() {
    cache = (BlogCache) Utils.getComponent(IDocumentReferenceCache.class, BlogCache.NAME);
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    cache.injectQueryManager(queryManagerMock);
    queryExecServiceMock = createMockAndAddToDefault(IQueryExecutionServiceRole.class);
    cache.injectQueryExecService(queryExecServiceMock);
    cache.blogService = createMockAndAddToDefault(IBlogServiceRole.class);
  }

  @After
  public void tearDown_BlogCacheTest() {
    cache.injectQueryManager(Utils.getComponent(QueryManager.class));
    cache.injectQueryExecService(Utils.getComponent(IQueryExecutionServiceRole.class));
    cache.blogService = Utils.getComponent(IBlogServiceRole.class);
  }

  @Test
  public void testGetCachedDocRefs() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef1 = new SpaceReference("blogSpace1", wikiRef);
    SpaceReference spaceRef2 = new SpaceReference("blogSpace2", wikiRef);
    SpaceReference spaceRef3 = new SpaceReference("blogSpace3", wikiRef);
    DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "blog1");
    DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "blog2");
    
    expectXWQL(wikiRef, Arrays.asList(docRef1, docRef2));
    expect(cache.blogService.getBlogSpaceRef(eq(docRef1))).andReturn(spaceRef1).once();
    expect(cache.blogService.getBlogSpaceRef(eq(docRef2))).andReturn(spaceRef2).once();

    replayDefault();
    assertEquals(ImmutableSet.of(docRef1), cache.getCachedDocRefs(spaceRef1));
    assertEquals(ImmutableSet.of(docRef2), cache.getCachedDocRefs(spaceRef2));
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(spaceRef3));
    assertEquals(ImmutableSet.of(docRef1, docRef2), cache.getCachedDocRefs(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_multiple() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
    DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "blog1");
    DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "blog2");
    
    expectXWQL(wikiRef, Arrays.asList(docRef1, docRef2));
    expect(cache.blogService.getBlogSpaceRef(eq(docRef1))).andReturn(spaceRef).once();
    expect(cache.blogService.getBlogSpaceRef(eq(docRef2))).andReturn(spaceRef).once();

    replayDefault();
    assertEquals(ImmutableSet.of(docRef1, docRef2), cache.getCachedDocRefs(spaceRef));
    assertEquals(ImmutableSet.of(docRef1, docRef2), cache.getCachedDocRefs(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_empty() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
    
    expectXWQL(wikiRef, Collections.<DocumentReference>emptyList());

    replayDefault();
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(spaceRef));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_multiWiki() throws Exception {
    WikiReference wikiRef1 = new WikiReference("wiki1");
    SpaceReference spaceRef1 = new SpaceReference("blogSpace1", wikiRef1);
    DocumentReference docRef1 = new DocumentReference(wikiRef1.getName(), "space", "blog1");
    WikiReference wikiRef2 = new WikiReference("wiki2");
    SpaceReference spaceRef2 = new SpaceReference("blogSpace2", wikiRef2);
    DocumentReference docRef2 = new DocumentReference(wikiRef2.getName(), "space", "blog2");
    SpaceReference spaceRef3 = new SpaceReference("blogSpace3", wikiRef1);
    
    expectXWQL(wikiRef1, Arrays.asList(docRef1));
    expect(cache.blogService.getBlogSpaceRef(eq(docRef1))).andReturn(spaceRef1).once();
    expectXWQL(wikiRef2, Arrays.asList(docRef2));
    expect(cache.blogService.getBlogSpaceRef(eq(docRef2))).andReturn(spaceRef2).once();

    replayDefault();
    assertEquals(ImmutableSet.of(docRef1), cache.getCachedDocRefs(spaceRef1));
    assertEquals(ImmutableSet.of(docRef2), cache.getCachedDocRefs(spaceRef2));
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(spaceRef3));
    assertEquals(ImmutableSet.of(docRef1), cache.getCachedDocRefs(wikiRef1));
    assertEquals(ImmutableSet.of(docRef2), cache.getCachedDocRefs(wikiRef2));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_nullKey() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    
    expectXWQL(wikiRef, Arrays.asList(docRef));
    expect(cache.blogService.getBlogSpaceRef(eq(docRef))).andReturn(null).once();

    replayDefault();
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_QueryException() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
    
    expectXWQL(wikiRef, null);

    replayDefault();
    try {
      cache.getCachedDocRefs(spaceRef);
      fail("expecting CacheLoadingException");
    } catch (CacheLoadingException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_XWikiException() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog1");
    
    expectXWQL(wikiRef, Arrays.asList(docRef));
    expect(cache.blogService.getBlogSpaceRef(eq(docRef))).andThrow(new XWikiException()
        ).once();

    replayDefault();
    try {
      cache.getCachedDocRefs(spaceRef);
      fail("expecting CacheLoadingException");
    } catch (CacheLoadingException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testFlush() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
    WikiReference otherWikiRef = new WikiReference("otherWiki");
    
    expectXWQL(wikiRef, Arrays.asList(docRef));
    expectXWQL(wikiRef, Arrays.asList(docRef));
    expect(cache.blogService.getBlogSpaceRef(eq(docRef))).andReturn(spaceRef).times(2);

    replayDefault();
    assertEquals(ImmutableSet.of(docRef), cache.getCachedDocRefs(spaceRef));
    cache.flush(wikiRef); // this flushes docRef
    assertEquals(ImmutableSet.of(docRef), cache.getCachedDocRefs(spaceRef));
    cache.flush(otherWikiRef); // this doesnt flush docRef
    assertEquals(ImmutableSet.of(docRef), cache.getCachedDocRefs(spaceRef));
    verifyDefault();
  }

  private void expectXWQL(WikiReference wikiRef, List<DocumentReference> ret
      ) throws Exception {
    Query queryMock = createMockAndAddToDefault(Query.class);
    String xwql = "select distinct doc.fullName from Document doc, doc.object("
        + "Celements2.BlogConfigClass) as obj";
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.setWiki(eq(wikiRef.getName()))).andReturn(queryMock).once();
    IExpectationSetters<List<DocumentReference>> expSetter = expect(
        queryExecServiceMock.executeAndGetDocRefs(same(queryMock)));
    if (ret != null) {
      expSetter.andReturn(ret).once();
    } else {
      expSetter.andThrow(new QueryException("", null, null)).once();
    }
  }

}
