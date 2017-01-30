package com.celements.blog.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.QueryException;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadException;
import com.celements.blog.article.ArticleLoadParameter;
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
  public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName);

  /**
   * @deprecated since 1.32 instead use {@link #getBlogConfigDocRef(SpaceReference)}
   * @param blogSpaceName
   * @return
   */
  @Deprecated
  public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName);

  /**
   * gets the blog ref for the given space reference
   *
   * @param spaceRef
   * @return
   * @throws QueryException
   * @throws XWikiException
   */
  public DocumentReference getBlogConfigDocRef(SpaceReference spaceRef) throws QueryException,
      XWikiException;

  /**
   * gets the space reference for the given blog ref
   *
   * @param blogConfDocRef
   * @return
   * @throws XWikiException
   */
  public SpaceReference getBlogSpaceRef(DocumentReference blogConfDocRef) throws XWikiException;

  /**
   * checks whether blog ref is subscribable
   *
   * @param blogConfDocRef
   * @return
   * @throws XWikiException
   */
  public boolean isSubscribable(DocumentReference blogConfDocRef) throws XWikiException;

  /**
   * gets all subscribed blog refs for the given blog ref
   *
   * @param blogConfDocRef
   * @return
   * @throws QueryException
   * @throws XWikiException
   */
  public List<DocumentReference> getSubribedToBlogs(DocumentReference blogConfDocRef)
      throws QueryException, XWikiException;

  /**
   * gets all subscribed blog space refs for the given blog ref
   *
   * @param blogConfDocRef
   * @return
   * @throws QueryException
   * @throws XWikiException
   */
  public List<SpaceReference> getSubribedToBlogsSpaceRefs(DocumentReference blogConfDocRef)
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
  public List<Article> getArticles(DocumentReference blogConfDocRef, ArticleLoadParameter param)
      throws ArticleLoadException;

}
