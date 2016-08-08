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
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.chart.IChartViewer;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.dialog.ChartMakerDialog;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.signal.ChartSelectionUpdateSignal;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;

/**
 * View for the latency analysis
 *
 * @author France Lapointe Nguyen
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SystemCallLatencyView extends AbstractSegmentStoreTableView {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Override
    protected AbstractSegmentStoreTableViewer createSegmentStoreViewer(TableViewer tableViewer) {
        return new SystemCallLatencyTableViewer(tableViewer);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);

        /* Add a menu for adding charts */
        Action addChart = new NewChartAction();
        addChart.setText(Messages.SystemCallLatencyView_NewCustomChart);

        IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
        menuMgr.add(addChart);
    }

    // ------------------------------------------------------------------------
    // Anonymous classes
    // ------------------------------------------------------------------------

    private class NewChartAction extends Action {
        @Override
        public void run() {
            AbstractSegmentStoreTableViewer viewer = SystemCallLatencyView.this.getSegmentStoreViewer();
            if (viewer == null) {
                return;
            }

            /* Get the segment store provider */
            ISegmentStoreProvider provider = viewer.getSegmentProvider();
            if (!(provider instanceof SystemCallLatencyAnalysis)) {
                return;
            }
            SystemCallLatencyAnalysis analysis = (SystemCallLatencyAnalysis) provider;

            /* Get the composite for putting the chart */
            Composite composite = SystemCallLatencyView.this.getContainer();
            if (composite == null) {
                return;
            }

            /* Open the chart maker dialog */
            ChartMakerDialog dialog = new ChartMakerDialog(checkNotNull(composite.getShell()), analysis);
            if (dialog.open() != Window.OK) {
                return;
            }

            /* Make sure the data for making a chart was generated */
            ChartData data = dialog.getDataSeries();
            ChartModel model = dialog.getChartModel();
            if (data == null || model == null) {
                return;
            }

            /* Make a chart with the factory constructor */
            IChartViewer.createChart(composite, data, model);

            /* Get the currently selected segment in the table */
            ISegment segment = (ISegment) viewer.getTableViewer().getStructuredSelection().getFirstElement();
            if (segment == null) {
                return;
            }

            /* Send a signal to the chart for the default selection */
            Set<Object> objects = new HashSet<>();
            objects.add(segment);

            ChartSelectionUpdateSignal newSignal = new ChartSelectionUpdateSignal(this, analysis, objects);
            TmfSignalManager.dispatchSignal(newSignal);
        }
    }

}
