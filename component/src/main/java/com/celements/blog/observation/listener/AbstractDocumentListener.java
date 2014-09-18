package com.celements.blog.observation.listener;

import org.apache.commons.logging.Log;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.blog.plugin.BlogClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public abstract class AbstractDocumentListener implements EventListener {

  @Requirement("celements.celBlogClasses")
  private IClassCollectionRole blogClasses;
  
  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

  @Requirement
  private ComponentManager componentManager;

  /**
   * The observation manager that will be use to fire user creation events.
   * Note: We can't have the OM as a requirement, since it would create an
   * infinite initialization loop, causing a stack overflow error (this event
   * listener would require an initialized OM and the OM requires a list of
   * initialized event listeners)
   */
  private ObservationManager observationManager;
  
  protected void notifyIfBlog(XWikiDocument doc, Class<? extends Event> eventClass) {
    DocumentReference classRef = getBlogClasses().getBlogConfigClassRef(
        doc.getDocumentReference().getWikiReference().getName());
    if (doc.getXObject(classRef) != null) {
      notifyEvent(eventClass, doc, null);
    } else {
      getLogger().trace("onEvent: no blog config class object found on doc '" + doc 
          + "', not notifiying event '" + eventClass);
    }
  }
  
  private void notifyEvent(Class<? extends Event> eventClass, Object source, Object data) {
    try {
      getObservationManager().notify(eventClass.newInstance(), source, data);
    } catch (IllegalAccessException exc) {
      getLogger().error("Error getting new instance", exc);
    } catch (InstantiationException exc) {
      getLogger().error("Error getting new instance", exc);
    }
  }

  private ObservationManager getObservationManager() {
    if (this.observationManager == null) {
      try {
        this.observationManager = componentManager.lookup(ObservationManager.class);  
      } catch (ComponentLookupException e) {
        throw new RuntimeException(
            "Cound not retrieve an Observation Manager against the component manager");
      }
    }
    return this.observationManager;
  }
  
  private BlogClasses getBlogClasses() {
    return (BlogClasses) blogClasses;
  }
  
  abstract protected Log getLogger();

}
