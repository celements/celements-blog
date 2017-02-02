/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.blog.plugin;

import static com.celements.common.test.CelementsTestUtils.*;
import static java.nio.charset.StandardCharsets.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiURLFactory;

public class NewsletterReceiversTest extends AbstractComponentTest {

  private NewsletterReceivers comp;

  @Before
  public void prepareTest() throws Exception {
    comp = new NewsletterReceivers();
  }

  private void unsubscribeLink(String email) throws Exception {
    String spaceName = "TestSpace";
    String docName = "BlogArticle";
    String urlParams = "xpage=celements_ajax&ajax_mode=BlogAjax&doaction=unsubscribe&emailadresse=";
    String urlStaticPart = "http://celements.com/" + spaceName + "/" + docName + "?" + urlParams;
    URL url = new URL(urlStaticPart + URLEncoder.encode(email, UTF_8.name()));
    IBlogServiceRole blogServiceMock = registerComponentMock(IBlogServiceRole.class);
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        spaceName, docName));
    BaseObject blogObj = new BaseObject();
    blogObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "Celements2",
        "BlogConfigClass"));
    blogObj.setIntValue("unsubscribe_info", 1);
    doc.addXObject(blogObj);
    expect(blogServiceMock.getBlogPageByBlogSpace(eq(spaceName))).andReturn(doc);
    XWikiURLFactory urlFactoryMock = createMockAndAddToDefault(XWikiURLFactory.class);
    getContext().setURLFactory(urlFactoryMock);
    expect(urlFactoryMock.createExternalURL(eq(spaceName), eq(docName), eq("view"), eq(urlParams
        + URLEncoder.encode(email, UTF_8.name())), (String) eq(null), eq(
            getContext().getDatabase()), same(getContext()))).andReturn(url);
    replayDefault();
    String link = comp.getUnsubscribeLink(spaceName, email);
    verifyDefault();
    assertEquals(url.toString(), link);
  }

  @Test
  public void testGetUnsubscribeLink() throws Exception {
    unsubscribeLink("test@clementes.com");
  }

  @Test
  public void testGetUnsubscribeLink_plusInEmail() throws Exception {
    unsubscribeLink("test+test@clementes..com");
  }

  @Test
  public void testGetNewsletterReceiversList() {
    // TODO implement - class (i.e. the constructor -> lazy loading) needs refactoring
    // first to seriously write tests.
  }

}
