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

/**
 * VCS Events Provider provide needed stuff for handling the event.
 */
public interface VCSEventsProvider {
    String getName();

    /**
     * Resolving VCS action name to an enum to handle it the same way for all vcs
     * 
     * @param action action to be converted to the corresponding enum
     * @return expected action to do or null
     */
    EVENT_ACTION getAction(String action);

    /**
     * Check if the provider is expecting to handle this filename, or not.
     *
     * @return true if Nuxeo must handle this file from the VCS.
     */
    boolean handleFilename(String filename);

    /**
     * Compute a unique key to identify a Document stored in the repository that is corresponding to. A good approach
     * can be to use the provider name inside.
     * 
     * @param remotePath VCS remote path
     * @param change VCS change set if needed
     * @return a unique key to identify the document
     */
    default String computeKey(String remotePath, String change) {
        return String.format("%s#%s#%s", getName(), remotePath, change);
    }

    /**
     * Return the Document's Type that must be created
     * 
     * @return a registered Document Type
     */
    default String getDocumentType() {
        return "File";
    }

    enum EVENT_ACTION {
        CREATE, UPDATE, DELETE, MOVE
    }
}