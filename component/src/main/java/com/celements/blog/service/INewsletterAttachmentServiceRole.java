package com.celements.blog.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.api.Attachment;

@ComponentRole
public interface INewsletterAttachmentServiceRole {
  public String embedImagesInContent(String content);
  
  public String getImageURL(String imgFullname, boolean embedImage);
  
  public void addAttachment(String attFullname);
  
  public List<Attachment> getAttachmentList(boolean includeImages);
}
