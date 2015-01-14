package com.celements.blog.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogCreatedEvent;
import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.observation.event.BlogUpdatedEvent;
import com.celements.blog.service.BlogService;
import com.celements.blog.service.IBlogServiceRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.blog.clearBlogCache")
public class ClearBlogCacheListener implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      ClearBlogCacheListener.class);
  
  @Requirement
  private IBlogServiceRole blogService;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new BlogCreatedEvent(), new BlogUpdatedEvent(),
        new BlogDeletedEvent());
  }

  @Override
  public String getName() {
    return "celements.blog.clearBlogCache";
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    if (blogService instanceof BlogService) {
      ((BlogService) blogService).clearBlogCache(webUtilsService.getWikiRef(
          (XWikiDocument) source));
    } else {
      LOGGER.error("Unknown blog service instance: " + blogService);
    }
  }

}
