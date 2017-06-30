package com.celements.blog.search;

import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
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
import com.google.common.collect.ImmutableSet;
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
  public Set<LuceneDocType> getDocTypes() {
    return ImmutableSet.of(LuceneDocType.DOC);
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    if (LuceneSearchService.DATE_PATTERN.matcher(searchTerm).matches()) {
      return searchService.createFieldRestriction(getArticleClassRef(),
          BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE, searchTerm, false);
    } else {
      QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
      grp.add(searchService.createFieldRestriction(getArticleClassRef(), "title", searchTerm));
      grp.add(searchService.createFieldRestriction(getArticleClassRef(), "extract", searchTerm));
      grp.add(searchService.createFieldRestriction(getArticleClassRef(), "content", searchTerm));
      return grp;
    }
  }

  @Override
  public Optional<DocumentReference> getLinkedClassRef() {
    return Optional.of(getArticleClassRef());
  }

  private DocumentReference getArticleClassRef() {
    return ((BlogClasses) blogClasses).getArticleClassRef(context.getWikiRef().getName());
  }

}
