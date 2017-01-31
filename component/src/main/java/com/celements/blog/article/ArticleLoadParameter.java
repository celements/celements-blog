package com.celements.blog.article;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.plugin.BlogClasses;

// TODO javadoc for fields (also note if one isn't regarded by an engine, e.g. offset,
// limit, sortFields not regarded in ArticleEngineHQL)
public class ArticleLoadParameter {

  public enum SubscriptionMode {
    SUBSCRIBED,
    UNSUBSCRIBED,
    UNDECIDED;
  }

  public enum DateMode {
    PUBLISHED,
    ARCHIVED,
    FUTURE;
  }

  // TODO test default
  private Date executionDate = new Date();
  private DocumentReference blogDocRef;
  private boolean withBlogArticles = true;
  private List<DocumentReference> subscribedToBlogs = Collections.emptyList();
  private Set<DateMode> dateModes = Collections.unmodifiableSet(new HashSet<DateMode>(Arrays.asList(
      DateMode.PUBLISHED)));
  private Set<SubscriptionMode> subsModes = Collections.emptySet();
  private String language = null;
  private String searchTerm = null;

  private int offset = 0;
  private int limit = 0;
  private List<String> sortFields = Arrays.asList("-" + BlogClasses.ARTICLE_CLASS + "."
      + BlogClasses.PROPERTY_ARTICLE_PUBLISH_DATE, "name");

  public ArticleLoadParameter() {
  }

  public Date getExecutionDate() {
    return executionDate;
  }

  public void setExecutionDate(Date executionDate) {
    if (executionDate != null) {
      this.executionDate = executionDate;
    } else {
      this.executionDate = new Date();
    }
  }

  public DocumentReference getBlogDocRef() {
    return blogDocRef;
  }

  public ArticleLoadParameter setBlogDocRef(DocumentReference blogDocRef) {
    this.blogDocRef = blogDocRef;
    return this;
  }

  public boolean isWithBlogArticles() {
    return withBlogArticles;
  }

  public void setWithBlogArticles(boolean withBlogArticles) {
    this.withBlogArticles = withBlogArticles;
  }

  public List<DocumentReference> getSubscribedToBlogs() {
    return subscribedToBlogs;
  }

  public ArticleLoadParameter setSubscribedToBlogs(List<DocumentReference> docRefs) {
    if (docRefs != null) {
      this.subscribedToBlogs = Collections.unmodifiableList(docRefs);
    } else {
      this.subscribedToBlogs = Collections.emptyList();
    }
    return this;
  }

  public Set<DateMode> getDateModes() {
    return dateModes;
  }

  public ArticleLoadParameter setDateModes(Set<DateMode> dateModes) {
    this.dateModes = Collections.unmodifiableSet(dateModes);
    return this;
  }

  public ArticleLoadParameter setDateModes(List<String> modeStrs) {
    Set<DateMode> modes = new HashSet<>();
    for (String modeStr : modeStrs) {
      modes.add(DateMode.valueOf(modeStr.toUpperCase()));
    }
    return setDateModes(modes);
  }

  public Set<SubscriptionMode> getSubscriptionModes() {
    return subsModes;
  }

  public ArticleLoadParameter setSubscriptionModes(Set<SubscriptionMode> subsModes) {
    this.subsModes = Collections.unmodifiableSet(subsModes);
    return this;
  }

  public ArticleLoadParameter setSubscriptionModes(List<String> modeStrs) {
    Set<SubscriptionMode> modes = new HashSet<>();
    for (String modeStr : modeStrs) {
      modes.add(SubscriptionMode.valueOf(modeStr.toUpperCase()));
    }
    return setSubscriptionModes(modes);
  }

  public String getLanguage() {
    return language;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public ArticleLoadParameter setLanguage(String language) {
    this.language = language;
    return this;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  public int getOffset() {
    return offset;
  }

  public ArticleLoadParameter setOffset(int offset) {
    if (offset > 0) {
      this.offset = offset;
    } else {
      this.offset = 0;
    }
    return this;
  }

  public int getLimit() {
    return limit;
  }

  public ArticleLoadParameter setLimit(int limit) {
    if (limit > 0) {
      this.limit = limit;
    } else {
      this.limit = 0;
    }
    return this;
  }

  public List<String> getSortFields() {
    return sortFields;
  }

  public ArticleLoadParameter setSortFields(List<String> sortFields) {
    if (sortFields != null) {
      this.sortFields = Collections.unmodifiableList(sortFields);
    } else {
      this.sortFields = Collections.emptyList();
    }
    return this;
  }

  @Override
  public String toString() {
    return "ArticleLoadParameter [executionDate=" + executionDate + ", blogDocRef=" + blogDocRef
        + ", withBlogArticles=" + withBlogArticles + ", subscribedToBlogs=" + subscribedToBlogs
        + ", dateModes=" + dateModes + ", subsModes=" + subsModes + ", language=" + language
        + ", offset=" + offset + ", limit=" + limit + ", sortFields=" + sortFields + ", searchTerm="
        + searchTerm + "]";
  }

}
