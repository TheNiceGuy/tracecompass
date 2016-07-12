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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartNumericalAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartData;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartModel;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.source.INumericalSource;
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

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Factor used in the computation of the logarithmic epsilon value
     */
    private static final double LOGSCALE_EPSILON_FACTOR = 100.0;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Logarithmic epsilon value to work around SWT limitations
     */
    private double fLogScaleEpsilon = ZERO_DOUBLE;
    /**
     * Minimum value of the Y data to work around SWT limitations
     */
    private double fMin;
    /**
     * Maximum value of the Y data to work around SWT limitations
     */
    private double fMax;
    /**
     * Map reprensenting categories on the X axis
     */
    private String @Nullable[] fCategories;
    /**
     * Data generated for the Y axis from the streams
     */
    private Map<@NonNull ISeries, @NonNull Double[]> fYData;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent
     *              parent composite
     * @param data
     *              configured data series for the chart
     * @param model
     *              chart model to use
     */
    public BarChart(Composite parent, ChartData data, ChartModel model) {
        super(parent, data, model);

        fYData = new HashMap<>();

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
                INumericalSource<@Nullable Number> source = checkNotNull(INumericalSource.class.cast(descriptor.getSource()));

                value = source.getStream()
                        .filter(Objects::nonNull)
                        .map(num -> getInternalDoubleValue(checkNotNull(num), fYInternalRange, fYExternalRange))
                        .filter(num -> num > 0)
                        .min(Double::compare)
                        .get()
                        .doubleValue();
                min = Math.min(min, value);

                value = source.getStream()
                        .map(num -> getInternalDoubleValue(checkNotNull(num), fYInternalRange, fYExternalRange))
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
            fMin = min;
            fMax = max;

            double delta = fMax - fMin;
            fLogScaleEpsilon = fMin - ((fMin * delta) / (LOGSCALE_EPSILON_FACTOR * fMax));

        }
    }

    @Override
    public void populate() {
        super.populate();

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
        if(getData().getX(0).getAspect() instanceof IDataChartNumericalAspect) {
            throw new IllegalArgumentException("Bar chart X axis cannot be numerical."); //$NON-NLS-1$
        }

        /* Make sure the Y axis is continuous */
        if(!(getData().getY(0).getAspect() instanceof IDataChartNumericalAspect)) {
            throw new IllegalArgumentException("Bar chart Y axis must be numerical."); //$NON-NLS-1$
        }

        /* Make sure there is only one X axis */
        if(countDiffAspects(getData().getXData()) > 1) {
            throw new IllegalArgumentException("Bar chart can only have one X axis."); //$NON-NLS-1$
        }
    }

    @Override
    public void computeRanges() {
        fXExternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));

        if(getData().getY(0).getAspect() instanceof IDataChartNumericalAspect) {
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

        /* Create a series for each descriptors */
        while(iteratorX.hasNext()) {
            DataDescriptor descriptor = checkNotNull(iteratorY.next());

            String title = descriptor.getAspect().getLabel();
            IBarSeries series = (IBarSeries) set.createSeries(SeriesType.BAR, title);

            getXSeries().put(checkNotNull(iteratorX.next()), checkNotNull(series));
            getYSeries().put(descriptor, checkNotNull(series));
        }
    }

    @Override
    public void setSeriesColor() {
        Iterator<Color> colors = Iterators.cycle(COLORS);

        for(ISeries series : getYSeries().values()) {
            ((IBarSeries) series).setBarColor(colors.next());
        }
    }

    @Override
    public void generateXData(List<DataDescriptor> descriptors) {
        DataDescriptor descriptor = checkNotNull(descriptors.get(0));

        /* Generate map of categories and integers at the same time */
        descriptor.getSource().getStream();
        fCategories = descriptor.getSource().getStream().toArray(size -> new String[size]);
    }

    @Override
    public void generateYData(List<DataDescriptor> descriptors) {
        for(DataDescriptor descriptor : descriptors) {
            INumericalSource<@Nullable Number> source = checkNotNull(INumericalSource.class.cast(descriptor.getSource()));
            @NonNull Double[] data;

            /* Generate data if the aspect is continuous */
            data = source.getStream()
                    .map(num -> {
                        if(num == null) {
                            return null;
                        }
                        return new Double(num.doubleValue());
                    }).toArray(size -> new Double[size]);

            fYData.put(checkNotNull(getYSeries().get(descriptor)), checkNotNull(data));
        }
    }

    @Override
    public void postProcessData() {
        for(ISeries serie : getChart().getSeriesSet().getSeries()) {
            String[] categories = checkNotNull(fCategories);
            Double[] yData = checkNotNull(fYData.get(serie));

            /* Make sure serie size matches */
            if(categories.length != yData.length) {
                throw new IllegalStateException("Series sizes don't match!"); //$NON-NLS-1$
            }

            /* Filter bad values */
            double[] yValid = new double[yData.length];
            for(int i = 0; i < categories.length; i++) {
                /* Null value for y is the same as zero */
                if(yData[i] == null) {
                    yValid[i] = ZERO_DOUBLE;
                } else {
                    yValid[i] = getInternalDoubleValue(checkNotNull(yData[i]), fYInternalRange, fYExternalRange);
                }

                /*
                 * Less or equal to 0 values can't be plotted on a log
                 * scale. We map them to the mean of the >=0 minimal value
                 * and the calculated log scale magic epsilon.
                 */
                if(getModel().isYLogscale() && yValid[i] <= ZERO_DOUBLE) {
                    yValid[i] = (fMin + fLogScaleEpsilon)/2.0;
                }
            }

            serie.setYSeries(yValid);
        }
    }

    @Override
    public void configureAxes() {
        /* Format X axes */
        Stream.of(getChart().getAxisSet().getXAxes()).forEach(cat -> cat.enableCategory(true));

        /* Format Y axes */
        for(IAxis axis : getChart().getAxisSet().getYAxes()) {
            IAxisTick tick = axis.getTick();
            tick.setFormat(getContinuousAxisFormatter(getData().getYData(), fYInternalRange, fYExternalRange));
        }
    }

    @Override
    protected void refreshDisplayLabels() {
        String[] categories = checkNotNull(fCategories);

        /* Only if we have at least 1 category */
        if (categories.length == 0) {
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
        String longestString = Arrays.stream(categories).max(Comparator.comparingInt(String::length)).orElse(categories[0]);

        /* Get the length and height of the longest label in pixels */
        Point pixels = gc.stringExtent(longestString);

        /* Completely arbitrary */
        int cutLen = 5;

        String[] displayCategories = new String[categories.length];
        if (pixels.x > lengthLimit) {
            /* We have to cut down some strings */
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].length() > cutLen) {
                    displayCategories[i] = categories[i].substring(0, cutLen) + ELLIPSIS;
                } else {
                    displayCategories[i] = categories[i];
                }
            }
        } else {
            /* All strings should fit */
            displayCategories = Arrays.copyOf(categories, categories.length);
        }
        xAxis.setCategorySeries(displayCategories);

        /* Cleanup */
        gc.dispose();
    }
}
