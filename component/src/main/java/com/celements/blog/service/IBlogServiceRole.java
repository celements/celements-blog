package com.celements.blog.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.QueryException;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadException;
import com.celements.blog.article.ArticleLoadParameter;
import com.celements.blog.dto.BlogConfig;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IBlogServiceRole {

  /**
   * @deprecated since 1.32 instead use {@link #getBlogConfigDocRef(SpaceReference)}
   * @param blogSpaceName
   * @return
   */
  @Deprecated
  DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName);

  /**
   * @deprecated since 1.32 instead use {@link #getBlogConfigDocRef(SpaceReference)}
   * @param blogSpaceName
   * @return
   */
  @Deprecated
  XWikiDocument getBlogPageByBlogSpace(String blogSpaceName);

  /**
   * gets the blog ref for the given space reference
   *
   * @param spaceRef
   * @return
   * @throws QueryException
   * @throws XWikiException
   */
  DocumentReference getBlogConfigDocRef(SpaceReference spaceRef) throws QueryException,
      XWikiException;

  /**
   * gets the space reference for the given blog ref
   *
   * @param blogConfDocRef
   * @return
   * @throws XWikiException
   */
  SpaceReference getBlogSpaceRef(DocumentReference blogConfDocRef) throws XWikiException;

  /**
   * checks whether blog ref is subscribable
   *
   * @param blogConfDocRef
   * @return
   * @throws XWikiException
   */
  boolean isSubscribable(DocumentReference blogConfDocRef) throws XWikiException;

  /**
   * gets all subscribed blog refs for the given blog ref
   *
   * @param blogConfDocRef
   * @return
   * @throws QueryException
   * @throws XWikiException
   */
  List<DocumentReference> getSubribedToBlogs(DocumentReference blogConfDocRef)
      throws QueryException, XWikiException;

  /**
   * gets all subscribed blog space refs for the given blog ref
   *
   * @param blogConfDocRef
   * @return
   * @throws QueryException
   * @throws XWikiException
   */
  List<SpaceReference> getSubribedToBlogsSpaceRefs(DocumentReference blogConfDocRef)
      throws QueryException, XWikiException;

  /**
   * gets articles as list for given {@link ArticleLoadParameter}
   *
   * @param blogConfDocRef
   *          may not be null
   * @param param
   *          may not be null
   * @return
   * @throws ArticleLoadException
   *           if there was an error loading articles
   */
  List<Article> getArticles(DocumentReference blogConfDocRef, ArticleLoadParameter param)
      throws ArticleLoadException;

  /**
   * get DTO blog config object
   *
   * @param blogConfDocRef
   * @return
   */
  @NotNull
  Optional<BlogConfig> getBlogConfig(@Nullable DocumentReference blogConfDocRef);

}
