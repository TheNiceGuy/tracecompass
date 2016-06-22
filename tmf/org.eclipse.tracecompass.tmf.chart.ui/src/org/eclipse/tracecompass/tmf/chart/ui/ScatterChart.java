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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

import com.google.common.collect.Iterators;

import org.swtchart.LineStyle;

/**
 * Class for building a scatter chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ScatterChart extends XYChartViewer {

    /**
     * This constructor creates the chart.
     *
     * @param parent parent composite
     * @param dataSeries configured data series for the chart
     */
    public ScatterChart(Composite parent, DataSeries dataSeries) {
        super(parent, dataSeries);
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public ISeries createSerie(String title) {
        ILineSeries series = (ILineSeries) getChart().getSeriesSet().createSeries(SeriesType.LINE, title);
        series.setLineStyle(LineStyle.NONE);
        return series;
    }

    @Override
    public double[] generateYData(DataDescriptor descriptor) {
        double[] data;

        if(descriptor.getAspect().isContinuous()) {
            // generate data if the aspect is continuous
            data = descriptor.getSource().getStreamNumerical()
                    .mapToDouble(num -> (double) num)
                    .toArray();
        } else {
            // generate unique position for each string
            generateLabelMap(descriptor.getSource().getStreamString(), getYMap());

            data = descriptor.getSource().getStreamString()
                    .map(str -> getYMap().get(str))
                    .mapToDouble(num -> (double) num)
                    .toArray();
        }

        return data;
    }

    @Override
    public double[] generateXData(DataDescriptor descriptor) {
        double[] data;

        if(descriptor.getAspect().isContinuous()) {
            // generate data if the aspect is continuous
            data = descriptor.getSource().getStreamNumerical()
                    .mapToDouble(num -> (double) num)
                    .toArray();
        } else {
            // generate unique position for each string
            generateLabelMap(descriptor.getSource().getStreamString(), getXMap());

            data = descriptor.getSource().getStreamString()
                    .map(str -> getXMap().get(str))
                    .mapToDouble(num -> (double) num)
                    .toArray();
        }

        return data;
    }

    @Override
    public void setSeriesColor() {
        Iterator<Color> colorsIt;

        colorsIt = Iterators.cycle(COLORS);

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            ((ILineSeries) series).setSymbolColor((colorsIt.next()));
            /*
             * Generate initial array of Color to enable per point color change
             * on selection in the future
             */
            ArrayList<Color> colors = new ArrayList<>();
            for (int i = 0; i < series.getXSeries().length; i++) {
                Color color = ((ILineSeries) series).getSymbolColor();
                colors.add(checkNotNull(color));
            }
            ((ILineSeries) series).setSymbolColors(colors.toArray(new Color[colors.size()]));
        }
    }
}
