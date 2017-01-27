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

import static java.nio.charset.StandardCharsets.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.blog.service.INewsletterAttachmentServiceRole;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.rendering.RenderCommand;
import com.celements.web.plugin.cmd.CelSendMail;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

public class NewsletterReceivers {

  private static Logger LOGGER = LoggerFactory.getLogger(NewsletterReceivers.class);
  private UserNameForUserDataCommand userNameForUserDataCmd = new UserNameForUserDataCommand();
  private RenderCommand renderCommand = new RenderCommand();

  private List<String> allAddresses = new ArrayList<>();
  private List<String[]> groups = new ArrayList<>();
  private List<String[]> groupUsers = new ArrayList<>();
  private List<String[]> users = new ArrayList<>();
  private List<String[]> addrLangs = new ArrayList<>();
  private List<String> addresses = new ArrayList<>();
  private List<EmailAddressDate> emailAddressDateList = new ArrayList<>();

  // use only if you inject the receivers!
  public NewsletterReceivers() {
  }

  @Deprecated
  public NewsletterReceivers(XWikiDocument blogDoc, XWikiContext context) throws XWikiException {
    addReceiverEMail(blogDoc);
    addNewsletterReceiver(blogDoc);
  }

  // TODO ADD UNIT TESTS!!!
  public NewsletterReceivers(XWikiDocument blogDoc) throws XWikiException {
    addReceiverEMail(blogDoc);
    addNewsletterReceiver(blogDoc);
  }

  void addNewsletterReceiver(XWikiDocument blogDoc) throws XWikiException {
    String blogFN = getWebUtilsService().serializeRef(blogDoc.getDocumentReference(), true);
    String xwql = "from doc.object(Celements.NewsletterReceiverClass) as nr "
        + "where nr.isactive = '1' and nr.subscribed = :subscribed";
    // String hql = "select nr.email,doc.date from Celements.NewsletterReceiverClass as nr, "
    // + " XWikiDocument as doc "
    // + "where doc.fullName = "
    // + " nr.isactive='1' "
    // + "and subscribed='" + blogDoc.getFullName() + "'";
    // List<String> nlRegAddresses = context.getWiki().search(hql, context);
    DocumentReference receverClassRef = getBlogClasses().getNewsletterReceiverClassRef(
        getContext().getDatabase());
    try {
      List<String> nlRegReceiverList = Utils.getComponent(QueryManager.class).createQuery(xwql,
          Query.XWQL).bindValue("subscribed", blogFN).execute();
      if (nlRegReceiverList != null) {
        LOGGER.info("Found " + nlRegReceiverList.size() + " Celements.NewsletterReceiverClass"
            + " object-subscriptions for blog " + blogFN);
        String blogSpace = blogDoc.getXObject(getBlogClasses().getBlogConfigClassRef(
            getContext().getDatabase())).getStringValue("blogspace");
        for (String nlRegReceiverFN : nlRegReceiverList) {
          DocumentReference nlRegReceiverDocRef = getWebUtilsService().resolveDocumentReference(
              nlRegReceiverFN);
          XWikiDocument receiverDoc = getContext().getWiki().getDocument(nlRegReceiverDocRef,
              getContext());
          List<BaseObject> recieverObjs = receiverDoc.getXObjects(receverClassRef);
          for (BaseObject receiverObj : recieverObjs) {
            String subscribedBlogs = receiverObj.getStringValue("subscribed");
            if ((subscribedBlogs != null) && (("," + subscribedBlogs + ",").indexOf("," + blogFN
                + ",") >= 0)) {
              String address = receiverObj.getStringValue("email");
              address = address.toLowerCase();
              if (!allAddresses.contains(address)) {
                String language = receiverObj.getStringValue("language");
                String firstname = "";
                String name = "";
                BaseObject contactObj = receiverDoc.getXObject(new DocumentReference(
                    getContext().getWiki().getDatabase(), "Celements", "ContactClass"));
                if (contactObj != null) {
                  firstname = contactObj.getStringValue("firstname");
                  name = contactObj.getStringValue("lastname");
                }
                if (getWebUtilsService().getAllowedLanguages(blogSpace).contains(language)) {
                  addrLangs.add(new String[] { "XWiki.XWikiGuest", address, language, firstname,
                      name });
                } else {
                  addresses.add(address);
                }
                allAddresses.add(address);
                emailAddressDateList.add(new EmailAddressDate(address, receiverDoc.getDate(),
                    language));
                LOGGER.info("reveiver added: " + address);
              }
            }
          }
        }
      }
    } catch (QueryException exp) {
      LOGGER.error("Failed to execute newsletter receiver xwql [" + xwql + "].", exp);
    }
  }

  void addReceiverEMail(XWikiDocument blogDoc) throws XWikiException {
    List<BaseObject> objs = blogDoc.getXObjects(getBlogClasses().getReceiverEMailClassRef(
        getContext().getDatabase()));
    LOGGER.debug("objs.size = " + (objs != null ? objs.size() : 0));
    if (objs != null) {
      for (BaseObject obj : objs) {
        LOGGER.debug("obj: " + obj);
        if (obj != null) {
          String receiverAdr = obj.getStringValue("email");
          String address = receiverAdr.toLowerCase();
          boolean active = (obj.getIntValue("is_active") == 1);
          boolean isMail = address.matches(
              "[\\w\\.]{1,}[@][\\w\\-\\.]{1,}([.]([\\w\\-\\.]{1,})){1,3}$");
          String type = obj.getStringValue("address_type");
          if (isMail && active && (!allAddresses.contains(address))) {
            addresses.add(address);
            allAddresses.add(address);
            emailAddressDateList.add(new EmailAddressDate(address, blogDoc.getDate(), null));
            LOGGER.info("reveiver added: " + address);
          } else {
            if (getContext().getWiki().exists(receiverAdr, getContext())) {
              parseDocument(receiverAdr, type, getContext());
            }
          }
        }
      }
    }
  }

  private BlogClasses getBlogClasses() {
    return (BlogClasses) Utils.getComponent(IClassCollectionRole.class, "celements.celBlogClasses");
  }

  private void parseDocument(String address, String type, XWikiContext context)
      throws XWikiException {
    XWikiDocument recDoc = context.getWiki().getDocument(address, context);
    BaseObject userObj = recDoc.getObject("XWiki.XWikiUsers");
    List<BaseObject> groupObjs = recDoc.getObjects("XWiki.XWikiGroups");
    if (userObj != null) {
      String email = userObj.getStringValue("email").toLowerCase();
      String language = userObj.getStringValue("admin_language");
      String firstname = userObj.getStringValue("first_name");
      String name = userObj.getStringValue("last_name");
      if ((email.trim().length() > 0) && (!allAddresses.contains(email))) {
        users.add(new String[] { recDoc.getFullName(), email, language, firstname, name });
        allAddresses.add(email);
        emailAddressDateList.add(new EmailAddressDate(email, recDoc.getDate(), language));
      }
    } else if ((groupObjs != null) && (groupObjs.size() > 0)) {
      int usersInGroup = parseGroupMembers(groupObjs, type, context);
      groups.add(new String[] { recDoc.getFullName(), Integer.toString(usersInGroup) });
    }
  }

  private int parseGroupMembers(List<BaseObject> groupObjs, String type, XWikiContext context)
      throws XWikiException {
    int usersInGroup = 0;
    for (BaseObject groupObj : groupObjs) {
      if ((groupObj != null) && (groupObj.getStringValue("member") != null)) {
        String userDocName = groupObj.getStringValue("member");
        if ((userDocName.trim().length() > 0) && context.getWiki().exists(userDocName, context)) {
          XWikiDocument userDoc = context.getWiki().getDocument(userDocName, context);
          BaseObject groupUserObj = userDoc.getObject("XWiki.XWikiUsers");
          if (groupUserObj != null) {
            String email = groupUserObj.getStringValue("email").toLowerCase();
            String language = groupUserObj.getStringValue("admin_language");
            String firstname = groupUserObj.getStringValue("first_name");
            String name = groupUserObj.getStringValue("last_name");
            if ((email.trim().length() > 0) && (!allAddresses.contains(email))) {
              usersInGroup++;
              allAddresses.add(email);
              emailAddressDateList.add(new EmailAddressDate(email, userDoc.getDate(), language));
              groupUsers.add(new String[] { userDocName, email, language, firstname, name });
            }
          }
        }
      }
    }
    return usersInGroup;
  }

  public List<String[]> sendArticleByMail(XWikiContext context) throws XWikiException {
    XWikiRequest request = context.getRequest();
    String articleName = request.get("sendarticle");
    String from = request.get("from");
    String replyTo = request.get("reply_to");
    String subject = request.get("subject");
    String testSend = request.get("testSend");

    boolean isTest = false;
    if ((testSend != null) && testSend.equals("1")) {
      isTest = true;
    }

    XWiki wiki = context.getWiki();
    List<String[]> result = new ArrayList<>();
    int successfullySent = 0;

    LOGGER.debug("articleName = " + articleName);
    LOGGER.debug("article exists = " + wiki.exists(articleName, context));
    if ((articleName != null) && (!"".equals(articleName.trim())) && (wiki.exists(articleName,
        context))) {
      XWikiDocument doc = wiki.getDocument(articleName, context);
      String baseURL = doc.getExternalURL("view", context);

      List<String[]> allUserMailPairs = null;
      LOGGER.debug("is test send: " + isTest);
      if (isTest) {
        String user = context.getUser();
        XWikiDocument userDoc = context.getWiki().getDocument(user, context);
        BaseObject userObj = userDoc.getObject("XWiki.XWikiUsers");
        if (userObj != null) {
          String email = userObj.getStringValue("email");
          if (email.trim().length() > 0) {
            allUserMailPairs = new ArrayList<>();
            String[] userFields = getUserAdminLanguage(user,
                getWebUtilsService().getDefaultLanguage());
            allUserMailPairs.add((String[]) ArrayUtils.addAll(new String[] { user, email },
                userFields));
          }
        }
      } else {
        allUserMailPairs = getNewsletterReceiversList();
      }

      String origUser = context.getUser();
      String origLanguage = context.getLanguage();
      VelocityContext vcontext = (VelocityContext) context.get("vcontext");
      Object origAdminLanguage = vcontext.get("admin_language");
      Object origMsgTool = vcontext.get("msg");
      Object origAdminMsgTool = vcontext.get("adminMsg");
      for (String[] userMailPair : allUserMailPairs) {
        String[] sendResult = sendNewsletterToOneReceiver(from, replyTo, subject, doc, baseURL,
            userMailPair, context);
        if ("0".equals(sendResult[1])) {
          successfullySent++;
        }
        result.add(sendResult);
      }
      context.setUser(origUser);
      context.setLanguage(origLanguage);
      vcontext.put("language", origLanguage);
      vcontext.put("admin_language", origAdminLanguage);
      vcontext.put("msg", origMsgTool);
      vcontext.put("adminMsg", origAdminMsgTool);

      setNewsletterSentObject(doc, from, replyTo, subject, successfullySent, isTest, context);
    }

    return result;
  }

  public List<String[]> sendNewsletterToInjectedReceiverList(List<DocumentReference> receivers,
      String from, String replyTo, String subject, XWikiDocument contentDoc, String baseURL) {
    List<String[]> results = Collections.emptyList();
    if ((receivers != null) && (receivers.size() > 0)) {
      results = new ArrayList<>();
      for (DocumentReference receiverDocRef : receivers) {
        try {
          XWikiDocument receiverDoc = getContext().getWiki().getDocument(receiverDocRef,
              getContext());
          BaseObject receiverObj = receiverDoc.getXObject(new DocumentReference(
              getContext().getDatabase(), "Celements", "NewsletterReceiverClass"));
          if (receiverObj.getIntValue("isactive") == 1) {
            String[] recData = new String[] { "XWiki.XWikiGuest", receiverObj.getStringValue(
                "email"), receiverObj.getStringValue("language"), "", "" };
            LOGGER.warn("Sending newsletter via injected list to [" + recData[1] + "]");
            sendNewsletterToOneReceiver(from, replyTo, subject, contentDoc, baseURL, recData,
                getContext());
          }
        } catch (XWikiException xwe) {
          LOGGER.error("Newsletter send via injected list failed", xwe);
        }
      }
    }
    return results;
  }

  String[] sendNewsletterToOneReceiver(String from, String replyTo, String subject,
      XWikiDocument doc, String baseURL, String[] userMailPair, XWikiContext context)
      throws XWikiException {
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    String[] result;
    LOGGER.debug("userMailPair: " + ArrayUtils.toString(userMailPair));
    context.setUser(userMailPair[0]);
    String language = userMailPair[2];
    context.setLanguage(language);
    vcontext.put("firstname", userMailPair[3]);
    vcontext.put("name", userMailPair[4]);
    vcontext.put("email", userMailPair[1]);
    vcontext.put("language", language);
    vcontext.put("newsletter_language", language);
    vcontext.put("admin_language", language);
    XWikiMessageTool msgTool = getWebUtilsService().getMessageTool(language);
    vcontext.put("msg", msgTool);
    vcontext.put("adminMsg", msgTool);

    if (context.getWiki().checkAccess("view", doc, context)) {
      String senderContextLang = context.getLanguage();
      context.setLanguage(language);
      String htmlContent = getHtmlContent(doc, baseURL);
      context.setLanguage(language);
      htmlContent += getUnsubscribeFooter(userMailPair[1], doc);
      context.setLanguage(senderContextLang);
      XWikiMessageTool messageTool = getWebUtilsService().getMessageTool(language);
      String textContent = messageTool.get("cel_newsletter_text_only_message", Arrays.asList(
          doc.getExternalURL("view", context)));
      textContent += getUnsubscribeFooter(userMailPair[1], doc);

      int singleResult = sendMail(from, replyTo, userMailPair[1], subject, baseURL, htmlContent,
          textContent, context);
      result = new String[] { userMailPair[1], Integer.toString(singleResult) };
    } else {
      LOGGER.warn("Tried to send " + doc + " to user " + userMailPair[0] + " which"
          + " has no view rights on this Document.");
      List<String> params = new ArrayList<>();
      params.add(doc.toString());
      XWikiMessageTool messageTool = getWebUtilsService().getMessageTool(language);
      result = new String[] { userMailPair[1], messageTool.get(
          "cel_blog_newsletter_receiver_no_rights", params) };
    }
    return result;
  }

  List<String[]> getNewsletterReceiversList() {
    ArrayList<String[]> allUserMailPairs = new ArrayList<>();
    allUserMailPairs.addAll(groupUsers);
    allUserMailPairs.addAll(users);
    allUserMailPairs.addAll(addrLangs);
    String defaultLanguage = getWebUtilsService().getDefaultLanguage();
    for (String address : addresses) {
      String mailUser = "XWiki.XWikiGuest";
      String[] userFields = new String[] { defaultLanguage, "", "" };
      String addrUser = null;
      try {
        addrUser = userNameForUserDataCmd.getUsernameForUserData(address, "email", getContext());
      } catch (XWikiException e) {
        LOGGER.error("Exception getting username for user email '" + address + "'.", e);
      }
      if ((addrUser != null) && (addrUser.length() > 0)) {
        mailUser = addrUser;
        userFields = getUserAdminLanguage(mailUser, defaultLanguage);
      }
      allUserMailPairs.add((String[]) ArrayUtils.addAll(new String[] { mailUser, address },
          userFields));
    }
    return allUserMailPairs;
  }

  private String[] getUserAdminLanguage(String mailUser, String defaultLanguage) {
    String userLanguage = defaultLanguage;
    String firstname = "";
    String name = "";
    try {
      DocumentReference userDocRef = getWebUtilsService().resolveDocumentReference(mailUser);
      XWikiDocument mailUserDoc = getContext().getWiki().getDocument(userDocRef, getContext());
      BaseObject mailUserObj = mailUserDoc.getXObject(new DocumentReference(
          userDocRef.getWikiReference().getName(), "XWiki", "XWikiUsers"));
      String userAdminLanguage = mailUserObj.getStringValue("admin_language");
      if ((userAdminLanguage != null) && !"".equals(userAdminLanguage)) {
        userLanguage = userAdminLanguage;
      }
      firstname = mailUserObj.getStringValue("first_name");
      name = mailUserObj.getStringValue("last_name");
    } catch (XWikiException exp) {
      LOGGER.error("Exception getting userdoc to find admin-language ['" + mailUser + "]'.", exp);
    }
    return new String[] { userLanguage, firstname, name };
  }

  private String getUnsubscribeFooter(String emailAddress, XWikiDocument blogDocument)
      throws XWikiException {
    String unsubscribeFooter = "";
    if (!"".equals(getUnsubscribeLink(blogDocument.getSpace(), emailAddress))) {
      XWikiMessageTool messageTool = getWebUtilsService().getMessageTool(
          getContext().getLanguage());
      unsubscribeFooter = messageTool.get("cel_newsletter_unsubscribe_footer", Arrays.asList(
          getUnsubscribeLink(blogDocument.getSpace(), emailAddress)));
    }
    return unsubscribeFooter;
  }

  String getUnsubscribeLink(String blogSpace, String emailAddresse) throws XWikiException {
    String unsubscribeLink = "";
    XWikiDocument blogDocument = BlogUtils.getInstance().getBlogPageByBlogSpace(blogSpace,
        getContext());
    BaseObject blogObj = blogDocument.getObject("Celements2.BlogConfigClass", false, getContext());
    if ((blogObj != null) && (blogObj.getIntValue("unsubscribe_info") == 1)) {
      try {
        unsubscribeLink = blogDocument.getExternalURL("view",
            "xpage=celements_ajax&ajax_mode=BlogAjax&doaction=unsubscribe&emailadresse="
                + URLEncoder.encode(emailAddresse, UTF_8.name()), getContext());
      } catch (UnsupportedEncodingException e) {
        LOGGER.error("UTF-8 unsupported.");
      }
    }
    return unsubscribeLink;
  }

  private String getHtmlContent(XWikiDocument doc, String baseURL) throws XWikiException {
    String header = "";
    if ((baseURL != null) && !"".equals(baseURL.trim())) {
      header = "<base href='" + baseURL + "' />\n";
    }
    String renderLang = getContext().getLanguage();
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    XWikiMessageTool msgTool = getWebUtilsService().getMessageTool(renderLang);
    DocumentReference headerRef = getWebUtilsService().resolveDocumentReference(
        "LocalMacros.NewsletterHTMLheader");
    if (getContext().getWiki().exists(headerRef, getContext())) {
      LOGGER.debug("Additional header found.");
      LOGGER.debug("doc=" + doc + ", context.language=" + getContext().getLanguage());
      LOGGER.debug("context=" + getContext());
      vcontext.put("msg", msgTool);
      vcontext.put("adminMsg", msgTool);
      header += renderCommand.renderDocument(headerRef, renderLang);
      LOGGER.debug("Additional header rendered.");
    } else {
      LOGGER.debug("No additional header. Doc does not exist: " + headerRef);
    }
    LOGGER.debug("rendering content in " + renderLang);
    getContext().setLanguage(renderLang);
    renderCommand.setDefaultPageType("RichText");
    vcontext.put("msg", msgTool);
    vcontext.put("adminMsg", msgTool);
    String content = renderCommand.renderCelementsDocument(doc.getDocumentReference(), renderLang,
        "view");
    content = Utils.replacePlaceholders(content, getContext());
    if (getContext().getWiki().getXWikiPreferenceAsInt("newsletter_embed_all_images",
        "celements.newsletter.embedAllImages", 0, getContext()) == 1) {
      content = getNewsletterAttachmentService().embedImagesInContent(content);
    }
    String footer = "";
    DocumentReference footerRef = getWebUtilsService().resolveDocumentReference(
        "LocalMacros.NewsletterHTMLfooter");
    if (getContext().getWiki().exists(footerRef, getContext())) {
      getContext().setLanguage(renderLang);
      LOGGER.debug("Additional footer found.");
      LOGGER.debug("doc=" + doc + ", context.language=" + getContext().getLanguage());
      LOGGER.debug("context=" + getContext());
      vcontext.put("msg", msgTool);
      vcontext.put("adminMsg", msgTool);
      footer += renderCommand.renderDocument(footerRef, renderLang) + "\n";
      LOGGER.debug("Additional footer rendered.");
    } else {
      LOGGER.debug("No additional footer. Doc does not exist: " + footerRef);
    }
    XWikiMessageTool messageTool = getWebUtilsService().getMessageTool(renderLang);
    footer += messageTool.get("cel_newsletter_html_footer_message", Arrays.asList(
        doc.getExternalURL("view", getContext())));
    LOGGER.debug("Header: [" + header + "]");
    LOGGER.debug("Footer: [" + footer + "]");
    return header + content + footer;
  }

  private int sendMail(String from, String replyTo, String to, String subject, String baseURL,
      String htmlContent, String textContent, XWikiContext context) throws XWikiException {
    try {
      if ((to != null) && (to.trim().length() == 0)) {
        to = null;
      }
      Map<String, String> otherHeader = new HashMap<>();
      otherHeader.put("Content-Location", baseURL);

      LOGGER.info("NewsletterReceivers: sendMail from [" + from + "], replyTo [" + replyTo
          + "], to [" + to + "], subject [" + subject + "].");
      CelSendMail sender = new CelSendMail();
      sender.setFrom(from);
      sender.setReplyTo(replyTo);
      sender.setTo(to);
      sender.setSubject(subject);
      sender.setHtmlContent(htmlContent, false);
      sender.setTextContent(textContent);
      sender.setOthers(otherHeader);
      sender.setAttachments(getNewsletterAttachmentService().getAttachmentList(true));
      return sender.sendMail();
    } finally {
      getNewsletterAttachmentService().clearAttachmentList();
    }
  }

  private void setNewsletterSentObject(XWikiDocument doc, String from, String replyTo,
      String subject, int nrOfSent, boolean isTest, XWikiContext context) throws XWikiException {
    BaseObject configObj = doc.getObject("Classes.NewsletterConfigClass");
    if (configObj == null) {
      configObj = doc.newObject("Classes.NewsletterConfigClass", context);
    }

    configObj.set("from_address", from, context);
    configObj.set("reply_to_address", replyTo, context);
    configObj.set("subject", subject, context);

    if ((nrOfSent > 0) && !isTest) {
      setNewsletterHistory(configObj, nrOfSent, context);
    }

    context.getWiki().saveDocument(doc, context);
  }

  private void setNewsletterHistory(BaseObject configObj, int nrOfSent, XWikiContext context) {
    int timesSent = configObj.getIntValue("times_sent");
    configObj.set("times_sent", timesSent + 1, context);
    configObj.set("last_sent_date", new Date(), context);
    configObj.set("last_sender", context.getUser(), context);
    configObj.set("last_sent_recipients", nrOfSent, context);
  }

  public boolean hasReceivers() {
    return getAllAddresses().size() > 0;
  }

  public boolean hasReceiverGroups() {
    return getGroups().size() > 0;
  }

  public boolean hasSingleReceivers() {
    return (getUsers().size() > 0) || (getAddresses().size() > 0);
  }

  public boolean hasUsers() {
    return getUsers().size() > 0;
  }

  public boolean hasAdresses() {
    return getAddresses().size() > 0;
  }

  public List<String> getAllAddresses() {
    return allAddresses;
  }

  public List<String[]> getGroups() {
    return groups;
  }

  public List<String[]> getUsers() {
    return users;
  }

  public List<String> getAddresses() {
    return addresses;
  }

  public List<EmailAddressDate> getAddressesOrderByDate() {
    return emailAddressDateList;
  }

  public int getNrOfReceivers() {
    return allAddresses.size();
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  private INewsletterAttachmentServiceRole getNewsletterAttachmentService() {
    return Utils.getComponent(INewsletterAttachmentServiceRole.class);
  }

}
