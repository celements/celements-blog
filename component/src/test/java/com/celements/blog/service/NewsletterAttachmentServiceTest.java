package com.celements.blog.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.filebase.IAttachmentServiceRole;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

public class NewsletterAttachmentServiceTest extends AbstractComponentTest {

  IAttachmentServiceRole attService;
  NewsletterAttachmentService service;
  XWiki xwiki;

  @Before
  public void prepare() throws Exception {
    attService = registerComponentMock(IAttachmentServiceRole.class);
    getXContext().put("vcontext", new VelocityContext());
    xwiki = getMock(XWiki.class);
    service = getBeanFactory().getBean(NewsletterAttachmentService.class);
  }

  @Test
  public void testGetEmbedAttList_null() {
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_noList() {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    vcontext.put("nlEmbedAttList", "test");
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_emptyList() {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    List<Attachment> list = new ArrayList<>();
    vcontext.put("nlEmbedAttList", list);
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_nonAttachmentList() {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    List<String> list = new ArrayList<>();
    list.add("test");
    vcontext.put("nlEmbedAttList", list);
    assertNull(service.getAttachmentList(true));
  }

  @Test
  public void testGetEmbedAttList_validList() {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    List<Attachment> list = new ArrayList<>();
    Attachment att = new Attachment(null, new XWikiAttachment(), getContext());
    list.add(att);
    vcontext.put("nlEmbedAttList", list);
    List<Attachment> resList = service.getAttachmentList(true);
    assertNotNull(resList);
    assertSame(att, resList.get(0));
  }

  @Test
  public void testGetImageURL_notEmbedded() throws Exception {
    service.attUrlCmd = createDefaultMock(AttachmentURLCommand.class);
    String expectedResult = "/download/Test/Img/file.jpg";
    expect(service.attUrlCmd.getAttachmentURL("Test.Img;file.jpg", "download", getContext()))
        .andReturn(expectedResult);
    replayDefault();
    assertTrue(service.getImageURL("Test.Img;file.jpg", false).startsWith(expectedResult));
    verifyDefault();
  }

  @Test
  public void testGetImageURL_embedded() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    String expectedResult = "cid:file.jpg";
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.jpg"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    replayDefault();
    assertEquals(expectedResult, service.getImageURL("Test.Img;file.jpg", true));
    verifyDefault();
  }

  @Test
  public void testEmbedImagesInContent() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.jpg"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    String imgTag = "<img class=\"abc\" src=\"/download/Test/Img/file.jpg?bla=123\" />";
    String content = "Test text with " + imgTag + " image included";
    replayDefault();
    String result = service.embedImagesInContent(content);
    verifyDefault();
    assertTrue(result, result.contains("src=\"cid:file.jpg\""));
    assertFalse(result, result.contains("/download/"));
  }

  @Test
  public void testEmbedImagesInContent_inner() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.jpg"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    String imgTag = "<img class=\"abc\" src=\"/download/Test/Img/file.jpg?bla=123\" />";
    String content = "Test text with " + imgTag + " image included";
    Set<String> tags = new HashSet<>();
    tags.add(imgTag);
    replayDefault();
    String result = service.embedImagesInContent(content, tags);
    verifyDefault();
    assertTrue(result, result.contains("src=\"cid:file.jpg\""));
    assertFalse(result, result.contains("/download/"));
  }

  @Test
  public void testEmbedImagesInContent_inner_externalURLwww() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.jpg"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    String imgTag = "<img class=\"abc\" src=\"www.test.com/download/Test/Img/file.jpg?"
        + "bla=123\" />";
    String content = "Test text with " + imgTag + " image included";
    Set<String> tags = new HashSet<>();
    tags.add(imgTag);
    replayDefault();
    String result = service.embedImagesInContent(content, tags);
    verifyDefault();
    assertTrue(result, result.contains("src=\"cid:file.jpg\""));
    assertFalse(result, result.contains("/download/"));
    assertFalse(result, result.contains("www"));
  }

  @Test
  public void testEmbedImagesInContent_inner_externalURLhttp() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.jpg"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    String imgTag = "<img class=\"abc\" src=\"http://www.test.com/download/Test/Img/"
        + "file.jpg?bla=123\" />";
    String content = "Test text with " + imgTag + " image included";
    Set<String> tags = new HashSet<>();
    tags.add(imgTag);
    replayDefault();
    String result = service.embedImagesInContent(content, tags);
    verifyDefault();
    assertTrue(result, result.contains("src=\"cid:file.jpg\""));
    assertFalse(result, result.contains("/download/"));
    assertFalse(result, result.contains("http"));
  }

  @Test
  public void testAddAttachment() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.pdf"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    replayDefault();
    service.addAttachment("Test.Img;file.pdf");
    verifyDefault();
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
  public void testGetAttachmentForFullname() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.jpg"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    replayDefault();
    assertEquals(att.getFilename(), service.getAttachmentForFullname(
        "Test.Img;file.jpg").getFilename());
    verifyDefault();
  }

  @Test
  public void testClearAttachmentList() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.pdf"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext()));
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
    replayDefault();
    service.addAttachment("Test.Img;file.pdf");
    service.clearAttachmentList();
    verifyDefault();
    List<Attachment> atts = service.getAttachmentList(false);
    assertTrue("clearAttachmentList must clear the list.", (atts == null) || atts.isEmpty());
    List<Attachment> atts2 = service.getAttachmentList(true);
    assertTrue("clearAttachmentList must clear both list.", (atts2 == null) || atts2.isEmpty());
  }

  @Test
  public void testClearAttachmentList_add_afterClear() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Test", "Img");
    XWikiDocument doc = createDefaultMock(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    expect(attService.getAttachmentNameEqual(same(doc), eq("file.pdf"))).andReturn(att).anyTimes();
    expect(attService.getApiAttachment(same(att))).andReturn(new Attachment(new Document(doc,
        getContext()), att, getContext())).anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    replayDefault();
    service.addAttachment("Test.Img;file.pdf");
    service.clearAttachmentList();
    service.addAttachment("Test.Img;file.pdf");
    verifyDefault();
    List<Attachment> atts = service.getAttachmentList(false);
    assertNotNull(atts);
    assertEquals(1, atts.size());
  }

}
