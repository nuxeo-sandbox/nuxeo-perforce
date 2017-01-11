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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;

public class VCSEventsServiceImpl extends DefaultComponent implements VCSEventsService {

    private static final Log log = LogFactory.getLog(VCSEventsServiceImpl.class);

    Map<String, VCSEventsProvider> providers = new HashMap<>();

    private static final String KEY_PROP = "dc:source";

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
    public DocumentModel createDocumentModel(CoreSession session, String filename, String type, String remoteKey) {
        DocumentModel doc = session.createDocumentModel(getRootPath(), filename, type);
        doc.setPropertyValue(KEY_PROP, remoteKey);
        return doc;
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

    protected void register(VCSEventsProvider transformer) {
        providers.put(transformer.getName(), transformer);
    }
}
