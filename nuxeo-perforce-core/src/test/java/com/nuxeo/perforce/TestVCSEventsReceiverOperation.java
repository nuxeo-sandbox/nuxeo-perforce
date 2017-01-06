package com.nuxeo.perforce;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("com.nuxeo.perforce.nuxeo-perforce-core")
public class TestVCSEventsReceiverOperation {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    private Object runOperation(String filePath, String action, String charge) throws OperationException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("filePath", filePath);
        params.put("action", action);
        params.put("provider", VCSEventsPerforce.NAME);
        params.put("charge", charge);

        return automationService.run(ctx, VCSEventsReceiverOperation.ID, params);
    }

    @Test
    public void createDocument() throws OperationException {
        Object ret = runOperation("//Nuxeo/main/something/test/blabla.mp4", "add", null);
        assertTrue(ret instanceof DocumentModel);

        ret = runOperation("//blasd/test.txt", "add", null);
        assertNull(ret);
    }

}
