package com.celements.blog.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.api.Attachment;

@ComponentRole
public interface INewsletterAttachmentServiceRole {
  public String embedImagesInContent(String content);
  
  String getImageURL(String imgFullname, boolean embedImage);
  
  void addAttachment(String attFullname);
  
  List<Attachment> getAttachmentList(boolean includeImages);
}
