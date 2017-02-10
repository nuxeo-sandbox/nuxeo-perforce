package com.nuxeo.perforce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.operations.document.FetchDocument;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "com.nuxeo.perforce.nuxeo-perforce-core", "org.nuxeo.ecm.platform.types.core",
        "org.nuxeo.ecm.platform.filemanager.core", "com.nuxeo.perforce.nuxeo-perforce-core-test:OSGI-INF/test-configuration-service-contrib.xml" })
public class TestVCSEventsReceiverOperation {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected VCSEventsService vcsService;

    private Object runOperation(String filePath, String action, String change) throws OperationException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("filePath", filePath);
        params.put("action", action);
        params.put("provider", VCSEventsPerforce.NAME);
        params.put("change", change);

        return automationService.run(ctx, VCSEventsReceiverOperation.ID, params);
    }

    private DocumentModel fetchDocument(String path) throws OperationException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("value", path);
        return (DocumentModel) automationService.run(ctx, FetchDocument.ID, params);
    }

    @Test
    public void createDocument() throws OperationException {
        Object ret = runOperation("//Nuxeo/main/something/createFile/blabla.mp4", "add", null);
        assertTrue(ret instanceof DocumentModel);
        DocumentModel doc = (DocumentModel) ret;
        assertNotNull(doc.getPropertyValue("dc:source"));

        ret = runOperation("//blasd/createFile.txt", "add", null);
        assertNull(ret);
    }

    @Test
    public void updateDocument() throws OperationException {
        String fileName = "toto.mp4";
        String filePath = "//depot/" + fileName;
        VCSEventsProvider perforce = vcsService.getEventsProvider("perforce");
        String remoteKey = perforce.computeKey(filePath, "");

        final Calendar[] modified = new Calendar[1];
        final String[] path = new String[1];

        TransactionHelper.runInTransaction(() -> {
            DocumentModel doc = vcsService.createDocumentModel(perforce, session, fileName, remoteKey);
            doc = session.createDocument(doc);
            session.save();

            modified[0] = (Calendar) doc.getPropertyValue("dc:modified");
            path[0] = doc.getPathAsString();
        });

        DocumentModel documentModel = fetchDocument(path[0]);
        assertEquals(modified[0], documentModel.getPropertyValue("dc:modified"));

        Object ret = runOperation(filePath, "edit", "");
        assertTrue(ret instanceof DocumentModel);

        DocumentModel doc = (DocumentModel) ret;
        assertNotEquals(modified[0], doc.getPropertyValue("dc:modified"));
    }

    @Test
    public void updateNotFoundDocument() throws OperationException {
        Object ret = runOperation("//depot/notcreated/file.avi", "edit", "");
        assertTrue(ret instanceof DocumentModel);
    }

    @Test
    public void deleteDocument() throws OperationException {
        String filePath = "//depot/createFile.mp4";
        Object ret = runOperation(filePath, "add", "10");
        assertTrue(ret instanceof DocumentModel);

        ret = runOperation(filePath, "delete", "");
        assertNull(ret);

        ret = runOperation("//unknown/file.avi", "delete", "");
        assertNull(ret);
    }
}
