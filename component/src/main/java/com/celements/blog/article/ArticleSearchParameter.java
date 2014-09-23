package com.celements.blog.article;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xwiki.model.reference.DocumentReference;

// TODO javadoc for fields (see BlogPlugin)
public class ArticleSearchParameter {
  
  public enum SubscriptionMode {
    SUBSCRIBED, UNSUBSCRIBED, UNDECIDED;
  }
  
  public enum DateMode {
    PUBLISHED, ARCHIVED, FUTURE;
  }

  private static final Set<DateMode> DEFAULT_DATE_MODES = Collections.unmodifiableSet(
      new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED)));
  private static final Set<SubscriptionMode> DEFAULT_SUBSCRIPTION_MODES = 
      Collections.unmodifiableSet(new HashSet<SubscriptionMode>(Arrays.asList(
      SubscriptionMode.SUBSCRIBED)));
  private static final int DEFAULT_OFFSET = 0;
  private static final int DEFAULT_LIMIT = 0;

  private Date executionDate = new Date();
  private DocumentReference blogDocRef;
  private boolean withBlogArticles = true;
  private List<DocumentReference> subscribedToBlogs = Collections.emptyList();
  private Set<DateMode> dateModes = DEFAULT_DATE_MODES;
  private Set<SubscriptionMode> subsModes = DEFAULT_SUBSCRIPTION_MODES;
  private String language = null;
  private int offset = DEFAULT_OFFSET;
  private int limit = DEFAULT_LIMIT;
  private List<String> sortFields = Collections.emptyList();
  
  public ArticleSearchParameter() {
  }

  public Date getExecutionDate() {
    return executionDate;
  }

  public void setExecutionDate(Date executionDate) {
    this.executionDate = executionDate;
  }

  public DocumentReference getBlogDocRef() {
    return blogDocRef;
  }
  
  public ArticleSearchParameter setBlogDocRef(DocumentReference blogDocRef) {
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

  public ArticleSearchParameter setSubscribedToBlogs(List<DocumentReference> docRefs) {
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

  public ArticleSearchParameter setDateModes(Set<DateMode> dateModes) {
    if ((dateModes != null) && dateModes.size() > 0) {
      this.dateModes = Collections.unmodifiableSet(dateModes);
    } else {
      this.dateModes = DEFAULT_DATE_MODES;
    }
    return this;
  }

  public Set<SubscriptionMode> getSubscriptionModes() {
    return subsModes;
  }

  public ArticleSearchParameter setSubscriptionModes(Set<SubscriptionMode> subsModes) {
    if ((subsModes != null) && subsModes.size() > 0) {
      this.subsModes = Collections.unmodifiableSet(subsModes);
    } else {
      this.subsModes = DEFAULT_SUBSCRIPTION_MODES;
    }
    return this;
  }

  public String getLanguage() {
    return language;
  }

  public ArticleSearchParameter setLanguage(String language) {
    this.language = language;
    return this;
  }

  public int getOffset() {
    return offset;
  }

  public ArticleSearchParameter setOffset(int offset) {
    if (offset > 0) {
      this.offset = offset;
    } else {
      this.offset = DEFAULT_OFFSET;
    }
    return this;
  }

  public int getLimit() {
    return limit;
  }

  public ArticleSearchParameter setLimit(int limit) {
    if (limit > 0) {
      this.limit = limit;
    } else {
      this.limit = DEFAULT_LIMIT;
    }
    return this;
  }

  public List<String> getSortFields() {
    return sortFields;
  }

  public ArticleSearchParameter setSortFields(List<String> sortFields) {
    if (sortFields != null) {
      this.sortFields = Collections.unmodifiableList(sortFields);
    } else {
      this.sortFields = Collections.emptyList();
    }
    return this;
  }

  @Override
  public String toString() {
    return "ArticleSearchParameter [executionDate=" + executionDate + ", blogDocRef="
        + blogDocRef + ", withBlogArticles=" + withBlogArticles
        + ", subscribedToBlogs=" + subscribedToBlogs + ", dateModes=" + dateModes
        + ", subsModes=" + subsModes + ", language=" + language + ", offset=" + offset
        + ", limit=" + limit + ", sortFields=" + sortFields + "]";
  }

}
