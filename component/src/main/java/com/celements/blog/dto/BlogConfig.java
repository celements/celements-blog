package com.celements.blog.dto;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.reference.SpaceReference;

import com.celements.blog.plugin.BlogClasses;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class BlogConfig {

  public final SpaceReference configSpaceRef;
  public final int articlePerPage;
  public final String viewType;

  public static class Builder {

    private SpaceReference configSpaceRef;
    private int articlePerPage;
    private String viewType;

    public Builder setConfigSpaceRef(SpaceReference spaceRef) {
      configSpaceRef = spaceRef;
      return this;
    }

    public Builder setArticlePerPage(int artPerPage) {
      this.articlePerPage = artPerPage;
      return this;
    }

    public Builder setViewType(String viewType) {
      this.viewType = viewType;
      return this;
    }

    public BlogConfig build() {
      return new BlogConfig(this);
    }

    public static Builder from(BaseObject configObj) {
      Builder builder = new Builder();
      RefBuilder.from(getContext().getWikiRef())
          .space(configObj.getStringValue(BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE))
          .buildOpt(SpaceReference.class)
          .ifPresent(builder::setConfigSpaceRef);
      builder.setArticlePerPage(
          configObj.getIntValue(BlogClasses.PROPERTY_BLOG_CONFIG_ARTICLE_PER_PAGE));
      builder.setViewType(configObj.getStringValue(BlogClasses.PROPERTY_BLOG_CONFIG_VIEW_TYPE));
      return builder;
    }

    private static ModelContext getContext() {
      return Utils.getComponent(ModelContext.class);
    }
  }

  private BlogConfig(Builder builder) {
    this.configSpaceRef = builder.configSpaceRef;
    this.articlePerPage = builder.articlePerPage;
    this.viewType = builder.viewType;
  }

}
