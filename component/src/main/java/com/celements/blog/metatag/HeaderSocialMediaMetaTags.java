package com.celements.blog.metatag;

import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.QueryException;

import com.celements.blog.article.Article;
import com.celements.blog.plugin.EmptyArticleException;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.metatag.MetaTag;
import com.celements.metatag.MetaTagProviderRole;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.XWikiException;

@Immutable
@Component(HeaderSocialMediaMetaTags.COMPONENT_NAME)
public class HeaderSocialMediaMetaTags implements MetaTagProviderRole {

  public static final String COMPONENT_NAME = "HeaderSocialMediaMetaTags";

  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderSocialMediaMetaTags.class);

  @Requirement
  private ModelContext modelContext;

  @Requirement
  private IBlogServiceRole blogService;

  @Override
  public @NotNull List<MetaTag> getHeaderMetaTags() {
    if (isBlogArticle()) {
      try {
        Article article = new Article(modelContext.getDoc(), modelContext.getXWikiContext());
        return article.getArticleSocialMediaTags(modelContext.getXWikiContext().getLanguage());
      } catch (XWikiException | EmptyArticleException excp) {
        LOGGER.error("Exception getting article for doc [{}]", modelContext.getDoc(), excp);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public @NotNull List<MetaTag> getBodyMetaTags() {
    return Collections.emptyList();
  }

  boolean isBlogArticle() {
    SpaceReference spaceRef = modelContext.getDoc().getDocumentReference().getLastSpaceReference();
    try {
      return null != blogService.getBlogConfigDocRef(spaceRef);
    } catch (QueryException | XWikiException excp) {
      LOGGER.warn("Exception determining blog config for space {}", spaceRef, excp);
    }
    return false;
  }
}
