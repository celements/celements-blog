package com.celements.blog.article;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryException;

import com.celements.blog.service.IBlogServiceRole;
import com.celements.parents.IDocParentProviderRole;
import com.xpn.xwiki.XWikiException;

@Component(ArticleParent.DOC_PROVIDER_NAME)
public class ArticleParent implements IDocParentProviderRole {
  
  private static Logger _LOGGER = LoggerFactory.getLogger(ArticleParent.class);

  public static final String DOC_PROVIDER_NAME = "celblog";
  
  @Requirement
  private IBlogServiceRole blogService;

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef) {
    ArrayList<DocumentReference> docParents = new ArrayList<DocumentReference>();
    try {
      DocumentReference nextParent = blogService.getBlogConfigDocRef(
          docRef.getLastSpaceReference());
      if(nextParent != null) {
        docParents.add(nextParent);
      }
    } catch (QueryException exp) {
      _LOGGER.error("Failed to get parent reference. ", exp);
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get parent reference. ", exp);
    }
    return docParents;
  }
  
  void injectBlogService(IBlogServiceRole blogService) {
    this.blogService = blogService;
  }

}
