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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.tracecompass.tmf.chart.core.source.IStringSource;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartRange;
import org.eclipse.tracecompass.tmf.chart.ui.format.LabelFormat;
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
public class ScatterChart extends XYChartViewer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Map linking X string categories to integer
     */
    private BiMap<@Nullable String, Integer> fXMap;
    /**
     * Map linking Y string categories to integer
     */
    private BiMap<@Nullable String, Integer> fYMap;
    /**
     * Map used for showing X categories on the axis
     */
    private @Nullable BiMap<@Nullable String, Integer> fVisibleXMap;
    /**
     * Map used for showing Y categories on the axis
     */
    private @Nullable BiMap<@Nullable String, Integer> fVisibleYMap;
    /**
     * Data generated for the X axis from the streams
     */
    private Map<ISeries, Double[]> fXData;
    /**
     * Data generated for the Y axis from the streams
     */
    private Map<ISeries, Double[]> fYData;

    // ------------------------------------------------------------------------
    // Operations
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
    public ScatterChart(Composite parent, ChartData data, ChartModel model) {
        super(parent, data, model);

        fXMap = checkNotNull(HashBiMap.create());
        fYMap = checkNotNull(HashBiMap.create());

        fXData = new HashMap<>();
        fYData = new HashMap<>();
    }

    @Override
    public void computeRanges() {
        /* Compute X range */
        if (getData().getX(0).getAspect() instanceof IDataChartNumericalAspect) {
            fXExternalRange = getRange(getData().getXData(), false);
        } else {
            fXExternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));
        }

        /* Compute Y range */
        if (getData().getY(0).getAspect() instanceof IDataChartNumericalAspect) {
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
        while (iteratorX.hasNext()) {
            DataDescriptor descriptor = checkNotNull(iteratorY.next());

            String title = descriptor.getAspect().getLabel();
            ILineSeries series = (ILineSeries) set.createSeries(SeriesType.LINE, title);
            series.setLineStyle(LineStyle.NONE);

            getXSeries().put(checkNotNull(iteratorX.next()), series);
            getYSeries().put(descriptor, series);
        }
    }

    @Override
    public void setSeriesColor() {
        Iterator<Color> colors = Iterators.cycle(COLORS);

        for (ISeries series : getYSeries().values()) {
            ((ILineSeries) series).setSymbolColor((colors.next()));
        }
    }

    @Override
    public void generateXData(List<DataDescriptor> descriptors) {
        for (DataDescriptor descriptor : descriptors) {
            @NonNull
            Double[] data;

            if (descriptor.getAspect() instanceof IDataChartNumericalAspect) {
                INumericalSource<@Nullable Number> source = checkNotNull(INumericalSource.class.cast(descriptor.getSource()));

                /* Generate data if the aspect is continuous */
                data = source.getStream()
                        .map(num -> {
                            if (num == null) {
                                return null;
                            }
                            return getInternalDoubleValue(num, fXInternalRange, fXExternalRange);
                        }).toArray(size -> new Double[size]);
            } else {
                IStringSource source = checkNotNull(IStringSource.class.cast(descriptor.getSource()));

                /* Generate unique position for each string */
                generateLabelMap(source.getStream(), fXMap);

                data = source.getStream()
                        .map(str -> new Double(checkNotNull(fXMap.get(str))))
                        .toArray(size -> new Double[size]);
            }

            fXData.put(checkNotNull(getXSeries().get(descriptor)), checkNotNull(data));
        }
    }

    @Override
    public void generateYData(List<DataDescriptor> descriptors) {
        for (DataDescriptor descriptor : descriptors) {
            @NonNull
            Double[] data;

            if (descriptor.getAspect() instanceof IDataChartNumericalAspect) {
                INumericalSource<@Nullable Number> source = checkNotNull(INumericalSource.class.cast(descriptor.getSource()));

                /* Generate data if the aspect is continuous */
                data = source.getStream()
                        .map(num -> {
                            if (num == null) {
                                return null;
                            }
                            return getInternalDoubleValue(num, fYInternalRange, fYExternalRange);
                        }).toArray(size -> new Double[size]);
            } else {
                IStringSource source = checkNotNull(IStringSource.class.cast(descriptor.getSource()));

                /* Generate unique position for each string */
                generateLabelMap(source.getStream(), fYMap);

                data = source.getStream()
                        .map(str -> new Double(checkNotNull(fYMap.get(str))))
                        .toArray(size -> new Double[size]);
            }

            fYData.put(checkNotNull(getYSeries().get(descriptor)), checkNotNull(data));
        }
    }

    @Override
    public void postProcessData() {
        for (ISeries serie : getChart().getSeriesSet().getSeries()) {
            Double[] xData = checkNotNull(fXData.get(serie));
            Double[] yData = checkNotNull(fYData.get(serie));

            /* Make sure serie size matches */
            if (xData.length != yData.length) {
                throw new IllegalStateException("Series sizes don't match!"); //$NON-NLS-1$
            }

            /* Filter bad values */
            Double[] xValid = new Double[xData.length];
            Double[] yValid = new Double[yData.length];
            int size = 0;
            for (int i = 0; i < xData.length; i++) {
                /* Reject tuple containing null values */
                if (xData[i] == null || yData[i] == null) {
                    continue;
                }

                /* X logscale cannot have negative values */
                if (getModel().isXLogscale() && xData[i] <= 0) {
                    continue;
                }

                /* Y logscale cannot have negative values */
                if (getModel().isYLogscale() && yData[i] <= 0) {
                    continue;
                }

                xValid[i] = xData[i];
                yValid[i] = yData[i];
                size++;
            }

            /* Generate final double values */
            double[] xFinal = new double[size];
            double[] yFinal = new double[size];
            for (int i = size = 0; i < xValid.length; i++) {
                if (xValid[i] == null || yValid[i] == null) {
                    continue;
                }

                xFinal[size] = xValid[i];
                yFinal[size] = yValid[i];
                size++;
            }

            /* Configure the serie */
            serie.setXSeries(xFinal);
            serie.setYSeries(yFinal);
        }
    }

    @Override
    public void configureAxes() {
        /* Format X axes */
        for (IAxis axis : getChart().getAxisSet().getXAxes()) {
            IAxisTick tick = axis.getTick();

            if (checkNotNull(isXContinuous())) {
                tick.setFormat(getContinuousAxisFormatter(
                        getData().getXData(),
                        fXInternalRange,
                        fXExternalRange));
            } else {
                fVisibleXMap = copyBiMap(fXMap);

                tick.setFormat(new LabelFormat(fVisibleXMap));
                updateTickMark(checkNotNull(fVisibleXMap), tick, getChart().getPlotArea().getSize().x);
            }

        }

        /* Format Y axes */
        for (IAxis axis : getChart().getAxisSet().getYAxes()) {
            IAxisTick tick = axis.getTick();

            if (checkNotNull(isYContinuous())) {
                tick.setFormat(getContinuousAxisFormatter(
                        getData().getYData(),
                        fYInternalRange,
                        fYExternalRange));
            } else {
                fVisibleYMap = copyBiMap(fYMap);

                tick.setFormat(new LabelFormat(fVisibleYMap));
                updateTickMark(checkNotNull(fVisibleYMap), tick, getChart().getPlotArea().getSize().y);
            }
        }
    }

    @Override
    protected void refreshDisplayLabels() {
        BiMap<@Nullable String, Integer> visibleXMap = checkNotNull(fVisibleXMap);

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
        String longestString = fXMap.keySet().stream()
                .max(Comparator.comparingInt(String::length))
                .orElse(fXMap.keySet().iterator().next());

        /* Get the length and height of the longest label in pixels */
        Point pixels = gc.stringExtent(longestString);

        /* Completely arbitrary */
        int cutLen = 5;

        if (pixels.x > lengthLimit) {
            /* We have to cut down some strings */
            for (Entry<@Nullable String, Integer> entry : fXMap.entrySet()) {
                String reference = entry.getKey();

                if (reference == null) {
                    continue;
                }

                if (reference.length() > cutLen) {
                    String key = reference.substring(0, cutLen) + ELLIPSIS;
                    visibleXMap.remove(reference);
                    visibleXMap.put(key, checkNotNull(entry.getValue()));
                } else {
                    visibleXMap.inverse().remove(entry.getValue());
                    visibleXMap.put(reference, checkNotNull(entry.getValue()));
                }
            }
        } else {
            /* All strings should fit */
            resetBiMap(fXMap, visibleXMap);
        }

        for (IAxis axis : getChart().getAxisSet().getXAxes()) {
            IAxisTick tick = axis.getTick();
            tick.setFormat(new LabelFormat(visibleXMap));
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
}
