package com.celements.migrator;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component("NewsletterReceiversToLowerCase")
public class NewsletterReceiversToLowerCaseMigrator extends AbstractCelementsHibernateMigrator {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      NewsletterReceiversToLowerCaseMigrator.class);
  private XWikiHibernateStore store;

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context
      ) throws XWikiException {
    DocumentReference recObjRef = new DocumentReference(context.getDatabase(), 
        "Celements", "NewsletterReceiverClass");
    Map<String, String> lowerMap = new HashMap<String, String>();
    Map<String, String> upperMap = new LinkedHashMap<String, String>();
    buildMaps(lowerMap, upperMap, context);
    mLogger.info(context.getDatabase() + ": found " + upperMap.size() + " / " 
        + (upperMap.size() + lowerMap.size()) + " with upper case letters.");
    for (String key : upperMap.keySet()) {
      DocumentReference upperRef = new DocumentReference(context.getDatabase(), 
          getDocSpace(upperMap, key), getDocName(upperMap, key));
      XWikiDocument upperDoc = context.getWiki().getDocument(upperRef, context);
      BaseObject upperObj = upperDoc.getXObject(recObjRef, getObjNr(upperMap, key));
      String loKey = key.toLowerCase();
      if(lowerMap.containsKey(loKey)) {
        DocumentReference lowerRef = new DocumentReference(context.getDatabase(), 
            getDocSpace(lowerMap, loKey), getDocName(lowerMap, loKey));
        XWikiDocument lowerDoc = context.getWiki().getDocument(lowerRef, context);
        BaseObject lowerObj = lowerDoc.getXObject(recObjRef, getObjNr(lowerMap, loKey));
        if((lowerObj.getIntValue("isactive") == 1) 
            && (upperObj.getIntValue("isactive") == 0)) {
          lowerObj.setIntValue("isactive", 0);
          context.getWiki().saveDocument(lowerDoc, context);
          mLogger.info(context.getDatabase() + ": deactivated " + loKey);
        }
        if(upperDoc.getXObjectSize(recObjRef) <= 1) {
          context.getWiki().deleteDocument(upperDoc, context);
          mLogger.info(context.getDatabase() + ": removed duplicat document " + key);
        } else {
          upperDoc.removeXObject(upperObj);
          mLogger.info(context.getDatabase() + ": removed duplicat object " + key);
        }
      } else {
        mLogger.info(context.getDatabase() + ": changed " + key);
        upperObj.setStringValue("email", upperObj.getStringValue("email"
            ).toLowerCase());
        context.getWiki().saveDocument(upperDoc, context);
        DocumentReference docRef = upperDoc.getDocumentReference();
        String fullname = docRef.getSpaceReferences().get(0).getName() + "." 
            + docRef.getName();
        lowerMap.put(loKey, fullname + ";" + upperObj.getNumber());
      }
    }
  }

  void buildMaps(Map<String, String> lowerMap, Map<String, String> upperMap,
      XWikiContext context) throws XWikiException {
    List<Object[]> data = getAllReceiversWithCapitals(context);
    if(data != null) {
      for (Object[] row : data) {
        String keyUp = row[0] + "\\" + row[1];
        String key = row[0].toString().toLowerCase() + "\\" + row[1].toString(
            ).toLowerCase();
        if(row[0].equals(row[0].toString().toLowerCase())) {
          if(!lowerMap.containsKey(key)) {
            lowerMap.put(key, row[3] + ";" + row[2]);
          } else {
            mLogger.error(context.getDatabase() + ": found multiple objects for " 
                + row[0] + " subscribing to " + row[1]);
          }
        } else {
          if(!upperMap.containsKey(keyUp)) {
            upperMap.put(keyUp, row[3] + ";" + row[2]);
          } else {
            mLogger.error(context.getDatabase() + ": found multiple objects for " 
                + row[0] + " subscribing to " + row[1]);
          }
        }
      }
    }
  }

  String getDocSpace(Map<String, String> map, String key) {
    return map.get(key).split(";")[0].split("\\.")[0];
  }
  
  String getDocName(Map<String, String> map, String key) {
    return map.get(key).split(";")[0].split("\\.")[1];
  }
  
  Integer getObjNr(Map<String, String> map, String key) {
    return Integer.parseInt(map.get(key).split(";")[1]);
  }
  
  List<Object[]> getAllReceiversWithCapitals(XWikiContext context) 
      throws XWikiException {
    List<Object[]> receivers = Collections.emptyList();
    DocumentReference classRef = new DocumentReference(context.getDatabase(), "Celements",
        "NewsletterReceiverClass");
    if(context.getWiki().exists(classRef, context)) {
      XWikiDocument xdoc = context.getWiki().getDocument(classRef, context);
      if("internal".equals(xdoc.getXClass().getCustomMapping())){
        String query = "select nro.email, nro.subscribed, obj.number, obj.name from " +
            "XWikiDocument as doc, BaseObject as obj, " +
            "Celements.NewsletterReceiverClass as nro " +
            "where doc.translation=0 " +
            "and doc.fullName=obj.name " +
            "and obj.id=nro.id.id " +
            "and obj.className='Celements.NewsletterReceiverClass'";
        receivers = getStore(context).search(query, 0, 0, context);
      }
    }
    return receivers;
  }

  XWikiHibernateStore getStore(XWikiContext context) {
    if(store == null) {
      store = context.getWiki().getHibernateStore();
    }
    return store;
  }
  
  void injectStore(XWikiHibernateStore store) {
    this.store = store;
  }
  
  public String getDescription() {
    return "'Changing all NewsletterReceivers to lower case.'";
  }

  public String getName() {
    return "NewsletterReceiversToLowerCase";
  }

  /**
   * getVersion is using days since
   * 1.1.2010 until the day of committing this migration
   * 21.6.2011 -> 536
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(606);
  }

}
