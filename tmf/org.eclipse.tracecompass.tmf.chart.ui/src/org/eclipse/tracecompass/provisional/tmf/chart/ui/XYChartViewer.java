/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.provisional.tmf.chart.ui;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.math.BigDecimal;
import java.text.Format;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.AbstractAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartDecimalUnitFormat;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartRange;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartTimeStampFormat;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.ITitle;

/**
 * Abstract XYChart Viewer for charts.
 *
 * @author Michael Jeanson
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class XYChartViewer implements IChartViewer {
    /**
     * Ellipsis character
     */
    protected static final String ELLIPSIS = "…"; //$NON-NLS-1$
    /**
     * String representing unknown values. Can be present even in numerical
     * aspects!
     */
    protected static final String UNKNOWN = "?"; //$NON-NLS-1$
    /**
     * Zero long value
     */
    protected static final long ZERO_LONG = 0L;
    /**
     * Zero double value
     */
    protected static final double ZERO_DOUBLE = 0.0;
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
    protected static final DecimalUnitFormat NANO_TO_SECS_FORMATTER = new DecimalUnitFormat(0.000000001);
    /**
     * Default decimal formatter.
     */
    protected static final DecimalUnitFormat DECIMAL_FORMATTER = new DecimalUnitFormat();
    /**
     * Symbol for seconds (used in the custom ns -> s conversion)
     */
    private static final String SECONDS_SYMBOL = "s"; //$NON-NLS-1$
    /**
     * Symbol for nanoseconds (used in the custom ns -> s conversion)
     */
    private static final String NANOSECONDS_SYMBOL = "ns"; //$NON-NLS-1$
    /**
     * Maximum amount of digits that can be represented into a double
     * */
    private static final int BIG_DECIMAL_DIVISION_SCALE = 22;

//    /**
//     * Function to use to map Strings read from the data table to doubles for
//     * use in SWTChart series.
//     */
//    protected static final ToDoubleFunction<@Nullable String> DOUBLE_MAPPER = str -> {
//        if (str == null || str.equals(UNKNOWN)) {
//            return ZERO_LONG;
//        }
//        return Double.parseDouble(str);
//    };

//    private final Listener fResizeListener = event -> {
//        /* Refresh the titles to fit the current chart size */
//        refreshDisplayTitles();
//
//        /* Refresh the Axis labels to fit the current chart size */
//        refreshDisplayLabels();
//    };

    private Composite fParent;

    private final ChartData fResultTable; /** TODO: rename */
    private final ChartModel fChartModel; /** TODO: rename */

    private final Chart fChart;
    private final Button fCloseButton;

    private final String fChartTitle;

    private String fXLabel;
    private @Nullable String fXUnits;

    private String fYLabel;
    private @Nullable String fYUnits;

    private boolean fSelected;
    private Set<Integer> fSelection;

    /**
     * Creates a Viewer instance based on SWTChart.
     *
     * @param parent
     *            The parent composite to draw in.
     * @param resultTable
     *            The result table containing the data from which to build the
     *            chart
     * @param chartModel
     *            The information about the chart to build
     */
    public XYChartViewer(Composite parent, ChartData resultTable, ChartModel chartModel) {
        //super(parent);

        fParent = parent;
        fResultTable = resultTable;
        fChartModel = chartModel;
        fSelection = new HashSet<>();

        fXLabel = ""; //$NON-NLS-1$
        fYLabel = ""; //$NON-NLS-1$

        fChart = new Chart(parent, SWT.NONE);
        fChart.addControlListener(new ResizeEvent());

        /* Set X axis title */
        if (fResultTable.getXData().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * X axis.
             */
            innerSetXTitle(getXAxisAspects().get(0).getName(), getXAxisAspects().get(0).getUnits());
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectsUnits = getXAxisAspects().stream()
                .map(aspect -> aspect.getUnits())
                .distinct()
                .count();

            long nbDiffAspectName = getXAxisAspects().stream()
                    .map(aspect -> aspect.getName())
                    .distinct()
                    .count();

            String xBaseTitle = Messages.XYChartViewer_DefaultAxisTitle;
            if (nbDiffAspectName == 1) {
                xBaseTitle = getXAxisAspects().get(0).getName();
            }

            String units = null;
            if (nbDiffAspectsUnits == 1) {
                /* All aspects use the same unit type */
                units = getXAxisAspects().get(0).getUnits();
            }

            innerSetXTitle(xBaseTitle, units);
        }

        /* Set Y axis title */
        if (fResultTable.getYData().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * Y axis (and hide the legend).
             */
            innerSetYTitle(getYAxisAspects().get(0).getName(), getYAxisAspects().get(0).getUnits());

            /* Hide the legend */
            fChart.getLegend().setVisible(false);
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectsUnits = getYAxisAspects().stream()
                .map(aspect -> aspect.getUnits())
                .distinct()
                .count();

            long nbDiffAspectName = getYAxisAspects().stream()
                    .map(aspect -> aspect.getName())
                    .distinct()
                    .count();

            String yBaseTitle = Messages.XYChartViewer_DefaultAxisTitle;
            if (nbDiffAspectName == 1) {
                yBaseTitle = getYAxisAspects().get(0).getName();
            }

            String units = null;
            if (nbDiffAspectsUnits == 1) {
                /* All aspects use the same unit type */
                units = getYAxisAspects().get(0).getUnits();
            }

            innerSetYTitle(yBaseTitle, units);

            /* Put legend at the bottom */
            fChart.getLegend().setPosition(SWT.BOTTOM);
        }

        /* Set Chart title */
        fChartTitle = generateTitle();

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

        // create the close button
        Image close = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
        fCloseButton = new Button(fChart, SWT.PUSH);
        fCloseButton.setSize(25, 25);
        fCloseButton.setLocation(fChart.getSize().x-fCloseButton.getSize().x-5, 5);
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
    }

    private String generateTitle() {
        return  fChart.getAxisSet().getXAxis(0).getTitle().getText() + ' ' + Messages.XYChartViewer_TitleVersus + ' ' +
                fChart.getAxisSet().getYAxis(0).getTitle().getText();
    }

    /**
     * Set the Y axis title and refresh the chart.
     *
     * @param label
     *            the label string.
     * @param units
     *            the units string.
     */
    protected void setYTitle(@Nullable String label, @Nullable String units) {
        innerSetYTitle(label, units);
    }

    private void innerSetYTitle(@Nullable String label, @Nullable String units) {
        fYLabel = nullToEmptyString(label);
        innerSetYUnits(units);
        refreshDisplayTitles();
    }

    /**
     * Set the units on the Y Axis title and refresh the chart.
     *
     * @param units
     *            the units string.
     */
    protected void setYUnits(@Nullable String units) {
        innerSetYUnits(units);
    }

    private void innerSetYUnits(@Nullable String units) {
        /*
         * All time durations in nanoseconds, on the charts we use an axis
         * formater that converts back to seconds as a base unit and then
         * uses prefixes like nano and milli depending on the range.
         *
         * So set the units to seconds in the title to match the base unit of
         * the formater.
         */
        if (NANOSECONDS_SYMBOL.equals(units)) {
            fYUnits = SECONDS_SYMBOL;
        } else {
            fYUnits = units;
        }
        refreshDisplayTitles();
    }

    /**
     * Get the Y axis title string.
     *
     * If the units is non-null, the title will be: "label (units)"
     *
     * If the units is null, the title will be: "label"
     *
     * @return the title of the Y axis.
     */
    protected String getYTitle() {
        if (fYUnits == null) {
            return fYLabel;
        }
        return fYLabel + " (" + fYUnits + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Set the X axis title and refresh the chart.
     *
     * @param label
     *            the label string.
     * @param units
     *            the units string.
     */
    protected void setXTitle(@Nullable String label, @Nullable String units) {
        innerSetXTitle(label, units);
    }

    private void innerSetXTitle(@Nullable String label, @Nullable String units) {
        fXLabel = nullToEmptyString(label);
        innerSetXUnits(units);
        refreshDisplayTitles();
    }

    /**
     * Set the units on the X Axis title.
     *
     * @param units
     *            the units string
     */
    protected void setXUnits(@Nullable String units) {
        innerSetXUnits(units);
    }

    private void innerSetXUnits(@Nullable String units) {
        /* The time duration formatter converts ns to s on the axis */
        if (NANOSECONDS_SYMBOL.equals(units)) {
            fXUnits = SECONDS_SYMBOL;
        } else {
            fXUnits = units;
        }
        refreshDisplayTitles();
    }

    /**
     * Get the X axis title string.
     *
     * If the units is non-null, the title will be: "label (units)"
     *
     * If the units is null, the title will be: "label"
     *
     * @return the title of the Y axis.
     */
    protected String getXTitle() {
        if (fXUnits == null) {
            return fXLabel;
        }
        return fXLabel + " (" + fXUnits + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Util method to check if a list of descriptors contains only continuous aspects.
     *
     * @param axisDescriptors
     *            The list of descriptors to check.
     * @return true is all aspects are continuous, otherwise false.
     */
    protected static boolean areAspectsContinuous(List<DataDescriptor> axisDescriptors) {
        return axisDescriptors.stream().allMatch(desc -> desc.getAspect().isContinuous());
    }

    /**
     * Util method to check if a list of descriptors contains only timestamp aspects.
     *
     * @param axisDescriptors
     *            The list of descriptors to check.
     * @return true is all aspects are time stamps, otherwise false.
     */
    protected static boolean areAspectsTimeStamp(List<DataDescriptor> axisDescriptors) {
        return axisDescriptors.stream().allMatch(desc -> desc.getAspect().isTimeStamp());
    }

    /**
     * Util method to check if a list of descriptors contains only duration aspects.
     *
     * @param axisDescriptors
     *            The list of descriptors to check.
     * @return true is all aspects are time durations, otherwise false.
     */
    protected static boolean areAspectsTimeDuration(List<DataDescriptor> axisDescriptors) {
        return axisDescriptors.stream().allMatch(desc -> desc.getAspect().isTimeDuration());
    }

    /**
     * Util method that will return a formatter based on the aspects linked to an axis
     *
     * If all aspects are time stamps, return a timestamp formatter tuned to the interval.
     * If all aspects are time durations, return the nanoseconds to seconds formatter.
     * Otherwise, return the generic decimal formatter.
     *
     * @param axisDescriptors
     *            The list of descriptors of the axis
     * @param internalRange
     *            The internal range for value transformation
     * @param externalRange
     *            The external range for value transformation
     * @return a formatter for the axis.
     */
    protected static Format getContinuousAxisFormatter(List<DataDescriptor> axisDescriptors, @Nullable ChartRange internalRange, @Nullable ChartRange externalRange) {
        Format formatter = DECIMAL_FORMATTER;

        if (areAspectsTimeStamp(axisDescriptors)) {
            /* Set a TimeStamp formatter depending on the duration between the first and last value */
            BigDecimal max = new BigDecimal(Long.MIN_VALUE);
            BigDecimal min = new BigDecimal(Long.MAX_VALUE);

            for (DataDescriptor data : axisDescriptors) {
                Number number = ((INumericalSource) data).getStreamNumber().max(new NumberComparator()).get();

                BigDecimal current = new BigDecimal(number.toString());
                max = current.max(max);
                min = current.min(min);
            }

//            for (LamiTableEntry entry : entries) {
//                for (LamiTableEntryAspect aspect : axisAspects) {
//                    @Nullable Number number = aspect.resolveNumber(entry);
//                    if (number != null) {
//                        BigDecimal current = new BigDecimal(number.toString());
//                        max = current.max(max);
//                        min = current.min(min);
//                    }
//                }
//            }

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
            ((ChartDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((ChartDecimalUnitFormat) formatter).setExternalRange(externalRange);
        } else if (areAspectsTimeDuration(axisDescriptors)) {
            /* Set the time duration formatter. */
            formatter = NANO_TO_SECS_FORMATTER;
            ((ChartDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((ChartDecimalUnitFormat) formatter).setExternalRange(externalRange);
        } else {
            /*
             * For other numeric aspects, use the default decimal unit formatter.
             */
            formatter = DECIMAL_FORMATTER;
            ((ChartDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((ChartDecimalUnitFormat) formatter).setExternalRange(externalRange);
        }

        return formatter;
    }

    /**
     * Get the chart data.
     *
     * @return The chart result table.
     */
    protected ChartData getResultTable() {
        return fResultTable;
    }

    /**
     * Get the chart model.
     *
     * @return The chart model.
     */
    protected ChartModel getChartModel() {
        return fChartModel;
    }

    /**
     * Get the chart object.
     * @return The chart object.
     */
    protected Chart getChart() {
        return fChart;
    }

    /**
     * Is a selection made in the chart.
     *
     * @return true if there is a selection.
     */
    protected boolean isSelected() {
        return fSelected;
    }

    /**
     * Set the selection index.
     *
     * @param selection the index to select.
     */
    protected void setSelection(Set<Integer> selection) {
        fSelection = selection;
        fSelected = !selection.isEmpty();
    }

    /**
     * Unset the chart selection.
     */
    protected void unsetSelection() {
        fSelection.clear();
        fSelected = false;
    }

    /**
     * Get the current selection index.
     *
     * @return the current selection index.
     */
    protected Set<Integer> getSelection() {
        return fSelection;
    }

//    @Override
//    public @Nullable Control getControl() {
//        return fChart.getParent();
//    }
//
//    @Override
//    public void refresh() {
//        Display.getDefault().asyncExec(() -> {
//            if (!fChart.isDisposed()) {
//                fChart.redraw();
//            }
//        });
//    }

    @Override
    public void dispose() {
        Composite parent = fChart.getParent();
        fChart.dispose();
        parent.layout();
    }

    /**
     * Get a list of all the aspect of the Y axis.
     *
     * @return The aspects for the Y axis
     */
    protected List<AbstractAspect> getYAxisAspects() {
        List<AbstractAspect> yAxisAspects = new ArrayList<>();

        for(DataDescriptor desc : getResultTable().getYData()) {
            yAxisAspects.add(checkNotNull(desc.getAspect()));
        }

        return yAxisAspects;
    }

    /**
     * Get a list of all the aspect of the X axis.
     *
     * @return The aspects for the X axis
     */
    protected List<AbstractAspect> getXAxisAspects() {
        List<AbstractAspect> xAxisAspects = new ArrayList<>();

        for(DataDescriptor desc : getResultTable().getXData()) {
            xAxisAspects.add(checkNotNull(desc.getAspect()));
        }

        return xAxisAspects;
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

            // Cleanup
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
        refreshDisplayTitle(chartTitle, fChartTitle, chartRect.width);

        ITitle xTitle = checkNotNull(fChart.getAxisSet().getXAxis(0).getTitle());
        refreshDisplayTitle(xTitle, getXTitle(), plotRect.width);

        ITitle yTitle = checkNotNull(fChart.getAxisSet().getYAxis(0).getTitle());
        refreshDisplayTitle(yTitle, getYTitle(), plotRect.height);
    }

    /**
     * Refresh the axis labels to fit the current chart size.
     */
    protected abstract void refreshDisplayLabels();

//    /**
//     * Redraw the chart.
//     */
//    protected void redraw() {
//        refresh();
//    }

//    /**
//     * Signal handler for selection update.
//     *
//     * @param signal
//     *          The selection update signal
//     */
//    @TmfSignalHandler
//    public void updateSelection(LamiSelectionUpdateSignal signal) {
//        if (getResultTable().hashCode() != signal.getSignalHash() || equals(signal.getSource())) {
//            /* The signal is not for us */
//            return;
//        }
//        setSelection(signal.getEntryIndex());
//
//        redraw();
//    }

    /**
     * Get a {@link ChartRange} that covers all data points in the chart data.
     * <p>
     * The returned range will be the minimum and maximum of the resolved values
     * of the passed aspects for all result entries. If <code>clampToZero</code>
     * is true, a positive minimum value will be clamped down to zero.
     *
     * @param descriptors
     *            The descriptors that the range will represent
     * @param clampToZero
     *            If true, a positive minimum value will be clamped down to zero
     * @return the range
     */
    protected ChartRange getRange(List<DataDescriptor> descriptors, boolean clampToZero) {
        /* Find the minimum and maximum values */
        BigDecimal min = new BigDecimal(Long.MAX_VALUE);
        BigDecimal max = new BigDecimal(Long.MIN_VALUE);

        for (DataDescriptor data : descriptors) {
            Number number = ((INumericalSource) data).getStreamNumber().max(new NumberComparator()).get();

            BigDecimal current = new BigDecimal(number.toString());
            max = current.max(max);
            min = current.min(min);
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
     * support Double and we support more precision, loss of precision might
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

    private class ResizeEvent implements ControlListener {
        @Override
        public void controlMoved(ControlEvent e) {}

        @Override
        public void controlResized(ControlEvent e) {
            /* Refresh the titles to fit the current chart size */
            refreshDisplayTitles();

            /* Refresh the Axis labels to fit the current chart size */
            refreshDisplayLabels();

            /* Relocate the close button */
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
     * FIXME: inefficient?
     */
    private static class NumberComparator implements Comparator<Number> {
        @Override
        public int compare(Number a, Number b){
            return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
        }
    }
}
