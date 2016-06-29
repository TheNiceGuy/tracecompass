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
import java.text.Format;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.NumberComparator;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.provisional.tmf.chart.ui.IChartViewer;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartDecimalUnitFormat;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartRange;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.chart.ui.format.MapFormat;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
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
    /**
     *  Zero long value
     */
    protected static final long ZERO_LONG = 0L;
    /**
     * Zero double value
     */
    protected static final double ZERO_DOUBLE = 0.0;
    /**
     *  Maximum amount of digits that can be represented into a double
     */
    private static final int BIG_DECIMAL_DIVISION_SCALE = 22;
    /**
     * Time stamp formatter for intervals in the days range.
     */
    protected static final ChartTimeStampFormat DAYS_FORMATTER = new ChartTimeStampFormat("dd HH:mm"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the hours range.
     */
    protected static final ChartTimeStampFormat HOURS_FORMATTER = new ChartTimeStampFormat("HH:mm"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the minutes range.
     */
    protected static final ChartTimeStampFormat MINUTES_FORMATTER = new ChartTimeStampFormat("mm:ss"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the seconds range.
     */
    protected static final ChartTimeStampFormat SECONDS_FORMATTER = new ChartTimeStampFormat("ss"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the milliseconds range.
     */
    protected static final ChartTimeStampFormat MILLISECONDS_FORMATTER = new ChartTimeStampFormat("ss.SSS"); //$NON-NLS-1$
    /**
     * Decimal formatter to display nanoseconds as seconds.
     */
    protected static final DecimalUnitFormat NANO_TO_SECS_FORMATTER = new ChartDecimalUnitFormat(0.000000001);
    /**
     * Default decimal formatter.
     */
    protected static final DecimalUnitFormat DECIMAL_FORMATTER = new ChartDecimalUnitFormat();

    private Composite fParent;
    private Button fCloseButton;
    private String fChartTitle;
    private String fXTitle;
    private String fYTitle;
    private Chart fChart;
    private ChartData fData;
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
            fCloseButton.setLocation(fChart.getSize().x-fCloseButton.getSize().x-5, 5);
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
     * This method makes sure that the {@link ChartData}} is
     * properly built for making XY charts.
     */
    protected void assertData() {
        if(getData().getXData().size() != getData().getYData().size()) {
            throw new IllegalArgumentException("Number of Y series does not match with X series"); //$NON-NLS-1$
        }

        /* Make sure X axis has the same continuity for each aspect */
        if(getData().countDiffXData() > 1) {
            long count = getData().getYData().stream()
                    .map(descriptor -> descriptor.getAspect().isContinuous())
                    .distinct()
                    .count();
            if(count != 1) {
                throw new IllegalArgumentException("Each aspect must have the same continuity."); //$NON-NLS-1$
            }

            /* Make sure X logscale is disabled if data is discontinuous */
            boolean continuous = getData().getXData().get(0).getAspect().isContinuous();
            boolean logscale = getModel().isXLogscale();
            if(!continuous && logscale) {
                throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
            }
        }

        /* Make sure Y axis has the same continuity for each aspect */
        if(getData().countDiffYData() > 1) {
            long count = getData().getYData().stream()
                    .map(descriptor -> descriptor.getAspect().isContinuous())
                    .distinct()
                    .count();
            if(count != 1) {
                throw new IllegalArgumentException("Each aspect must have the same continuity."); //$NON-NLS-1$
            }

            /* Make sure Y logscale is disabled if data is discontinuous */
            boolean continuous = getData().getYData().get(0).getAspect().isContinuous();
            boolean logscale = getModel().isYLogscale();
            if(!continuous && logscale) {
                throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
            }
        }
    }

    protected abstract void createSeries();

    /**
     * This methods changes the color of the series.
     */
    protected abstract void setSeriesColor();

    /**
     * This method generates Y data from a data descriptor.
     *
     * @param descriptors descriptor to generate data from
     * @return list of data for the Y axis
     */
    protected abstract List<double[]> generateYData(List<DataDescriptor> descriptors);

    /**
     * This method generates X data from a data descriptor.
     *
     * @param descriptors descriptor to generate data from
     * @return list of data for the X axis
     */
    protected abstract List<double[]> generateXData(List<DataDescriptor> descriptors);

    /**
     * Constructor.
     *
     * @param parent parent composite
     * @param data configured data series for the chart
     * @param model chart model to use
     */
    public XYChartViewer(Composite parent, ChartData data, ChartModel model) {
        fParent = parent;
        fData = data;
        fModel = model;

        assertData();

        fChartTitle = null;
        fChart = new Chart(parent, SWT.NONE);
        fYMap = checkNotNull(HashBiMap.create());
        fXMap = checkNotNull(HashBiMap.create());

        /* Generate titles */
        generateXTitle();
        generateYTitle();

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
        fCloseButton.setSize(25, 25);
        fCloseButton.setLocation(fChart.getSize().x-fCloseButton.getSize().x-5, 5);
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

//        /* configure the chart */
//        fChart.getAxisSet().adjustRange();
//        fChart.addControlListener(new ResizeEvent());
    }

    protected void populate() {
        /* Create series */
        createSeries();
        setSeriesColor();

        /* Generate data series */
        List<double[]> xData = generateXData(fData.getXData());
        List<double[]> yData = generateYData(fData.getYData());

        for(int i = 0; i < fChart.getSeriesSet().getSeries().length; i++) {
            fChart.getSeriesSet().getSeries()[i].setXSeries(xData.get(i));
            fChart.getSeriesSet().getSeries()[i].setYSeries(yData.get(i));
        }

        Stream.of(getChart().getAxisSet().getYAxes()).forEach(axis -> axis.enableLogScale(getModel().isXLogscale()));
        Stream.of(getChart().getAxisSet().getYAxes()).forEach(axis -> axis.enableLogScale(getModel().isYLogscale()));

        /* Format X ticks */
        if(!fXMap.isEmpty()) {
            for(IAxis axis : fChart.getAxisSet().getAxes()) {
                IAxisTick xTick = axis.getTick();
                xTick.setFormat(new MapFormat(checkNotNull(fXMap)));
                updateTickMark(checkNotNull(fXMap), xTick, fChart.getPlotArea().getSize().x);
            }
        }

        /* Format Y ticks */
        if(!fYMap.isEmpty()) {
            for(IAxis axis : fChart.getAxisSet().getAxes()) {
                IAxisTick yTick = axis.getTick();
                yTick.setFormat(new MapFormat(checkNotNull(fYMap)));
                updateTickMark(checkNotNull(fYMap), yTick, fChart.getPlotArea().getSize().y);
            }
        }

        /* Adjust the chart range */
      //  getChart().getAxisSet().adjustRange();
    }

    private void generateXTitle() {
        if(fData.getXData().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * X axis.
             */
            fXTitle = fData.getXData().get(0).getAspect().getLabel();
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectName = fData.getXData().stream()
                    .map(descriptor -> descriptor.getAspect().getName())
                    .distinct()
                    .count();

            long nbDiffAspectsUnits = fData.getXData().stream()
                    .map(descriptor -> descriptor.getAspect().getUnits())
                    .distinct()
                    .count();

            fXTitle = "Value"; //$NON-NLS-1$
            if(nbDiffAspectName == 1) {
                fXTitle = fData.getXData().get(0).getAspect().getName();
            }

            String units = null;
            if(nbDiffAspectsUnits == 1) {
                units = fData.getXData().get(0).getAspect().getUnits();
            }

            if(units != null) {
                fXTitle = fXTitle + ' ' + '(' + units + ')';
            }
        }
    }

    private void generateYTitle() {
        if(fData.getYData().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * Y axis (and hide the legend).
             */
            fYTitle = fData.getYData().get(0).getAspect().getLabel();

            /* Hide the legend */
            fChart.getLegend().setVisible(false);
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectName = fData.getYData().stream()
                    .map(descriptor -> descriptor.getAspect().getName())
                    .distinct()
                    .count();

            long nbDiffAspectsUnits = fData.getYData().stream()
                    .map(descriptor -> descriptor.getAspect().getUnits())
                    .distinct()
                    .count();

            fYTitle = "Value"; //$NON-NLS-1$
            if(nbDiffAspectName == 1) {
                fYTitle = fData.getXData().get(0).getAspect().getName();
            }

            String units = null;
            if(nbDiffAspectsUnits == 1) {
                units = fData.getXData().get(0).getAspect().getUnits();
            }

            if(units != null) {
                fYTitle = fYTitle + ' ' + '(' + units + ')';
            }

            /* Put legend at the bottom */
            fChart.getLegend().setPosition(SWT.BOTTOM);
        }
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
     * @return data to make a chart from
     */
    public ChartData getData() {
        return fData;
    }

    /**
     * @return model to make a chart from
     */
    public ChartModel getModel() {
        return fModel;
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

    /**
     * Get a {@link ChartRange} that covers all data points in the data.
     * <p>
     * The returned range will be the minimum and maximum of the resolved values
     * of the passed aspects for all result entries. If <code>clampToZero</code>
     * is true, a positive minimum value will be clamped down to zero.
     *
     * @param descriptors
     *            The data sources that the range will represent
     * @param clampToZero
     *            If true, a positive minimum value will be clamped down to zero
     * @return the range
     */
    protected ChartRange getRange(List<DataDescriptor> descriptors, boolean clampToZero) {
        /* Find the minimum and maximum values */
        BigDecimal min = new BigDecimal(Long.MAX_VALUE);
        BigDecimal max = new BigDecimal(Long.MIN_VALUE);
        BigDecimal current;
        Number num;

        NumberComparator<Number> comparator = new NumberComparator<>();
        for(DataDescriptor descriptor : descriptors) {
            INumericalSource source = (INumericalSource) descriptor.getSource();

            num = source.getStreamNumber()
                    .min(comparator::compare)
                    .get();
            current = new BigDecimal(num.toString());
            min = current.min(min);

            num = source.getStreamNumber()
                    .max(comparator::compare)
                    .get();
            current = new BigDecimal(num.toString());
            max = current.max(min);
        }

        if (clampToZero) {
            min.min(BigDecimal.ZERO);
        }

        /* Do not allow a range with a zero delta default to 1 */
        if (max.equals(min)) {
            max = min.add(BigDecimal.ONE);
        }

        return new ChartRange(checkNotNull(min), checkNotNull(max));
    }

    /**
     * Transform an external value into an internal value. Since SWTChart only
     * support Double and we can pass Long values, loss of precision might
     * happen. To minimize this, transform the raw values to an internal
     * representation based on a linear transformation.
     *
     * The internal value =
     *
     * ((rawValue - rawMinimum) * (internalRangeDelta/rawRangeDelta)) +
     * internalMinimum
     *
     * @param number
     *            The number to transform
     * @param internalRange
     *            The internal range definition to be used
     * @param externalRange
     *            The external range definition to be used
     * @return the transformed value in Double comprised inside the internal
     *         range
     */
    protected static double getInternalDoubleValue(Number number, ChartRange internalRange, ChartRange externalRange) {
        BigDecimal value = new BigDecimal(number.toString());

        if (externalRange.getDelta().compareTo(BigDecimal.ZERO) == 0) {
            return internalRange.getMinimum().doubleValue();
        }

        BigDecimal internalValue = value
                .subtract(externalRange.getMinimum())
                .multiply(internalRange.getDelta())
                .divide(externalRange.getDelta(), BIG_DECIMAL_DIVISION_SCALE, BigDecimal.ROUND_DOWN)
                .add(internalRange.getMinimum());

        return internalValue.doubleValue();
    }

    /**
     * Util method to check if a list of aspects are all continuous.
     *
     * @param descriptors
     *            The list of aspects to check.
     * @return true is all aspects are continuous, otherwise false.
     */
    protected static boolean areAspectsContinuous(List<DataDescriptor> descriptors) {
        return descriptors.stream().allMatch(aspect -> aspect.getAspect().isContinuous());
    }

    /**
     * Util method to check if a list of aspects are all time stamps.
     *
     * @param descriptors
     *            The list of aspects to check.
     * @return true is all aspects are time stamps, otherwise false.
     */
    protected static boolean areAspectsTimeStamp(List<DataDescriptor> descriptors) {
        return descriptors.stream().allMatch(aspect -> aspect.getAspect().isTimeStamp());
    }

    /**
     * Util method to check if a list of aspects are all time durations.
     *
     * @param descriptors
     *            The list of aspects to check.
     * @return true is all aspects are time durations, otherwise false.
     */
    protected static boolean areAspectsTimeDuration(List<DataDescriptor> descriptors) {
        return descriptors.stream().allMatch(aspect -> aspect.getAspect().isTimeDuration());
    }

    /**
     * Util method that will return a formatter based on the aspects linked to an axis
     *
     * If all aspects are time stamps, return a timestamp formatter tuned to the interval.
     * If all aspects are time durations, return the nanoseconds to seconds formatter.
     * Otherwise, return the generic decimal formatter.
     *
     * @param axisAspects
     *            The list of aspects of the axis.
     * @param internalRange
     *            The internal range for value transformation
     * @param externalRange
     *            The external range for value transformation
     * @return a formatter for the axis.
     */
    protected static Format getContinuousAxisFormatter(List<DataDescriptor> axisAspects, @Nullable ChartRange internalRange, @Nullable ChartRange externalRange) {
        Format formatter = DECIMAL_FORMATTER;

        if(areAspectsTimeStamp(axisAspects)) {
            /* Set a TimeStamp formatter depending on the duration between the first and last value */
            BigDecimal max = new BigDecimal(Long.MIN_VALUE);
            BigDecimal min = new BigDecimal(Long.MAX_VALUE);

            NumberComparator<Number> comparator = new NumberComparator<>();
            for (DataDescriptor descriptor : axisAspects) {
                INumericalSource source = (INumericalSource) descriptor.getSource();
                String value;
                BigDecimal current;

                value = source.getStreamNumber()
                        .max(comparator::compare)
                        .get().toString();
                current = new BigDecimal(value);
                max = current.max(max);

                value = source.getStreamNumber()
                        .min(comparator::compare)
                        .get().toString();
                current = new BigDecimal(value);
                min = current.min(min);
            }

            long duration = max.subtract(min).longValue();
            if (duration > TimeUnit.DAYS.toNanos(1)) {
                formatter = DAYS_FORMATTER;
            } else if (duration > TimeUnit.HOURS.toNanos(1)) {
                formatter = HOURS_FORMATTER;
            } else if (duration > TimeUnit.MINUTES.toNanos(1)) {
                formatter = MINUTES_FORMATTER;
            } else if (duration > TimeUnit.SECONDS.toNanos(15)) {
                formatter = SECONDS_FORMATTER;
            } else {
                formatter = MILLISECONDS_FORMATTER;
            }
            ((ChartTimeStampFormat) formatter).setInternalRange(internalRange);
            ((ChartTimeStampFormat) formatter).setExternalRange(externalRange);
        } else if (areAspectsTimeDuration(axisAspects)) {
            /* Set the time duration formatter. */
            formatter = NANO_TO_SECS_FORMATTER;
            ((ChartDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((ChartDecimalUnitFormat) formatter).setExternalRange(externalRange);
        } else {
            /*
             * For other numeric aspects, use the default lami decimal unit
             * formatter.
             */
            formatter = DECIMAL_FORMATTER;
            ((ChartDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((ChartDecimalUnitFormat) formatter).setExternalRange(externalRange);
        }

        return formatter;
    }
}
