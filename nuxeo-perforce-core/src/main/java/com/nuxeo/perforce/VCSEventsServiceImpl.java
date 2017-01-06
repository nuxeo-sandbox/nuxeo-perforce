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
import java.util.Map;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;

public class VCSEventsServiceImpl extends DefaultComponent implements VCSEventsService {

    Map<String, VCSEventsProvider> providers = new HashMap<>();

    @Override
    public void applicationStarted(ComponentContext context) {
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
    public DocumentModel createDocumentModel(CoreSession session, String filename, String type) {
        return session.createDocumentModel(Framework.getService(VCSEventsService.class).getRootPath(), filename, type);
    }

    @Override
    public DocumentModel searchDocumentModel(CoreSession session, String key) {
        DocumentModelList res = session.query("Select * from Document ");
        if (res.size() > 1) {
            throw new NuxeoException("Find several stored document with key: " + key + " can't find the correct one.");
        }

        if (res.size() == 0) {
            return null;
        }

        return res.get(0);
    }

    protected void register(VCSEventsProvider transformer) {
        providers.put(transformer.getName(), transformer);
    }
}
