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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IStringSource;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartRange;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

import com.google.common.collect.Iterators;

/**
 * Class for a bar chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class BarChart extends XYChartViewer {

    private static final double LOGSCALE_EPSILON_FACTOR = 100.0;

    private double fLogScaleEpsilon = ZERO_DOUBLE;
    private double fMin;
    private double fMax;

    /**
     * Map reprensenting categories on the X axis
     */
    private String[] fCategories;

    /**
     * Constructor.
     *
     * @param parent parent composite
     * @param dataSeries configured data series for the chart
     * @param model chart model to use
     */
    public BarChart(Composite parent, ChartData dataSeries, ChartModel model) {
        super(parent, dataSeries, model);

        /* Enable categories */
        getChart().getAxisSet().getXAxis(0).enableCategory(true);

        /*
         * Log scale magic course 101:
         *
         * It uses the relative difference divided by a factor
         * (100) to get as close as it can to the actual minimum but still a
         * little bit smaller. This is used as a workaround of SWTCHART
         * limitations regarding custom scale drawing in log scale mode, bogus
         * representation of NaN double values and limited support of multiple
         * size series.
         *
         * This should be good enough for most users.
         */
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double value;

        if(getModel().isYLogscale()) {
            /* Find minimum and maximum values excluding <= 0 values */
            for(DataDescriptor descriptor : getData().getYData()) {
                INumericalSource source = (INumericalSource) descriptor.getSource();

                value = source.getStreamNumber()
                        .map(num -> getInternalDoubleValue(num, fYInternalRange, fYExternalRange))
                        .filter(num -> num > 0)
                        .min(Double::compare)
                        .get()
                        .doubleValue();
                min = Math.min(min, value);

                value = source.getStreamNumber()
                        .map(num -> getInternalDoubleValue(num, fYInternalRange, fYExternalRange))
                        .filter(num -> num > 0)
                        .max(Double::compare)
                        .get()
                        .doubleValue();
                max = Math.max(max, value);
            }

            if (min == Double.MAX_VALUE) {
                return;
            }

            /* Calculate epsilon */
            double delta = max - min;
            fLogScaleEpsilon = min - ((min * delta) / (LOGSCALE_EPSILON_FACTOR * max));

            /**TODO*/
            fMin = min;
            fMax = max;
        }
    }

    @Override
    public void populate() {
        super.populate();

        for(IAxis yAxis : getChart().getAxisSet().getYAxes()) {
            yAxis.getTick().setFormat(getContinuousAxisFormatter(getData().getYData(), fYInternalRange, fYExternalRange));

            /*
             * SWTChart workaround: SWTChart fiddles with tick mark visibility based
             * on the fact that it can parse the label to double or not.
             *
             * If the label happens to be a double, it checks for the presence of
             * that value in its own tick labels to decide if it should add it or
             * not. If it happens that the parsed value is already present in its
             * map, the tick gets a visibility of false.
             *
             * The X axis does not have this problem since SWTCHART checks on label
             * angle, and if it is != 0 simply does no logic regarding visibility.
             * So simply set a label angle of 1 to the axis.
             */
            yAxis.getTick().setTickLabelAngle(1);
        }

        /* Adjust the chart range */
        getChart().getAxisSet().adjustRange();

        if (getModel().isYLogscale() && fLogScaleEpsilon != fMax) {
            getChart().getAxisSet().getYAxis(0).setRange(new Range(fLogScaleEpsilon, fMax));
        }

        /* Once the chart is filled, refresh the axis labels */
        refreshDisplayLabels();
    }

    @Override
    public void assertData() {
        super.assertData();

        /* Make sure the X axis is not continuous */
        if(getData().getXData().get(0).getAspect().isContinuous()) {
            throw new IllegalArgumentException("Bar chart X axis cannot be numerical."); //$NON-NLS-1$
        }

        /* Make sure the Y axis is continuous */
        if(!getData().getYData().get(0).getAspect().isContinuous()) {
            throw new IllegalArgumentException("Bar chart Y axis must be numerical."); //$NON-NLS-1$
        }

        /* Make sure there is only one X axis */
        if(getData().countDiffXData() > 1) {
            throw new IllegalArgumentException("Bar chart can only have one X axis."); //$NON-NLS-1$
        }
    }

    @Override
    public void computeRanges() {
        fXExternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));

        if(getData().getYData().get(0).getAspect().isContinuous()) {
            fYExternalRange = getRange(getData().getYData(), true);
        } else {
            fYExternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));
        }
    }

    @Override
    public void createSeries() {
        ISeriesSet set = getChart().getSeriesSet();

        Iterator<DataDescriptor> iteratorX = getData().getXData().iterator();
        Iterator<DataDescriptor> iteratorY = getData().getYData().iterator();

        while(iteratorX.hasNext()) {
            DataDescriptor descriptor = iteratorY.next();

            String title = descriptor.getAspect().getLabel();
            IBarSeries series = (IBarSeries) set.createSeries(SeriesType.BAR, title);
            series.setBarPadding(50);

            fXSeries.put(iteratorX.next(), series);
            fYSeries.put(descriptor, series);
        }
    }

    @Override
    public void setSeriesColor() {
        Iterator<Color> colors = Iterators.cycle(COLORS);

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            ((IBarSeries) series).setBarColor(colors.next());
        }
    }

    @Override
    public void generateXData(List<DataDescriptor> descriptors) {
        DataDescriptor descriptor = descriptors.get(0);

        /* Generate map of categories and integers at the same time */
        fCategories = ((IStringSource) descriptor.getSource()).getStreamString().toArray(size -> new String[size]);
    }

    @Override
    public void generateYData(List<DataDescriptor> descriptors) {
        double[] data;

        for(DataDescriptor descriptor : descriptors) {
            /* Generate data if the aspect is continuous */
            Double[] temp = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                    .map(num -> new Double(num.doubleValue()))
                    .toArray(size -> new Double[size]);

            data = new double[temp.length];
            for(int j = 0; j < temp.length; j++) {
                /* Null value for y is the same as zero */
                if(temp[j] == null) {
                    temp[j] = ZERO_DOUBLE;
                } else {
                    temp[j] = getInternalDoubleValue(temp[j], fYInternalRange, fYExternalRange);
                }

                /*
                 * Less or equal to 0 values can't be plotted on a log
                 * scale. We map them to the mean of the >=0 minimal value
                 * and the calculated log scale magic epsilon.
                 */
                if(getModel().isYLogscale() && temp[j] <= ZERO_DOUBLE) {
                    temp[j] = (fMin + fLogScaleEpsilon)/2.0;
                }

                data[j] = temp[j];
            }

            checkNotNull(fYSeries.get(descriptor)).setYSeries(data);
        }
    }

    @Override
    public void configureAxis() {
        /* Format X axis */
        Stream.of(getChart().getAxisSet().getXAxes()).forEach(cat -> cat.enableCategory(true));

        /* Format Y axis */
        for(IAxis axis : getChart().getAxisSet().getYAxes()) {
            IAxisTick tick = axis.getTick();
            tick.setFormat(getContinuousAxisFormatter(getData().getYData(), fXExternalRange, fXExternalRange));
        }
    }

    @Override
    protected void refreshDisplayLabels() {
        /* Only if we have at least 1 category */
        if (fCategories.length == 0) {
            return;
        }

        /* Only refresh if labels are visible */
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        if (!xAxis.getTick().isVisible() || !xAxis.isCategoryEnabled()) {
            return;
        }

        /*
         * Shorten all the labels to 5 characters plus "…" when the longest
         * label length is more than 50% of the chart height.
         */
        Rectangle rect = getChart().getClientArea();
        int lengthLimit = (int) (rect.height * 0.40);

        GC gc = new GC(getParent());
        gc.setFont(xAxis.getTick().getFont());

        /* Find the longest category string */
        String longestString = Arrays.stream(fCategories).max(Comparator.comparingInt(String::length)).orElse(fCategories[0]);

        /* Get the length and height of the longest label in pixels */
        Point pixels = gc.stringExtent(longestString);

        /* Completely arbitrary */
        int cutLen = 5;

        String[] displayCategories = new String[fCategories.length];
        if (pixels.x > lengthLimit) {
            /* We have to cut down some strings */
            for (int i = 0; i < fCategories.length; i++) {
                if (fCategories[i].length() > cutLen) {
                    displayCategories[i] = fCategories[i].substring(0, cutLen) + ELLIPSIS;
                } else {
                    displayCategories[i] = fCategories[i];
                }
            }
        } else {
            /* All strings should fit */
            displayCategories = Arrays.copyOf(fCategories, fCategories.length);
        }
        xAxis.setCategorySeries(displayCategories);

        /* Cleanup */
        gc.dispose();
    }
}
