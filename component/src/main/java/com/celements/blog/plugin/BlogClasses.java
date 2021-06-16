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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celBlogClasses")
public class BlogClasses extends AbstractClassCollection {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlogClasses.class);

  public static final String NEWSLETTER_RECEIVER_CLASS_DOC = "NewsletterReceiverClass";
  public static final String NEWSLETTER_RECEIVER_CLASS_SPACE = "Celements";
  public static final String NEWSLETTER_RECEIVER_CLASS = NEWSLETTER_RECEIVER_CLASS_SPACE + "."
      + NEWSLETTER_RECEIVER_CLASS_DOC;

  public static final String BLOG_CONFIG_CLASS_DOC = "BlogConfigClass";
  public static final String BLOG_CONFIG_CLASS_SPACE = "Celements2";
  public static final String BLOG_CONFIG_CLASS = BLOG_CONFIG_CLASS_SPACE + "."
      + BLOG_CONFIG_CLASS_DOC;
  public static final String PROPERTY_BLOG_CONFIG_BLOGSPACE = "blogspace";
  public static final String PROPERTY_BLOG_CONFIG_SUBSCRIBE_TO = "subscribe_to";
  public static final String PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE = "is_subscribable";
  public static final String PROPERTY_BLOG_CONFIG_MAX_NUM_CHARS_FIELD = "max_num_chars";

  public static final String ARTICLE_CLASS_DOC = "ArticleClass";
  public static final String ARTICLE_CLASS_SPACE = "XWiki";
  public static final String ARTICLE_CLASS = ARTICLE_CLASS_SPACE + "." + ARTICLE_CLASS_DOC;
  public static final String PROPERTY_ARTICLE_LANG = "lang";
  public static final String PROPERTY_ARTICLE_PUBLISH_DATE = "publishdate";
  public static final String PROPERTY_ARTICLE_ARCHIVE_DATE = "archivedate";
  public static final String PROPERTY_ARTICLE_IS_SUBSCRIBABLE = "isSubscribable";

  public static final String RECEIVER_E_MAIL_CLASS_DOC = "ReceiverEMail";
  public static final String RECEIVER_E_MAIL_CLASS_SPACE = "Celements2";
  public static final String RECEIVER_E_MAIL_CLASS = RECEIVER_E_MAIL_CLASS_SPACE + "."
      + RECEIVER_E_MAIL_CLASS_DOC;

  public static final String NEWSLETTER_CONFIG_CLASS_DOC = "NewsletterConfigClass";
  public static final String NEWSLETTER_CONFIG_CLASS_SPACE = "Classes";
  public static final String NEWSLETTER_CONFIG_CLASS = NEWSLETTER_CONFIG_CLASS_SPACE + "."
      + NEWSLETTER_CONFIG_CLASS_DOC;

  public static final String BLOG_ARTICLE_SUBSCRIPTION_CLASS_DOC = "BlogArticleSubscriptionClass";
  public static final String BLOG_ARTICLE_SUBSCRIPTION_CLASS_SPACE = "Celements2";
  public static final String BLOG_ARTICLE_SUBSCRIPTION_CLASS = BLOG_ARTICLE_SUBSCRIPTION_CLASS_SPACE
      + "." + BLOG_ARTICLE_SUBSCRIPTION_CLASS_DOC;
  public static final String PROPERTY_ARTICLE_SUBSCRIPTION_SUBSCRIBER = "subscriber";
  public static final String PROPERTY_ARTICLE_SUBSCRIPTION_DO_SUBSCRIBE = "doSubscribe";

  public BlogClasses() {}

  @Override
  public String getConfigName() {
    return "blog";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getNewsletterReceiverClass();
    getBlogConfigClass();
    getArticleClass();
    getReceiverEMailClass();
    getNewsletterConfigClass();
    getBlogArticleSubscriptionClass();
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
    } catch (XWikiException xwe) {
      LOGGER.error("getNewsletterReceiverClass failed", xwe);
      doc = new XWikiDocument(newsletterReceiverClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(newsletterReceiverClassRef);

    needsUpdate |= bclass.addTextField("email", "E-Mail", 30);
    needsUpdate |= bclass.addTextField("language", "Language code (ISO 639-1)", 30);
    needsUpdate |= bclass.addBooleanField("isactive", "Is Active", "yesno");
    needsUpdate |= bclass.addTextField("subscribed", "Subscribed to Newsletter(s) - "
        + "separated by ','", 30);
    /*
     * String hql = "select distinct doc.fullName " + "from XWikiDocument as doc, " +
     * "BaseObject as obj, " + "IntegerProperty as isnl " + "where obj.name=doc.fullName " +
     * "and obj.className='Celements2.BlogConfigClass' " + "and isnl.id.id=obj.id " +
     * "and isnl.id.name='is_newsletter' " + "and isnl.value='1' " + "order by doc.fullName";
     * needsUpdate |= bclass.addDBListField("subscribed", "Subscribed to Newsletter(s)", 5, true,
     * hql);
     */

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getBlogConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, BLOG_CONFIG_CLASS_SPACE, BLOG_CONFIG_CLASS_DOC);
  }

  BaseClass getBlogConfigClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;

    DocumentReference blogConfigClassRef = getBlogConfigClassRef(getContext().getDatabase());
    try {
      doc = xwiki.getDocument(blogConfigClassRef, getContext());
    } catch (Exception e) {
      doc = new XWikiDocument(blogConfigClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(blogConfigClassRef);
    needsUpdate |= bclass.addBooleanField(PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE,
        PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, "yesno");
    needsUpdate |= bclass.addTextField(PROPERTY_BLOG_CONFIG_SUBSCRIBE_TO,
        PROPERTY_BLOG_CONFIG_SUBSCRIBE_TO, 30);
    needsUpdate |= bclass.addNumberField("art_per_page", "art_per_page", 5, "integer");
    needsUpdate |= bclass.addBooleanField("is_newsletter", "is_newsletter", "yesno");
    needsUpdate |= bclass.addTextField("from_address", "from_address", 30);
    needsUpdate |= bclass.addTextField("reply_to_address", "reply_to_address", 30);
    needsUpdate |= bclass.addBooleanField("unsubscribe_info", "unsubscribe_info", "yesno");
    needsUpdate |= bclass.addTextField("template", "template", 30);
    needsUpdate |= bclass.addStaticListField("blogeditor", "blogeditor", 1, false, "plain|wysiwyg",
        "select");
    needsUpdate |= bclass.addTextField(PROPERTY_BLOG_CONFIG_BLOGSPACE,
        PROPERTY_BLOG_CONFIG_BLOGSPACE, 30);
    needsUpdate |= bclass.addStaticListField("viewtype", "viewtype", 1, false, "title|extract|full",
        "select");
    needsUpdate |= bclass.addBooleanField("has_comments", "has_comments", "yesno");
    needsUpdate |= bclass.addNumberField(PROPERTY_BLOG_CONFIG_MAX_NUM_CHARS_FIELD,
        "max number of characters in extract", 5, "integer");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getArticleClassRef(String wikiName) {
    return new DocumentReference(wikiName, ARTICLE_CLASS_SPACE, ARTICLE_CLASS_DOC);
  }

  BaseClass getArticleClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;

    DocumentReference articleClassRef = getArticleClassRef(getContext().getDatabase());
    try {
      doc = xwiki.getDocument(articleClassRef, getContext());
    } catch (Exception e) {
      doc = new XWikiDocument(articleClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(articleClassRef);
    needsUpdate |= bclass.addTextAreaField("extract", "extract", 80, 15);
    needsUpdate |= bclass.addTextAreaField("title", "title", 80, 15);
    // category
    needsUpdate |= bclass.addTextAreaField("content", "content", 80, 15);
    needsUpdate |= bclass.addNumberField("id", "id", 30, "integer");
    needsUpdate |= bclass.addTextField(PROPERTY_ARTICLE_LANG, PROPERTY_ARTICLE_LANG, 30);
    needsUpdate |= bclass.addTextField("blogeditor", "blogeditor", 30);
    needsUpdate |= addDateField(bclass, PROPERTY_ARTICLE_PUBLISH_DATE,
        PROPERTY_ARTICLE_PUBLISH_DATE, "dd.MM.yyyy HH:mm", 0, 0, getRegexDate(false, true),
        "cel_blog_validation_publishdate");
    needsUpdate |= bclass.addBooleanField("hasComments", "hasComments", "yesno");
    needsUpdate |= addDateField(bclass, PROPERTY_ARTICLE_ARCHIVE_DATE,
        PROPERTY_ARTICLE_ARCHIVE_DATE, "dd.MM.yyyy HH:mm", 0, 0, getRegexDate(true, true),
        "cel_blog_validation_archivedate");
    needsUpdate |= bclass.addBooleanField(PROPERTY_ARTICLE_IS_SUBSCRIBABLE,
        PROPERTY_ARTICLE_IS_SUBSCRIBABLE, "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getReceiverEMailClassRef(String wikiName) {
    return new DocumentReference(wikiName, RECEIVER_E_MAIL_CLASS_SPACE, RECEIVER_E_MAIL_CLASS_DOC);
  }

  BaseClass getReceiverEMailClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;

    DocumentReference receiverEMailClassRef = getReceiverEMailClassRef(getContext().getDatabase());
    try {
      doc = xwiki.getDocument(receiverEMailClassRef, getContext());
    } catch (Exception e) {
      doc = new XWikiDocument(receiverEMailClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(receiverEMailClassRef);
    needsUpdate |= bclass.addTextField("email", "email", 30);
    needsUpdate |= bclass.addBooleanField("is_active", "is_active", "yesno");
    needsUpdate |= bclass.addStaticListField("address_type", "address_type", 1, false, "to|cc|bcc",
        "select");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getNewsletterConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, NEWSLETTER_CONFIG_CLASS_SPACE,
        NEWSLETTER_CONFIG_CLASS_DOC);
  }

  BaseClass getNewsletterConfigClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;

    DocumentReference newsletterConfigClassRef = getNewsletterConfigClassRef(
        getContext().getDatabase());
    try {
      doc = xwiki.getDocument(newsletterConfigClassRef, getContext());
    } catch (Exception e) {
      doc = new XWikiDocument(newsletterConfigClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(newsletterConfigClassRef);
    needsUpdate |= bclass.addNumberField("times_sent", "times_sent", 30, "integer");
    needsUpdate |= bclass.addDateField("last_sent_date", "last_sent_date", null, 0);
    needsUpdate |= bclass.addTextField("last_sender", "last_sender", 30);
    needsUpdate |= bclass.addNumberField("last_sent_recipients", "last_sent_recipients", 30,
        "integer");
    needsUpdate |= bclass.addTextField("from_address", "from_address", 30);
    needsUpdate |= bclass.addTextField("reply_to_address", "reply_to_address", 30);
    needsUpdate |= bclass.addTextField("subject", "subject", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getBlogArticleSubscriptionClassRef(String wikiName) {
    return new DocumentReference(wikiName, BLOG_ARTICLE_SUBSCRIPTION_CLASS_SPACE,
        BLOG_ARTICLE_SUBSCRIPTION_CLASS_DOC);
  }

  BaseClass getBlogArticleSubscriptionClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;

    DocumentReference getBlogArticleSubscriptionClassRef = getBlogArticleSubscriptionClassRef(
        getContext().getDatabase());
    try {
      doc = xwiki.getDocument(getBlogArticleSubscriptionClassRef, getContext());
    } catch (Exception e) {
      doc = new XWikiDocument(getBlogArticleSubscriptionClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(getBlogArticleSubscriptionClassRef);
    needsUpdate |= bclass.addTextField(PROPERTY_ARTICLE_SUBSCRIPTION_SUBSCRIBER,
        PROPERTY_ARTICLE_SUBSCRIPTION_SUBSCRIBER, 30);
    needsUpdate |= bclass.addBooleanField(PROPERTY_ARTICLE_SUBSCRIPTION_DO_SUBSCRIBE,
        PROPERTY_ARTICLE_SUBSCRIPTION_DO_SUBSCRIBE, "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  private String getRegexDate(boolean allowEmpty, boolean withTime) {
    String regex = "(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4})";
    if (withTime) {
      regex += " ([01][0-9]|2[0-4])(\\:[0-5][0-9])";
    }
    return "/" + (allowEmpty ? "(^$)|" : "") + "^(" + regex + ")$" + "/";
  }

}
