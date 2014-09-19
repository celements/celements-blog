package com.celements.blog.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryManager;

import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.migrator.AbstractCelementsHibernateMigrator;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component("AddBlogDateValidationMigrator")
public class AddBlogDateValidationMigrator extends AbstractCelementsHibernateMigrator {
  
  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      AddBlogDateValidationMigrator.class);
  
  @Requirement
  private QueryManager queryManager;
  
  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;
  
  @Requirement
  private IWebUtilsService webUtilsService;

  public String getName() {
    return "AddBlogDateValidationMigrator";
  }

  public String getDescription() {
    return "Add a RegExp to the Datefields and change DateFormat";
  }

  /**
   * getVersion is using days since 1.1.2010 until the day of committing this
   * migration 28.08.2014 -> 1713
   * http://www.convertunits.com/dates/from/Jan+1,+2010/to/Sep+10,+2014
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(1713);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    XWikiDocument doc = context.getWiki().getDocument(((BlogClasses) blogClasses
        ).getArticleClassRef(context.getDatabase()), context);
    BaseClass bClass = doc.getXClass();
    DateClass publishDateElement = (DateClass) bClass.get("publishdate");
    DateClass archiveDateElement = (DateClass) bClass.get("archivedate");
    publishDateElement.setValidationRegExp(
        "/^((0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4}) " +
        "([01][0-9]|2[0-4])(\\:[0-5][0-9]))$/");
    publishDateElement.setValidationMessage("cel_blog_validation_publishdate");
    publishDateElement.setDateFormat("dd.MM.yyyy HH:mm");
    archiveDateElement.setValidationRegExp(
        "/(^$)|^((0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4}) " +
        "([01][0-9]|2[0-4])(\\:[0-5][0-9]))$/");
    archiveDateElement.setValidationMessage("cel_blog_validation_archivedate");
    archiveDateElement.setDateFormat("dd.MM.yyyy HH:mm");
    context.getWiki().saveDocument(doc, context);
  }
  
}