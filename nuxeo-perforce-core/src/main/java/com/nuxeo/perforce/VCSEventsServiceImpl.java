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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.AbstractBlob;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;

public class VCSEventsServiceImpl extends DefaultComponent implements VCSEventsService {

    private static final Log log = LogFactory.getLog(VCSEventsServiceImpl.class);

    Map<String, VCSEventsProvider> providers = new HashMap<>();

    private static final String KEY_PROP = "dc:source";

    // Create a PageProvider
    private static final String QUERY = "Select * from Document where " + KEY_PROP
            + " = '%s' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0";

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);

        register(new VCSEventsPerforce());
    }

    @Override
    public VCSEventsProvider getEventsProvider(String name) {
        return providers.getOrDefault(name, null);
    }

    @Override
    public String getRootPath() {
        return "/default-domain/workspaces";
    }

    @Override
    public DocumentModel createDocumentModel(VCSEventsProvider provider, CoreSession session, String filePath,
            String change) {
        String filename = FileUtils.getFileName(filePath);

        try {
            DocumentModel doc = fileManager().createDocumentFromBlob(session, getBlob(provider, filePath, change),
                    getRootPath(), false, filename);

            provider.extractMetadata(filePath).entrySet().forEach(e -> doc.setProperties(e.getKey(), e.getValue()));
            doc.setPropertyValue(KEY_PROP, provider.computeKey(filePath, change));
            doc.setPropertyValue("dc:description", filePath);

            return session.saveDocument(doc);
        } catch (IOException e) {
            throw new NuxeoException(e);
        }
    }

    @Override
    public DocumentModel updateDocumentModel(VCSEventsProvider provider, CoreSession session, String filePath,
            String change) {
        try {
            DocumentModel doc = searchDocumentModel(session, provider.computeKey(filePath, change));
            if (doc == null) {
                return createDocumentModel(provider, session, filePath, change);
            } else {
                doc.setPropertyValue("file:content", getBlob(provider, filePath, change));
                doc.setPropertyValue("dc:description", filePath);
                return session.saveDocument(doc);
            }
        } catch (IOException e) {
            throw new NuxeoException(e);
        }
    }

    @Override
    public DocumentModel searchDocumentModel(CoreSession session, String key) {
        DocumentModelList res = session.query(String.format(QUERY, key));
        if (res.size() == 0) {
            return null;
        }

        if (res.size() > 1) {
            // XXX Should not happen
            List<String> paths = res.stream().map(s -> s.getPathAsString()).collect(Collectors.toList());
            log.warn(String.format("Found several Document for %s:\n", key, StringUtils.join(paths)));
        }

        return res.get(0);
    }

    protected FileManager fileManager() {
        return Framework.getService(FileManager.class);
    }

    protected void register(VCSEventsProvider transformer) {
        providers.put(transformer.getName(), transformer);
    }

    public AbstractBlob getBlob(VCSEventsProvider provider, String filePath, String change) throws IOException {
        String filename = FileUtils.getFileName(filePath);
        return (AbstractBlob) provider.getBlobProvider().readBlob(provider.buildBlobInfo(filePath, filename, change));
    }
}
