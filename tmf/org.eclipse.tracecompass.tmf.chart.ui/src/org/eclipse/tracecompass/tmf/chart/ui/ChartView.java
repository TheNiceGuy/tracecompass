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
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
//import org.eclipse.tracecompass.provisional.tmf.chart.ui.BarChartViewer;
import org.eclipse.tracecompass.provisional.tmf.chart.ui.IChartViewer;
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
//        // create a chart
//        Chart chart = new Chart(parent, SWT.NONE);
//
//        // set titles
//        chart.getTitle().setText("Bar Chart Example");
//        chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points");
//        chart.getAxisSet().getYAxis(0).getTitle().setText("Amplitude");
//
//        // create bar series
//        IBarSeries barSeries = (IBarSeries) chart.getSeriesSet()
//            .createSeries(SeriesType.BAR, "bar series");
//
//        double[] ySeries = {1,6,2,4,6};
//        double[] xSeries = {10,20,30,40,20};
//        barSeries.setYSeries(ySeries);
//        barSeries.setXSeries(xSeries);
//
//        // adjust the axis range
//        chart.getAxisSet().adjustRange();

        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

        Button button = new Button(sashForm, SWT.PUSH);
        button.setText("Click Me");
        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ChartMakerDialog maker = new ChartMakerDialog(parent.getShell());

                maker.open();
                ChartData chartData = maker.getDataSeries();
                ChartModel chartModel = maker.getChartModel();
                if(chartData == null || chartModel == null) {
                    return;
                }

                //BarChartViewer bar = new BarChartViewer(sashForm, chartData, chartModel);
                IChartViewer.createChart(sashForm, chartData, chartModel);
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
