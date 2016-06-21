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
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.eclipse.tracecompass.tmf.chart.ui.format.MapFormat;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;

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

        BiMap<@Nullable String, Integer> yMap = checkNotNull(HashBiMap.create());
        BiMap<@Nullable String, Integer> xMap = checkNotNull(HashBiMap.create());

        double[] yData;
        double[] xData;

        // create a series for each Y aspects
        List<ILineSeries> seriesList = new ArrayList<>();
        for(DataDescriptor descriptor : dataSeries.getYData()) {
            String title = descriptor.getAspect().getLabel();

            if(descriptor.getAspect().isContinuous()) {
                // generate data if the aspect is continuous
                yData = descriptor.getSource().getStreamNumerical()
                        .mapToDouble(num -> (double) num)
                        .toArray();
            } else {
                // generate unique position for each string
                generateLabelMap(descriptor.getSource().getStreamString(), yMap);

                yData = descriptor.getSource().getStreamString()
                        .map(str -> yMap.get(str))
                        .mapToDouble(num -> (double) num)
                        .toArray();
            }

            // add the serie into the list
            ILineSeries series = (ILineSeries) fChart.getSeriesSet()
                    .createSeries(SeriesType.LINE, title);

            series.setYSeries(yData);
            seriesList.add(series);
        }

        // create a series for each X aspects
        if(dataSeries.getXData().getAspect().isContinuous()) {
            // generate data if the aspect is continuous
            xData = dataSeries.getXData().getSource().getStreamNumerical()
                    .mapToDouble(num -> (double) num)
                    .toArray();
        } else {
            // generate unique position for each string
            generateLabelMap(dataSeries.getXData().getSource().getStreamString(), xMap);

            xData = dataSeries.getXData().getSource().getStreamString()
                    .map(str -> xMap.get(str))
                    .mapToDouble(num -> (double) num)
                    .toArray();
        }

        // link each Y series with the unique X serie
        for(ILineSeries series : seriesList) {
            series.setXSeries(xData);
            series.setLineStyle(LineStyle.NONE);
        }

        // format X ticks
        if(dataSeries.getXData().getAspect().isContinuous()) {

        } else {
            IAxisTick xTick = fChart.getAxisSet().getXAxis(0).getTick();
            xTick.setFormat(new MapFormat(checkNotNull(xMap)));
            updateTickMark(checkNotNull(xMap), xTick, fChart.getPlotArea().getSize().x);
        }

        // format Y ticks
        if(dataSeries.getYData().iterator().next().getAspect().isContinuous()) {

        } else {
            IAxisTick yTick = fChart.getAxisSet().getYAxis(0).getTick();
            yTick.setFormat(new MapFormat(checkNotNull(yMap)));
            updateTickMark(checkNotNull(yMap), yTick, fChart.getPlotArea().getSize().y);
        }

        // configure the chart
        fChart.getAxisSet().adjustRange();
        fChart.getAxisSet().getXAxis(0).enableLogScale(fSeries.isXLogscale());
        fChart.getAxisSet().getYAxis(0).enableLogScale(fSeries.isYLogscale());

        // configure the color of each serie
        setLineSeriesColor();
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    private static void generateLabelMap(Stream<String> stream, BiMap<@Nullable String, Integer> map) {
        stream.distinct().forEach(str -> map.put(str, map.size()));
    }

    private static void updateTickMark(BiMap<@Nullable String, Integer> map, IAxisTick tick, int availableLenghtPixel) {
        int nbLabels = Math.max(1, map.size());
        int stepSizePixel = availableLenghtPixel / nbLabels;
        /*
         * This step is a limitation on swtchart side regarding minimal grid
         * step hint size. When the step size are smaller it get defined as the
         * "default" value for the axis instead of the smallest one.
         */
        if (IAxisTick.MIN_GRID_STEP_HINT > stepSizePixel) {
            stepSizePixel = (int) IAxisTick.MIN_GRID_STEP_HINT;
        }
        tick.setTickMarkStepHint(stepSizePixel);
    }

    private void setLineSeriesColor() {
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
