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
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.eclipse.tracecompass.tmf.chart.ui.format.MapFormat;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.IAxisTick;
import org.swtchart.ISeries;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Abstract class for XY charts.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class XYChartViewer implements IChartViewer {

    private Button fCloseButton;
    private Chart fChart;
    private DataSeries fSeries;
    private BiMap<@Nullable String, Integer> fYMap;
    private BiMap<@Nullable String, Integer> fXMap;

    private class ResizeEvent implements ControlListener {
        @Override
        public void controlMoved(ControlEvent e) {}

        @Override
        public void controlResized(ControlEvent e) {
            // relocate the close button
            fCloseButton.setLocation(fChart.getSize().x-fCloseButton.getSize().x-10, 10);
        }
    }

    private class CloseButtonEvent implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            dispose();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    private class MouseEnterEvent implements Listener {
        @Override
        public void handleEvent(Event event) {
            Control control = (Control) event.widget;
            Point display = control.toDisplay(event.x, event.y);
            Point location = getChart().getParent().toControl(display);

            /*
             * Only set to visible if we are at the right location, in the
             * right shell.
             */
            boolean visible = getChart().getBounds().contains(location) &&
                    control.getShell().equals(getChart().getShell());
            fCloseButton.setVisible(visible);
        }
    }

    private class MouseExitEvent implements Listener {
        @Override
        public void handleEvent(Event event) {
            fCloseButton.setVisible(false);
        }
    }

    /**
     * This method generates Y data from a data descriptor.
     *
     * @param descriptor descriptor to generate data from
     * @return list of double for the Y axis
     */
    public abstract double[] generateYData(DataDescriptor descriptor);

    /**
     * This method generates X data from a data descriptor.
     *
     * @param descriptor descriptor to generate data from
     * @return list of double for the X axis
     */
    public abstract double[] generateXData(DataDescriptor descriptor);

    /**
     * This method creates a new serie depending of the chart type.
     *
     * @param title name of the chart
     * @return serie for a chart
     */
    public abstract ISeries createSerie(String title);

    /**
     * This methods changes the color of the series.
     */
    public abstract void setSeriesColor();

    /**
     * Constructor.
     *
     * @param parent parent composite
     * @param dataSeries configured data series for the chart
     */
    public XYChartViewer(Composite parent, DataSeries dataSeries) {
        fChart = new Chart(parent, SWT.NONE);
        fSeries = dataSeries;
        fYMap = checkNotNull(HashBiMap.create());
        fXMap = checkNotNull(HashBiMap.create());

        double[] yData = {0};
        double[] xData;

        // title and axis names
        fChart.getTitle().setText(generateTitle());
        fChart.getLegend().setPosition(SWT.TOP);
        fChart.getAxisSet().getXAxis(0).getTitle().setText(generateXTitle());
        fChart.getAxisSet().getYAxis(0).getTitle().setText(generateYTitle());

        // rotate X axis ticks
        fChart.getAxisSet().getXAxis(0).getTick().setTickLabelAngle(90);

        // create a series for each Y aspects
        List<ISeries> seriesList = new ArrayList<>();
        for(DataDescriptor descriptor : dataSeries.getYData()) {
            yData = generateYData(descriptor);

            // add the serie into the list
            ISeries series = createSerie(descriptor.getAspect().getLabel());

            series.setYSeries(yData);
            seriesList.add(series);
        }

        // create a series for each X aspects
        xData = generateXData(dataSeries.getXData());

        // link each Y series with the unique X serie
        for(ISeries series : seriesList) {
            series.setXSeries(xData);
        }

        // format X ticks
        if(!fXMap.isEmpty()) {
            IAxisTick xTick = fChart.getAxisSet().getXAxis(0).getTick();
            xTick.setFormat(new MapFormat(checkNotNull(fXMap)));
            updateTickMark(checkNotNull(fXMap), xTick, fChart.getPlotArea().getSize().x);
        }

        // format Y ticks
        if(!fYMap.isEmpty()) {
            IAxisTick yTick = fChart.getAxisSet().getYAxis(0).getTick();
            yTick.setFormat(new MapFormat(checkNotNull(fYMap)));
            updateTickMark(checkNotNull(fYMap), yTick, fChart.getPlotArea().getSize().y);
        }

        // configure the chart
        fChart.getAxisSet().adjustRange();
        fChart.getAxisSet().getXAxis(0).enableLogScale(fSeries.isXLogscale());
        fChart.getAxisSet().getYAxis(0).enableLogScale(fSeries.isYLogscale());
        fChart.addControlListener(new ResizeEvent());

        // configure the color of each serie
        setSeriesColor();

        // create the close button
        Image close = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
        fCloseButton = new Button(fChart, SWT.PUSH);
        fCloseButton.setSize(30, 30);
        fCloseButton.setLocation(fChart.getSize().x-fCloseButton.getSize().x-10, 10);
        fCloseButton.setImage(close);
        fCloseButton.addSelectionListener(new CloseButtonEvent());

        // add listeners for the visibility of the close button
        Listener mouseEnter = new MouseEnterEvent();
        Listener mouseExit = new MouseExitEvent();
        fChart.getDisplay().addFilter(SWT.MouseEnter, mouseEnter);
        fChart.getDisplay().addFilter(SWT.MouseExit, mouseExit);
        fChart.addDisposeListener(event -> {
            fChart.getDisplay().removeFilter(SWT.MouseEnter, mouseEnter);
            fChart.getDisplay().removeFilter(SWT.MouseExit, mouseExit);
        });

        // set color
        Color black = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
        fChart.getTitle().setForeground(black);
        fChart.getAxisSet().getXAxis(0).getTitle().setForeground(black);
        fChart.getAxisSet().getXAxis(0).getTick().setForeground(black);
        fChart.getAxisSet().getYAxis(0).getTitle().setForeground(black);
        fChart.getAxisSet().getYAxis(0).getTick().setForeground(black);
    }

    @Override
    public void dispose() {
        Composite parent = fChart.getParent();
        fChart.dispose();
        parent.layout();
    }

    /**
     * @return receiver's chart
     */
    public Chart getChart() {
        return fChart;
    }

    /**
     * @return map of categories for the Y axis
     */
    public BiMap<@Nullable String, Integer> getYMap() {
        return fYMap;
    }

    /**
     * @return map of categories for the X axis
     */
    public BiMap<@Nullable String, Integer> getXMap() {
        return fXMap;
    }

    /**
     * @return plotted data series
     */
    public DataSeries getSeries() {
        return fSeries;
    }

    /**
     * This method creates unique categorie from a stream.
     *
     * @param stream stream of data
     * @param map map of categories to fill
     */
    protected static void generateLabelMap(Stream<String> stream, BiMap<@Nullable String, Integer> map) {
        stream.distinct().forEach(str -> map.put(str, map.size()));
    }

    private String generateTitle() {
        return generateXTitle() + " vs. " + generateYTitle();
    }

    private String generateXTitle() {
        return fSeries.getXData().getAspect().getLabel();
    }

    private String generateYTitle() {
        DataDescriptor descriptor = fSeries.getYData().iterator().next();

        if(fSeries.getYData().size() == 1) {
            return descriptor.getAspect().getLabel();
        }

        return "TODO";
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
}
