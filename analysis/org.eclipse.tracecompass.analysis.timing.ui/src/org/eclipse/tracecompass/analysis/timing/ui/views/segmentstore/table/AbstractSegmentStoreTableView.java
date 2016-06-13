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
 *   Bernd Hufmann - Move abstract class to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallTableProvider;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiDurationAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiProcessNameAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimestampAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiXYSeriesDescription;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiDuration;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiSystemCall;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimestamp;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers.ILamiViewer;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiSeriesDialog;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ResultTable;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.TableEntry;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataInteger;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataString;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel.ChartType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableClass;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.json.JSONException;

import com.google.common.collect.ImmutableList;

/**
 * View for displaying a segment store analysis in a table.
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public abstract class AbstractSegmentStoreTableView extends TmfView {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private @Nullable AbstractSegmentStoreTableViewer fSegmentStoreViewer;
    private @Nullable Composite fComposite;

    // ------------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------------

    private class NewChartAction extends Action {
        private List<LamiTableEntryAspect> x = new ArrayList<>();
        private List<LamiTableEntryAspect> y = new ArrayList<>();

        public NewChartAction() {
            x.add(new LamiTimestampAspect("Start Time", 0));
            x.add(new LamiTimestampAspect("End Time", 1));
            x.add(new LamiDurationAspect("Duration", 2));
            x.add(new LamiProcessNameAspect("System Call", 3));
            y.add(new LamiTimestampAspect("Start Time", 0));
            y.add(new LamiTimestampAspect("End Time", 1));
            y.add(new LamiDurationAspect("Duration", 2));
            y.add(new LamiProcessNameAspect("System Call", 3));
        }

        @Override
        public void run() {
            IStructuredContentProvider contentProvider = checkNotNull(ArrayContentProvider.getInstance());

            try {
                LamiData data = LamiData.createFromObject(123);

                System.out.println("asd " + data.getClass());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ImmutableList.Builder<LamiTableEntry> dataBuilder = new ImmutableList.Builder<>();
            long min = -1;
            long max = -1;

            // Creating list
            if(fSegmentStoreViewer !=  null) {
                ISegmentStoreProvider provider = fSegmentStoreViewer.getSegmentProvider();

                if(provider != null) {
                    ISegmentStore<ISegment> store = provider.getSegmentStore();
                    if(store != null) {
                        SystemCallTableProvider pro = new SystemCallTableProvider(store);
                        ResultTable rTable = pro.getResultTable();

                        if(rTable != null) {
                            rTable.getEntries().stream().map(entry -> ((DataString)entry.getValue(3))).forEach(System.out::println);
                        }

                        for(ISegment seg : store) {
                            SystemCall sc = (SystemCall) seg;

                            if(min > sc.getStart() || min == -1) {
                                min = sc.getStart();
                            }

                            if(max < sc.getEnd()) {
                                max = sc.getEnd();
                            }

                            List<LamiData> entry = new ArrayList<>();
                            entry.add(new LamiTimestamp(sc.getStart()));
                            entry.add(new LamiTimestamp(sc.getEnd()));
                            entry.add(new LamiDuration(sc.getLength()));
                            entry.add(new LamiSystemCall(sc.getName()));

                            dataBuilder.add(new LamiTableEntry(entry));
                        }
                    }
                }
            }

            LamiTimeRange range = new LamiTimeRange(min, max);
            LamiTableClass tableClass = new LamiTableClass("string1", "string2", x, Collections.EMPTY_SET);

            LamiResultTable resultTable = new LamiResultTable(range, tableClass, dataBuilder.build());
            System.out.println();

            // TESTING ACCESS TO COLUMN NAME
            if(fSegmentStoreViewer != null) {
                TableViewer table = fSegmentStoreViewer.getTableViewer();

                for (int i = 0; i < table.getTable().getColumnCount(); i++) {
                    //System.out.println(table.getTable().getColumn(i).getText());
                }
            }

            if(fComposite == null) {
                return;
            }

            // CREATE DIALOG
            LamiSeriesDialog dialog = new LamiSeriesDialog(
                    fComposite.getShell(),
                    ChartType.BAR_CHART,
                    x, y,
                    contentProvider,
                    new LabelProvider() {
                        @Override
                        public String getText(@Nullable Object element) {
                            return ((LamiTableEntryAspect) checkNotNull(element)).getLabel();
                        }
                    },
                    contentProvider,
                    new LabelProvider() {
                        @Override
                        public String getText(@Nullable Object element) {
                            return ((LamiTableEntryAspect) checkNotNull(element)).getLabel();
                        }
                    });

             // SHOW DIALOG
             if (dialog.open() != Window.OK) {
                 return;
             }

             // GET SELECTED X-Y
             List<LamiXYSeriesDescription> results = Arrays.stream(dialog.getResult())
                     .map(serie -> (LamiXYSeriesDescription) serie)
                     .collect(Collectors.toList());

             List<String> xAxisColString = new ArrayList<>();
             List<String> yAxisColString = new ArrayList<>();

             xAxisColString = results.stream()
                     .map(element -> element.getXAspect().getLabel())
                     .distinct()
                     .collect(Collectors.toList());

             yAxisColString = results.stream()
                     .map(element -> element.getYAspect().getLabel())
                     .distinct()
                     .collect(Collectors.toList());

             System.out.println(min + " " + max);

             LamiChartModel model = new LamiChartModel(ChartType.XY_SCATTER,
                     "",
                     xAxisColString,
                     yAxisColString,

                     false, false);

             ILamiViewer.createLamiChart(fComposite, resultTable, model);
             System.out.println(results);
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public AbstractSegmentStoreTableView() {
        super(""); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(@Nullable Composite parent) {
        SashForm sf = new SashForm(parent, SWT.NONE);
        TableViewer tableViewer = new TableViewer(sf, SWT.FULL_SELECTION | SWT.VIRTUAL);
        fSegmentStoreViewer = createSegmentStoreViewer(tableViewer);
        setInitialData();

        fComposite = parent;
        IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
        IAction clickAction = new NewChartAction();
        clickAction.setText("Chart");

        menuMgr.add(clickAction);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void setFocus() {
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.getTableViewer().getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.dispose();
        }
    }

    /**
     * Returns the latency analysis table viewer instance
     *
     * @param tableViewer
     *            the table viewer to ause
     * @return the latency analysis table viewer instance
     */
    protected abstract AbstractSegmentStoreTableViewer createSegmentStoreViewer(TableViewer tableViewer);

    /**
     * Get the table viewer
     *
     * @return the table viewer, useful for testing
     */
    @Nullable
    public AbstractSegmentStoreTableViewer getSegmentStoreViewer() {
        return fSegmentStoreViewer;
    }

    /**
     * Set initial data into the viewer
     */
    private void setInitialData() {
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.setData(fSegmentStoreViewer.getSegmentProvider());
        }
    }
}
