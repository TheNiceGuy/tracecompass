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
import java.util.Iterator;
import java.util.List;
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

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This constructor creates the chart.
     *
     * @param parent parent composite
     * @param dataSeries configured data series for the chart
     * @param model chart model to use
     */
    public ScatterChart(Composite parent, ChartData dataSeries, ChartModel model) {
        super(parent, dataSeries, model);

        fXMap = checkNotNull(HashBiMap.create());
        fYMap = checkNotNull(HashBiMap.create());
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
        double[] data;

        for(DataDescriptor descriptor : descriptors) {
            if(descriptor.getAspect().isContinuous()) {
                /* Generate data if the aspect is continuous */
                data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                        .mapToDouble(num -> num.doubleValue())
                        .toArray();
            } else {
                /* Generate unique position for each string */
                generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), fXMap);

                data = ((IStringSource) descriptor.getSource()).getStreamString()
                        .map(str -> checkNotNull(fXMap.get(str)))
                        .mapToDouble(num -> (double) num)
                        .toArray();
            }

            checkNotNull(getXSeries().get(descriptor)).setXSeries(data);
        }
    }

    @Override
    public void generateYData(List<DataDescriptor> descriptors) {
        double[] data;

        for(DataDescriptor descriptor : descriptors) {
            if(descriptor.getAspect().isContinuous()) {
                /* Generate data if the aspect is continuous */
                data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                        .mapToDouble(num -> num.doubleValue())
                        .toArray();
            } else {
                /* Generate unique position for each string */
                generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), fYMap);

                data = ((IStringSource) descriptor.getSource()).getStreamString()
                        .map(str -> checkNotNull(fYMap.get(str)))
                        .mapToDouble(num -> (double) num)
                        .toArray();
            }

            checkNotNull(getYSeries().get(descriptor)).setYSeries(data);
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
        if (fVisibleXMap.size() == 0) {
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
     *              the map to copy
     * @return a copy of the map
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
     *              the reference map
     * @param map
     *              the map to modify
     */
    public static <K,V> void resetBiMap(BiMap<K,V> reference, BiMap<K,V> map) {
        for(Entry<K,V> entry : reference.entrySet()) {
            map.remove(entry.getValue());
            map.put(entry.getKey(), entry.getValue());
        }
    }
}
