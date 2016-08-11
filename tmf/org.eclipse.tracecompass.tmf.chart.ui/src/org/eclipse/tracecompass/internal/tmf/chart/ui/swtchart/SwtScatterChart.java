/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.swtchart;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.text.Format;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IDataResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.tmf.chart.ui.aggregator.NumericalConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.NumericalConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.ScatterStringConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYSeriesConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.eclipse.tracecompass.internal.tmf.chart.ui.format.LabelFormat;
import org.eclipse.tracecompass.internal.tmf.chart.ui.swtchart.SwtPoint;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;

/**
 * Class for building a scatter chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public final class SwtScatterChart extends SwtXYChartViewer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int SELECTION_SNAP_RANGE_MULTIPLIER = 20;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Range map for the X axis
     */
    private ChartRangeMap fXRanges = new ChartRangeMap();
    /**
     * Range map for the Y axis
     */
    private ChartRangeMap fYRanges = new ChartRangeMap();
    /**
     * Map linking X string categories to integer
     */
    private BiMap<String, Integer> fXStringMap = checkNotNull(HashBiMap.create());
    /**
     * Map linking Y string categories to integer
     */
    private BiMap<String, Integer> fYStringMap = checkNotNull(HashBiMap.create());
    /**
     * Map used for showing X categories on the axis
     */
    private BiMap<String, Integer> fVisibleXMap = checkNotNull(HashBiMap.create());
    /**
     * Map used for showing Y categories on the axis
     */
    private BiMap<String, Integer> fVisibleYMap = checkNotNull(HashBiMap.create());
    /**
     * Coordinates in pixels of the currently hovered point
     */
    private Point fHoveringPoint = new Point(-1, -1);
    /**
     * The SWT reference of the currently hovered point
     */
    private @Nullable SwtPoint fHoveredPoint;

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
    public SwtScatterChart(Composite parent, ChartData data, ChartModel model) {
        super(parent, data, model);

        /* Add the mouse hovering listener */
        getChart().getPlotArea().addMouseMoveListener(new MouseHoveringListener());

        /* Add the mouse exit listener */
        getChart().getPlotArea().addListener(SWT.MouseExit, new MouseExitListener());

        /* Add the paint listener */
        getChart().getPlotArea().addPaintListener(new ScatterPainterListener());

        populate();
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public XYChartConsumer createChartConsumer() {
        List<XYSeriesConsumer> series = new ArrayList<>();

        getData().getChartSeries().stream().forEach(s -> {
            IDataConsumer xConsumer;
            IDataConsumer yConsumer;

            /* Create a predicate for the X descriptor */
            IDataResolver<?, ?> xResolver = s.getX().getResolver();
            Predicate<@Nullable Number> xPredicate;
            if (getModel().isXLogscale()) {
                INumericalResolver<Object, Number> resolver = INumericalResolver.class.cast(xResolver);
                xPredicate = new LogarithmicPredicate(resolver);
            } else {
                xPredicate = Objects::nonNull;
            }

            /* Create a consumer for the X descriptor */
            if (getXDescriptorsInfo().areNumerical()) {
                INumericalResolver<Object, Number> resolver = INumericalResolver.class.cast(xResolver);
                xConsumer = new NumericalConsumer(resolver, xPredicate);
            } else {
                xConsumer = new ScatterStringConsumer(xResolver, fXStringMap);
            }

            /* Create a predicate for the Y descriptor */
            IDataResolver<?, ?> yResolver = s.getY().getResolver();
            Predicate<@Nullable Number> yPredicate;
            if (getModel().isYLogscale()) {
                INumericalResolver<Object, Number> resolver = INumericalResolver.class.cast(xResolver);
                yPredicate = new LogarithmicPredicate(resolver);
            } else {
                yPredicate = Objects::nonNull;
            }

            /* Create a consumer for the Y descriptor */
            if (getYDescriptorsInfo().areNumerical()) {
                INumericalResolver<Object, Number> resolver = INumericalResolver.class.cast(xResolver);
                yConsumer = new NumericalConsumer(resolver, yPredicate);
            } else {
                yConsumer = new ScatterStringConsumer(yResolver, fYStringMap);
            }

            /* Create consumer for this series */
            series.add(new XYSeriesConsumer(s, xConsumer, yConsumer));
        });

        /* Create a numerical aggregator if the X axis is numerical */
        NumericalConsumerAggregator xAggregator;
        if (getXDescriptorsInfo().areNumerical()) {
            xAggregator = new NumericalConsumerAggregator();
        } else {
            xAggregator = null;
        }

        /* Create a numerical aggregator if the Y axis is numerical */
        NumericalConsumerAggregator yAggregator;
        if (getYDescriptorsInfo().areNumerical()) {
            yAggregator = new NumericalConsumerAggregator();
        } else {
            yAggregator = null;
        }

        /* Create the chart consumer */
        return new XYChartConsumer(series, xAggregator, yAggregator);
    }

    @Override
    public void createSeries(Map<@NonNull ChartSeries, @NonNull ISeries> mapper) {
        ISeriesSet set = getChart().getSeriesSet();
        Iterator<Color> colors = Iterators.cycle(COLORS);

        /* Create a SWT series for each chart series */
        getData().getChartSeries().stream().forEach(chartSeries -> {
            String title = chartSeries.getY().getLabel();
            ILineSeries swtSeries = (ILineSeries) set.createSeries(SeriesType.LINE, title);
            swtSeries.setLineStyle(LineStyle.NONE);
            swtSeries.setSymbolColor((colors.next()));

            /* Put the series into the map */
            mapper.put(chartSeries, swtSeries);
        });
    }

    @Override
    public void configureSeries(Map<@NonNull ISeries, Object[]> mapper) {
        XYChartConsumer chartConsumer = getChartConsumer();

        /* Obtain the X ranges if possible */
        NumericalConsumerAggregator xAggregator = (NumericalConsumerAggregator) chartConsumer.getXAggregator();
        if (xAggregator != null) {
            if (getModel().isXLogscale()) {
                fXRanges = clampInputDataRange(xAggregator.getChartRanges());
            } else {
                fXRanges = xAggregator.getChartRanges();
            }
        }

        /* Obtain the Y ranges if possible */
        NumericalConsumerAggregator yAggregator = (NumericalConsumerAggregator) chartConsumer.getYAggregator();
        if (yAggregator != null) {
            if (getModel().isYLogscale()) {
                fYRanges = clampInputDataRange(yAggregator.getChartRanges());
            } else {
                fYRanges = yAggregator.getChartRanges();
            }
        }

        /* Generate data for each SWT series */
        for (XYSeriesConsumer seriesConsumer : chartConsumer.getSeries()) {
            double[] xData;
            double[] yData;
            Object[] object = seriesConsumer.getConsumedElements().toArray();

            /* Generate data for the X axis */
            if (getXDescriptorsInfo().areNumerical()) {
                NumericalConsumer consumer = (NumericalConsumer) seriesConsumer.getXConsumer();
                int size = consumer.getData().size();

                xData = new double[size];
                for (int i = 0; i < size; i++) {
                    Number number = checkNotNull(consumer.getData().get(i));
                    xData[i] = fXRanges.getInternalValue(number).doubleValue();
                }
            } else {
                ScatterStringConsumer consumer = (ScatterStringConsumer) seriesConsumer.getXConsumer();
                int size = consumer.getList().size();

                xData = new double[size];
                for (int i = 0; i < size; i++) {
                    String str = consumer.getList().get(i);
                    xData[i] = checkNotNull(fXStringMap.get(str));
                }
            }

            /* Generate data for the Y axis */
            if (getYDescriptorsInfo().areNumerical()) {
                NumericalConsumer consumer = (NumericalConsumer) seriesConsumer.getYConsumer();

                yData = new double[consumer.getData().size()];
                for (int i = 0; i < yData.length; i++) {
                    Number number = checkNotNull(consumer.getData().get(i));
                    yData[i] = fYRanges.getInternalValue(number).doubleValue();
                }
            } else {
                ScatterStringConsumer consumer = (ScatterStringConsumer) seriesConsumer.getYConsumer();

                yData = new double[consumer.getList().size()];
                for (int i = 0; i < yData.length; i++) {
                    String str = consumer.getList().get(i);
                    yData[i] = checkNotNull(fYStringMap.get(str));
                }
            }

            /* Set the data for the SWT series */
            ISeries series = checkNotNull(getSeriesMap().get(seriesConsumer.getSeries()));
            series.setXSeries(xData);
            series.setYSeries(yData);

            /* Create a series mapper */
            mapper.put(series, checkNotNull(object));
        }
    }

    @Override
    public void configureAxes() {
        /* Format X axes */
        Stream.of(getChart().getAxisSet().getXAxes()).forEach(a -> {
            IAxisTick tick = checkNotNull(a.getTick());
            Format format;

            /* Give a continuous formatter if the descriptors are numericals */
            if (getXDescriptorsInfo().areNumerical()) {
                format = getContinuousAxisFormatter(fXRanges, getXDescriptorsInfo());
            } else {
                fVisibleXMap = copyBiMap(fXStringMap);
                format = new LabelFormat(fVisibleXMap);
                updateTickMark(fVisibleXMap, tick, getChart().getPlotArea().getSize().x);
            }

            tick.setFormat(format);
        });

        /* Format Y axes */
        Stream.of(getChart().getAxisSet().getYAxes()).forEach(a -> {
            IAxisTick tick = checkNotNull(a.getTick());
            Format format;

            /* Give a continuous formatter if the descriptors are numericals. */
            if (getYDescriptorsInfo().areNumerical()) {
                format = getContinuousAxisFormatter(fYRanges, getYDescriptorsInfo());
            } else {
                fVisibleYMap = copyBiMap(fYStringMap);
                format = new LabelFormat(fVisibleYMap);
                updateTickMark(fVisibleYMap, tick, getChart().getPlotArea().getSize().y);
            }

            tick.setFormat(format);
        });
    }

    @Override
    protected void refreshDisplayLabels() {

        /**
         * TODO: support for the Y axis too
         */

        /* Only refresh if labels are visible */
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        if (!xAxis.getTick().isVisible()) {
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
        String longestString = fXStringMap.keySet().stream()
                .max(Comparator.comparingInt(String::length))
                .orElse(fXStringMap.keySet().iterator().next());

        /* Get the length and height of the longest label in pixels */
        Point pixels = gc.stringExtent(longestString);

        /* Completely arbitrary */
        int cutLen = 5;

        if (pixels.x > lengthLimit) {
            /* We have to cut down some strings */
            for (Entry<@Nullable String, Integer> entry : fXStringMap.entrySet()) {
                String reference = entry.getKey();

                if (reference == null) {
                    continue;
                }

                if (reference.length() > cutLen) {
                    String key = reference.substring(0, cutLen) + ELLIPSIS;
                    fVisibleXMap.remove(reference);
                    fVisibleXMap.put(key, entry.getValue());
                } else {
                    fVisibleXMap.inverse().remove(entry.getValue());
                    fVisibleXMap.put(reference, entry.getValue());
                }
            }
        } else {
            /* All strings should fit */
            resetBiMap(fXStringMap, fVisibleXMap);
        }

        for (IAxis axis : getChart().getAxisSet().getXAxes()) {
            IAxisTick tick = axis.getTick();
            tick.setFormat(new LabelFormat(fVisibleXMap));
        }

        /* Cleanup */
        gc.dispose();
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    /**
     * Util method used to create a copy of a bimap.
     *
     * @param reference
     *            Map to copy
     * @return A copy of the map
     */
    public static <K, V> BiMap<K, V> copyBiMap(BiMap<K, V> reference) {
        BiMap<K, V> copy = checkNotNull(HashBiMap.create());

        for (Entry<K, V> entry : reference.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }

        return copy;
    }

    /**
     * Util method used to reset a bimap from a reference. It similaire to
     * {@link #copyBiMap(BiMap)} except that it does not create a new map.
     *
     * @param reference
     *            Reference map
     * @param map
     *            Map to modify
     */
    public static <K, V> void resetBiMap(BiMap<K, V> reference, BiMap<K, V> map) {
        for (Entry<K, V> entry : reference.entrySet()) {
            map.inverse().remove(entry.getValue());
            map.put(entry.getKey(), entry.getValue());
        }
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    private final class MouseHoveringListener implements MouseMoveListener {
        @Override
        public void mouseMove(@Nullable MouseEvent event) {
            if (event == null) {
                return;
            }

            double closestDistance = -1.0;

            boolean found = false;
            for (ISeries swtSeries : getChart().getSeriesSet().getSeries()) {
                ILineSeries series = (ILineSeries) swtSeries;

                for (int i = 0; i < series.getXSeries().length; i++) {
                    Point dataPoint = series.getPixelCoordinates(i);

                    /*
                     * Find the distance between the data point and the mouse
                     * location and compare it to the symbol size * the range
                     * multiplier, so when a user hovers the mouse near the dot
                     * the cursor cross snaps to it.
                     */
                    int snapRangeRadius = series.getSymbolSize() * SELECTION_SNAP_RANGE_MULTIPLIER;

                    /*
                     * FIXME: if and only if performance of this code is an
                     * issue for large sets, this can be accelerated by getting
                     * the distance squared, and if it is smaller than
                     * snapRangeRadius squared, then check hypot.
                     */
                    double distance = Math.hypot(dataPoint.x - event.x, dataPoint.y - event.y);

                    if (distance < snapRangeRadius) {
                        if (closestDistance == -1 || distance < closestDistance) {
                            fHoveringPoint.x = dataPoint.x;
                            fHoveringPoint.y = dataPoint.y;

                            SwtPoint point = fHoveredPoint;
                            if (point == null) {
                                fHoveredPoint = new SwtPoint(series, i);
                            } else {
                                point.setSeries(series);
                                point.setIndex(i);
                            }

                            closestDistance = distance;
                            found = true;
                        }
                    }
                }
            }

            /* Check if a point was found */
            if (!found) {
                fHoveredPoint = null;
            }

            refresh();
        }
    }

    private final class MouseExitListener implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event != null) {
                fHoveringPoint.x = -1;
                fHoveringPoint.y = -1;

                fHoveredPoint = null;

                refresh();
            }
        }
    }

    private final class ScatterPainterListener implements PaintListener {
        @Override
        public void paintControl(@Nullable PaintEvent event) {
            if (event == null) {
                return;
            }

            GC gc = event.gc;
            if (gc == null) {
                return;
            }

            /* Draw the hovering cross */
            drawHoveringCross(gc);
        }

        private void drawHoveringCross(GC gc) {
            if (fHoveredPoint == null) {
                return;
            }

            gc.setLineWidth(1);
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            /* Vertical line */
            gc.drawLine(fHoveringPoint.x, 0, fHoveringPoint.x, getChart().getPlotArea().getSize().y);

            /* Horizontal line */
            gc.drawLine(0, fHoveringPoint.y, getChart().getPlotArea().getSize().x, fHoveringPoint.y);
        }
    }

}
