/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartData;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartModel;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

public class ChartView extends TmfView {
    /**
     * The view ID as defined in plugin.xml
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.tmf.chart.ui.chartview"; //$NON-NLS-1$

    public ChartView() {
        super(ID);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

        Button button = new Button(sashForm, SWT.PUSH);
        button.setText("Click Me"); //$NON-NLS-1$
        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(@Nullable SelectionEvent e) {
                if (parent == null) {
                    return;
                }

                org.eclipse.tracecompass.tmf.chart.ui.dialog.ChartMakerDialog maker =
                        new org.eclipse.tracecompass.tmf.chart.ui.dialog.ChartMakerDialog(checkNotNull(parent.getShell()), IDataChartModel.getInstances().get(0));
                maker.open();

//                maker.open();
//                ChartData chartData = maker.getDataSeries();
//                ChartModel chartModel = maker.getChartModel();
//                if (chartData == null || chartModel == null) {
//                    return;
//                }
//
//                IChartViewer.createChart(sashForm, chartData, chartModel);
            }

            @Override
            public void widgetDefaultSelected(@Nullable SelectionEvent e) {
                System.out.println(IDataChartModel.getInstances().size());
            }
        });
    }

    @Override
    public void setFocus() {

    }

}
