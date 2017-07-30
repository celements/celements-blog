package com.celements.blog.search;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.model.context.ModelContext;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(BlogWebSearchPackage.NAME)
public class BlogWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "blog";

  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ModelContext context;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public boolean isRequired(XWikiDocument cfgDoc) {
    return false;
  }

  @Override
  public LuceneDocType getDocType() {
    return LuceneDocType.DOC;
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.AND);
    grp.add(searchService.createFieldRestriction(getArticleClassDocRef(),
        BlogClasses.PROPERTY_ARTICLE_LANG, context.getXWikiContext().getLanguage()));
    if (LuceneSearchService.DATE_PATTERN.matcher(searchTerm).matches()) {
      grp.add(searchService.createFieldRestriction(getArticleClassDocRef(),
          BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE, searchTerm, false));
    } else {
      QueryRestrictionGroup orGrp = searchService.createRestrictionGroup(Type.OR);
      orGrp.add(searchService.createFieldRestriction(getArticleClassDocRef(), "title", searchTerm));
      orGrp.add(searchService.createFieldRestriction(getArticleClassDocRef(), "extract",
          searchTerm));
      orGrp.add(searchService.createFieldRestriction(getArticleClassDocRef(), "content",
          searchTerm));
      grp.add(orGrp);
    }
    return grp;
  }

  @Override
  public Optional<ClassReference> getLinkedClassRef() {
    return Optional.of(new ClassReference(getArticleClassDocRef()));
  }

  DocumentReference getArticleClassDocRef() {
    return ((BlogClasses) blogClasses).getArticleClassRef(context.getWikiRef().getName());
  }

}
