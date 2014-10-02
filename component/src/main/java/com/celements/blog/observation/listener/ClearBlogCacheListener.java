package com.celements.blog.observation.listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.blog.observation.event.BlogCreatedEvent;
import com.celements.blog.observation.event.BlogDeletedEvent;
import com.celements.blog.service.BlogService;
import com.celements.blog.service.IBlogServiceRole;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.blog.clearBlogCache")
public class ClearBlogCacheListener implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      ClearBlogCacheListener.class);
  
  @Requirement
  private IBlogServiceRole blogService;

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new BlogCreatedEvent(), new BlogDeletedEvent());
  }

  @Override
  public String getName() {
    return "celements.blog.clearBlogCache";
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    if (blogService instanceof BlogService) {
      for (WikiReference wikiRef : getWikiRefsForDoc((XWikiDocument) source)) {
        ((BlogService) blogService).clearBlogCache(wikiRef);
      }
    } else {
      LOGGER.error("Unknown blog service instance: " + blogService);
    }
  }

  private Set<WikiReference> getWikiRefsForDoc(XWikiDocument doc) {
    Set<WikiReference> wikiRefs = new HashSet<WikiReference>();
    for (SpaceReference spaceRef : doc.getDocumentReference().getSpaceReferences()) {
      wikiRefs.add((WikiReference) spaceRef.extractReference(EntityType.WIKI));
    }
    return wikiRefs;
  }

}
