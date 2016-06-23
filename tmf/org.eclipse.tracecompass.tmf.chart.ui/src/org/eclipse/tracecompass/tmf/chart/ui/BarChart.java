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

import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IStringSource;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

import com.google.common.collect.Iterators;

/**
 * Class for a bar chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class BarChart extends XYChartViewer {

    /**
     * Constructor.
     *
     * @param parent parent composite
     * @param dataSeries configured data series for the chart
     */
    public BarChart(Composite parent, DataSeries dataSeries) {
        super(parent, dataSeries);
    }

    @Override
    public ISeries createSerie(String title) {
        IBarSeries series = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR, title);
        series.setBarPadding(50);
        return series;
    }

    @Override
    public double[] generateYData(DataDescriptor descriptor) {
        double[] data;

        if(descriptor.getAspect().isContinuous()) {
            // generate data if the aspect is continuous
            data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                    .mapToDouble(num -> num.doubleValue())
                    .toArray();
        } else {
            // generate unique position for each string
            generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), getYMap());

            data = ((IStringSource) descriptor.getSource()).getStreamString()
                    .map(str -> getYMap().get(str))
                    .map(num -> checkNotNull(num))
                    .mapToDouble(num -> (double) num)
                    .toArray();
        }

        return data;
    }

    @Override
    public double[] generateXData(DataDescriptor descriptor) {
        double[] data;

        if(descriptor.getAspect().isContinuous()) {
            generateLabelMap(((INumericalSource) descriptor.getSource()).getStreamNumber()
                    .map(num -> num.toString()), getXMap());

            // generate data
            data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                    .map(num -> num.toString())
                    .map(str -> getXMap().get(str))
                    .map(num -> checkNotNull(num))
                    .mapToDouble(num -> num.doubleValue())
                    .toArray();
        } else {
            generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), getXMap());

            // generate data
            data = ((IStringSource) descriptor.getSource()).getStreamString()
                    .map(str -> getXMap().get(str))
                    .map(num -> checkNotNull(num))
                    .mapToDouble(num -> (double) num)
                    .toArray();
        }

        return data;
    }

    @Override
    public void setSeriesColor() {
        Iterator<Color> colors = Iterators.cycle(COLORS);

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            ((IBarSeries) series).setBarColor(colors.next());
        }
    }
}
