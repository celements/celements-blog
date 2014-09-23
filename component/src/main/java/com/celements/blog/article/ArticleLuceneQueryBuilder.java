package com.celements.blog.article;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.blog.article.ArticleSearchParameter.DateMode;
import com.celements.blog.article.ArticleSearchParameter.SubscriptionMode;
import com.celements.blog.plugin.BlogClasses;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component
public class ArticleLuceneQueryBuilder implements IArticleLuceneQueryBuilderRole {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      ArticleLuceneQueryBuilder.class);
  
  @Requirement
  private IBlogServiceRole blogService;
  
  @Requirement
  private ILuceneSearchService searchService;
  
  @Requirement
  private INextFreeDocRole nextFreeDocService;
  
  @Requirement
  private IWebUtilsService webUtilsService;
  
  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;

  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public LuceneQuery build(ArticleSearchParameter param) throws XWikiException {
    LuceneQuery query = null;
    QueryRestrictionGroup blogOrSubsGrp = searchService.createRestrictionGroup(Type.OR);    
    blogOrSubsGrp.add(getBlogRestriction(param));
    blogOrSubsGrp.add(getSubsRestrictions(param));    
    if (!blogOrSubsGrp.isEmpty()) {
      String database = param.getBlogDocRef().getWikiReference().getName();
      query = searchService.createQuery(database);
      DocumentReference articleClassRef = getBlogClasses().getArticleClassRef(database);
      query.add(searchService.createObjectRestriction(articleClassRef));
      if (StringUtils.isNotBlank(param.getLanguage())) {
        query.add(searchService.createFieldRestriction(articleClassRef, 
            BlogClasses.PROPERTY_ARTICLE_LANG, "\"" + param.getLanguage() + "\""));
      }    
      query.add(blogOrSubsGrp);
    }
    return query;
  }

  // TODO test
  private IQueryRestriction getBlogRestriction(ArticleSearchParameter param
      ) throws XWikiException {
    QueryRestrictionGroup restr = null;
    SpaceReference blogSpaceRef = blogService.getBlogSpaceRef(param.getBlogDocRef());
    if (param.isWithBlogArticles() && hasRights(blogSpaceRef, "view")) {
      IQueryRestriction dateRestr = getDateRestrictions(param.getDateModes(), 
          param.getExecutionDate(), hasRights(blogSpaceRef, "edit"));
      if (dateRestr != null) {
        restr = searchService.createRestrictionGroup(Type.AND);
        restr.add(searchService.createSpaceRestriction(blogSpaceRef));
        restr.add(dateRestr);
      }
    }
    return restr;
  }

  // TODO test
  private QueryRestrictionGroup getSubsRestrictions(ArticleSearchParameter param
      ) throws XWikiException {
    QueryRestrictionGroup ret = null;
    QueryRestrictionGroup subsOrGrp = searchService.createRestrictionGroup(Type.OR);
    for (DocumentReference docRef : param.getSubscribedToBlogs()) {
      SpaceReference spaceRef = blogService.getBlogSpaceRef(docRef);
      if (hasRights(spaceRef, "view")) {
        boolean hasEditRights = hasRights(spaceRef, "edit");
        IQueryRestriction artSubsRestr = getArticleSubsRestrictions(
            param.getSubscriptionModes(), param.getBlogDocRef(), hasEditRights);
        IQueryRestriction dateRestr = getDateRestrictions(param.getDateModes(), 
            param.getExecutionDate(), hasEditRights);
        if ((artSubsRestr != null) && (dateRestr != null)) {
          QueryRestrictionGroup spaceGrp = searchService.createRestrictionGroup(Type.AND);
          spaceGrp.add(searchService.createSpaceRestriction(spaceRef));
          spaceGrp.add(artSubsRestr);
          spaceGrp.add(dateRestr);
          subsOrGrp.add(spaceGrp);
        }
      }
    }
    if (!subsOrGrp.isEmpty()) {
      ret = searchService.createRestrictionGroup(Type.AND);
      DocumentReference articleClassRef = getBlogClasses().getArticleClassRef(
          param.getBlogDocRef().getWikiReference().getName());
      ret.add(searchService.createFieldRestriction(articleClassRef, 
          BlogClasses.PROPERTY_ARTICLE_IS_SUBSCRIBABLE, "\"1\""));
      ret.add(subsOrGrp);
    }
    return ret;
  }

  QueryRestrictionGroup getDateRestrictions(Set<DateMode> modes, Date date, 
      boolean hasEditRights) {
    QueryRestrictionGroup ret = null;
    if ((modes.size() < DateMode.values().length) || !hasEditRights) {
      // TODO not-restrictions shouldn't be inclusive of date but rather 
      //      {date TO highdate]). this doesn't work for now, see Ticket #7245
      IQueryRestriction publishRestr = searchService.createFromToDateRestriction(
          ARTICLE_FIELD_PUBLISH, null,  date, true);
      IQueryRestriction notPublishRestr = searchService.createFromToDateRestriction(
          ARTICLE_FIELD_PUBLISH, date,  null, true);
      IQueryRestriction archiveRestr = searchService.createFromToDateRestriction(
          ARTICLE_FIELD_ARCHIVE, null, date, true);
      IQueryRestriction notArchiveRestr = searchService.createFromToDateRestriction(
          ARTICLE_FIELD_ARCHIVE, date,  null, true);      
      if (modes.contains(DateMode.PUBLISHED)) {
        QueryRestrictionGroup restrs = searchService.createRestrictionGroup(Type.AND);
        restrs.add(publishRestr);
        restrs.add(notArchiveRestr);
        ret = addRestrToGrp(ret, restrs);
      }
      if (modes.contains(DateMode.FUTURE) && hasEditRights) {
        ret = addRestrToGrp(ret, notPublishRestr);
      }
      if (modes.contains(DateMode.ARCHIVED)) {
        ret = addRestrToGrp(ret, archiveRestr);
      }    
    } else {
      ret = searchService.createRestrictionGroup(Type.OR);
    }
    return ret;
  }
  
  private QueryRestrictionGroup addRestrToGrp(QueryRestrictionGroup grp, 
      IQueryRestriction restr) {
    if (grp == null) {
      grp = searchService.createRestrictionGroup(Type.OR);
    }
    grp.add(restr);
    return grp;
  }

  QueryRestrictionGroup getArticleSubsRestrictions(Set<SubscriptionMode> modes, 
      DocumentReference blogConfDocRef, boolean hasEditRights) {
    QueryRestrictionGroup ret = null;
    DocumentReference classRef = getBlogClasses().getBlogArticleSubscriptionClassRef(
        blogConfDocRef.getWikiReference().getName());
    if ((modes.size() < SubscriptionMode.values().length) || !hasEditRights) {
      QueryRestrictionGroup artSubsRestrs = searchService.createRestrictionGroup(Type.OR);
      boolean undecided = modes.contains(SubscriptionMode.UNDECIDED) && hasEditRights;
      if (modes.contains(SubscriptionMode.SUBSCRIBED) != undecided) {
        artSubsRestrs.add(searchService.createFieldRestriction(classRef, 
            BlogClasses.PROPERTY_ARTICLE_SUBSCRIPTION_DO_SUBSCRIBE, "\"1\""));
      }
      if ((modes.contains(SubscriptionMode.UNSUBSCRIBED) != undecided) && hasEditRights) {
        artSubsRestrs.add(searchService.createFieldRestriction(classRef, 
            BlogClasses.PROPERTY_ARTICLE_SUBSCRIPTION_DO_SUBSCRIBE, "\"0\""));
      }
      if (!artSubsRestrs.isEmpty()) {
        ret = searchService.createRestrictionGroup(Type.AND);
        ret.add(searchService.createObjectRestriction(classRef));
        ret.add(searchService.createFieldRefRestriction(classRef, 
            BlogClasses.PROPERTY_ARTICLE_SUBSCRIPTION_SUBSCRIBER, blogConfDocRef));
        ret.add(artSubsRestrs);
        ret.setNegate(undecided); // TODO test this if working as intended...
      }
    } else {
      ret = searchService.createRestrictionGroup(Type.AND);
    }
    return ret;
  }
  
  private boolean hasRights(SpaceReference spaceRef, String rights) throws XWikiException {
    boolean ret = false;
    if (spaceRef != null) {
      DocumentReference docRef = nextFreeDocService.getNextUntitledPageDocRef(spaceRef);
      String fullName = webUtilsService.getRefDefaultSerializer().serialize(docRef);
      ret = getContext().getWiki().getRightService().hasAccessLevel(rights, getContext(
          ).getUser(), fullName, getContext());
    }
    LOGGER.debug("hasRights for spaceRef [" + spaceRef + "] and rights [" + rights 
        + "] returned [" + ret + "]");
    return ret;
  }

  private BlogClasses getBlogClasses() {
    return (BlogClasses) blogClasses;
  }

  void injectNextFreeDocService(INextFreeDocRole nextFreeDocService) {
    this.nextFreeDocService = nextFreeDocService;
  }

}
