package com.nuxeo.perforce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;
import com.nuxeo.perforce.VCSEventsProvider.EVENT_ACTION;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy({ "com.nuxeo.perforce.nuxeo-perforce-core", "org.nuxeo.ecm.core.mimetype" })
public class TestVCSEventsService {

    @Inject
    protected VCSEventsService vCSEventsService;

    @Inject
    protected MimetypeRegistry registryService;

    @Test
    public void testService() {
        assertNotNull(vCSEventsService);
        assertNotNull(registryService);
        assertNotNull(registryService.getExtensionsFromMimetypeName("test.txt"));
    }

    @Test
    public void testEventsTransformer() {
        assertNull(vCSEventsService.getEventsProvider("test"));
        assertNotNull(vCSEventsService.getEventsProvider("perforce"));
    }

    @Test
    public void testPerforceImageVideoHandling() {
        VCSEventsPerforce perforce = new VCSEventsPerforce();
        assertFalse(perforce.handleFilename("toto.txt"));
        assertFalse(perforce.handleFilename("/something/more/long/toto.pdf"));
        assertFalse(perforce.handleFilename("/something/more/long/toto.zip"));

        assertTrue(perforce.handleFilename("/something/more/long/toto.jpg"));
        assertTrue(perforce.handleFilename("/something/more/long/toto.mp4"));
        assertTrue(perforce.handleFilename("/something/more/long/toto.mp3"));
    }

    @Test
    public void testPerforceActionResolution() {
        VCSEventsPerforce perforce = new VCSEventsPerforce();
        assertEquals(EVENT_ACTION.CREATE, perforce.getAction("add"));
        assertEquals(EVENT_ACTION.UPDATE, perforce.getAction("edit"));
        assertEquals(EVENT_ACTION.DELETE, perforce.getAction("delete"));
        assertNull(perforce.getAction("move"));
    }
}
