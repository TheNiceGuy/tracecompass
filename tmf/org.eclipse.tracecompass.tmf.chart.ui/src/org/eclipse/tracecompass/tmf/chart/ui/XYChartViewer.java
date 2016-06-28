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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.provisional.tmf.chart.ui.IChartViewer;
import org.eclipse.tracecompass.tmf.chart.ui.format.MapFormat;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.IAxisTick;
import org.swtchart.ISeries;
import org.swtchart.ITitle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Abstract class for XY charts.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class XYChartViewer implements IChartViewer {
    /**
     * Ellipsis character
     */
    protected static final String ELLIPSIS = "…"; //$NON-NLS-1$

    private Composite fParent;
    private Button fCloseButton;
    private String fChartTitle;
    private String fXTitle;
    private String fYTitle;
    private Chart fChart;
    private ChartData fSeries;
    private ChartModel fModel;
    private BiMap<@Nullable String, Integer> fYMap;
    private BiMap<@Nullable String, Integer> fXMap;

    private class ResizeEvent implements ControlListener {
        @Override
        public void controlMoved(ControlEvent e) {}

        @Override
        public void controlResized(ControlEvent e) {
            /* refresh titles */
            refreshDisplayTitles();

            /* relocate the close button */
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
     * @param model chart model to use
     */
    public XYChartViewer(Composite parent, ChartData dataSeries, ChartModel model) {
        fParent = parent;
        fChartTitle = null;
        fChart = new Chart(parent, SWT.NONE);
        fSeries = dataSeries;
        fModel = model;
        fYMap = checkNotNull(HashBiMap.create());
        fXMap = checkNotNull(HashBiMap.create());

        double[] yData = {0};
        double[] xData;

        /* set titles */
        generateXTitle();
        generateYTitle();

        /* create a series for each Y aspects */
        List<ISeries> seriesList = new ArrayList<>();
        for(DataDescriptor descriptor : dataSeries.getYData()) {
            yData = generateYData(descriptor);

            /* add the serie into the list */
            ISeries series = createSerie(descriptor.getAspect().getLabel());

            series.setYSeries(yData);
            seriesList.add(series);
        }

        /* create a series for each X aspects */
        xData = generateXData(dataSeries.getXData().get(0));

        /* link each Y series with the unique X serie */
        for(ISeries series : seriesList) {
            series.setXSeries(xData);
        }

        /* format X ticks */
        if(!fXMap.isEmpty()) {
            IAxisTick xTick = fChart.getAxisSet().getXAxis(0).getTick();
            xTick.setFormat(new MapFormat(checkNotNull(fXMap)));
            updateTickMark(checkNotNull(fXMap), xTick, fChart.getPlotArea().getSize().x);
        }

        /* format Y ticks */
        if(!fYMap.isEmpty()) {
            IAxisTick yTick = fChart.getAxisSet().getYAxis(0).getTick();
            yTick.setFormat(new MapFormat(checkNotNull(fYMap)));
            updateTickMark(checkNotNull(fYMap), yTick, fChart.getPlotArea().getSize().y);
        }

        /* Set all titles and labels font color to black */
        fChart.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getYAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getYAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

        /* Set X label 90 degrees */
        fChart.getAxisSet().getXAxis(0).getTick().setTickLabelAngle(90);

        /* Refresh the titles to fit the current chart size */
        refreshDisplayTitles();

        /* create the close button */
        Image close = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
        fCloseButton = new Button(fChart, SWT.PUSH);
        fCloseButton.setSize(30, 30);
        fCloseButton.setLocation(fChart.getSize().x-fCloseButton.getSize().x-10, 10);
        fCloseButton.setImage(close);
        fCloseButton.addSelectionListener(new CloseButtonEvent());

        /* add listeners for the visibility of the close button */
        Listener mouseEnter = new MouseEnterEvent();
        Listener mouseExit = new MouseExitEvent();
        fChart.getDisplay().addFilter(SWT.MouseEnter, mouseEnter);
        fChart.getDisplay().addFilter(SWT.MouseExit, mouseExit);
        fChart.addDisposeListener(event -> {
            fChart.getDisplay().removeFilter(SWT.MouseEnter, mouseEnter);
            fChart.getDisplay().removeFilter(SWT.MouseExit, mouseExit);
        });

        /* configure the chart */
        fChart.getAxisSet().adjustRange();
        fChart.getAxisSet().getXAxis(0).enableLogScale(fModel.isXLogscale());
        fChart.getAxisSet().getYAxis(0).enableLogScale(fModel.isYLogscale());
        fChart.addControlListener(new ResizeEvent());

        /* configure the color of each serie */
        setSeriesColor();
    }

    @Override
    public void dispose() {
        fChart.dispose();
        fParent.layout();
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
    public ChartData getSeries() {
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

    private String getTitle() {
        if(fChartTitle == null) {
            return fXTitle + " vs. " + fYTitle; //$NON-NLS-1$
        }

        return fChartTitle;
    }

    private void generateXTitle() {
        if(fSeries.getXData().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * X axis.
             */
            fXTitle = fSeries.getXData().get(0).getAspect().getLabel();
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectName = fSeries.getXData().stream()
                    .map(descriptor -> descriptor.getAspect().getName())
                    .distinct()
                    .count();

            long nbDiffAspectsUnits = fSeries.getXData().stream()
                    .map(descriptor -> descriptor.getAspect().getUnits())
                    .distinct()
                    .count();

            fXTitle = "Value"; //$NON-NLS-1$
            if(nbDiffAspectName == 1) {
                fXTitle = fSeries.getXData().get(0).getAspect().getName();
            }

            String units = null;
            if(nbDiffAspectsUnits == 1) {
                units = fSeries.getXData().get(0).getAspect().getUnits();
            }

            if(units == null) {
                fXTitle = fXTitle + ' ' + '(' + units + ')';
            }
        }
    }

    private void generateYTitle() {
        if(fSeries.getYData().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * Y axis (and hide the legend).
             */
            fYTitle = fSeries.getYData().get(0).getAspect().getLabel();

            /* Hide the legend */
            fChart.getLegend().setVisible(false);
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectName = fSeries.getYData().stream()
                    .map(descriptor -> descriptor.getAspect().getName())
                    .distinct()
                    .count();

            long nbDiffAspectsUnits = fSeries.getYData().stream()
                    .map(descriptor -> descriptor.getAspect().getUnits())
                    .distinct()
                    .count();

            fYTitle = "Value"; //$NON-NLS-1$
            if(nbDiffAspectName == 1) {
                fYTitle = fSeries.getXData().get(0).getAspect().getName();
            }

            String units = null;
            if(nbDiffAspectsUnits == 1) {
                units = fSeries.getXData().get(0).getAspect().getUnits();
            }

            if(units != null) {
                fYTitle = fYTitle + ' ' + '(' + units + ')';
            }

            /* Put legend at the bottom */
            fChart.getLegend().setPosition(SWT.BOTTOM);
        }
    }

    /**
     * Set the ITitle object text to a substring of canonicalTitle that when
     * rendered in the chart will fit maxPixelLength.
     */
    private void refreshDisplayTitle(ITitle title, String canonicalTitle, int maxPixelLength) {
        if (title.isVisible()) {
            String newTitle = canonicalTitle;

            /* Get the title font */
            Font font = title.getFont();

            GC gc = new GC(fParent);
            gc.setFont(font);

            /* Get the length and height of the canonical title in pixels */
            Point pixels = gc.stringExtent(canonicalTitle);

            /*
             * If the title is too long, generate a shortened version based on the
             * average character width of the current font.
             */
            if (pixels.x > maxPixelLength) {
                int charwidth = gc.getFontMetrics().getAverageCharWidth();

                int minimum = 3;

                int strLen = ((maxPixelLength / charwidth) - minimum);

                if (strLen > minimum) {
                    newTitle = canonicalTitle.substring(0, strLen) + ELLIPSIS;
                } else {
                    newTitle = ELLIPSIS;
                }
            }

            title.setText(newTitle);

            /* Cleanup */
            gc.dispose();
        }
    }

    /**
     * Refresh the Chart, XAxis and YAxis titles to fit the current
     * chart size.
     */
    private void refreshDisplayTitles() {
        Rectangle chartRect = fChart.getClientArea();
        Rectangle plotRect = fChart.getPlotArea().getClientArea();

        ITitle chartTitle = checkNotNull(fChart.getTitle());
        refreshDisplayTitle(chartTitle, getTitle(), chartRect.width);

        ITitle xTitle = checkNotNull(fChart.getAxisSet().getXAxis(0).getTitle());
        refreshDisplayTitle(xTitle, fXTitle, plotRect.width);

        ITitle yTitle = checkNotNull(fChart.getAxisSet().getYAxis(0).getTitle());
        refreshDisplayTitle(yTitle, fYTitle, plotRect.height);
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
