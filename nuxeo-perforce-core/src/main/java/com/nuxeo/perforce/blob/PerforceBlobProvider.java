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

import com.google.common.base.Splitter;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.*;
import com.perforce.p4java.option.server.GetFileContentsOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerAddress.Protocol;
import com.perforce.p4java.server.ServerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.blob.AbstractBlobProvider;
import org.nuxeo.ecm.core.blob.BlobManager.BlobInfo;
import org.nuxeo.ecm.core.blob.ManagedBlob;
import org.nuxeo.ecm.core.blob.SimpleManagedBlob;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.perforce.p4java.PropertyDefs.P4JAVA_PROP_KEY_PREFIX;
import static com.perforce.p4java.core.file.FileSpecBuilder.makeFileSpecList;
import static com.perforce.p4java.server.IServerAddress.Protocol.P4JAVA;

public class PerforceBlobProvider extends AbstractBlobProvider {
    public static final String ID = "perforce";

    private static final String HOST = "host";

    private static final String PROTOCOL = "protocol";

    private static final Log log = LogFactory.getLog(PerforceBlobProvider.class);

    private static final char BLOB_KEY_SEPARATOR = ':';

    protected GetFileContentsOptions opts;

    protected IServer server;

    @Override
    public void initialize(String blobProviderId, Map<String, String> properties) throws IOException {
        super.initialize(blobProviderId, properties);

        String host = Framework.getProperty(String.format("%s%s", P4JAVA_PROP_KEY_PREFIX, HOST), "localhost:1666");
        this.properties.putIfAbsent(HOST, host);
        this.properties.putIfAbsent(PROTOCOL, P4JAVA.toString());

        opts = new GetFileContentsOptions(false, true);

        connect();
    }

    protected void connect() throws IOException {
        if (Framework.isTestModeSet()) {
            return;
        }

        if (server == null || !server.isConnected()) {
            try {
                String serverURI = String.format("%s://%s", Protocol.fromString(this.properties.get(PROTOCOL)),
                        this.properties.get(HOST));

                Map<Object, Object> p4props = Framework.getProperties()
                                                       .entrySet()
                                                       .stream()
                                                       .filter(e -> e.getKey()
                                                                     .toString()
                                                                     .startsWith(P4JAVA_PROP_KEY_PREFIX))
                                                       .collect(Collectors.toMap(Map.Entry::getKey,
                                                               Map.Entry::getValue));

                Properties props = new Properties();
                props.putAll(p4props);

                server = ServerFactory.getOptionsServer(serverURI, props);

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
        return makeFileSpecList(toPath(blob));
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

    public List<String> listAllDepotFilesPath() throws ConnectionException, AccessException {
        List<IFileSpec> list = makeFileSpecList("//...");
        return server.getDepotFiles(list, false)
                     .stream()
                     .filter(s -> !s.getAction().toString().endsWith("delete"))
                     .map(IFileSpec::getDepotPathString)
                     .collect(Collectors.toList());
    }

    @Override
    public boolean supportsUserUpdate() {
        return false;
    }
}
