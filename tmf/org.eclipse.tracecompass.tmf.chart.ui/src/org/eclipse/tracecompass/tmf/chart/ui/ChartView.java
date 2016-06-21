/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

public class ChartView extends TmfView {
    /**
     *  The view ID as defined in plugin.xml
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.tmf.chart.ui.chartview"; //$NON-NLS-1$

    public ChartView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

        Button button = new Button(sashForm, SWT.PUSH);
        button.setText("Click Me");

        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ChartMakerDialog maker = new ChartMakerDialog(parent.getShell());

                maker.open();
                DataSeries dataSeries = maker.getDataSeries();
                if(dataSeries == null) {
                    return;
                }

//                ChartDrawer chart = new ChartDrawer(sashForm, dataSeries);
//                chart.create();

                IChartViewer chart = IChartViewer.createChart(sashForm, dataSeries);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                System.out.println(AbstractDataModel.getInstances().size());
            }
          });
    }

    @Override
    public void setFocus() {

    }

}
