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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IStringSource;
import org.eclipse.tracecompass.tmf.chart.ui.format.ChartRange;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

import com.google.common.collect.Iterators;

/**
 * Class for a bar chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class BarChart extends XYChartViewer {

    private static final double LOGSCALE_EPSILON_FACTOR = 100.0;

    private ChartRange fYInternalRange;
    private ChartRange fYExternalRange;

    private double fLogScaleEpsilon = ZERO_DOUBLE;
    private double fMax;

    /**
     * Constructor.
     *
     * @param parent parent composite
     * @param dataSeries configured data series for the chart
     * @param model chart model to use
     */
    public BarChart(Composite parent, ChartData dataSeries, ChartModel model) {
        super(parent, dataSeries, model);

        fYInternalRange = new ChartRange(checkNotNull(BigDecimal.ZERO), checkNotNull(BigDecimal.ONE));
        fYExternalRange = getRange(dataSeries.getYData(), true);

        /*
         * Log scale magic course 101:
         *
         * It uses the relative difference divided by a factor
         * (100) to get as close as it can to the actual minimum but still a
         * little bit smaller. This is used as a workaround of SWTCHART
         * limitations regarding custom scale drawing in log scale mode, bogus
         * representation of NaN double values and limited support of multiple
         * size series.
         *
         * This should be good enough for most users.
         */
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double value;

        if(getModel().isYLogscale()) {
            /* Find minimum and maximum values excluding <= 0 values */
            for(DataDescriptor descriptor : getData().getYData()) {
                INumericalSource source = (INumericalSource) descriptor.getSource();

                value = source.getStreamNumber()
                        .map(num -> getInternalDoubleValue(num, fYInternalRange, fYExternalRange))
                        .max(Double::compare)
                        .get()
                        .doubleValue();
                max = Math.max(max, value);

                value = source.getStreamNumber()
                        .map(num -> getInternalDoubleValue(num, fYInternalRange, fYExternalRange))
                        .min(Double::compare)
                        .get()
                        .doubleValue();
                min = Math.min(min, value);
            }
        }

        if (min == Double.MAX_VALUE) {
            return;
        }

        /* Calculate epsilon */
        double delta = max - min;
        fLogScaleEpsilon = min - ((min * delta) / (LOGSCALE_EPSILON_FACTOR * max));

        /**TODO*/
        fMax = max;
    }

    @Override
    public void populate() {
        super.populate();

        for(IAxis yAxis : getChart().getAxisSet().getYAxes()) {
            yAxis.getTick().setFormat(getContinuousAxisFormatter(getData().getYData(), fYInternalRange, fYExternalRange));

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

        if (getModel().isYLogscale() && fLogScaleEpsilon != fMax) {
            getChart().getAxisSet().getYAxis(0).setRange(new Range(fLogScaleEpsilon, fMax));
        }
    }

    @Override
    public void assertData() {
        super.assertData();

        /* Make sure there is only one X axis */
        if(getData().countDiffXData() > 1) {
            throw new IllegalArgumentException("Bar chart can only have one X axis."); //$NON-NLS-1$
        }
    }

    @Override
    public void createSeries() {
        ISeriesSet set = getChart().getSeriesSet();

        for(DataDescriptor descriptor : getData().getYData()) {
            String title = descriptor.getAspect().getLabel();

            IBarSeries series = (IBarSeries) set.createSeries(SeriesType.BAR, title);
            series.setBarPadding(50);
        }
    }

    @Override
    public void setSeriesColor() {
        Iterator<Color> colors = Iterators.cycle(COLORS);

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            ((IBarSeries) series).setBarColor(colors.next());
        }
    }

    @Override
    public List<double[]> generateXData(List<DataDescriptor> descriptors) {
        double[] data;

        ArrayList<double[]> dataList = new ArrayList<>();
        DataDescriptor descriptor = descriptors.get(0);
        if(descriptor.getAspect().isContinuous()) {
            /* generate unique position for each number */
            generateLabelMap(((INumericalSource) descriptor.getSource()).getStreamNumber()
                    .map(num -> num.toString()), getXMap());

            data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                    .map(num -> getXMap().get(num.toString()))
                    .mapToDouble(num -> num.doubleValue())
                    .toArray();
        } else {
            /* generate unique position for each string */
            generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), getXMap());

            data = ((IStringSource) descriptor.getSource()).getStreamString()
                    .map(str -> getXMap().get(str))
                    .mapToDouble(num -> (double) num)
                    .toArray();
        }

        /* each series has the same X data */
        for(int i = 0; i < descriptors.size(); i++) {
            dataList.add(data);
        }

        return dataList;
    }

    @Override
    public List<double[]> generateYData(List<DataDescriptor> descriptors) {
        double[] data;

        ArrayList<double[]> dataList = new ArrayList<>();
        for(DataDescriptor descriptor : descriptors) {
            if(descriptor.getAspect().isContinuous()) {
                /* generate data if the aspect is continuous */
                data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
                        .mapToDouble(num -> num.doubleValue())
                        .toArray();
            } else {
                /* generate unique position for each string */
                generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), getYMap());

                data = ((IStringSource) descriptor.getSource()).getStreamString()
                        .map(str -> getYMap().get(str))
                        .map(num -> checkNotNull(num))
                        .mapToDouble(num -> (double) num)
                        .toArray();
            }

            dataList.add(data);
        }

        return dataList;
    }
}
