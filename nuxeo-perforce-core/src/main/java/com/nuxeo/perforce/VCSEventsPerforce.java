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

import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

public class VCSEventsPerforce implements VCSEventsProvider {

    public static final String NAME = "perforce";

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
        return null;
    }

    @Override
    public boolean handleFilename(String filename) {
        String mimeType = Framework.getService(MimetypeRegistry.class).getMimetypeFromFilename(filename);
        return mimeType != null && anyStartsWith(mimeType, "audio", "video", "image");
    }

    private static boolean anyStartsWith(String mimeType, String... medias) {
        return Arrays.stream(medias).anyMatch(mimeType::startsWith);
    }
}
