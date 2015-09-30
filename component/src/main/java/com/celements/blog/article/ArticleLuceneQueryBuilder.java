package com.celements.blog.article;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.blog.article.ArticleLoadParameter.DateMode;
import com.celements.blog.article.ArticleLoadParameter.SubscriptionMode;
import com.celements.blog.plugin.BlogClasses;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.rights.AccessLevel;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;

@Component
public class ArticleLuceneQueryBuilder implements IArticleLuceneQueryBuilderRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ArticleLuceneQueryBuilder.class);
  
  @Requirement
  IBlogServiceRole blogService;
  
  @Requirement
  ILuceneSearchService searchService;
  
  @Requirement
  IWebUtilsService webUtils;
  
  @Requirement("celements.celBlogClasses")
  IClassCollectionRole blogClasses;

  @Override
  public LuceneQuery build(ArticleLoadParameter param) throws XWikiException {
    LuceneQuery query = null;
    if (param.getBlogDocRef() != null) {
      QueryRestrictionGroup blogOrSubsGrp = searchService.createRestrictionGroup(Type.OR);    
      blogOrSubsGrp.add(getBlogRestriction(param));
      blogOrSubsGrp.add(getSubsRestrictions(param));
      if (!blogOrSubsGrp.isEmpty()) {
        WikiReference wikiRef = param.getBlogDocRef().getWikiReference();
        query = searchService.createQuery();
        query.setWiki(wikiRef);
        query.add(getBlogSearchTermRestriction(param));
        DocumentReference articleClassRef = getBlogClasses().getArticleClassRef(
            wikiRef.getName());
        query.add(searchService.createObjectRestriction(articleClassRef));
        if (StringUtils.isNotBlank(param.getLanguage())) {
          query.add(searchService.createFieldRestriction(articleClassRef, 
              BlogClasses.PROPERTY_ARTICLE_LANG, "\"" + param.getLanguage() + "\""));
        }    
        query.add(blogOrSubsGrp);
      }
    }
    LOGGER.info("Built '" + query + "' for '" + param + "'");
    return query;
  }

  IQueryRestriction getBlogRestriction(ArticleLoadParameter param
      ) throws XWikiException {
    QueryRestrictionGroup restr = null;
    SpaceReference blogSpaceRef = blogService.getBlogSpaceRef(param.getBlogDocRef());
    if (param.isWithBlogArticles() && webUtils.hasAccessLevel(blogSpaceRef, 
        AccessLevel.VIEW)) {
      IQueryRestriction dateRestr = getDateRestrictions(param.getDateModes(), 
          param.getExecutionDate(), webUtils.hasAccessLevel(blogSpaceRef, 
          AccessLevel.EDIT));
      if (dateRestr != null) {
        restr = searchService.createRestrictionGroup(Type.AND);
        restr.add(searchService.createSpaceRestriction(blogSpaceRef));
        restr.add(dateRestr);
      }
    }
    LOGGER.trace("got blog restriction " + restr + "' for '" + param + "'");
    return restr;
  }
  
  IQueryRestriction getBlogSearchTermRestriction(ArticleLoadParameter param
      ) throws XWikiException {
    QueryRestrictionGroup restr = null;
    if (param.isWithBlogArticles() && (param.getSearchTerm() != null)) {
      restr = searchService.createRestrictionGroup(Type.OR);
      restr.add(new QueryRestriction("XWiki.ArticleClass.extract", param.getSearchTerm()));
      restr.add(new QueryRestriction("XWiki.ArticleClass.title", param.getSearchTerm()));
      restr.add(new QueryRestriction("XWiki.ArticleClass.content", param.getSearchTerm()));
    }
    LOGGER.trace("got blog restriction " + restr + "' for '" + param + "'");
    return restr;
  }

  QueryRestrictionGroup getSubsRestrictions(ArticleLoadParameter param
      ) throws XWikiException {
    QueryRestrictionGroup ret = null;
    QueryRestrictionGroup subsOrGrp = searchService.createRestrictionGroup(Type.OR);
    for (DocumentReference docRef : param.getSubscribedToBlogs()) {
      SpaceReference spaceRef = blogService.getBlogSpaceRef(docRef);
      if (webUtils.hasAccessLevel(spaceRef, AccessLevel.VIEW)) {
        subsOrGrp.add(getSubsSpaceRestriction(param, spaceRef));
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
    LOGGER.trace("got subs restriction " + ret + "' for '" + param + "'");
    return ret;
  }

  QueryRestrictionGroup getSubsSpaceRestriction(ArticleLoadParameter param, 
      SpaceReference spaceRef) throws XWikiException {
    QueryRestrictionGroup subsSpaceGrp = null;
    boolean hasEditRights = webUtils.hasAccessLevel(spaceRef, AccessLevel.EDIT);
    IQueryRestriction dateRestr = getDateRestrictions(param.getDateModes(), 
        param.getExecutionDate(), hasEditRights);
    IQueryRestriction artSubsRestr = getArticleSubsRestrictions(
        param.getSubscriptionModes(), param.getBlogDocRef(), hasEditRights);
    if ((artSubsRestr != null) && (dateRestr != null)) {
      subsSpaceGrp = searchService.createRestrictionGroup(Type.AND);
      subsSpaceGrp.add(searchService.createSpaceRestriction(spaceRef));
      subsSpaceGrp.add(dateRestr);
      subsSpaceGrp.add(artSubsRestr);
    } else {
      LOGGER.debug("no date restriction '" + dateRestr + "' or article subs "
          + "restriction '" + artSubsRestr + "' for space '" + spaceRef 
          + "' with edit righs '" + hasEditRights + "'");
    }
    LOGGER.trace("got space restriction '" + subsSpaceGrp + "' for space: " + spaceRef);
    return subsSpaceGrp;
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
    if ((modes.size() < SubscriptionMode.values().length) || !hasEditRights) {
      QueryRestrictionGroup artSubsRestrs = searchService.createRestrictionGroup(Type.OR);
      DocumentReference classRef = getBlogClasses().getBlogArticleSubscriptionClassRef(
          blogConfDocRef.getWikiReference().getName());
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
        ret.add(artSubsRestrs);
        ret.add(searchService.createFieldRefRestriction(classRef, 
            BlogClasses.PROPERTY_ARTICLE_SUBSCRIPTION_SUBSCRIBER, blogConfDocRef));
        ret.setNegate(undecided);
      }
      LOGGER.trace("getArticleSubsRestrictions: for modes '{}', hasEditRights '{}', "
          + "undecided '{}' returns '{}'", modes, hasEditRights, undecided, ret);
    } else {
      ret = searchService.createRestrictionGroup(Type.AND);
    }
    return ret;
  }

  private BlogClasses getBlogClasses() {
    return (BlogClasses) blogClasses;
  }

}
