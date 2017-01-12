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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

/**
 * Ignored as it can only be used locally to createFile a running server
 */
@Ignore
public class TestPerforceBlobProvider {

    IServer server;

    @Before
    public void before() throws ConnectionException, ConfigException, NoSuchObjectException, ResourceException,
            URISyntaxException, RequestException, AccessException {
        server = ServerFactory.getServer("p4java://localhost:1666", null);
        server.connect();
    }

    @After
    public void after() throws ConnectionException, AccessException {
        if (server.isConnected()) {
            server.disconnect();
        }
    }

    @Test
    public void testClient() throws ConnectionException, ConfigException, NoSuchObjectException, ResourceException,
            URISyntaxException, AccessException, RequestException {
        List<IFileSpec> files = server.getDepotFiles(
                FileSpecBuilder.makeFileSpecList("//Nuxeo/main/src/main/blabla/coucou/createFile.java"), false);
        assertEquals(1, files.size());
    }

    @Test
    public void testStream() throws P4JavaException, IOException {
        List<IFileSpec> files = server.getDepotFiles(FileSpecBuilder.makeFileSpecList("//Nuxeo/main/testdsd.png"),
                false);
        assertEquals(1, files.size());

        IFileSpec fileSpec = files.get(0);

        GetFileContentsOptions pts = new GetFileContentsOptions(false, true);

        try (InputStream is = fileSpec.getContents(pts)) {
            File file = createFile(is);
            System.out.println(file.getAbsolutePath());
        }
    }

    @Test
    public void testListAllFiles() throws ConnectionException, AccessException {
        List<IFileSpec> iFileSpecs = FileSpecBuilder.makeFileSpecList("//...");
        IFileSpec file = iFileSpecs.get(0);
        List<IFileSpec> depotFiles = server.getDepotFiles(iFileSpecs, false);
        List<IFileSpec> delete = depotFiles.stream().filter(s -> !s.getAction().toString().endsWith("delete")).collect(
                Collectors.toList());
        assertEquals(5, delete.size());
    }

    protected File createFile(InputStream is) throws IOException {
        if (is == null) {
            throw new NullPointerException("null inputstream");
        }
        File file;
        try {
            file = File.createTempFile("nxblob-", ".tmp", null);
            try (OutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(is, out);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        return file;
    }
}
