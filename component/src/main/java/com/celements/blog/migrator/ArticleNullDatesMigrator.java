package com.celements.blog.migrator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.blog.plugin.BlogClasses;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.migrator.AbstractCelementsHibernateMigrator;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component("ArticleNullDates")
public class ArticleNullDatesMigrator extends AbstractCelementsHibernateMigrator {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      ArticleNullDatesMigrator.class);

  @Requirement
  private QueryManager queryManager;
  
  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  public String getName() {
    return "ArticleNullDates";
  }

  @Override
  public String getDescription() {
    return "Sets null publish and archive dates for Articles to a value";
  }

  /**
   * getVersion is using days since
   * 1.1.2010 until the day of committing this migration
   * 19.09.2014 -> 1722
   * http://www.wolframalpha.com/input/?i=days+since+01.01.2010
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(1722);
  }


  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context
      ) throws XWikiException {
    try {
      List<String> results = queryManager.createQuery(getXWQL(), Query.XWQL).execute();
      LOGGER.debug("Got results for xwql '" + getXWQL() + "': " + results);
      if (results != null) {
        for (String fullName : results) {
          DocumentReference docRef = webUtilsService.resolveDocumentReference(fullName);
          context.getWiki().saveDocument(context.getWiki().getDocument(docRef, context), 
              "article null dates migration", context);
        }
      }
    } catch (QueryException qexc) {
      LOGGER.error("Exception executing query: " + getXWQL(), qexc);
    }
  }
  
  String getXWQL() {
    return "from doc.object(" + BlogClasses.ARTICLE_CLASS + ") art where art." 
        + BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE + " is null or art." 
        + BlogClasses.PROPERTY_ARTICLE_ARCHIVE_DATE + " is null";
  }

  void injectQueryManager(QueryManager queryManager) {
    this.queryManager = queryManager;
  }
  
}
