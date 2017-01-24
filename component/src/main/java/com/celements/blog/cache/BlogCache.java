package com.celements.blog.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.QueryManager;

import com.celements.blog.plugin.BlogClasses;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.cache.AbstractDocumentReferenceCache;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

@Component(BlogCache.NAME)
public class BlogCache extends AbstractDocumentReferenceCache<SpaceReference> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlogCache.class);

  public static final String NAME = "BlogCache";

  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;

  /*
   * not loaded as requirement due to cyclic dependency
   */
  IBlogServiceRole blogService;

  @Override
  protected DocumentReference getCacheClassRef(WikiReference wikiRef) {
    return ((BlogClasses) blogClasses).getBlogConfigClassRef(wikiRef.getName());
  }

  @Override
  protected Collection<SpaceReference> getKeysForResult(DocumentReference blogConfDocRef)
      throws XWikiException {
    SpaceReference spaceRef = getBlogService().getBlogSpaceRef(blogConfDocRef);
    if (spaceRef != null) {
      return Arrays.asList(spaceRef);
    } else {
      return Collections.emptyList();
    }
  }

  private IBlogServiceRole getBlogService() {
    if (blogService == null) {
      blogService = Utils.getComponent(IBlogServiceRole.class);
    }
    return blogService;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  void injectQueryManager(QueryManager queryManager) {
    this.queryManager = queryManager;
  }

  void injectQueryExecService(IQueryExecutionServiceRole queryExecService) {
    this.queryExecService = queryExecService;
  }

}
