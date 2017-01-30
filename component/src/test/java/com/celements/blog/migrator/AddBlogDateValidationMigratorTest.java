package com.celements.blog.migrator;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.migrations.celSubSystem.ICelementsMigrator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.web.Utils;

public class AddBlogDateValidationMigratorTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;

  private AddBlogDateValidationMigrator migrator;

  @Before
  public void setUp_DocumentMetaDataMigratorTest() {
    context = getContext();
    xwiki = getWikiMock();
    context.setWiki(xwiki);
    migrator = (AddBlogDateValidationMigrator) Utils.getComponent(ICelementsMigrator.class,
        "AddBlogDateValidationMigrator");
  }

  @Test
  public void testGetName() {
    assertEquals("AddBlogDateValidationMigrator", migrator.getName());
  }

  @Test
  public void testMigrate() throws Exception {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "XWiki",
        "ArticleClass");
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(docMock).once();

    BaseClass bClass = new BaseClass();
    bClass.addDateField("publishdate", "publishdate", "dd.MM.yyyy HH:mm", 0);
    bClass.addDateField("archivedate", "archivedate", "dd.MM.yyyy HH:mm", 0);
    expect(docMock.getXClass()).andReturn(bClass).once();

    xwiki.saveDocument(docMock, context);
    expectLastCall();

    replayDefault();
    migrator.migrate(null, context);
    verifyDefault();

    assertTrue(((DateClass) bClass.get("publishdate")).getValidationRegExp().length() > 0);
    assertTrue(((DateClass) bClass.get("archivedate")).getValidationRegExp().length() > 0);
    assertEquals("cel_blog_validation_publishdate", ((DateClass) bClass.get(
        "publishdate")).getValidationMessage());
    assertEquals("cel_blog_validation_archivedate", ((DateClass) bClass.get(
        "archivedate")).getValidationMessage());
    assertEquals(((DateClass) bClass.get("publishdate")).getDateFormat(), "dd.MM.yyyy HH:mm");
    assertEquals(((DateClass) bClass.get("archivedate")).getDateFormat(), "dd.MM.yyyy HH:mm");
  }

}
