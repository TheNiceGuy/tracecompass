/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.swt;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IDataResolver;
import org.eclipse.tracecompass.internal.tmf.chart.ui.aggregator.NumericalConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.BarStringConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.NumericalConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYSeriesConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;

import com.google.common.collect.Iterators;

/**
 * Class for a bar chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SwtBarChart extends SwtXYChartViewer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Range map for the Y axis since it must be numerical
     */
    private ChartRangeMap fYRanges = new ChartRangeMap();
    /**
     * Map reprensenting categories on the X axis
     */
    private String @Nullable [] fCategories;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent
     *            parent composite
     * @param data
     *            configured data series for the chart
     * @param model
     *            chart model to use
     */
    public SwtBarChart(Composite parent, ChartData data, ChartModel model) {
        super(parent, data, model);
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void validateChartData() {
        super.validateChartData();

        /* Make sure the X axis is not continuous */
        if (getXDescriptorsInfo().areNumericals()) {
            throw new IllegalArgumentException("Bar chart X axis cannot be numerical."); //$NON-NLS-1$
        }

        /**
         * TODO: allow Y discontinuous by mapping each string to a number
         */

        /* Make sure the Y axis is continuous */
        if (!getYDescriptorsInfo().areNumericals()) {
            throw new IllegalArgumentException("Bar chart Y axis must be numerical."); //$NON-NLS-1$
        }

        /**
         * TODO: allow multiple X axes
         */

        /* Make sure there is only one X axis */
        if (countDistinctDescriptors(getXDescriptors()) > 1) {
            throw new IllegalArgumentException("Bar chart can only have one X axis."); //$NON-NLS-1$
        }
    }

    @Override
    public XYChartConsumer createChartConsumer() {
        List<XYSeriesConsumer> series = new ArrayList<>();

        getData().getChartSeries().stream().forEach(s -> {
            IDataConsumer xConsumer;
            IDataConsumer yConsumer;

            /* Create a string consumer for the X descriptor */
            xConsumer = new BarStringConsumer(checkNotNull(s.getX().getResolver()));

            /* Create a predicate for the Y descriptor */
            IDataResolver<?, ?> yResolver = checkNotNull(s.getY().getResolver());
            Predicate<@Nullable Number> yPredicate;
            if (getModel().isYLogscale()) {
                yPredicate = new LogarithmicPredicate(yResolver);
            } else {
                yPredicate = Objects::nonNull;
            }

            /* Create a numerical consumer for the Y descriptor */
            yConsumer = new NumericalConsumer(yResolver, yPredicate);

            /* Create consumer for this series */
            series.add(new XYSeriesConsumer(s, xConsumer, yConsumer));
        });

        /* Create a numerical aggregator if the Y axis is numerical */
        NumericalConsumerAggregator yAggregator = new NumericalConsumerAggregator();

        /* Create the chart consumer */
        return new XYChartConsumer(series, null, yAggregator);
    }

    @Override
    public void createSeries(Map<@NonNull ChartSeries, @NonNull ISeries> mapper) {
        ISeriesSet set = getChart().getSeriesSet();
        Iterator<Color> colors = Iterators.cycle(COLORS);

        /* Create a SWT series for each chart series */
        getData().getChartSeries().stream().forEach(chartSeries -> {
            String title = chartSeries.getY().getLabel();
            IBarSeries swtSeries = (IBarSeries) set.createSeries(SeriesType.BAR, title);
            swtSeries.setBarPadding(20);
            swtSeries.setBarColor(colors.next());

            /* Put the series into the map */
            mapper.put(chartSeries, swtSeries);
        });
    }

    @Override
    public void configureSeries(Map<@NonNull ISeries, Object[]> mapper) {
        XYChartConsumer chartConsumer = getChartConsumer();
        NumericalConsumerAggregator aggregator = (NumericalConsumerAggregator) checkNotNull(chartConsumer.getYAggregator());

        /* Clamp the Y ranges */
        fYRanges = clampExternalRange(checkNotNull(aggregator.getChartRanges()));

        /* Generate data for each SWT series */
        for (XYSeriesConsumer seriesConsumer : chartConsumer.getSeries()) {
            BarStringConsumer xconsumer = (BarStringConsumer) seriesConsumer.getXConsumer();
            NumericalConsumer yConsumer = (NumericalConsumer) seriesConsumer.getYConsumer();
            Object[] object = seriesConsumer.getObject().toArray();

            /* Generate categories for the X axis */
            fCategories = xconsumer.getList().stream().toArray(size -> new String[size]);

            /* Generate numerical data for the Y axis */
            double[] yData = new double[yConsumer.getData().size()];
            for (int i = 0; i < yData.length; i++) {
                Number number = checkNotNull(yConsumer.getData().get(i));
                yData[i] = fYRanges.getInternalValue(number).doubleValue();
            }

            /* Set the data for the SWT series */
            ISeries series = checkNotNull(getSeriesMap().get(seriesConsumer.getSeries()));
            series.setYSeries(yData);

            /* Create a series mapper */
            mapper.put(series, checkNotNull(object));
        }
    }

    @Override
    public void configureAxes() {
        /* Format X axes */
        Stream.of(getChart().getAxisSet().getXAxes()).forEach(a -> {
            a.enableCategory(true);
            a.setCategorySeries(fCategories);
        });

        /* Format Y axes */
        Stream.of(getChart().getAxisSet().getYAxes()).forEach(a -> {
            IAxisTick tick = a.getTick();
            tick.setFormat(getContinuousAxisFormatter(fYRanges, getYDescriptorsInfo()));
        });
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

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    private static long countDistinctDescriptors(Stream<IDataChartDescriptor<?, ?>> descriptors) {
        return descriptors.distinct().count();
    }

}
