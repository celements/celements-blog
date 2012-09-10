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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component
public class BlogClasses extends AbstractClassCollection {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      BlogClasses.class);
  
  public static final String NEWSLETTER_RECEIVER_CLASS_DOC = "NewsletterReceiverClass";
  public static final String NEWSLETTER_RECEIVER_CLASS_SPACE = "Celements";
  public static final String NEWSLETTER_RECEIVER_CLASS = NEWSLETTER_RECEIVER_CLASS_SPACE
        + "." + NEWSLETTER_RECEIVER_CLASS_DOC;

  public BlogClasses() {}
  
  public String getConfigName() {
    return "blog";
  }
  
  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  @Override
  protected void initClasses() throws XWikiException {
    getNewsletterReceiverClass();
  }

  public DocumentReference getNewsletterReceiverClassRef(String wikiName) {
    return new DocumentReference(wikiName, NEWSLETTER_RECEIVER_CLASS_SPACE,
        NEWSLETTER_RECEIVER_CLASS_DOC);
  }

  BaseClass getNewsletterReceiverClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;
    
    DocumentReference newsletterReceiverClassRef = getNewsletterReceiverClassRef(
        getContext().getDatabase());
    try {
      doc = xwiki.getDocument(newsletterReceiverClassRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
      doc = new XWikiDocument(newsletterReceiverClassRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(newsletterReceiverClassRef);
    
    needsUpdate |= bclass.addTextField("email", "E-Mail", 30);
    needsUpdate |= bclass.addBooleanField("isactive", "Is Active", "yesno");
    needsUpdate |= bclass.addTextField("subscribed", "Subscribed to Newsletter(s) - " +
        "separated by ','", 30);
/*    String hql = "select distinct doc.fullName " +
        "from XWikiDocument as doc, " +
        "BaseObject as obj, " +
        "IntegerProperty as isnl " +
        "where obj.name=doc.fullName " +
        "and obj.className='Celements2.BlogConfigClass' " +
        "and isnl.id.id=obj.id " +
        "and isnl.id.name='is_newsletter' " +
        "and isnl.value='1' " +
        "order by doc.fullName";
    needsUpdate |= bclass.addDBListField("subscribed", "Subscribed to Newsletter(s)", 5, 
        true, hql);
*/
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }
  
}
