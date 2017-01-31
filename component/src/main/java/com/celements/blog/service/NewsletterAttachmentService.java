package com.celements.blog.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class NewsletterAttachmentService implements INewsletterAttachmentServiceRole {

  public static final String DEFAULT_NL_NO_IMG_ATT_LIST = "nlEmbedNoImgAttList";

  public static final String DEFAULT_NL_ATTACHMENT_LIST = "nlEmbedAttList";

  private static final Logger LOGGER = LoggerFactory.getLogger(NewsletterAttachmentService.class);

  @Requirement
  Execution execution;

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  IAttachmentServiceRole attService;

  @Override
  public String embedImagesInContent(String content) {
    Pattern pattern = Pattern.compile("<img .*?>");
    Matcher matcher = pattern.matcher(content);
    Set<String> images = new HashSet<>();
    while (matcher.find()) {
      images.add(matcher.group());
    }
    return embedImagesInContent(content, images);
  }

  String embedImagesInContent(String content, Set<String> imgTags) {
    for (String tag : imgTags) {
      String strippedTag = tag.replaceAll(".*src=\"(.*?)\\?.*?\".*", "$1");
      String imgFullname = strippedTag.replaceAll("((http://)?[a-zA-Z0-9\\.-_]*)?/?"
          + "(download)?/(.*)/(.*)/(.*?)", "$4.$5;$6");
      String replStr = Pattern.quote(tag.replaceAll(".*src=\"(.*?)\".*", "$1"));
      content = content.replaceAll(replStr, getImageURL(imgFullname, true));
    }
    return content;
  }

  @Override
  public String getImageURL(String imgFullname, boolean embedImage) {
    String imgURL = "";
    AttachmentURLCommand attURL = new AttachmentURLCommand();
    if (embedImage) {
      extendAttachmentList(getAttachmentForFullname(imgFullname), DEFAULT_NL_ATTACHMENT_LIST);
      imgURL = "cid:" + attURL.getAttachmentName(imgFullname);
    } else {
      imgURL = attURL.getAttachmentURL(imgFullname, "download", getContext());
    }
    return imgURL;
  }

  @Override
  public void addAttachment(String attFullname) {
    Attachment att = getAttachmentForFullname(attFullname);
    extendAttachmentList(att, DEFAULT_NL_ATTACHMENT_LIST);
    extendAttachmentList(att, DEFAULT_NL_NO_IMG_ATT_LIST);
  }

  @Override
  public List<Attachment> getAttachmentList(boolean includingImages) {
    String param = DEFAULT_NL_ATTACHMENT_LIST;
    if (!includingImages) {
      param = DEFAULT_NL_NO_IMG_ATT_LIST;
    }
    return getAttachmentList(param, false);
  }

  @SuppressWarnings("unchecked")
  List<Attachment> getAttachmentList(String param, boolean create) {
    Object contextVal = getVcontext().get(param);
    List<Attachment> embedList = null;
    if ((contextVal instanceof List<?>) && !((List<Attachment>) contextVal).isEmpty()
        && (((List<Attachment>) contextVal).get(0) instanceof Attachment)) {
      embedList = (List<Attachment>) contextVal;
    }
    if ((embedList == null) && create) {
      embedList = new ArrayList<Attachment>();
    }
    return embedList;
  }

  void extendAttachmentList(Attachment att, String param) {
    List<Attachment> attList = getAttachmentList(param, true);
    attList.add(att);
    getVcontext().put(param, attList);
  }

  Attachment getAttachmentForFullname(String imgFullname) {
    AttachmentURLCommand attURL = new AttachmentURLCommand();
    Attachment att = null;
    try {
      XWikiDocument attDoc = getContext().getWiki().getDocument(webUtils.resolveDocumentReference(
          attURL.getPageFullName(imgFullname)), getContext());
      XWikiAttachment xatt = attService.getAttachmentNameEqual(attDoc, attURL.getAttachmentName(
          imgFullname));
      att = attService.getApiAttachment(xatt);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception getting attachment Document.", xwe);
    } catch (AttachmentNotExistsException anee) {
      LOGGER.error("Attachment [{}] not found.", imgFullname, anee);
    } catch (NoAccessRightsException nore) {
      LOGGER.error("No access rights on attachment [{}]", imgFullname, nore);
    }
    return att;
  }

  XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  VelocityContext getVcontext() {
    return (VelocityContext) (getContext().get("vcontext"));
  }

  @Override
  public void clearAttachmentList() {
    clearAttachmentList(DEFAULT_NL_ATTACHMENT_LIST);
    clearAttachmentList(DEFAULT_NL_NO_IMG_ATT_LIST);
  }

  public void clearAttachmentList(String param) {
    if (getVcontext().containsKey(param)) {
      getVcontext().remove(param);
    }
  }

}
