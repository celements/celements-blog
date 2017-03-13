package com.celements.blog.metatag;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.blog.article.Article;
import com.celements.blog.plugin.EmptyArticleException;
import com.celements.metatag.MetaTag;
import com.celements.metatag.MetaTagProviderRole;
import com.celements.model.context.ModelContext;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.xpn.xwiki.XWikiException;

@Component(HeaderSocialMediaMetaTags.COMPONENT_NAME)
public class HeaderSocialMediaMetaTags implements MetaTagProviderRole {

  public static final String COMPONENT_NAME = "HeaderSocialMediaMetaTags";

  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderSocialMediaMetaTags.class);

  @Requirement
  private IPageTypeResolverRole ptResolver;

  @Requirement
  private ModelContext modelContext;

  @Override
  public List<MetaTag> getHeaderMetaTags() {
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
  public List<MetaTag> getBodyMetaTags() {
    return Collections.emptyList();
  }

  boolean isBlogArticle() {
    PageTypeReference ptRef = ptResolver.getPageTypeRefForDoc(modelContext.getDoc());
    return (ptRef != null) && "Article".equals(ptRef.getConfigName());
  }
}
