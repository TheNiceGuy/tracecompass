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

import java.math.BigDecimal;
import java.text.Format;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.chart.IChartViewer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.IChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.DescriptorsInformation;
import org.eclipse.tracecompass.internal.tmf.chart.ui.format.ChartDecimalUnitFormat;
import org.eclipse.tracecompass.internal.tmf.chart.ui.format.ChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ISeries;
import org.swtchart.ITitle;

/**
 * Abstract class for XY charts. These kind of charts can take as many X and Y
 * descriptors. Bar charts and scatter charts are examples of possible XY
 * charts.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class SwtXYChartViewer extends TmfViewer implements IChartViewer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Ellipsis character
     */
    protected static final String ELLIPSIS = "…"; //$NON-NLS-1$
    /**
     * Zero long value
     */
    protected static final long ZERO_LONG = 0L;
    /**
     * Zero double value
     */
    protected static final double ZERO_DOUBLE = 0.0;
    /**
     * Factor used in the computation of the logarithmic epsilon value
     */
    private static final double LOGSCALE_EPSILON_FACTOR = 100.0;
    /**
     * Time stamp formatter for intervals in the days range
     */
    protected static final ChartTimeStampFormat DAYS_FORMATTER = new ChartTimeStampFormat("dd HH:mm", null); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the hours range
     */
    protected static final ChartTimeStampFormat HOURS_FORMATTER = new ChartTimeStampFormat("HH:mm", null); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the minutes range
     */
    protected static final ChartTimeStampFormat MINUTES_FORMATTER = new ChartTimeStampFormat("mm:ss", null); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the seconds range
     */
    protected static final ChartTimeStampFormat SECONDS_FORMATTER = new ChartTimeStampFormat("ss", null); //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the milliseconds range
     */
    protected static final ChartTimeStampFormat MILLISECONDS_FORMATTER = new ChartTimeStampFormat("ss.SSS", null); //$NON-NLS-1$
    /**
     * Decimal formatter to display nanoseconds as seconds
     */
    protected static final DecimalUnitFormat NANO_TO_SECS_FORMATTER = new ChartDecimalUnitFormat(0.000000001, null);
    /**
     * Default decimal formatter
     */
    protected static final DecimalUnitFormat DECIMAL_FORMATTER = new ChartDecimalUnitFormat(null);
    /**
     * Symbol for seconds (used in the custom ns -> s conversion)
     */
    private static final String SECONDS_SYMBOL = "s"; //$NON-NLS-1$
    /**
     * Symbol for nanoseconds (used in the custom ns -> s conversion)
     */
    private static final String NANOSECONDS_SYMBOL = "ns"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Top-right close button
     */
    private final Button fCloseButton;
    /**
     * SWT Chart object
     */
    private final Chart fChart;
    /**
     * Data to plot into the chart
     */
    private final ChartData fData;
    /**
     * Model used to make the chart
     */
    private final ChartModel fModel;
    /**
     * X axis title
     */
    private final String fXTitle;
    /**
     * Y axis title
     */
    private final String fYTitle;
    /**
     * Information about the set of X descriptors
     */
    private final DescriptorsInformation fXInformation;
    /**
     * Information about the set of Y descriptors
     */
    private final DescriptorsInformation fYInformation;
    /**
     * Chart consumer for processing data
     */
    private @Nullable XYChartConsumer fChartConsumer;
    /**
     * Map between series and SWT series
     */
    private final Map<ChartSeries, ISeries> fSeriesMap = new HashMap<>();
    /**
     * Map between SWT series and consumed objects
     */
    private final Map<ISeries, Object[]> fObjectMap = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for the chart. Any final class that derives this class must
     * call the {@link #populate()} method to create the rest of the chart.
     *
     * @param parent
     *            A parent composite
     * @param data
     *            A configured data series for the chart
     * @param model
     *            A chart model to use
     */
    public SwtXYChartViewer(Composite parent, ChartData data, ChartModel model) {
        fParent = parent;
        fData = data;
        fModel = model;
        fXInformation = new DescriptorsInformation(getXDescriptors());
        fYInformation = new DescriptorsInformation(getYDescriptors());

        validateChartData();

        fChart = new Chart(parent, SWT.NONE);

        /* Generate titles */
        fXTitle = generateXTitle();
        fYTitle = generateYTitle();

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
        fCloseButton.setLocation(fChart.getSize().x - fCloseButton.getSize().x - 5, 5);
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
     * This method is called after the constructor by the factory constructor.
     * While everything could be put in the constructor directly, splitting the
     * constructor simply makes the code cleaner by reducing the number of null
     * checks.
     */
    public void populate() {
        /* Create the consumer for the data */
        XYChartConsumer chartConsumer = createChartConsumer();
        fChartConsumer = chartConsumer;

        /* Process all the objects from the stream of data */
        fData.getDataProvider().getSource().forEach(o -> {
            chartConsumer.accept(o);
        });
        chartConsumer.finish();

        /* Create the SWT series */
        createSeries(fSeriesMap);
        configureSeries(fObjectMap);

        /* Adjust the chart range */
        getChart().getAxisSet().adjustRange();

        /* Configure axes */
        configureAxes();
        Arrays.stream(getChart().getAxisSet().getXAxes()).forEach(a -> a.enableLogScale(getModel().isXLogscale()));
        Arrays.stream(getChart().getAxisSet().getYAxes()).forEach(a -> a.enableLogScale(getModel().isYLogscale()));

        for (IAxis yAxis : getChart().getAxisSet().getYAxes()) {
            /*
             * SWTChart workaround: SWTChart fiddles with tick mark visibility
             * based on the fact that it can parse the label to double or not.
             *
             * If the label happens to be a double, it checks for the presence
             * of that value in its own tick labels to decide if it should add
             * it or not. If it happens that the parsed value is already present
             * in its map, the tick gets a visibility of false.
             *
             * The X axis does not have this problem since SWTCHART checks on
             * label angle, and if it is != 0 simply does no logic regarding
             * visibility. So simply set a label angle of 1 to the axis.
             */
            yAxis.getTick().setTickLabelAngle(1);
        }
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        fChart.dispose();
        fParent.layout();
    }

    @Override
    public @Nullable Control getControl() {
        return fChart.getParent();
    }

    @Override
    public void refresh() {
        Display.getDefault().asyncExec(() -> {
            if (!fChart.isDisposed()) {
                fChart.redraw();
            }
        });
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * This method creates the {@link XYChartConsumer} for the implemented
     * chart. This consumer is used for consuming all the objects of the data
     * stream.
     *
     * @see IChartConsumer
     * @see XYChartConsumer
     *
     * @return The chart consumer
     */
    protected abstract XYChartConsumer createChartConsumer();

    /**
     * This method creates the SWT series and maps them to our own
     * {@link ChartSeries}. It should also handle things like the color of the
     * SWT series.
     *
     * @param mapper
     *            The mapper to put the series in
     */
    protected abstract void createSeries(Map<ChartSeries, ISeries> mapper);

    /**
     * This methods processes the data created from the {@link XYChartConsumer}
     * and configures the SWT series. Since SWT Chart only supports double value
     * for numbers, this method should convert the {@link Number} into double
     * and sets them into the {@link ISeries}.
     * <p>
     * In order to allow signals and selection, we need to know which object is
     * link to a point in a certain SWT series. To do this, we use a map between
     * the objects that have been consumed by a {@link XYChartConsumer} and the
     * series itself.
     *
     * @param mapper
     *            The mapper to put the list of object in
     */
    protected abstract void configureSeries(Map<ISeries, Object[]> mapper);

    /**
     * This method configures the axes.
     */
    protected abstract void configureAxes();

    /**
     * This method refreshed the display labels.
     */
    protected abstract void refreshDisplayLabels();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This method makes sure that the {@link ChartData}} is properly built for
     * making XY charts.
     */
    protected void validateChartData() {
        /* Make sure X logscale is disabled if data is discontinuous */
        if (!getXDescriptorsInfo().areNumerical() && getModel().isXLogscale()) {
            throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
        }

        /* Make sure Y logscale is disabled if data is discontinuous */
        if (!getYDescriptorsInfo().areNumerical() && getModel().isYLogscale()) {
            throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
        }
    }

    /**
     * @return The title of the chart
     */
    protected String getTitle() {
        return getModel().getTitle();
    }

    /**
     * This method generate the X axis title.
     *
     * @return The X axis title
     */
    protected String generateXTitle() {
        String title;

        if (getData().getChartSeries().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * X axis.
             */
            title = fData.getX(0).getLabel();
        } else {
            /*
             * There are multiple series in the chart, if they all share the
             * same units, display that.
             */
            long nbDiffDescriptorName = getXDescriptors().stream()
                    .map(descriptor -> descriptor.getName())
                    .distinct()
                    .count();

            long nbDiffDescriptorUnits = getXDescriptors().stream()
                    .map(descriptor -> descriptor.getUnit())
                    .distinct()
                    .count();

            title = "Value"; //$NON-NLS-1$
            if (nbDiffDescriptorName == 1) {
                title = fData.getX(0).getName();
            }

            String units = null;
            if (nbDiffDescriptorUnits == 1) {
                units = fData.getX(0).getUnit();
            }

            if (units != null) {
                title = title + ' ' + '(' + getUnit(units) + ')';
            }
        }

        return title;
    }

    /**
     * This method generate the Y axis title.
     *
     * @return The Y axis title
     */
    protected String generateYTitle() {
        String title;

        if (getData().getChartSeries().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * Y axis (and hide the legend).
             */
            title = fData.getY(0).getLabel();

            /* Hide the legend */
            fChart.getLegend().setVisible(false);
        } else {
            /*
             * There are multiple series in the chart, if they all share the
             * same units, display that.
             */
            long nbDiffDescriptorName = getYDescriptors().stream()
                    .map(descriptor -> descriptor.getName())
                    .distinct()
                    .count();

            long nbDiffDescriptorUnits = getYDescriptors().stream()
                    .map(descriptor -> descriptor.getUnit())
                    .distinct()
                    .count();

            title = "Value"; //$NON-NLS-1$
            if (nbDiffDescriptorName == 1) {
                title = fData.getY(0).getName();
            }

            String units = null;
            if (nbDiffDescriptorUnits == 1) {
                units = fData.getY(0).getUnit();
            }

            if (units != null) {
                title = title + ' ' + '(' + getUnit(units) + ')';
            }

            /* Put legend at the bottom */
            fChart.getLegend().setPosition(SWT.BOTTOM);
        }

        return title;
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
             * If the title is too long, generate a shortened version based on
             * the average character width of the current font.
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
     * Refresh the Chart, XAxis and YAxis titles to fit the current chart size.
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
     * Util method to clamp the input data range of a range map. It returns the
     * map passed in parameter for chaining a method.
     *
     * @param map
     *            The map to clamp the input data range
     * @return The same map passed in parameter
     */
    protected static ChartRangeMap clampInputDataRange(ChartRangeMap map) {
        map.getInputDataRange().clamp();

        return map;
    }

    /**
     * Util method to convert a unit into its SI base unit.
     * <p>
     * TODO: might be a great idea to have some kind of classes/interfaces that
     * handle that kind of conversion. It would be more scalable than this
     * simple util function which is hackish. They could be directly implemented
     * into the descriptors too, rather than using string.
     *
     * @param base
     *            Unit we want to retrieve the SI base
     * @return The SI base unit
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
     * Util method to compute the magic epsilon factor used in the mapping of
     * logarithmic value in a SWT chart.
     * <p>
     * <b>Log scale magic course 101:</b>
     * <p>
     * It uses the relative difference divided by a factor (100) to get as close
     * as it can to the actual minimum but still a little bit smaller. This is
     * used as a workaround of SWT chart limitations regarding custom scale
     * drawing in log scale mode, bogus representation of NaN double values and
     * limited support of multiple size series. This should be good enough for
     * most users.
     *
     * @param map
     *            The positive range map
     * @return The computed factor
     */
    protected static double getLogarithmicEspilon(ChartRangeMap map) {
        BigDecimal minBig = map.getInputDataRange().getMinimum();
        BigDecimal maxBig = map.getInputDataRange().getMaximum();

        /* Convert numbers to internal double values */
        double min = map.getInternalValue(minBig).doubleValue();
        double max = map.getInternalValue(maxBig).doubleValue();

        /* Calculate epsilon */
        double delta = map.getInputDataRange().getDelta().doubleValue();
        return min - ((min * delta) / (LOGSCALE_EPSILON_FACTOR * max));
    }

    /**
     * Util method to update tick mark of an axis. This step is a limitation on
     * swtchart side regarding minimal grid step hint size. When the step size
     * are smaller it get defined as the "default" value for the axis instead of
     * the smallest one.
     *
     * @param map
     *            Map of labels used to compute the minimum size required
     * @param tick
     *            Axis tick to update
     * @param availableLenghtPixel
     *            Available lenght in pixel
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
     * Util method that will return a formatter based on the descriptors linked
     * to an axis.
     *
     * If all descriptors are time stamps, return a timestamp formatter tuned to
     * the interval. If all descriptors are time durations, return the
     * nanoseconds to seconds formatter. Otherwise, return the generic decimal
     * formatter.
     *
     * @param map
     *            The range map that the formatter will use
     * @param info
     *            Informations of the descriptors tied to the formatter
     * @return The formatter for the axis.
     */
    protected static Format getContinuousAxisFormatter(ChartRangeMap map, DescriptorsInformation info) {
        Format formatter;

        if (info.areTimestamp()) {
            BigDecimal max = map.getInputDataRange().getMinimum();
            BigDecimal min = map.getInputDataRange().getMaximum();

            /* Find the best formatter for our values */
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

            /* Update time ranges in the formatter */
            ((ChartTimeStampFormat) formatter).setRangeMap(map);
        } else if (info.areDuration()) {
            /* Use the time duration formatter */
            formatter = NANO_TO_SECS_FORMATTER;

            /* Update time ranges in the formatter */
            ((ChartDecimalUnitFormat) formatter).setRangeMap(map);
        } else {
            /*
             * Use the default decimal formatter for other numeric descriptors
             */
            formatter = DECIMAL_FORMATTER;

            /* Update time ranges in the formatter */
            ((ChartDecimalUnitFormat) formatter).setRangeMap(map);
        }

        return formatter;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the SWT chart object.
     *
     * @return The receiver's chart
     */
    protected Chart getChart() {
        return fChart;
    }

    /**
     * Accessor that returns the chart data.
     *
     * @return The data to make a chart from
     */
    protected ChartData getData() {
        return fData;
    }

    /**
     * Accessor that returns the chart model.
     *
     * @return The model to make a chart from
     */
    protected ChartModel getModel() {
        return fModel;
    }

    /**
     * Accessor that returns the information about the set of X descriptors to
     * plot.
     *
     * @return Information about the X descriptors
     */
    protected DescriptorsInformation getXDescriptorsInfo() {
        return fXInformation;
    }

    /**
     * Accessor that returns the information about the set of Y descriptors to
     * plot.
     *
     * @return Information about the Y descriptors
     */
    protected DescriptorsInformation getYDescriptorsInfo() {
        return fYInformation;
    }

    /**
     * Accessor that returns the {@link XYChartConsumer} of this chart.
     *
     * @return The consumer of this chart
     */
    protected XYChartConsumer getChartConsumer() {
        return checkNotNull(fChartConsumer);
    }

    /**
     * Accessor that returns the map between data series and SWT series objects.
     *
     * @return The map between series and SWT series
     */
    protected Map<ChartSeries, ISeries> getSeriesMap() {
        return fSeriesMap;
    }

    /**
     * Accessor that returns the map between SWT series and the objects linked
     * to each point.
     *
     * @return The map between SWT series and consumed objects
     */
    protected Map<ISeries, Object[]> getObjectMap() {
        return fObjectMap;
    }

    /**
     * Accessor that returns the collection of the X descriptors.
     *
     * @return The stream of X descriptors
     */
    protected Collection<IDataChartDescriptor<?, ?>> getXDescriptors() {
        return checkNotNull(getData().getChartSeries().stream()
                .map(series -> series.getX())
                .collect(Collectors.toList()));
    }

    /**
     * Accessor that returns the collection of the Y descriptors.
     *
     * @return The stream of Y descriptors
     */
    protected Collection<IDataChartDescriptor<?, ?>> getYDescriptors() {
        return checkNotNull(getData().getChartSeries().stream()
                .map(series -> series.getY())
                .collect(Collectors.toList()));
    }

    // ------------------------------------------------------------------------
    // Anonymous Classes
    // ------------------------------------------------------------------------

    /**
     * Predicate that rejects null and negative values.
     */
    protected class LogarithmicPredicate implements Predicate<@Nullable Number> {
        private INumericalResolver<?, Number> fResolver;

        /**
         * Constructor.
         *
         * @param resolver
         *            The resolver used for getting the zero of the same type of
         *            number we are comparing
         */
        protected LogarithmicPredicate(INumericalResolver<?, Number> resolver) {
            fResolver = resolver;
        }

        @Override
        public boolean test(@Nullable Number t) {
            if (t == null) {
                return false;
            }

            return fResolver.getComparator().compare(t, fResolver.getZeroValue()) > 0;
        }
    }

    /**
     * Listener that handles resize events of the chart.
     */
    private class ResizeEvent implements ControlListener {
        @Override
        public void controlMoved(@Nullable ControlEvent e) {
        }

        @Override
        public void controlResized(@Nullable ControlEvent e) {
            /* Refresh titles */
            refreshDisplayTitles();

            /* Refresh the Axis labels to fit the current chart size */
            refreshDisplayLabels();

            /* Relocate the close button */
            fCloseButton.setLocation(fChart.getSize().x - fCloseButton.getSize().x - 5, 5);
        }
    }

    /**
     * Listener that handles events of the close button.
     */
    private class CloseButtonEvent implements SelectionListener {
        @Override
        public void widgetSelected(@Nullable SelectionEvent e) {
            dispose();
        }

        @Override
        public void widgetDefaultSelected(@Nullable SelectionEvent e) {
        }
    }

    /**
     * Listener that handles mouse events when the mouse enter the chart window.
     */
    private class MouseEnterEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            Control control = (Control) event.widget;
            Point display = control.toDisplay(event.x, event.y);
            Point location = getChart().getParent().toControl(display);

            /* Only set to visible if we are inside and in the right shell. */
            boolean inside = getChart().getBounds().contains(location);
            boolean shell = control.getShell().equals(getChart().getShell());
            fCloseButton.setVisible(inside && shell);
        }
    }

    /**
     * Listener that handles mouse events when the mouse exit the chart window.
     */
    private class MouseExitEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            fCloseButton.setVisible(false);
        }
    }

}
