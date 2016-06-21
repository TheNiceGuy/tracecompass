/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.swtchart.Chart;

public abstract class XYChartViewer implements IChartViewer {

    Chart fChart;
    DataSeries fSeries;

    public XYChartViewer(Composite parent, DataSeries series) {
        fChart = new Chart(parent, SWT.NONE);
        fSeries = series;

        // title and axis names
        fChart.getTitle().setText(generateTitle());
        fChart.getAxisSet().getXAxis(0).getTitle().setText(generateXTitle());
        fChart.getAxisSet().getYAxis(0).getTitle().setText(generateYTitle());

        // rotate X axis ticks
        fChart.getAxisSet().getXAxis(0).getTick().setTickLabelAngle(90);
    }

    public Chart getChart() {
        return fChart;
    }

    public DataSeries getSeries() {
        return fSeries;
    }

    private String generateTitle() {
        return generateXTitle() + " vs. " + generateYTitle();
    }

    private String generateXTitle() {
        return fSeries.getXData().getAspect().getLabel();
    }

    private String generateYTitle() {
        DataDescriptor descriptor = fSeries.getYData().iterator().next();

        if(fSeries.getYData().size() == 1) {
            return descriptor.getAspect().getLabel();
        }

        return "TODO";
    }
}
