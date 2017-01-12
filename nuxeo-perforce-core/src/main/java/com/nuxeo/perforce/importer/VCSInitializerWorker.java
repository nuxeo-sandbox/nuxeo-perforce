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

package com.nuxeo.perforce.importer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.runtime.api.Framework;

import com.nuxeo.perforce.VCSEventsProvider;
import com.nuxeo.perforce.VCSEventsService;

public class VCSInitializerWorker extends AbstractWork implements Work {
    public static final String NAME = "VCS File Initializer";

    private String providerName;

    private Set<String> filePaths = new HashSet<>();

    public VCSInitializerWorker(String providerName) {
        super(NAME);
        this.providerName = providerName;
    }

    public void addRemoteFile(Collection<String> files) {
        files.forEach(this::addRemoteFile);
    }

    public void addRemoteFile(String filePath) {
        filePaths.add(filePath);
    }

    @Override
    public void work() {
        openSystemSession();

        VCSEventsService service = Framework.getService(VCSEventsService.class);
        VCSEventsProvider provider = service.getEventsProvider(providerName);

        filePaths.forEach(f -> {
            // Assume to update Document in case it already exists
            service.updateDocumentModel(provider, session, f, null);
        });
    }

    @Override
    public String getTitle() {
        return NAME;
    }
}
