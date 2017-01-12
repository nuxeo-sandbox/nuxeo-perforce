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

import java.util.List;
import java.util.stream.Collectors;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

import com.google.common.collect.Lists;
import com.nuxeo.perforce.VCSEventsPerforce;
import com.nuxeo.perforce.VCSEventsProvider;
import com.nuxeo.perforce.VCSEventsService;
import com.nuxeo.perforce.blob.PerforceBlobProvider;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;

/**
 *
 */
@Operation(id = PerforceInitializer.ID, category = Constants.CAT_SERVICES, label = "Perforce Depot Initializer", description = "Describe here what your operation does.")
public class PerforceInitializer {

    public static final String ID = "Perforce.Initializer";

    private static final int BATCH = 20;

    @Context
    protected CoreSession session;

    @OperationMethod
    public void run() throws ConnectionException, AccessException {
        if (!((NuxeoPrincipal) session.getPrincipal()).isAdministrator()) {
            throw new NuxeoException("You must be an Administrator.");
        }

        VCSEventsService service = Framework.getService(VCSEventsService.class);
        VCSEventsProvider provider = service.getEventsProvider(VCSEventsPerforce.NAME);

        PerforceBlobProvider blobProvider = (PerforceBlobProvider) provider.getBlobProvider();
        // Get all Depot file and filter on only Perforce accepted mime-type
        List<String> files = blobProvider.listAllDepotFilesPath()
                                         .stream()
                                         .filter(provider::handleFilename)
                                         .collect(Collectors.toList());

        // Batch the result list to 20 files per work
        Lists.partition(files, BATCH).forEach(f -> {
            VCSInitializerWorker work = new VCSInitializerWorker(provider.getName());
            work.addRemoteFile(f);

            Framework.getService(WorkManager.class).schedule(work, true);
        });
    }
}
