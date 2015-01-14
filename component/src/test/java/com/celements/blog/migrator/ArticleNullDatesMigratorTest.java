package com.celements.blog.migrator;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryExecutor;
import org.xwiki.query.QueryManager;
import org.xwiki.query.internal.DefaultQuery;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.migrations.celSubSystem.ICelementsMigrator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class ArticleNullDatesMigratorTest extends AbstractBridgedComponentTestCase {
  
  private ArticleNullDatesMigrator migrator;
  
  private XWikiContext context;
  private XWiki xwiki;
  private QueryManager queryManagerMock;
  private QueryExecutor queryExecutorMock;

  @Before
  public void setUp_ArticleNullDatesMigratorTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    migrator = (ArticleNullDatesMigrator) Utils.getComponent(ICelementsMigrator.class, 
        "ArticleNullDates");
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    queryExecutorMock = createMockAndAddToDefault(QueryExecutor.class);
    migrator.injectQueryManager(queryManagerMock);
  }

  @Test
  public void testGetName() {
    assertEquals("ArticleNullDates", migrator.getName());
  }
  
  @Test
  public void testGetXWQL() {
    assertEquals("from doc.object(XWiki.ArticleClass) art where art.publishdate is null "
        + "or art.archivedate is null", migrator.getXWQL());
  }
  
  @Test
  public void testMigrate() throws Exception {
    Query query = new DefaultQuery("", queryExecutorMock);    
    expect(queryManagerMock.createQuery(eq(migrator.getXWQL()), eq("xwql"))).andReturn(
        query).once();
    expect(queryExecutorMock.execute(same(query))).andReturn(Arrays.<Object>asList(
        "space.article1", "space.article2")).once();
    List<DocumentReference> docRefs = Arrays.asList(new DocumentReference("xwikidb", 
        "space", "article1"), new DocumentReference("xwikidb", "space", "article2"));
    XWikiDocument doc1 = new XWikiDocument(docRefs.get(0));
    expect(xwiki.getDocument(eq(docRefs.get(0)), same(context))).andReturn(doc1).once();
    xwiki.saveDocument(same(doc1), eq("article null dates migration"), same(context));
    expectLastCall().once();
    XWikiDocument doc2 = new XWikiDocument(docRefs.get(1));
    expect(xwiki.getDocument(eq(docRefs.get(1)), same(context))).andReturn(doc2).once();
    xwiki.saveDocument(same(doc2), eq("article null dates migration"), same(context));
    expectLastCall().once();
    
    replayDefault();
    migrator.migrate(null, context);
    verifyDefault();
  }
  
}
