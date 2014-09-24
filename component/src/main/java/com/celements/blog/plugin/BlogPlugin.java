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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.QueryException;

import com.celements.blog.article.Article;
import com.celements.blog.article.ArticleLoadException;
import com.celements.blog.article.ArticleLoadParameter;
import com.celements.blog.article.ArticleLoadParameter.DateMode;
import com.celements.blog.article.ArticleLoadParameter.SubscriptionMode;
import com.celements.blog.service.BlogService;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.blog.service.INewsletterAttachmentServiceRole;
import com.celements.web.plugin.api.CelementsWebPluginApi;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.web.Utils;

public class BlogPlugin extends XWikiDefaultPlugin{
  
  private static final String DEFAULT_RECEIVER_SPACE = "NewsletterReceivers";
  private static Log LOGGER = LogFactory.getFactory().getInstance(BlogPlugin.class);
  IBlogServiceRole injected_BlogService;
  
  public BlogPlugin(String name, String className, XWikiContext context) {
    super(name, className, context);
    init(context);
  }
  
  @Override
  public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
    return new BlogPluginApi((BlogPlugin) plugin, context);
  }

  @Override
  public String getName() {
    LOGGER.debug("Entered method getName");
    return "celementsblog";
  }

  /**
   * @deprecated since 1.4 use BlogService.getBlogPageByBlogSpace instead
   */
  @Deprecated
  public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName,
      XWikiContext context) throws XWikiException{
    return BlogUtils.getInstance(
        ).getBlogPageByBlogSpace(blogSpaceName, context);
  }
  
  /**
   * @deprecated since 1.32 instead use {@link BlogService#getArticles(DocumentReference, 
   *        ArticleLoadParameter)}
   * 
   * @param blogArticleSpace Space where the blog's articles are saved.
   * @param subscribedBlogsStr Comma separated String with all the blog article spaces 
   *        the blog has subscribed to.
   * @param language default language
   * @param archiveOnly Only get articles from the archive (archivedate < now)
   * @param futurOnly Only get articles that are not yet published (publishdate > now)
   * @param subscribableOnly Only get articles from subscribed blogs, but not the ones 
   *        from the blog the user is on.
   * @param withArchive Include archived articles in the answer. Has no effect if 
   *        archiveOnly = true.
   * @param withFutur Include not yet published articles. Only possible if the page has 
   *        been saved with programmingrights or the user has edit right on the article. Has no effect if futurOnly = true.
   * @param withSubscribable Include articles from subscribed blogs.
   * @param withSubscribed Include articles the blog has subscribed to.
   * @param withUnsubscribed Include articles the blog has unsubscribed from. Only works 
   *        with edit rights or programmingrights.
   * @param withUndecided Include articles the blog has not yet desided about a 
   *        subscription. Only works with edit rights or programmingrights.
   * @param checkAccessRights Do pay attention to the rights. Default = true if no 
   *        programmingrights.
   * @param context
   * @return
   * @throws XWikiException
   */
  @Deprecated
  public List<Article> getBlogArticles(String blogArticleSpace, String subscribedBlogsStr,
      String language, boolean archiveOnly, boolean futurOnly, boolean subscribableOnly,
      boolean withArchive, boolean withFutur, boolean withSubscribable,
      boolean withSubscribed, boolean withUnsubscribed, boolean withUndecided, 
      boolean checkAccessRights, XWikiContext context) throws ArticleLoadException {
    try {
      SpaceReference spaceRef = new SpaceReference(blogArticleSpace, new WikiReference(
          context.getDatabase()));
      DocumentReference blogConfDocRef = getBlogService().getBlogConfigDocRef(spaceRef);
      ArticleLoadParameter param = new ArticleLoadParameter();
      param.setWithBlogArticles(!subscribableOnly);
      param.setLanguage(language);
      if (withSubscribable) {
        param.setSubscriptionModes(getSubsModes(withSubscribed, withUnsubscribed, 
            withUndecided));
      }
      param.setDateModes(getDateModes(archiveOnly, futurOnly, withArchive, withFutur));
      return getBlogService().getArticles(blogConfDocRef, param);
    } catch (XWikiException xwe) {
      throw new ArticleLoadException(xwe);
    } catch (QueryException qexc) {
      throw new ArticleLoadException(qexc);
    }
  }

  private Set<SubscriptionMode> getSubsModes(boolean withSubscribed, 
      boolean withUnsubscribed, boolean withUndecided) {
    Set<SubscriptionMode> subsModes = new HashSet<SubscriptionMode>();
    if (withSubscribed) {
      subsModes.add(SubscriptionMode.SUBSCRIBED);
    }
    if (withUnsubscribed) {
      subsModes.add(SubscriptionMode.UNSUBSCRIBED);
    }
    if (withUndecided) {
      subsModes.add(SubscriptionMode.UNDECIDED);
    }
    return subsModes;
  }

  private Set<DateMode> getDateModes(boolean archiveOnly, boolean futurOnly, 
      boolean withArchive, boolean withFutur) {
    Set<DateMode> dateModes = new HashSet<DateMode>();
    dateModes.add(DateMode.PUBLISHED);
    if (withArchive) {
      dateModes.add(DateMode.ARCHIVED);
    }
    if (withFutur) {
      dateModes.add(DateMode.FUTURE);
    }
    if (archiveOnly) {
      dateModes.removeAll(Arrays.asList(DateMode.PUBLISHED, DateMode.FUTURE));
    }
    if (futurOnly) {
      dateModes.removeAll(Arrays.asList(DateMode.PUBLISHED, DateMode.ARCHIVED));
    }
    return dateModes;
  }
  
  @SuppressWarnings("unchecked")
  public String subscribeNewsletter(boolean inactiveWithoutMail, XWikiContext context) 
      throws XWikiException{
    Map<String, String> request = new HashMap<String, String>();
    Map<String, String[]> req = context.getRequest().getParameterMap();
    for (String key : req.keySet()) {
      if((req.get(key) != null) && (req.get(key).length > 0)) {
        request.put(key, req.get(key)[0]);
      }
    }
    return subscribeNewsletter(inactiveWithoutMail, request, context);
  }
  
  public Map<String, String> batchImportReceivers(boolean inactive, String importData, 
      String newsletterFullName, XWikiContext context) {
    LOGGER.info("Starting batch newsletter receiver import.");
    Map<String, String> results = new TreeMap<String, String>();
    Map<String, String> data = new HashMap<String, String>();
    data.put("subsBlog", newsletterFullName);
    for (String emailCandidate : splitImportDataToEmailCandidates(importData)) {
      if(containsEmailAddress(emailCandidate)) {
        String email = extractEmailFromString(emailCandidate);
        email = email.toLowerCase();
        data.put("emailadresse", email);
        XWikiDocument userDoc = null;
        try {
          String userDocName = subscribeNewsletter(inactive, data, context);
          if("".equals(userDocName)) {
            userDocName = getSubscriberDoc(email, context);
          }
          userDoc = context.getWiki().getDocument(userDocName, context);
        } catch (XWikiException e) {
          LOGGER.error("Exception while subscribing email '" + email + "' to '" + 
              newsletterFullName + "'", e);
        }
        if(userDoc != null) {
          BaseObject subsObj = userDoc.getObject("Celements.NewsletterReceiverClass", 
              "subscribed", newsletterFullName, false);
          if((subsObj != null) && (subsObj.getIntValue("isactive") == 1)) {
            results.put(email, context.getMessageTool().get(
                "cel_newsletter_subscriber_active"));
          } else {
            results.put(email, context.getMessageTool().get(
                "cel_newsletter_subscriber_inactive"));
          }
        } else {
          results.put(email, context.getMessageTool().get(
              "cel_newsletter_subscriber_invalid"));
        }
      } else {
        if(emailCandidate.trim().length() > 0) {
          results.put(emailCandidate, context.getMessageTool().get(
              "cel_newsletter_subscriber_invalid"));
        }
      }
    }
    return results;
  }

  String[] splitImportDataToEmailCandidates(String importData) {
    return importData.split("[,\r\n]+");
  }

  String extractEmailFromString(String emailCandidate) {
    return emailCandidate.replaceAll(getContainsEmailRegex(), "$1");
  }

  boolean containsEmailAddress(String email) {
    return email.matches(getContainsEmailRegex());
  }

  String getContainsEmailRegex() {
    return ".*?([\\w+\\.\\-\\_]+[@][\\w\\-\\_]+([.][\\w\\-\\_]+)+).*";
  }
  
  public String subscribeNewsletter(boolean inactiveWithoutMail, Map<String, String> 
      request, XWikiContext context) throws XWikiException{
    XWiki wiki = context.getWiki();
    boolean subscribed = false;
    boolean needsSave = false;
    String email = request.get("emailadresse");
    email = email.toLowerCase();
    String docName = getSubscriberDocName(email, context);
    LOGGER.info("ReceiverDoc is " + docName);
    XWikiDocument receiverDoc = wiki.getDocument(docName, context);
    XWikiDocument blogDoc = getBlogDoc(request, context);
    BaseObject obj = receiverDoc.getObject("Celements.NewsletterReceiverClass", 
        "subscribed", blogDoc.getFullName(), false);
    if((email != null) && !"".equals(email.trim()) && (obj == null)){
      obj = receiverDoc.newObject("Celements.NewsletterReceiverClass", context);
      obj.setStringValue("email", email);
      if(!"XWiki.XWikiGuest".equals(context.getUser()) && !inactiveWithoutMail) {
        obj.setIntValue("isactive", 1);
      } else {
        obj.setIntValue("isactive", 0);
      }
      obj.setStringValue("subscribed", blogDoc.getFullName());
      needsSave = true;
      LOGGER.info("new ReceiverObj is " + obj);
      subscribed = true;
    }
    if((obj != null) && (request.get("language") != null) 
        && (request.get("language").length() > 0) 
        && (!request.get("language").equals(obj.getStringValue("language")))) {
      obj.setStringValue("language", request.get("language"));
      needsSave = true;
    }
    if((obj != null) && (obj.getIntValue("isactive") == 1) && inactiveWithoutMail) {
      obj.setIntValue("isactive", 0);
      needsSave = true;
    }
    if(needsSave) {
      wiki.saveDocument(receiverDoc, context);
    }
    LOGGER.trace("getStringValue: " + obj.getStringValue("subscribed"));
    if((obj != null) && (obj.getIntValue("isactive") != 1) 
        && "XWiki.XWikiGuest".equals(context.getUser()) && !inactiveWithoutMail) {
      sendNewsletterActivationMail(obj, blogDoc, docName, request, context);
      subscribed = true;
    }
    if(!subscribed) { docName = ""; }
    return docName;
  }
  
  void sendNewsletterActivationMail(BaseObject obj, XWikiDocument blogDoc, String docName,
      Map<String, String> request, XWikiContext context) throws XWikiException {
    String email = request.get("emailadresse");
    email = email.toLowerCase();
    if(context.getWiki().exists("Tools.NewsletterSubscriptionActivation", context)) {
      VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
      vcontext.put("activationKey", getActivationKey(obj, docName));
      vcontext.put("blog", blogDoc);
      vcontext.put("email", email);
      XWikiDocument emailContentDoc = context.getWiki()
          .getDocument("Tools.NewsletterSubscriptionActivation", context);
      BaseObject blogConf = blogDoc.getObject("Celements2.BlogConfigClass");
      boolean embedImages = blogConf.getIntValue("newsletterEmbedImages", 0) == 1;
      vcontext.put("embedImages", embedImages);
      String emailContent = emailContentDoc.getTranslatedContent(context);
      String htmlContent = context.getWiki().getRenderingEngine().interpretText(
          emailContent, context.getDoc(), context);
      String emailTitle = emailContentDoc.getTranslatedDocument(context).getTitle();
      String renderedTitle = context.getWiki().getRenderingEngine().interpretText(
          emailTitle, context.getDoc(), context);
      String from = "";
      String reply = "";
      if(blogConf != null) {
        from = blogConf.getStringValue("from_address");
        reply = blogConf.getStringValue("reply_to_address");
      }
      if((from == null) || "".equals(from.trim())) {
        from = context.getWiki().getXWikiPreference("admin_email", context);
      }
      if((reply == null) || "".equals(reply.trim())) {
        reply = from;
      }
      CelementsWebPluginApi celementsweb = (CelementsWebPluginApi)context.getWiki()
          .getPluginApi("celementsweb", context);
      celementsweb.getPlugin().sendMail(from, reply, email, null, null, renderedTitle, 
          htmlContent, "", getAllAttachmentsList(), null, context);
    } else {
      LOGGER.error("No newsletter activation Mail sent for '" + email + "'. No " +
          "Mailcontent found in Tools.NewsletterSubscriptionActivation");
    }
  }

  List<Attachment> getAllAttachmentsList() {
    return ((INewsletterAttachmentServiceRole)Utils.getComponent(
        INewsletterAttachmentServiceRole.class)).getAttachmentList(true);
  }

  String getActivationKey(BaseObject obj, String docName) {
    String key = docName + "|" + obj.getNumber();
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-1");
      digest.update(key.getBytes());
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("SHA-1 algorithm not available.");
    }
    String hash = "";
    if(digest != null) {
      hash = new String((new Hex()).encode(digest.digest()));
    }
    return hash;
  }

  private String getSubscriberDocName(String email, XWikiContext context) 
      throws XWikiException {
    XWiki wiki = context.getWiki();
    String docName = getSubscriberDoc(email, context);
    if("".equals(docName) || !wiki.exists(docName, context)) {
      while("".equals(docName) || wiki.exists(docName, context)) {
        docName = DEFAULT_RECEIVER_SPACE + "." + wiki.generateRandomString(16);
      }
    }
    return docName;
  }
  
  public boolean unsubscribeNewsletter(XWikiContext context) throws XWikiException{
    boolean unsubscribed = false;
    String email = context.getRequest().get("emailadresse");
    email = email.toLowerCase();
    XWikiDocument blogDoc = getBlogDoc(null, context);
    String subsDocName = getSubscriberDoc(email, context);
    if(!"".equals(subsDocName) && context.getWiki().exists(subsDocName, context)) {
      XWikiDocument subscribedDoc = context.getWiki().getDocument(subsDocName, context);
      BaseObject subsObj = subscribedDoc.getObject("Celements.NewsletterReceiverClass", 
          "subscribed", blogDoc.getFullName(), false);
      if(subsObj != null) {
        subsObj.setIntValue("isactive", 0);
        context.getWiki().saveDocument(subscribedDoc, context);
        //TODO send email to notice unsubscription - necessary?
        unsubscribed = true;
      }
    }
    BaseObject obj = blogDoc.getObject("Celements2.ReceiverEMail", "email", email, false);
    if((obj != null) && (email != "")){
      obj.setIntValue("is_active", 0);
      context.getWiki().saveDocument(blogDoc, context);
      //TODO send email to notice unsubscription - necessary?
      unsubscribed = true;
    }
    
    return unsubscribed;
  }
  
  public String getSubscriberDoc(String email, XWikiContext context) 
      throws XWikiException {
    email = email.toLowerCase();
    String hql = "select distinct doc.fullName " +
        "from XWikiDocument as doc, BaseObject as obj, " +
        "Celements.NewsletterReceiverClass as nr " +
        "where doc.fullName=obj.name " +
        "and obj.id=nr.id " +
        "and nr.email='" + email + "'";
    List<String> docs = context.getWiki().search(hql, context);
    String docName = "";
    if((docs != null) && (docs.size() > 0)) {
      docName = docs.get(0);
    }
    return docName;
  }
  
  public boolean activateSubscriber(XWikiContext context) throws XWikiException{
    boolean activated = false;
    String email = context.getRequest().get("emailadresse");
    email = email.toLowerCase();
    String activationKey = context.getRequest().get("ak");
    String subscriberDocName = getSubscriberDoc(email, context);
    if(!"".equals(subscriberDocName.trim()) && context.getWiki().exists(subscriberDocName,
        context)) {
      XWikiDocument subscriberDoc = context.getWiki().getDocument(subscriberDocName, 
          context);
      List<BaseObject> subscriptions = subscriberDoc.getObjects(
          "Celements.NewsletterReceiverClass");
      if(subscriptions != null) {
        for (BaseObject subscription : subscriptions) {
          if((subscription != null) && (subscription.getIntValue("isactive") != 1) 
              && activationKey.equals(getActivationKey(subscription, 
              subscriberDoc.getFullName()))) {
            subscription.setIntValue("isactive", 1);
            context.getWiki().saveDocument(subscriberDoc, context);
            activated = true;
          }
        }
      }
    }
    return activated;
  }
  
  public XWikiDocument getBlogDoc(Map<String, String> request, XWikiContext context) 
      throws XWikiException {
    XWikiDocument doc = context.getDoc();
    if((doc.getObject("Celements2.BlogConfigClass") == null) && (doc.getObject(
        "XWiki.ArticleClass") != null)){
      doc = getBlogPageByBlogSpace(doc.getSpace(), context);
    }
    if(request != null) {
      String subscribeBlog = request.get("subsBlog");
      if((subscribeBlog != null) && !"".equals(subscribeBlog.trim()) && 
          context.getWiki().exists(subscribeBlog, context)) {
        doc = context.getWiki().getDocument(subscribeBlog, context);
      }
    }
    LOGGER.info("BlogDoc is " + doc.getFullName());
    return doc;
  }

  /**
   * Get the previous or next article in the blog. Does not take into account subscribed 
   * blogs or archived articles.
   * @param article
   * @param next true gets the next, false the previous article
   * @return
   */
  public Article getNeighbourArticle(Article article, boolean next, 
      XWikiContext context) {
    String aSpace = article.getDocName().substring(0, article.getDocName().indexOf('.'));
    List<Article> articles = null;
    try {
      articles = getBlogArticles(aSpace, "", context.getLanguage(), false, 
          false, false, false, true, true, true, false, true, true, context);
    } catch (ArticleLoadException exc) {
      LOGGER.error("could not get articles for blog", exc);
    }
    Article nArticle = null;
    if(articles != null) {
      for (int i = 0; i < articles.size(); i++) {
        Article tmpArt = articles.get(i);
        if(article.getDocName().equals(tmpArt.getDocName())) {
          if(next) {
            if((i+1) == articles.size()) {
              nArticle = articles.get(0);
            } else {
              nArticle = articles.get(i+1);
            }
          } else {
            if(i == 0) {
              nArticle = articles.get(articles.size() - 1);
            } else {
              nArticle = articles.get(i-1);
            }
          }
        }
      }
    }
    return nArticle;
  }

  private IBlogServiceRole getBlogService() {
    if (injected_BlogService != null) {
      return injected_BlogService; 
    }
    return Utils.getComponent(IBlogServiceRole.class);
  }

}
