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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.ecm.core.blob.BlobManager.BlobInfo;
import org.nuxeo.ecm.core.blob.BlobProvider;
import org.nuxeo.ecm.platform.mimetype.MimetypeNotFoundException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.config.ConfigurationService;

import com.nuxeo.perforce.blob.PerforceBlobProvider;

public class VCSEventsPerforce implements VCSEventsProvider {

    public static final String NAME = "perforce";

    protected final static String FILE_TYPES_DEFAULT = null; // This means handle ALL content types.

    protected static final String FILE_TYPES_KEY = "com.nuxeo.perforce.fileTypes";

    private static final Log log = LogFactory.getLog(VCSEventsPerforce.class);

    private static boolean anyStartsWith(String mimeType, String[] medias) {
        return Arrays.stream(medias).anyMatch(mimeType::startsWith);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EVENT_ACTION getAction(String action) {
        switch (action) {
        case "edit":
            return EVENT_ACTION.UPDATE;
        case "delete":
            return EVENT_ACTION.DELETE;
        case "add":
            return EVENT_ACTION.CREATE;
        }

        throw new UnsupportedOperationException("Unknown action: " + action);
    }

    @Override
    public BlobInfo buildBlobInfo(String remotePath, String filename, String change) {
        BlobInfo blobInfo = new BlobInfo();
        blobInfo.key = PerforceBlobProvider.ID + ":" + remotePath + ":" + change;
        blobInfo.filename = filename;
        blobInfo.mimeType = getMimeTypeFromFilename(filename);
        return blobInfo;
    }

    @Override
    public BlobProvider getBlobProvider() {
        return Framework.getService(BlobManager.class).getBlobProvider(PerforceBlobProvider.ID);
    }

    @Override
    public Map<String, Map<String, Object>> extractMetadata(String filePath) {
        return Collections.EMPTY_MAP;
    }

    @Override
    public boolean handleFilename(String filename) {
        // Get the list of supported file types.
        ConfigurationService configurationService = Framework.getService(ConfigurationService.class);
        String fileTypes = configurationService.getProperty(FILE_TYPES_KEY, FILE_TYPES_DEFAULT);

        if (fileTypes != null) {
            String[] fileTypesArray = fileTypes.split(",");
            String mimeType = getMimeTypeFromFilename(filename);
            return mimeType != null && anyStartsWith(mimeType, fileTypesArray);
        } else {
            return true;
        }
    }

    private String getMimeTypeFromFilename(String filename) {
        try {
            return Framework.getService(MimetypeRegistry.class).getMimetypeFromFilename(filename);
        } catch (MimetypeNotFoundException e) {
            log.debug("Unable to find MimeType for filename: " + filename);
            return null;
        }
    }
}
