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
package com.celements.blog.migrator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.migrator.AbstractCelementsHibernateMigrator;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.web.Utils;

@Component("NewsletterReceiversRemoveDoubles")
public class NewsletterReceiversRemoveDoublesMigrator extends AbstractCelementsHibernateMigrator {

  @Requirement
  QueryManager queryManager;

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      NewsletterReceiversRemoveDoublesMigrator.class);

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    DocumentReference recObjRef = new DocumentReference(context.getDatabase(), "Celements",
        "NewsletterReceiverClass");
    String xwql = "from doc.object(Celements.NewsletterReceiverClass) as obj";
    try {
      List<String> receivers = queryManager.createQuery(xwql, Query.XWQL).execute();
      Map<String, Object[]> receiversMap = new HashMap<String, Object[]>();
      if (receivers != null) {
        for (String docName : receivers) {
          if (docName != null) {
            XWikiDocument recDoc = context.getWiki().getDocument(
                getWebUtils().resolveDocumentReference(docName), context);
            BaseObject recObj = recDoc.getXObject(recObjRef);
            String recMail = recObj.getStringValue("email");
            int isActive = recObj.getIntValue("isactive", 0);
            if (!"".equals(recMail)) {
              String recSubscr = recObj.getStringValue("subscribed");
              String recKey = recMail + "," + recSubscr;
              if (receiversMap.containsKey(recKey)) {
                int duplActive = (Integer) receiversMap.get(recKey)[0];
                XWikiDocument duplDoc = (XWikiDocument) receiversMap.get(recKey)[1];
                if (!recDoc.getDocumentReference().equals(duplDoc.getDocumentReference())) {
                  if ((isActive == 1) || (duplActive != 1)) {
                    recDoc.removeXObject(recObj);
                    context.getWiki().saveDocument(recDoc, "Removed douplicate", context);
                  } else {
                    duplDoc.removeXObject((BaseObject) receiversMap.get(recKey)[2]);
                    context.getWiki().saveDocument(duplDoc, "Removed douplicate", context);
                    receiversMap.put(recMail + "," + recSubscr, new Object[] { isActive, recDoc,
                        recObj });
                  }
                }
                LOGGER.info("Remove duplicate of " + recMail + " - " + recSubscr);
              } else {
                receiversMap.put(recMail + "," + recSubscr, new Object[] { isActive, recDoc,
                    recObj });
              }
            } else {
              LOGGER.info("Empty NewsletterReceiver found on doc " + recDoc.getDocumentReference());
            }
          }
        }
      }
    } catch (QueryException qe) {
      LOGGER.error("Exception cleaning duplicate newsletter receivers", qe);
    }
  }

  IWebUtilsService getWebUtils() {
    return (IWebUtilsService) Utils.getComponent(IWebUtilsService.class);
  }

  @Override
  public String getDescription() {
    return "Delete all NewsletterReceivers duplicates, created by double clicking.";
  }

  @Override
  public String getName() {
    return "NewsletterReceiversRemoveDoubles";
  }

  /**
   * getVersion is using days since 1.1.2010 until the day of committing this migration 28.06.2013
   * -> 1274 use: http://www.wolframalpha.com
   */
  @Override
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(1275);
  }

}
