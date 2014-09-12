package com.celements.blog.article;

import java.util.Collections;
import java.util.List;

import org.xwiki.model.reference.SpaceReference;

import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.xpn.xwiki.web.Utils;

// TODO javadoc for fields (see BlogPlugin)
public class ArticleSearchQuery {
  
  private ILuceneSearchService searchService;

  private final SpaceReference blogSpaceRef;
  private final String database;

  // TODO define default values
  private int offset = 0;
  private int limit = 0;
  private List<String> sortFields = Collections.emptyList();
  private boolean skipChecks = false;
  private List<SpaceReference> subscribedBlogs = Collections.emptyList();
  private String language = null;
  private boolean archiveOnly;
  private boolean futurOnly;
  private boolean subscribableOnly;
  private boolean withArchive;
  private boolean withFutur;
  private boolean withSubscribable;
  private boolean withSubscribed;
  private boolean withUnsubscribed;
  private boolean withUndecided;
  
  public ArticleSearchQuery(String database, SpaceReference blogSpaceRef) {
    this.database = database;
    this.blogSpaceRef = blogSpaceRef;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public List<String> getSortFields() {
    return sortFields;
  }

  public void setSortFields(List<String> sortFields) {
    if (sortFields == null) {
      this.sortFields = Collections.emptyList();
    } else {
      this.sortFields = Collections.unmodifiableList(sortFields);
    }
  }

  public boolean isSkipChecks() {
    return skipChecks;
  }

  public void setSkipChecks(boolean skipChecks) {
    this.skipChecks = skipChecks;
  }

  public List<SpaceReference> getSubscribedBlogs() {
    return subscribedBlogs;
  }

  public void setSubscribedBlogs(List<SpaceReference> subscribedBlogs) {
    if (subscribedBlogs == null) {
      subscribedBlogs = Collections.emptyList();
    }
    this.subscribedBlogs = Collections.unmodifiableList(subscribedBlogs);
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public boolean isArchiveOnly() {
    return archiveOnly;
  }

  public void setArchiveOnly(boolean archiveOnly) {
    this.archiveOnly = archiveOnly;
  }

  public boolean isFuturOnly() {
    return futurOnly;
  }

  public void setFuturOnly(boolean futurOnly) {
    this.futurOnly = futurOnly;
  }

  public boolean isSubscribableOnly() {
    return subscribableOnly;
  }

  public void setSubscribableOnly(boolean subscribableOnly) {
    this.subscribableOnly = subscribableOnly;
  }

  public boolean isWithArchive() {
    return withArchive;
  }

  public void setWithArchive(boolean withArchive) {
    this.withArchive = withArchive;
  }

  public boolean isWithFutur() {
    return withFutur;
  }

  public void setWithFutur(boolean withFutur) {
    this.withFutur = withFutur;
  }

  public boolean isWithSubscribable() {
    return withSubscribable;
  }

  public void setWithSubscribable(boolean withSubscribable) {
    this.withSubscribable = withSubscribable;
  }

  public boolean isWithSubscribed() {
    return withSubscribed;
  }

  public void setWithSubscribed(boolean withSubscribed) {
    this.withSubscribed = withSubscribed;
  }

  public boolean isWithUnsubscribed() {
    return withUnsubscribed;
  }

  public void setWithUnsubscribed(boolean withUnsubscribed) {
    this.withUnsubscribed = withUnsubscribed;
  }

  public boolean isWithUndecided() {
    return withUndecided;
  }

  public void setWithUndecided(boolean withUndecided) {
    this.withUndecided = withUndecided;
  }

  public SpaceReference getBlogSpaceRef() {
    return blogSpaceRef;
  }

  public String getDatabase() {
    return database;
  }

  public LuceneQueryApi getAsLuceneQuery() {
    LuceneQueryApi query = getSearchService().createQuery(getDatabase());
    // TODO
    return query;
  }
  
  @Override
  public String toString() {
    // TODO
    return getAsLuceneQuery().getQueryString();
  }
  
  private ILuceneSearchService getSearchService() {
    if (searchService == null) {
      searchService = Utils.getComponent(ILuceneSearchService.class);
    }
    return searchService;
  }

}
