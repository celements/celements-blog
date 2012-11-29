package com.celements.blog.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class BlogService implements IBlogServiceRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(BlogService.class);

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  QueryManager queryManager;

  @Requirement
  Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName) {
    try {
      String xwql = "from doc.object(Celements2.BlogConfigClass) as bconfig";
      xwql += " where bconfig.blogspace = :blogSpaceName";
      Query query = queryManager.createQuery(xwql, Query.XWQL);
      query.bindValue("blogSpaceName", blogSpaceName);
      List<String> blogList = query.setLimit(1).execute();
      if (blogList.size() > 0) {
        DocumentReference blogDocRef = webUtilsService.resolveDocumentReference(
            blogList.get(0));
        if (getContext().getWiki().exists(blogDocRef, getContext())) {
          return blogDocRef;
        }
      }
    } catch (QueryException queryExp) {
      LOGGER.error("Failed to parse xwql query to get BlogDocRef for blog space ["
          + blogSpaceName + "].", queryExp);
    }
    return null;
  }

  public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName) {
    DocumentReference blogDocRef = getBlogDocRefByBlogSpace(blogSpaceName);
    if (blogDocRef != null) {
      try {
        return getContext().getWiki().getDocument(blogDocRef, getContext());
      } catch (XWikiException exp) {
        LOGGER.error("Failed to get blog document for blog space [" + blogSpaceName
            + "].", exp);
      }
    }
    return null;
  }

}
