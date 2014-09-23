package com.celements.blog.article;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.blog.article.ArticleSearchParameter.DateMode;
import com.celements.blog.article.ArticleSearchParameter.SubscriptionMode;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class ArticleEngineLuceneTest extends AbstractBridgedComponentTestCase {
  
  private DocumentReference blogConfDocRef = new DocumentReference("wiki", "space", "blog");
  
  private ArticleEngineLucene engine;

  private XWiki xwiki;
  private XWikiContext context;
  private XWikiRightService rightsServiceMock;
  private ILuceneSearchService searchServiceMock;

  @Before
  public void setUp_ArticleEngineLuceneTest() {
    xwiki = getWikiMock();
    context = getContext();
    engine = (ArticleEngineLucene) Utils.getComponent(IArticleEngineRole.class, "lucene");
    rightsServiceMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightsServiceMock).anyTimes();
    searchServiceMock = createMockAndAddToDefault(ILuceneSearchService.class);
//    engine.injectSearchService(searchServiceMock);
  }

}
