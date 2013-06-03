package com.celements.blog.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.web.Utils;

public class NewsletterAttachmentServiceTest extends AbstractBridgedComponentTestCase {

  NewsletterAttachmentService service;

  @Before
  public void setUp_NewsletterAttachmentServiceTest() throws Exception {
    getContext().put("vcontext", new VelocityContext());
    service = new NewsletterAttachmentService();
    service.execution = Utils.getComponent(Execution.class);
    service.webUtils = Utils.getComponent(IWebUtilsService.class);
  }

  @Test
  public void testGetEmbedAttList_null() {
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_noList() {
    VelocityContext vcontext = (VelocityContext)getContext().get("vcontext");
    vcontext.put("nlEmbedAttList", "test");
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_emptyList() {
    VelocityContext vcontext = (VelocityContext)getContext().get("vcontext");
    List<Attachment> list = new ArrayList<Attachment>();
    vcontext.put("nlEmbedAttList", list);
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_nonAttachmentList() {
    VelocityContext vcontext = (VelocityContext)getContext().get("vcontext");
    List<String> list = new ArrayList<String>();
    list.add("test");
    vcontext.put("nlEmbedAttList", list);
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_validList() {
    VelocityContext vcontext = (VelocityContext)getContext().get("vcontext");
    List<Attachment> list = new ArrayList<Attachment>();
    Attachment att = new Attachment(null, new XWikiAttachment(), getContext());
    list.add(att);
    vcontext.put("nlEmbedAttList", list);
    List<Attachment> resList = service.getAttachmentList(true);
    assertNotNull(resList);
    assertSame(att, resList.get(0));
  }
  
//  public String getImageURL(String imgFullname, boolean embedImage)
//  public String embedImagesInContent(String content)
//  String embedImagesInContent(String content, Set<String> imgTags)
//  public void addAttachment(String attFullname)
//  List<Attachment> getAttachmentList(String param)
//  void extendAttachmentList(Attachment att, String param)
//  Attachment getAttachmentForFullname(String imgFullname)
  
}
