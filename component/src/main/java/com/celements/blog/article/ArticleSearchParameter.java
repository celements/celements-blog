package com.celements.blog.article;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

// TODO javadoc for fields (see BlogPlugin)
public class ArticleSearchParameter {
  
  public enum SubscriptionMode {
    BLOG, SUBSCRIBED, UNSUBSCRIBED, UNDECIDED;
  }
  
  public enum DateMode {
    PUBLISHED, ARCHIVED, FUTURE;
  }

  private static final int DEFAULT_OFFSET = 0;
  private static final int DEFAULT_LIMIT = 0;
  private static final boolean DEFAULT_SKIP_CHECKS = false;
  private static final Set<SubscriptionMode> DEFAULT_SUBSCRIPTION_MODES = 
      Collections.unmodifiableSet(new HashSet<SubscriptionMode>(Arrays.asList(
      SubscriptionMode.BLOG, SubscriptionMode.SUBSCRIBED)));
  private static final Set<DateMode> DEFAULT_DATE_MODES = Collections.unmodifiableSet(
      new HashSet<DateMode>(Arrays.asList(DateMode.PUBLISHED)));

  private SpaceReference blogSpaceRef;
  private List<DocumentReference> subscribedToBlogs = Collections.emptyList();

  private int offset = DEFAULT_OFFSET;
  private int limit = DEFAULT_LIMIT;
  private List<String> sortFields = Collections.emptyList();
  private boolean skipChecks = DEFAULT_SKIP_CHECKS;
  private String language = null;
  private Set<SubscriptionMode> subsModes = DEFAULT_SUBSCRIPTION_MODES;
  private Set<DateMode> dateModes = DEFAULT_DATE_MODES;
  
  public ArticleSearchParameter() {
  }

  public SpaceReference getBlogSpaceRef() {
    return blogSpaceRef;
  }
  
  public ArticleSearchParameter setBlogSpaceRef(SpaceReference blogSpaceRef) {
    this.blogSpaceRef = blogSpaceRef;
    return this;
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

  public boolean isSkipChecks() {
    return skipChecks;
  }

  public ArticleSearchParameter setSkipChecks(boolean skipChecks) {
    this.skipChecks = skipChecks;
    return this;
  }

  public String getLanguage() {
    return language;
  }

  public ArticleSearchParameter setLanguage(String language) {
    this.language = language;
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

  @Override
  public String toString() {
    return "ArticleSearchParameter [blogSpaceRef=" + blogSpaceRef
        + ", subscribedToBlogs=" + subscribedToBlogs + ", offset=" + offset + ", limit=" 
        + limit + ", sortFields=" + sortFields + ", skipChecks=" + skipChecks 
        + ", language=" + language + ", subsModes=" + subsModes + ", dateModes=" 
        + dateModes + "]";
  }

}
