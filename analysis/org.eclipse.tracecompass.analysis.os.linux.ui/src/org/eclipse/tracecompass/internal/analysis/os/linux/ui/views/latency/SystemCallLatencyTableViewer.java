/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.signal.ChartSelectionUpdateSignal;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Displays the latency analysis data in a column table
 *
 * @author France Lapointe Nguyen
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SystemCallLatencyTableViewer extends AbstractSegmentStoreTableViewer {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            The table viewer
     */
    public SystemCallLatencyTableViewer(TableViewer tableViewer) {
        super(tableViewer);

        getTableViewer().getTable().addSelectionListener(new TableSelectionListener());
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, SystemCallLatencyAnalysis.class, SystemCallLatencyAnalysis.ID);
    }

    // ------------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------------

    /**
     * This method handles chart selection update signal that comes from a
     * custom chart.
     *
     * @param signal
     *            The update sent by the chart
     */
    @TmfSignalHandler
    public void updateSelection(ChartSelectionUpdateSignal signal) {
        /* Make sure we are not sending a signal to ourself */
        if (signal.getSource() == this) {
            return;
        }

        /* Get the analysis that provides data */
        ISegmentStoreProvider provider = getSegmentProvider();
        if (!(provider instanceof SystemCallLatencyAnalysis)) {
            return;
        }
        SystemCallLatencyAnalysis analysis = (SystemCallLatencyAnalysis) provider;

        /* Make sure the signal comes from the data provider's scope */
        if (analysis != signal.getDataProvider()) {
            return;
        }

        /**
         * The segment store table does not allow multiple selected objects. If
         * we receive multiple objects, we try to find the latest object
         * selected. Then, we send a new signal to the chart to make sure it
         * only has one selected object.
         */
        Object selection = getTableViewer().getStructuredSelection().getFirstElement();

        /* Find the first item that is not the currently selected item */
        Set<Object> objects = signal.getSelectedObject();
        Iterator<Object> iterator = objects.iterator();
        while (iterator.hasNext()) {
            if (objects.size() == 1) {
                selection = checkNotNull(iterator.next());
            } else {
                Object next = checkNotNull(iterator.next());

                /* Replace only with the new object */
                if (selection != next) {
                    selection = next;
                    break;
                }
            }

        }

        /* Create the selection for the table */
        StructuredSelection tableSelection = new StructuredSelection(selection);
        objects.clear();
        if (selection != null) {
            objects.add(selection);
        }

        /* Update the selection in the table */
        getTableViewer().setSelection(tableSelection, true);

        /* Send the new signal */
        ChartSelectionUpdateSignal newSignal = new ChartSelectionUpdateSignal(this, analysis, objects);
        TmfSignalManager.dispatchSignal(newSignal);

        /* Convert the segment into a selection range */
        TmfSelectionRangeUpdatedSignal updateSignal;
        ISegment segment = (ISegment) selection;
        if (segment != null) {
            ITmfTimestamp start = TmfTimestamp.fromNanos(segment.getStart());
            ITmfTimestamp end = TmfTimestamp.fromNanos(segment.getEnd());
            updateSignal = new TmfSelectionRangeUpdatedSignal(this.getTableViewer(), start, end);

            /* Send a selection range update for all Trace Compass */
            TmfSignalManager.dispatchSignal(updateSignal);
        }
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    /**
     * Listener that sends a signal to the chart when a new item is selected.
     */
    private class TableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(@Nullable SelectionEvent event) {
            ISegment segment = ((ISegment) checkNotNull(event).item.getData());
            if (segment == null) {
                return;
            }

            /* Get the analysis that provides data */
            ISegmentStoreProvider provider = getSegmentProvider();
            if (!(provider instanceof SystemCallLatencyAnalysis)) {
                return;
            }
            SystemCallLatencyAnalysis analysis = (SystemCallLatencyAnalysis) provider;

            /* Send a signal to the chart for the default selection */
            TableViewer table = SystemCallLatencyTableViewer.this.getTableViewer();
            if (table == null) {
                return;
            }

            Set<Object> objects = new HashSet<>();
            objects.add(segment);

            ChartSelectionUpdateSignal newSignal = new ChartSelectionUpdateSignal(table, analysis, objects);
            TmfSignalManager.dispatchSignal(newSignal);
        }
    }

}
