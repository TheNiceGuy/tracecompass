/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.uml2sd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.uml2sd.TmfAsyncSequenceDiagramEvent;
import org.junit.Test;

/**
 * TmfAsyncSequenceDiagramEventTest
 */
public class TmfAsyncSequenceDiagramEventTest {

    private final @NonNull String fTypeId  = "Some type";
    private final String fLabel0  = "label1";
    private final String fLabel1  = "label2";
    private final String[] fLabels  = new String[] { fLabel0, fLabel1 };

    private final ITmfTimestamp fTimestamp1 = TmfTimestamp.create(12345, (byte) 2);
    private final ITmfTimestamp fTimestamp2 = TmfTimestamp.create(12350, (byte) 2);
    private final TmfEventType fType       = new TmfEventType(fTypeId, TmfEventField.makeRoot(fLabels));

    private final ITmfEvent fEvent1;
    private final ITmfEvent fEvent2;
    private final TmfEventField fContent1;
    private final TmfEventField fContent2;

    /**
     * Constructor for the test case
     */
    public TmfAsyncSequenceDiagramEventTest() {
        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content", null);
        fEvent1 = new TmfEvent(null, ITmfContext.UNKNOWN_RANK, fTimestamp1, fType, fContent1);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content", null);
        fEvent2 = new TmfEvent(null, ITmfContext.UNKNOWN_RANK, fTimestamp2, fType, fContent2);
    }

    /**
     * Main test
     */
    @Test
    public void testTmfAsyncSequenceDiagramEvent() {
        TmfAsyncSequenceDiagramEvent event = null;

        // Check for illegal arguments (i.e. null for the parameters)
        try {
            event = new TmfAsyncSequenceDiagramEvent(null, null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("startEvent=null"));
        }

        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1,  fEvent2, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("sender=null"));
        }

        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, fEvent2, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("receiver=null"));
        }

        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, fEvent2, "sender", null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("name=null"));
        }

        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, null, "sender", "receiver", "signal");
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("endEvent=null"));
        }

        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, fEvent2, "sender", "receiver", "signal");
            // success
            assertEquals("testTmfAsyncSequenceDiagramEvent", 0, event.getStartTime().compareTo(fTimestamp1));
            assertEquals("testTmfAsyncSequenceDiagramEvent", 0, event.getEndTime().compareTo(fTimestamp2));
            assertEquals("testTmfAsyncSequenceDiagramEvent", "sender", event.getSender());
            assertEquals("testTmfAsyncSequenceDiagramEvent", "receiver", event.getReceiver());
            assertEquals("testTmfAsyncSequenceDiagramEvent", "signal", event.getName());

        } catch (IllegalArgumentException e) {
            fail();
        }
    }
}
