package com.celements.blog.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.cache.BlogCache;
import com.celements.blog.observation.event.BlogCreatedEvent;
import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.observation.event.BlogUpdatedEvent;
import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(BlogCache.NAME)
public class BlogCacheFlushingListener implements EventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlogCacheFlushingListener.class);

  @Requirement(BlogCache.NAME)
  IDocumentReferenceCache<String> navCache;

  @Requirement
  IWebUtilsService webUtils;

  @Override
  public String getName() {
    return BlogCache.NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new BlogCreatedEvent(), new BlogUpdatedEvent(),
        new BlogDeletedEvent());
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    LOGGER.debug("onEvent: event '{}', source '{}', data '{}'", event, source, data);
    if (source instanceof XWikiDocument) {
      navCache.flush(webUtils.getWikiRef((XWikiDocument) source));
    } else {
      LOGGER.error("onEvent: unable to flush cache for source '{}'", source);
    }
  }

}
