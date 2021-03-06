/*
 *
 *  * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and others.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * Contributors:
 *  *     Nuxeo
 *
 */
package com.nuxeo.perforce;

import java.io.IOException;
import java.util.MissingFormatArgumentException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

/**
 *
 */
@Operation(id = VCSEventsReceiverOperation.ID, category = Constants.CAT_DOCUMENT, label = "VCSEventsReceiver", description = "Describe here what your operation does.")
public class VCSEventsReceiverOperation {

    public static final String ID = "Document.VCSEventsReceiverOperation";

    private static final Log log = LogFactory.getLog(VCSEventsReceiverOperation.class);

    @Context
    protected CoreSession session;

    @Param(name = "filePath")
    protected String filePath;

    @Param(name = "action")
    protected String action;

    @Param(name = "provider")
    protected String provider;

    @Param(name = "change", required = false)
    protected String change;

    protected String filename;

    private static VCSEventsService getService() {
        return Framework.getService(VCSEventsService.class);
    }

    @OperationMethod
    public DocumentModel run() throws IOException {
        VCSEventsService service = getService();
        VCSEventsProvider provider = service.getEventsProvider(this.provider);

        if (provider == null) {
            throw new MissingFormatArgumentException("Unable to find a VCS provider named: " + this.provider);
        }

        filename = FileUtils.getFileName(filePath);
        if (!provider.handleFilename(filename)) {
            return null;
        }

        DocumentModel doc;
        String documentKey = provider.computeKey(filePath, change);

        switch (provider.getAction(this.action)) {
        case CREATE:
            doc = service.createDocumentModel(provider, session, filePath, change);
            break;
        case DELETE:
            doc = service.searchDocumentModel(session, documentKey);
            if (doc != null) {
                session.removeDocument(doc.getRef());
                doc = null;
            } else {
                log.warn("Unable to find a corresponding document for key: " + documentKey);
            }
            break;
        case UPDATE:
            doc = service.updateDocumentModel(provider, session, filePath, change);
            break;
        case MOVE:
            // XXX Considering move like a DELETE + CREATE for now.
            throw new UnsupportedOperationException("Not yet implemented.");
        default:
            throw new UnsupportedOperationException(
                    String.format("Not handle action '%s' for provider '%s'", this.action, this.provider));
        }

        return doc;
    }
}
