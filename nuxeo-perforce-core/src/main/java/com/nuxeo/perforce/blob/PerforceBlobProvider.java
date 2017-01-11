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

package com.nuxeo.perforce.blob;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.blob.AbstractBlobProvider;
import org.nuxeo.ecm.core.blob.BlobManager.BlobInfo;
import org.nuxeo.ecm.core.blob.ManagedBlob;
import org.nuxeo.ecm.core.blob.SimpleManagedBlob;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.runtime.api.Framework;

import com.google.common.base.Splitter;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.NoSuchObjectException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.exception.ResourceException;
import com.perforce.p4java.option.server.GetFileContentsOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;

public class PerforceBlobProvider extends AbstractBlobProvider {
    public static final String ID = "perforce";

    private static final String P4HOST = "P4HOST";

    private static final Log log = LogFactory.getLog(PerforceBlobProvider.class);

    private static final char BLOB_KEY_SEPARATOR = ':';

    protected GetFileContentsOptions opts;

    protected IServer server;

    @Override
    public void initialize(String blobProviderId, Map<String, String> properties) throws IOException {
        super.initialize(blobProviderId, properties);

        String host = Framework.getProperty(P4HOST, "localhost:1666");
        this.properties.putIfAbsent(P4HOST, host);

        opts = new GetFileContentsOptions(false, true);

        connect();
    }

    protected void connect() throws IOException {
        if (Framework.isTestModeSet()) {
            return;
        }

        if (server == null || !server.isConnected()) {
            try {
                server = ServerFactory.getServer("p4java://" + this.properties.get(P4HOST), null);
                server.connect();
            } catch (ConnectionException | AccessException | RequestException | ConfigException | ResourceException
                    | URISyntaxException | NoSuchObjectException e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public void close() {
        if (server != null && server.isConnected()) {
            try {
                server.disconnect();
            } catch (ConnectionException | AccessException e) {
                log.error("Unable to disconnect from server: " + e.getMessage());
                log.debug(e);
            }
        }
    }

    @Override
    public Blob readBlob(BlobInfo blobInfo) throws IOException {
        return new SimpleManagedBlob(blobInfo);
    }

    @Override
    public InputStream getStream(ManagedBlob blob) throws IOException {
        try {
            List<IFileSpec> depotFiles = server.getDepotFiles(getFiles(blob), false);

            if (depotFiles.size() != 1) {
                throw new IOException("No only a single file found (" + depotFiles.size() + "): " + blob);
            }

            return depotFiles.get(0).getContents(opts);
        } catch (P4JavaException e) {
            throw new IOException(e);
        }
    }

    protected List<IFileSpec> getFiles(ManagedBlob blob) {
        return FileSpecBuilder.makeFileSpecList(toPath(blob));
    }

    protected String toPath(ManagedBlob blob) {
        List<String> keyParts = Splitter.on(BLOB_KEY_SEPARATOR).splitToList(blob.getKey());
        // 0: providerID
        // 1: Path
        return keyParts.get(1);
    }

    @Override
    public String writeBlob(Blob blob, Document document) throws IOException {
        throw new UnsupportedOperationException("Unable to update Perforce assets.");
    }

    @Override
    public boolean supportsUserUpdate() {
        return false;
    }
}
