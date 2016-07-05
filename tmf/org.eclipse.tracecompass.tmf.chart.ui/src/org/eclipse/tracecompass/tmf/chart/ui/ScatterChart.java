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
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IStringSource;
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
    private BiMap<@Nullable String, @NonNull Integer> fXMap;
    /**
     * Map linking Y string categories to integer
     */
    private BiMap<@Nullable String, @NonNull Integer> fYMap;
    /**
     * Map used for showing X categories on the axis
     */
    private BiMap<@Nullable String, @NonNull Integer> fVisibleXMap;
    /**
     * Map used for showing Y categories on the axis
     */
    private BiMap<@Nullable String, @NonNull Integer> fVisibleYMap;
    /**
     * Data generated for the X axis from the streams
     */
    private Map<@NonNull ISeries, @NonNull Double[]> fXData;
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
     * @param title
     *              title of the chart
     */
    public ScatterChart(Composite parent, ChartData data, ChartModel model, String title) {
        super(parent, data, model, title);

        fXMap = checkNotNull(HashBiMap.create());
        fYMap = checkNotNull(HashBiMap.create());

        fXData = new HashMap<>();
        fYData = new HashMap<>();
    }

    /**
     * Surcharged constructor.
     *
     * @param parent
     *              parent composite
     * @param data
     *              configured data series for the chart
     * @param model
     *              chart model to use
     */
    public ScatterChart(Composite parent, ChartData data, ChartModel model) {
        this(parent, data, model, null);
    }

    @Override
    public void computeRanges() {
        /* Compute X range */
        if(getData().getXData().get(0).getAspect().isContinuous()) {
            fXExternalRange = getRange(getData().getXData(), false);
        } else {
            fXExternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));
        }

        /* Compute Y range */
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

        /* Create a series for each descriptors */
        while(iteratorX.hasNext()) {
            DataDescriptor descriptor = iteratorY.next();

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

        for(ISeries series : getYSeries().values()) {
            ((ILineSeries) series).setSymbolColor((colors.next()));
        }
    }

    @Override
    public void generateXData(List<DataDescriptor> descriptors) {
        for(DataDescriptor descriptor : descriptors) {
            @NonNull Double[] data;

            if(descriptor.getAspect().isContinuous()) {
                /* Generate data if the aspect is continuous */
                data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                        .map(num -> {
                            if(num == null) {
                                return null;
                            }
                            return getInternalDoubleValue(num, fXInternalRange, fXExternalRange);
                        }).toArray(size -> new Double[size]);
            } else {
                /* Generate unique position for each string */
                generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), fXMap);

                data = ((IStringSource) descriptor.getSource()).getStreamString()
                        .map(str -> new Double(checkNotNull(fXMap.get(str))))
                        .toArray(size -> new Double[size]);
            }

            fXData.put(checkNotNull(getXSeries().get(descriptor)), data);
        }
    }

    @Override
    public void generateYData(List<DataDescriptor> descriptors) {
        for(DataDescriptor descriptor : descriptors) {
            @NonNull Double[] data;

            if(descriptor.getAspect().isContinuous()) {
                /* Generate data if the aspect is continuous */
                data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                        .map(num -> {
                            if(num == null) {
                                return null;
                            }
                            return getInternalDoubleValue(num, fYInternalRange, fYExternalRange);
                        }).toArray(size -> new Double[size]);
            } else {
                /* Generate unique position for each string */
                generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), fYMap);

                data = ((IStringSource) descriptor.getSource()).getStreamString()
                        .map(str -> new Double(checkNotNull(fYMap.get(str))))
                        .toArray(size -> new Double[size]);
            }

            fYData.put(checkNotNull(getYSeries().get(descriptor)), data);
        }
    }

    @Override
    public void postProcessData() {
        for(ISeries serie : getChart().getSeriesSet().getSeries()) {
            Double[] xData = fXData.get(serie);
            Double[] yData = fYData.get(serie);

            /* Make sure an array way returned */
            if(xData == null || yData == null) {
                continue;
            }

            /* Make sure serie size matches */
            if(xData.length != yData.length) {
                throw new IllegalStateException("Series sizes don't match!"); //$NON-NLS-1$
            }

            /* Filter bad values */
            Double[] xValid = new Double[xData.length];
            Double[] yValid = new Double[yData.length];
            int size = 0;
            for(int i = 0; i < xData.length; i++) {
                /* Reject tuple containing null values */
                if(xData[i] == null || yData[i] == null) {
                    continue;
                }

                /* X logscale cannot have negative values */
                if(getModel().isXLogscale() && xData[i] <= 0) {
                    continue;
                }

                /* Y logscale cannot have negative values */
                if(getModel().isYLogscale() && yData[i] <= 0) {
                    continue;
                }

                xValid[i] = xData[i];
                yValid[i] = yData[i];
                size++;
            }

            /* Generate final double values */
            double[] xFinal = new double[size];
            double[] yFinal = new double[size];
            for(int i = size = 0; i < xValid.length; i++) {
                if(xValid[i] == null || yValid[i] == null) {
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
        for(IAxis axis : getChart().getAxisSet().getXAxes()) {
            IAxisTick tick = axis.getTick();

            if(checkNotNull(isXContinuous())) {
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
        for(IAxis axis : getChart().getAxisSet().getYAxes()) {
            IAxisTick tick = axis.getTick();

            if(checkNotNull(isYContinuous())) {
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
        /* Only if we have at least 1 X category */
        if (fVisibleXMap == null) {
            return;
        }

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
            for(Entry<@Nullable String, Integer> entry : fXMap.entrySet()) {
                String reference = entry.getKey();

                if(reference == null) {
                    continue;
                }

                if(reference.length() > cutLen) {
                    String key = reference.substring(0, cutLen) + ELLIPSIS;
                    fVisibleXMap.remove(reference);
                    fVisibleXMap.put(key, checkNotNull(entry.getValue()));
                } else {
                    fVisibleXMap.inverse().remove(entry.getValue());
                    fVisibleXMap.put(reference, checkNotNull(entry.getValue()));
                }
            }
        } else {
            /* All strings should fit */
            resetBiMap(fXMap, fVisibleXMap);
        }

        for(IAxis axis : getChart().getAxisSet().getXAxes()) {
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
     *              Map to copy
     * @return A copy of the map
     */
    public static <K,V> BiMap<K,V> copyBiMap(BiMap<K,V> reference) {
        BiMap<K,V> copy = checkNotNull(HashBiMap.create());

        for(Entry<K,V> entry : reference.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }

        return copy;
    }

    /**
     * Util method used to reset a bimap from a reference. It similaire to
     * {@link #copyBiMap(BiMap)} except that it does not create a new map.
     *
     * @param reference
     *              Reference map
     * @param map
     *              Map to modify
     */
    public static <K,V> void resetBiMap(BiMap<K,V> reference, BiMap<K,V> map) {
        for(Entry<K,V> entry : reference.entrySet()) {
            map.inverse().remove(entry.getValue());
            map.put(entry.getKey(), entry.getValue());
        }
    }
}
