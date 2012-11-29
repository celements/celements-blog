package com.celements.blog.service;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IBlogServiceRole {

  public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName);

  public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName);

}
