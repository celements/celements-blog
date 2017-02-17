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
package com.celements.blog.article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.plugin.BlogClasses;
import com.celements.blog.plugin.EmptyArticleException;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.metatag.MetaTag;
import com.celements.metatag.MetaTagServiceRole;
import com.celements.metatag.enums.opengraph.EOpenGraph;
import com.celements.metatag.enums.twitter.ETwitter;
import com.celements.model.util.ModelUtils;
import com.celements.web.plugin.cmd.ConvertToPlainTextException;
import com.celements.web.plugin.cmd.PlainTextCommand;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.api.Property;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class Article extends Api {

  private static Logger LOGGER = LoggerFactory.getLogger(Article.class);

  public static final String BLOG_ARTICLE_SOCIAL_MEDIA_CONF_NAME = "blog.article.socialmediatags.active";
  public static final String BLOG_ARTICLE_TWITTER_SITE = "blog.twitter.account";
  public static final String BLOG_ARTICLE_TWITTER_CARD_TYPE = "blog.twitter.card.type";

  public static int MIN_SOCIAL_MEDIA_IMAGE_SIZE = 200;
  public static int MIN_SOCIAL_MEDIA_AREA_SIZE = MIN_SOCIAL_MEDIA_IMAGE_SIZE
      * MIN_SOCIAL_MEDIA_IMAGE_SIZE;

  private Map<String, com.xpn.xwiki.api.Object> articleObjMap;
  private Boolean isSubscribed;
  private Map<String, String> extract;
  private Map<String, Boolean> hasMoreLink;
  private Map<String, Boolean> hasMoreLinkDots;
  private String defaultLang;
  private DocumentReference articleDocRef;

  IBlogServiceRole injected_blogService;

  /**
   * For Test Use only!!!
   */
  Article(XWikiContext context) {
    super(context);
  }

  public Article(XWikiDocument articleDoc, XWikiContext context) throws XWikiException,
      EmptyArticleException {
    this(articleDoc.newDocument(context), context);
  }

  public Article(Document articleDoc, XWikiContext context) throws XWikiException,
      EmptyArticleException {
    this(articleDoc.getObjects("XWiki.ArticleClass"), articleDoc.getDocumentReference(), context);
  }

  public Article(List<com.xpn.xwiki.api.Object> objList, String space, XWikiContext context)
      throws XWikiException, EmptyArticleException {
    this(objList, getDocRefForObjList(objList, space), context);
  }

  public Article(List<com.xpn.xwiki.api.Object> objList, DocumentReference docRef,
      XWikiContext context) throws XWikiException, EmptyArticleException {
    super(context);
    articleDocRef = docRef;
    for (Object artObj : objList) {
      init(artObj, docRef.getLastSpaceReference().getName());
    }
    if (articleObjMap == null) {
      throw new EmptyArticleException();
    }
  }

  private static DocumentReference getDocRefForObjList(List<com.xpn.xwiki.api.Object> objList,
      String space) {
    for (Object artObj : objList) {
      if (artObj != null) {
        String fn = artObj.getName();
        if (!fn.startsWith(space + ".")) {
          fn = fn.replaceAll("^.*?(\\..*)$", space + "$1");
        }
        return getModelUtils().resolveRef(fn, DocumentReference.class);
      }
    }
    return null;
  }

  public void init(com.xpn.xwiki.api.Object obj, String space) {
    if (articleObjMap == null) {
      articleObjMap = new HashMap<>();
      defaultLang = context.getWiki().getSpacePreference("default_language", space, "", context);
    }
    LOGGER.debug("Init Article Object");
    if (obj != null) {
      Property prop = obj.getProperty("lang");
      if (prop != null) {
        articleObjMap.put((String) prop.getValue(), obj);
      } else {
        articleObjMap.put("", obj);
      }
    }
  }

  private void setSubscribed(DocumentReference blogConfDocRef) {
    XWikiDocument doc = null;
    // TODO why loading doc&objects here when already loaded articleObjMap could be used?
    try {
      doc = context.getWiki().getDocument(getDocName(), context);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception loading document", xwe);
    }
    if (doc != null) {
      String blogConfFN = Utils.getComponent(
          IWebUtilsService.class).getRefLocalSerializer().serialize(blogConfDocRef);
      BaseObject obj = doc.getObject("Celements2.BlogArticleSubscriptionClass", "subscriber",
          blogConfFN, false);
      LOGGER.info("Search for object with subscriber == '" + blogConfFN + "' had result: " + obj);
      if (obj != null) {
        int isSubscr = obj.getIntValue("doSubscribe");
        LOGGER.info("'" + doc.getFullName() + "' doSubscribe is: '" + isSubscr + "'");
        if (isSubscr == 1) {
          isSubscribed = true;
        } else if (isSubscr == 0) {
          isSubscribed = false;
        }
      } else {
        isSubscribed = null;
        LOGGER.info("'" + doc.getFullName() + "' doSubscribe is: '" + isSubscribed + "'");
      }
    }
  }

  public Date getPublishDate() {
    Date date = getDateProperty(getObj(defaultLang), "publishdate");
    return date;
  }

  public Date getPublishDate(String lang) {
    Date date = getDateProperty(getObj(lang), "publishdate");
    return date;
  }

  public Date getArchiveDate() {
    Date date = getDateProperty(getObj(defaultLang), "archivedate");
    return date;
  }

  public Date getArchiveDate(String lang) {
    Date date = getDateProperty(getObj(lang), "archivedate");
    return date;
  }

  /**
   * Get the effective language of the title. E.g. getTitleLang('fr') returns 'de' if default is
   * 'de' and there is no 'fr' translation.
   *
   * @param lang
   * @return
   */
  public String getTitleLang(String lang) {
    return getTitleDetailed(lang)[0];
  }

  public String getTitle(String lang) {
    return getTitleDetailed(lang)[1];
  }

  String[] getTitleDetailed(String lang) {
    String title = getStringProperty(getObj(lang), "title");
    if ((title == null) || "".equals(title.trim())) {
      title = getStringProperty(getObj(defaultLang), "title");
      lang = defaultLang;
    }
    return new String[] { lang, title };
  }

  int getMaxNumChars() {
    int maxNumChars = 250;
    String blogSpaceName = getDocumentReference().getLastSpaceReference().getName();
    XWikiDocument blogDoc = getBlogService().getBlogPageByBlogSpace(blogSpaceName);
    if (blogDoc != null) {
      BaseObject blogConfigObj = blogDoc.getXObject(getBlogClasses().getBlogConfigClassRef(
          getContext().getDatabase()));
      if ((blogConfigObj != null) && (blogConfigObj.getIntValue(
          BlogClasses.PROPERTY_BLOG_CONFIG_MAX_NUM_CHARS_FIELD, -1) > 0)) {
        maxNumChars = blogConfigObj.getIntValue(
            BlogClasses.PROPERTY_BLOG_CONFIG_MAX_NUM_CHARS_FIELD);
      }
    } else {
      LOGGER.info("BlogConfig document not found for space name [" + blogSpaceName + "].");
    }
    return maxNumChars;
  }

  /**
   * Get the effective language of the title. E.g. getExtractLang('fr', x) returns 'de' if default
   * is 'de' and there is no 'fr' translation.
   *
   * @param lang
   * @return
   */
  public String getExtractLang(String lang, boolean isViewtypeFull) {
    return getExtractDetailed(lang, isViewtypeFull, getMaxNumChars())[0];
  }

  public String getExtract(String lang, boolean isViewtypeFull) {
    return getExtractDetailed(lang, isViewtypeFull, getMaxNumChars())[1];
  }

  public String getExtract(String lang, boolean isViewtypeFull, int maxNumChars) {
    return getExtractDetailed(lang, isViewtypeFull, maxNumChars)[1];
  }

  public String[] getExtractDetailed(String lang, boolean isViewtypeFull, int maxNumChars) {
    String effectiveLang = lang;
    LOGGER.info("getExtract('" + lang + "', " + isViewtypeFull + "')");
    if ((extract == null) || !extract.containsKey(lang)) {
      String fullExtract = getStringProperty(getObj(lang), "extract");
      boolean needsMoreLink = true;
      boolean needsMoreLinkDots = false;
      if (isEmptyStringAndNotDefLang(fullExtract, lang)) {
        fullExtract = getStringProperty(getObj(defaultLang), "extract");
        effectiveLang = defaultLang;
      }
      if (fullExtract.trim().equals("") || isViewtypeFull) {
        fullExtract = getFullArticle(lang);
        needsMoreLink = false;
        if (isEmptyStringAndNotDefLang(fullExtract, lang)) {
          fullExtract = getFullArticle(defaultLang);
          effectiveLang = defaultLang;
        } else {
          effectiveLang = lang;
        }
        if (!isViewtypeFull && (fullExtract.length() > maxNumChars)) {
          fullExtract = fullExtract.substring(0, fullExtract.lastIndexOf(" ", maxNumChars) + 1);
          needsMoreLink = true;
          needsMoreLinkDots = true;
        }
      }

      if (extract == null) {
        extract = new HashMap<>();
      }
      extract.put(lang, fullExtract);
      if (hasMoreLink == null) {
        hasMoreLink = new HashMap<>();
      }
      hasMoreLink.put(lang, needsMoreLink);
      if (hasMoreLinkDots == null) {
        hasMoreLinkDots = new HashMap<>();
      }
      hasMoreLinkDots.put(lang, needsMoreLinkDots);
    }

    String extr = extract.get(lang);
    // mLogger.info("getExtract('" + lang + "', " + isViewtypeFull + "') => '" + extr + "'");
    // TODO check if this code is unreachable / senseless since last refactoring, since
    // fallback to default language is already handled earlier
    // if(isEmptyStringAndNotDefLang(extr, lang)){
    // extr = getExtract(defaultLang, isViewtypeFull);
    // }
    return new String[] { effectiveLang, extr };
  }

  boolean isEmptyStringAndNotDefLang(String string, String lang) {
    return (string.trim().length() <= 0) && !lang.equals(defaultLang);
  }

  public String getFullArticle(String lang) {
    return getStringProperty(getObj(lang), "content");
  }

  public String getEditor(String lang) {
    return getStringProperty(getObj(lang), "blogeditor");
  }

  public Boolean isCommentable() {
    return getBooleanProperty(getObj(defaultLang), "hasComments");
  }

  public Boolean isCommentable(String lang) {
    return getBooleanProperty(getObj(lang), "hasComments");
  }

  public Boolean isSubscribable() {
    return getBooleanProperty(getObj(defaultLang), "isSubscribable");
  }

  // Attention! May return null if not set so use -> if(isSubscribable(lang) == true)
  public Boolean isSubscribable(String lang) {
    return getBooleanProperty(getObj(lang), "isSubscribable");
  }

  public boolean isFromSubscribableBlog(String blogArticleSpace) {
    boolean result = true;
    if (getDocName().startsWith(blogArticleSpace + ".")) {
      result = false;
    }
    return result;
  }

  public DocumentReference getDocumentReference() {
    if (articleObjMap.size() > 0) {
      String firstKey = articleObjMap.keySet().iterator().next();
      com.xpn.xwiki.api.Object articleObj = articleObjMap.get(firstKey);
      LOGGER.debug("getDocumentReference: for [" + articleObj.getName() + "].");
      return getWebService().resolveDocumentReference(articleObj.getName());
    }
    return null;
  }

  /**
   * @deprecated since 2.18.0 instead use getDocumentReference()
   */
  @Deprecated
  public String getDocName() {
    for (String key : articleObjMap.keySet()) {
      return articleObjMap.get(key).getName();
    }
    return "";
  }

  public String getStringProperty(com.xpn.xwiki.api.Object obj, String name) {
    String result = "";
    if (obj != null) {
      Property prop = obj.getProperty(name);
      if (prop != null) {
        result = prop.getValue().toString();
      }
    }
    return result;
  }

  public Date getDateProperty(com.xpn.xwiki.api.Object obj, String name) {
    Date result = null;
    if (obj != null) {
      Property prop = obj.getProperty(name);
      if (prop != null) {
        result = (Date) prop.getValue();
      }
    }
    return result;
  }

  // not set = null, false = 0, true = 1
  public Boolean getBooleanProperty(com.xpn.xwiki.api.Object obj, String name) {
    Boolean result = null;
    if (obj != null) {
      Property prop = obj.getProperty(name);
      if (prop != null) {
        Integer val = (Integer) prop.getValue();
        if (val != null) {
          if (val == 1) {
            result = true;
          } else if (val == 0) {
            result = false;
          }
        }
      }
    }
    return result;
  }

  public com.xpn.xwiki.api.Object getObj(String lang) {
    com.xpn.xwiki.api.Object obj = null;
    if (articleObjMap.containsKey(lang)) {
      LOGGER.info("'" + getDocName() + "' - Getting object for lang '" + lang + "'");
      obj = articleObjMap.get(lang);
    } else {
      if (articleObjMap.containsKey(defaultLang)) {
        LOGGER.info("'" + getDocName() + "' - Getting object for defaultLang '" + lang + "'");
        obj = articleObjMap.get(defaultLang);
      } else {
        if (articleObjMap.containsKey("")) {
          LOGGER.info("'" + getDocName() + "' - Getting object failed for lang ''");
          obj = articleObjMap.get("");
        } else {
          LOGGER.info("'" + getDocName() + "' - Getting object failed for lang '" + lang
              + "' and defaultLang '" + defaultLang + "'");
        }
      }
    }
    LOGGER.info("Object found: doc name='" + ((obj != null) ? obj.getName() : "") + "' obj='" + obj
        + "'");
    return obj;
  }

  public Boolean isSubscribed() {
    DocumentReference blogConfDocRef = getContext().getDoc().getDocumentReference();
    if (getContext().get("vcontext") instanceof VelocityContext) {
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      if (vcontext.get("celldoc") instanceof Document) {
        blogConfDocRef = ((Document) vcontext.get("celldoc")).getDocumentReference();
      }
    }
    return isSubscribed(blogConfDocRef);
  }

  public Boolean isSubscribed(DocumentReference blogConfDocRef) {
    if (isSubscribed == null) {
      setSubscribed(blogConfDocRef);
    }
    return isSubscribed;
  }

  public boolean hasMoreLink(String lang, boolean isViewtypeFull) {
    if ((hasMoreLink == null) || !hasMoreLink.containsKey(lang)) {
      getExtract(lang, isViewtypeFull);
    }
    return hasMoreLink.get(lang);
  }

  public boolean hasMoreLinkDots(String lang, boolean isViewtypeFull) {
    if (hasMoreLink(lang, isViewtypeFull)) {
      getExtract(lang, isViewtypeFull);
    }
    return hasMoreLinkDots.get(lang);
  }

  private IWebUtilsService getWebService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private BlogClasses getBlogClasses() {
    return (BlogClasses) Utils.getComponent(IClassCollectionRole.class, "celements.celBlogClasses");
  }

  IBlogServiceRole getBlogService() {
    if (injected_blogService != null) {
      return injected_blogService;
    }
    return Utils.getComponent(IBlogServiceRole.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  @Override
  public String toString() {
    return "Article [docRef=" + getDocumentReference() + "]";
  }

  public String getExtractPlainTextEncoded(String lang, boolean isViewtypeFull, int maxNumChars) {
    String articleExtract = getExtract(lang, isViewtypeFull, maxNumChars);
    try {
      String plainExtract = new PlainTextCommand().convertHtmlToPlainText(articleExtract);
      return plainExtract.replaceAll("\"", "&quot;");
    } catch (ConvertToPlainTextException ctpte) {
      LOGGER.error("Failed to convert article extract to plain text for article {}", articleDocRef,
          ctpte);
    }
    return articleExtract;
  }

  public List<ImageUrlDim> getArticleImagesBySizeAsc(String lang) {
    List<ImageUrlDim> articleImages = extractImagesList(getFullArticle(lang));
    if (articleImages.isEmpty()) {
      articleImages = extractImagesList(getExtract(lang, false, getMaxNumChars()));
    }
    return articleImages;
  }

  List<ImageUrlDim> extractImagesList(String content) {
    String regex = "<img .*?src=['\"](.*?)['\"].*?/>";
    Map<Long, List<ImageUrlDim>> articleImages = new TreeMap<>();
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(content);
    while (m.find()) {
      String imgUrl = m.group(1);
      Long key = getImgUrlSizeKey(imgUrl);
      if (!articleImages.containsKey(key)) {
        articleImages.put(key, new ArrayList<ImageUrlDim>());
      }
      articleImages.get(key).add(getImgUrlExternal(imgUrl));
    }
    List<ImageUrlDim> sortedImages = new ArrayList<>();
    for (Long imgArea : articleImages.keySet()) {
      // Image size not extractable is in key == '-1' Don't include too small images.
      if ((imgArea == -1) || (imgArea >= (MIN_SOCIAL_MEDIA_AREA_SIZE))) {
        Collections.reverse(articleImages.get(imgArea));
        sortedImages.addAll(articleImages.get(imgArea));
      }
    }
    return sortedImages;
  }

  ImageUrlDim getImgUrlExternal(String imgUrl) {
    if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
      String action = imgUrl.replaceAll("^.*?/(.*?)/.*$", "$1");
      String space = imgUrl.replaceAll("^(.*?/){2}(.*?)/.*$", "$2");
      String docname = imgUrl.replaceAll("^(.*?/){3}(.*?)/.*$", "$2");
      String filename = imgUrl.replaceAll("^(.*?/){4}(.*?)(|\\?.*)$", "$2");
      String query = imgUrl.replaceAll("^.*\\?(.*)$", "$1");
      imgUrl = getContext().getURLFactory().createAttachmentURL(filename, space, docname, action,
          query, getContext().getDatabase(), getContext()).toString();
    }
    return new ImageUrlDim(imgUrl, parseImgUrlDimension(imgUrl, "celwidth"), parseImgUrlDimension(
        imgUrl, "celheight"));
  }

  Long getImgUrlSizeKey(String imgUrl) {
    int w = parseImgUrlDimension(imgUrl, "celwidth");
    int h = parseImgUrlDimension(imgUrl, "celheight");
    Long area = new Long(h * w);
    if (area < 0) {
      area = area * area;
    } else if ((h == -1) && (w == -1)) {
      area = -1l;
    }
    return area;
  }

  int parseImgUrlDimension(String imgUrl, String dimension) {
    String str = imgUrl.replaceAll("^.*" + dimension + "=(\\d+)(&.*)?$", "$1");
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException nfe) {
      LOGGER.debug("Exception while parsing Integer from [{}]", str, nfe);
    }
    return -1;
  }

  public String getExternalUrl() {
    String fullName = getModelUtils().serializeRefLocal(articleDocRef);
    try {
      return context.getWiki().getExternalURL(fullName, "view", "ref=socialmedia", context);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception while trying to get externalURL for doc {}", articleDocRef, xwe);
    }
    return context.getWiki().getURL(articleDocRef, "view", context);
  }

  /**
   * Adds social media meta tags to the collector, respecting the configuration. If the tags are
   * activate the calculated OpenGraph tags are added. Furthermore if a Twitter account is
   * configured (e.g. @example), the Twitter tags are added. The Twitter card type is configurable,
   * defaulting to "summary"
   *
   * @param language
   */
  public void addArticleSocialMediaTagsToCollector(String language) {
    if (1 == getConfigurationSource().getProperty(BLOG_ARTICLE_SOCIAL_MEDIA_CONF_NAME, 0)) {
      String externalUrl = getExternalUrl();
      List<ImageUrlDim> images = getArticleImagesBySizeAsc(language);
      getMetaTagService().addMetaTagToCollector(new MetaTag(EOpenGraph.OPENGRAPH_TYPE, "website"));
      for (ImageUrlDim image : images) {
        getMetaTagService().addMetaTagToCollector(new MetaTag(EOpenGraph.OPENGRAPH_IMAGE,
            image.getUrl()));
        getMetaTagService().addMetaTagToCollector(new MetaTag(
            EOpenGraph.OPENGRAPH_OPTIONAL_IMAGE_WIDTH, image.getWidth()));
        getMetaTagService().addMetaTagToCollector(new MetaTag(
            EOpenGraph.OPENGRAPH_OPTIONAL_IMAGE_HEIGHT, image.getHeight()));
      }
      getMetaTagService().addMetaTagToCollector(new MetaTag(EOpenGraph.OPENGRAPH_URL, externalUrl));
      String title = getTitle(language);
      getMetaTagService().addMetaTagToCollector(new MetaTag(EOpenGraph.OPENGRAPH_TITLE,
          title.replaceAll("\"", "&quot;")));
      // maxNumChars: e.g. for Facebook posts 300, for Facebook comments 110
      // viewTypeFull: maxNumChars has no influence if viewTypeFull == true
      String plainExtract = getExtractPlainTextEncoded(language, false, 450);
      getMetaTagService().addMetaTagToCollector(new MetaTag(
          EOpenGraph.OPENGRAPH_OPTIONAL_DESCRIPTION, plainExtract));
      String twitterSite = getConfigurationSource().getProperty(BLOG_ARTICLE_TWITTER_SITE);
      if (!Strings.isNullOrEmpty(twitterSite)) {
        getMetaTagService().addMetaTagToCollector(new MetaTag(ETwitter.TWITTER_CARD,
            getConfigurationSource().getProperty(BLOG_ARTICLE_TWITTER_CARD_TYPE, "summary")));
        getMetaTagService().addMetaTagToCollector(new MetaTag(ETwitter.TWITTER_SITE, twitterSite));
        String imageUrls = "";
        for (ImageUrlDim image : images) {
          if (imageUrls.length() > 0) {
            imageUrls += ",";
          }
          imageUrls += image.getUrl();
        }
        getMetaTagService().addMetaTagToCollector(new MetaTag(ETwitter.TWITTER_IMAGE, imageUrls));
      }
    }
  }

  private static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private static ConfigurationSource getConfigurationSource() {
    return Utils.getComponent(ConfigurationSource.class);
  }

  private static MetaTagServiceRole getMetaTagService() {
    return Utils.getComponent(MetaTagServiceRole.class);
  }

  class ImageUrlDim {

    private String url;
    private String width = "";
    private String height = "";

    public ImageUrlDim(String url, int width, int height) {
      this.url = url;
      if (width > 0) {
        this.width = Integer.toString(width);
      }
      if (height > 0) {
        this.height = Integer.toString(height);
      }
    }

    public String getUrl() {
      return url;
    }

    public String getWidth() {
      return width;
    }

    public String getHeight() {
      return height;
    }

  }

}
