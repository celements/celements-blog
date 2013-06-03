package com.celements.blog.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NewsletterAttachmentServiceTest extends AbstractBridgedComponentTestCase {

  NewsletterAttachmentService service;
  XWiki xwiki;
  
  @Before
  public void setUp_NewsletterAttachmentServiceTest() throws Exception {
    getContext().put("vcontext", new VelocityContext());
    xwiki = createMock(XWiki.class);
    getContext().setWiki(xwiki);
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
  
  @Test
  public void testGetImageURL_notEmbedded() throws XWikiException {
    String expectedResult = "/download/Test/Img/file.jpg";
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(doc.getAttachment(eq("file.jpg"))).andReturn(att).anyTimes();
    expect(doc.getAttachmentURL(eq("file.jpg"), eq("download") , same(getContext()))
        ).andReturn(expectedResult).once();
    expect(xwiki.getDocument(eq("Test.Img"), same(getContext()))).andReturn(doc).once();
    replay(doc, xwiki);
    assertTrue(service.getImageURL("Test.Img;file.jpg", false).startsWith(expectedResult));
    verify(doc, xwiki);
  }
  
  @Test
  public void testGetImageURL_embedded() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", 
        "Img");
    String expectedResult = "cid:file.jpg";
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(doc.getAttachment(eq("file.jpg"))).andReturn(att).anyTimes();
    expect(doc.clone()).andReturn(doc).once();
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    replay(doc, xwiki);
    assertEquals(expectedResult, service.getImageURL("Test.Img;file.jpg", true));
    verify(doc, xwiki);
  }
  
  @Test
  public void testEmbedImagesInContent() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", 
        "Img");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(doc.getAttachment(eq("file.jpg"))).andReturn(att).once();
    expect(doc.clone()).andReturn(doc).once();
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    String imgTag = "<img class=\"abc\" src=\"/download/Test/Img/file.jpg?bla=123\" />";
    String content = "Test text with " + imgTag + " image included";
    replay(doc, xwiki);
    String result = service.embedImagesInContent(content);
    verify(doc, xwiki);
    assertTrue(result, result.contains("src=\"cid:file.jpg\""));
    assertFalse(result, result.contains("/download/"));
  }
  
  @Test
  public void testEmbedImagesInContent_inner() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", 
        "Img");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(doc.getAttachment(eq("file.jpg"))).andReturn(att).once();
    expect(doc.clone()).andReturn(doc).once();
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    String imgTag = "<img class=\"abc\" src=\"/download/Test/Img/file.jpg?bla=123\" />";
    String content = "Test text with " + imgTag + " image included";
    Set<String> tags = new HashSet<String>();
    tags.add(imgTag);
    replay(doc, xwiki);
    String result = service.embedImagesInContent(content, tags);
    verify(doc, xwiki);
    assertTrue(result, result.contains("src=\"cid:file.jpg\""));
    assertFalse(result, result.contains("/download/"));
  }
  
  @Test
  public void testAddAttachment() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", 
        "Img");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(doc.getAttachment(eq("file.pdf"))).andReturn(att).once();
    expect(doc.clone()).andReturn(doc).once();
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    replay(doc, xwiki);
    service.addAttachment("Test.Img;file.pdf");
    verify(doc, xwiki);
    List<Attachment> atts = service.getAttachmentList(false);
    assertNotNull(atts);
    assertEquals(1, atts.size());
  }
  
  @Test
  public void testExtendAttachmentList() {
    Attachment att = new Attachment(null, null, getContext());
    service.extendAttachmentList(att, "nlEmbedNoImgAttList");
    List<Attachment> atts = service.getAttachmentList(false);
    assertNotNull(atts);
    assertEquals(1, atts.size());
    assertSame(att, atts.get(0));
  }
  
  @Test
  public void testGetAttachmentForFullname() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", 
        "Img");
    XWikiDocument doc = createMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(doc.getAttachment(eq("file.jpg"))).andReturn(att).once();
    expect(doc.clone()).andReturn(doc).once();
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    replay(doc, xwiki);
    assertEquals(att.getFilename(), service.getAttachmentForFullname("Test.Img;file.jpg"
        ).getFilename());
    verify(doc, xwiki);
  }
}
