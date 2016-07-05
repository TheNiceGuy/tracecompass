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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
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
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.provisional.tmf.chart.ui.IChartViewer;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartDecimalUnitFormat;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartRange;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartTimeStampFormat;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ISeries;
import org.swtchart.ITitle;

/**
 * Abstract class for XY charts.
 *
 * FIXME: value (s)
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class XYChartViewer implements IChartViewer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

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
     * Time stamp formatter for intervals in the days range
     */
    protected static final ChartTimeStampFormat DAYS_FORMATTER = new ChartTimeStampFormat("dd HH:mm"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the hours range
     */
    protected static final ChartTimeStampFormat HOURS_FORMATTER = new ChartTimeStampFormat("HH:mm"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the minutes range
     */
    protected static final ChartTimeStampFormat MINUTES_FORMATTER = new ChartTimeStampFormat("mm:ss"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the seconds range
     */
    protected static final ChartTimeStampFormat SECONDS_FORMATTER = new ChartTimeStampFormat("ss"); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the milliseconds range
     */
    protected static final ChartTimeStampFormat MILLISECONDS_FORMATTER = new ChartTimeStampFormat("ss.SSS"); //$NON-NLS-1$
    /**
     * Decimal formatter to display nanoseconds as seconds
     */
    protected static final DecimalUnitFormat NANO_TO_SECS_FORMATTER = new ChartDecimalUnitFormat(0.000000001);
    /**
     * Default decimal formatter
     */
    protected static final DecimalUnitFormat DECIMAL_FORMATTER = new ChartDecimalUnitFormat();
    /**
     * Symbol for seconds (used in the custom ns -> s conversion)
     * */
    private static final String SECONDS_SYMBOL = "s"; //$NON-NLS-1$
    /**
     * Symbol for nanoseconds (used in the custom ns -> s conversion)
     * */
    private static final String NANOSECONDS_SYMBOL = "ns"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Composite parent of the chart
     */
    private Composite fParent;
    /**
     * Top-right close button
     */
    private Button fCloseButton;
    /**
     * Title of the chart
     */
    private String fChartTitle;
    /**
     * SWT Chart object
     */
    private Chart fChart;
    /**
     * Data to plot into the chart
     */
    private ChartData fData;
    /**
     * Model used to make the chart
     */
    private ChartModel fModel;
    /**
     * X axis title
     */
    private String fXTitle;
    /**
     * Y axis title
     */
    private String fYTitle;
    /**
     * Map between data descriptors and X series
     */
    private Map<@NonNull DataDescriptor, @NonNull ISeries> fXSeries;
    /**
     * Map between data descriptors and Y series
     */
    private Map<@NonNull DataDescriptor, @NonNull ISeries> fYSeries;
    /**
     * Boolean indicating whether the X axis is continuous or not
     */
    private @Nullable Boolean fIsXContinuous;
    /**
     * Boolean indicating whether the Y axis is continuous or not
     */
    private @Nullable Boolean fIsYContinuous;
    /**
     * Internal X range of the chart
     */
    protected ChartRange fXInternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));
    /**
     * Internal Y range of the chart
     */
    protected ChartRange fYInternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));
    /**
     * External X range of the data
     */
    protected @Nullable ChartRange fXExternalRange = null;
    /**
     * External Y range of the data
     */
    protected @Nullable ChartRange fYExternalRange = null;

    // ------------------------------------------------------------------------
    // Important methods
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
    public XYChartViewer(Composite parent, ChartData data, ChartModel model, @Nullable String title) {
        fParent = parent;
        fData = data;
        fModel = model;
        fChartTitle = title;

        fIsXContinuous = areAspectsContinuous(getData().getXData());
        fIsYContinuous = areAspectsContinuous(getData().getYData());

        assertData();

        fXSeries = new HashMap<>();
        fYSeries = new HashMap<>();

        fChart = new Chart(parent, SWT.NONE);

        /* Compute ranges */
        computeRanges();

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

        /* Create the close button */
        Image close = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
        fCloseButton = new Button(fChart, SWT.PUSH);
        fCloseButton.setSize(25, 25);
        fCloseButton.setLocation(fChart.getSize().x-fCloseButton.getSize().x-5, 5);
        fCloseButton.setImage(close);
        fCloseButton.addSelectionListener(new CloseButtonEvent());

        /* Add listeners for the visibility of the close button and resizing */
        Listener mouseEnter = new MouseEnterEvent();
        Listener mouseExit = new MouseExitEvent();
        fChart.getDisplay().addFilter(SWT.MouseEnter, mouseEnter);
        fChart.getDisplay().addFilter(SWT.MouseExit, mouseExit);
        fChart.addDisposeListener(event -> {
            fChart.getDisplay().removeFilter(SWT.MouseEnter, mouseEnter);
            fChart.getDisplay().removeFilter(SWT.MouseExit, mouseExit);
        });
        fChart.addControlListener(new ResizeEvent());
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
    public XYChartViewer(Composite parent, ChartData data, ChartModel model) {
        this(parent, data, model, null);
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
        @Nullable Boolean xContinuous = isXContinuous();
        if(xContinuous == null) {
            throw new IllegalArgumentException("Each aspect must have the same continuity."); //$NON-NLS-1$
        }

        /* Make sure Y axis has the same continuity for each aspect */
        @Nullable Boolean yContinuous = isYContinuous();
        if(yContinuous == null) {
            throw new IllegalArgumentException("Each aspect must have the same continuity."); //$NON-NLS-1$
        }

        /* Make sure X logscale is disabled if data is discontinuous */
        if(!xContinuous && getModel().isXLogscale()) {
            throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
        }

        /* Make sure Y logscale is disabled if data is discontinuous */
        if(!yContinuous && getModel().isYLogscale()) {
            throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
        }
    }

    /**
     * This method should be called after the constructor. Normally, the factory constructor
     * {@link IChartViewer#createChart(Composite, ChartData, ChartModel, String)} handles that.
     */
    public void populate() {
        /* Create series */
        createSeries();
        setSeriesColor();

        /* Generate data series */
        generateXData(fData.getXData());
        generateYData(fData.getYData());
        postProcessData();

        Stream.of(getChart().getAxisSet().getYAxes()).forEach(axis -> axis.enableLogScale(getModel().isXLogscale()));
        Stream.of(getChart().getAxisSet().getYAxes()).forEach(axis -> axis.enableLogScale(getModel().isYLogscale()));

        configureAxes();

        for(IAxis yAxis : getChart().getAxisSet().getYAxes()) {
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
    }

    @Override
    public void dispose() {
        fChart.dispose();
        fParent.layout();
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * This method is used to compute the external ranges of the data. It is
     * called by the constructor.
     */
    protected abstract void computeRanges();

    /**
     * This method is used to create series. It is called by the {@link #populate()}}
     * method.
     */
    protected abstract void createSeries();

    /**
     * This method changes the color of the series. It is called by the
     * {@link #populate()}}  method.
     */
    protected abstract void setSeriesColor();

    /**
     * This method generate and set Y data for each serie. It is called by the
     * {@link #populate()}}  method.
     *
     * @param descriptors
     *              the descriptors to generate data from
     */
    protected abstract void generateYData(List<DataDescriptor> descriptors);

    /**
     * This method generate and set X data for each serie. It is called by the
     * {@link #populate()}} method.
     *
     * @param descriptors
     *              the descriptors to generate data from
     */
    protected abstract void generateXData(List<DataDescriptor> descriptors);

    /**
     * This method post processes the data generated by {@link #generateXData(List)}
     * and {@link #generateYData(List)} in order to affect it to the serie. For
     * example, it should reject wrong values before putting it in the serie. It
     * is called by the {@link #populate()} method.
     */
    protected abstract void postProcessData();

    /**
     * This method configures the axes. It is called by the {@link #populate()}
     * method.
     */
    protected abstract void configureAxes();

    /**
     * This method refreshed the display labels. It is called by the {@link #populate()}
     * method.
     */
    protected abstract void refreshDisplayLabels();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @return title of the chart
     */
    protected String getTitle() {
        if(fChartTitle == null) {
            return fXTitle + " vs. " + fYTitle; //$NON-NLS-1$
        }

        return fChartTitle;
    }

    /**
     * This method generate the X axis title.
     */
    protected void generateXTitle() {
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
                fXTitle = fXTitle + ' ' + '(' + getUnit(units) + ')';
            }
        }
    }

    /**
     * This method generate the Y axis title.
     */
    protected void generateYTitle() {
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
                units = fData.getYData().get(0).getAspect().getUnits();
            }

            if(units != null) {
                fYTitle = fYTitle + ' ' + '(' + getUnit(units) + ')';
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

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    /**
     * Util method to count the number of different aspects contains in a list
     * of data descriptors.
     *
     * @param descriptors
     *              the list of data descriptors
     * @return
     *              the number of different aspects
     */
    protected static long countDiffAspects(List<DataDescriptor> descriptors) {
        return descriptors.stream()
                .map(descriptor -> descriptor.getAspect().hashCode())
                .distinct()
                .count();
    }

    /**
     * Util method to check if a list of data descriptors is all continuous.
     *
     * @param descriptors
                    the list of data descriptors to check
     * @return
     *              true is all aspects are continuous,
     *              false if all aspects are discontinuous or
     *              null if it shares both continuity
     */
    protected static @Nullable Boolean areAspectsContinuous(List<DataDescriptor> descriptors) {
        long count = descriptors.stream()
                .map(descriptor -> descriptor.getAspect().isContinuous())
                .distinct()
                .count();

        if(count == 1) {
            return descriptors.get(0).getAspect().isContinuous();
        }

        return null;
    }

    /**
     * Utils method to convert a unit into its SI base unit.
     * <p>
     * TODO: might be a great idea to have some kind of classes/interfaces
     * that handle that kind of conversion. It would be more scalable than
     * this simple util function which is hackish. They could be directly
     * implemented into the aspects too, rather than using string.
     *
     * @param base
     *              the unit we want to retrieve the SI base
     * @return the SI base unit
     */
    protected static String getUnit(String base) {
        String unit;

        if (NANOSECONDS_SYMBOL.equals(base)) {
            unit = SECONDS_SYMBOL;
        } else {
            unit = base;
        }

        return unit;
    }

    /**
     * Util method to create unique categories from a stream.
     *
     * @param stream
     *              stream of string to generate labels from
     * @param map
     *              map of categories to generate labels into
     */
    protected static void generateLabelMap(Stream<String> stream, Map<@Nullable String, @NonNull Integer> map) {
        stream.distinct().forEach(str -> map.putIfAbsent(str, map.size()));
    }

    /**
     * Util method to update tick mark of an axis. This step is a limitation
     * on swtchart side regarding minimal grid step hint size. When the step
     * size are smaller it get defined as the "default" value for the axis
     * instead of the smallest one.
     *
     * @param map
     *              map of labels used to compute the minimum size required
     * @param tick
     *              axis tick to update
     * @param availableLenghtPixel
     *              available lenght in pixel
     */
    protected static void updateTickMark(Map<@Nullable String, Integer> map, IAxisTick tick, int availableLenghtPixel) {
        int nbLabels = Math.max(1, map.size());
        int stepSizePixel = availableLenghtPixel / nbLabels;

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
     *            the data sources that the range will represent
     * @param clampToZero
     *            if true, a positive minimum value will be clamped down to zero
     * @return the range
     */
    protected static ChartRange getRange(List<DataDescriptor> descriptors, boolean clampToZero) {
        /* Find the minimum and maximum values */
        BigDecimal min = new BigDecimal(Long.MAX_VALUE);
        BigDecimal max = new BigDecimal(Long.MIN_VALUE);
        BigDecimal current;
        Number num;

        for(DataDescriptor descriptor : descriptors) {
            INumericalSource source = (INumericalSource) descriptor.getSource();

            num = source.getStreamNumber()
                    .filter(Objects::nonNull)
                    .min(source::compare)
                    .orElse(Long.MAX_VALUE);
            current = new BigDecimal(checkNotNull(num).toString());
            min = current.min(min);

            num = source.getStreamNumber()
                    .filter(Objects::nonNull)
                    .max(source::compare)
                    .orElse(Long.MIN_VALUE);
            current = new BigDecimal(checkNotNull(num).toString());
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
     * The internal value = ((rawValue - rawMinimum) *
     * (internalRangeDelta / rawRangeDelta)) + internalMinimum
     *
     * @param number
     *              the number to transform
     * @param internalRange
     *              the internal range definition to be used
     * @param externalRange
     *              the external range definition to be used
     * @return
     *              the transformed value in Double comprised inside the
     *              internal range
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
     * Util method that will return a formatter based on the aspects linked to an axis
     *
     * If all aspects are time stamps, return a timestamp formatter tuned to the interval.
     * If all aspects are time durations, return the nanoseconds to seconds formatter.
     * Otherwise, return the generic decimal formatter.
     *
     * @param axisAspects
     *              the list of aspects of the axis
     * @param internalRange
     *              the internal range for value transformation
     * @param externalRange
     *              the external range for value transformation
     * @return a formatter for the axis.
     */
    protected static Format getContinuousAxisFormatter(List<DataDescriptor> axisAspects, @Nullable ChartRange internalRange, @Nullable ChartRange externalRange) {
        Format formatter = DECIMAL_FORMATTER;

        if(areAspectsTimeStamp(axisAspects)) {
            /* Set a TimeStamp formatter depending on the duration between the first and last value */
            BigDecimal max = new BigDecimal(Long.MIN_VALUE);
            BigDecimal min = new BigDecimal(Long.MAX_VALUE);

            for (DataDescriptor descriptor : axisAspects) {
                INumericalSource source = (INumericalSource) descriptor.getSource();
                String value;
                BigDecimal current;

                value = checkNotNull(source.getStreamNumber()
                        .filter(Objects::nonNull)
                        .min(source::compare)
                        .orElse(Long.MAX_VALUE))
                        .toString();
                current = new BigDecimal(value);
                min = current.min(min);

                value = checkNotNull(source.getStreamNumber()
                        .filter(Objects::nonNull)
                        .max(source::compare)
                        .orElse(Long.MIN_VALUE))
                        .toString();
                current = new BigDecimal(value);
                max = current.max(max);
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
            /* For other numeric aspects, use the default decimal unit formatter. */
            formatter = DECIMAL_FORMATTER;

            ((ChartDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((ChartDecimalUnitFormat) formatter).setExternalRange(externalRange);
        }

        return formatter;
    }

    /**
     * Util method to check if a list of aspects are all time stamps.
     *
     * @param descriptors
     *              the list of aspects to check.
     * @return true is all aspects are time stamps, otherwise false.
     */
    protected static boolean areAspectsTimeStamp(List<DataDescriptor> descriptors) {
        return descriptors.stream().allMatch(aspect -> aspect.getAspect().isTimeStamp());
    }

    /**
     * Util method to check if a list of aspects are all time durations.
     *
     * @param descriptors
     *              the list of aspects to check.
     * @return true is all aspects are time durations, otherwise false.
     */
    protected static boolean areAspectsTimeDuration(List<DataDescriptor> descriptors) {
        return descriptors.stream().allMatch(aspect -> aspect.getAspect().isTimeDuration());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return receiver's composite parent
     */
    public Composite getParent() {
        return fParent;
    }

    /**
     * @return receiver's chart
     */
    public Chart getChart() {
        return fChart;
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
     * @return maps between X series and data descriptors
     */
    public Map<@NonNull DataDescriptor, @NonNull ISeries> getXSeries() {
        return fXSeries;
    }

    /**
     * @return maps between Y series and data descriptors
     */
    public Map<@NonNull DataDescriptor, @NonNull ISeries> getYSeries() {
        return fYSeries;
    }

    /**
     * This accessor return whether the X axis is continuous or not. If null
     * is returned, then the axis is both continuous and discontinuous.
     *
     * @return boolean whether X axis is continuous or not
     */
    public @Nullable Boolean isXContinuous() {
        return fIsXContinuous;
    }

    /**
     * This accessor return whether the Y axis is continuous or not. If null
     * is returned, then the axis is both continuous and discontinuous.
     *
     * @return boolean whether Y axis is continuous or not
     */
    public @Nullable Boolean isYContinuous() {
        return fIsYContinuous;
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    private class ResizeEvent implements ControlListener {
        @Override
        public void controlMoved(ControlEvent e) {}

        @Override
        public void controlResized(ControlEvent e) {
            /* Refresh titles */
            refreshDisplayTitles();

            /* Refresh the Axis labels to fit the current chart size */
            refreshDisplayLabels();

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

            /* Only set to visible if we are at the right location, in the right shell. */
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
}
