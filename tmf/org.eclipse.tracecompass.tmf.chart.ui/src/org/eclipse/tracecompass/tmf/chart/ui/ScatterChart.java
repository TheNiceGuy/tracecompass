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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.swtchart.IBarSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

import com.google.common.collect.Iterators;

/**
 * Class for building a scatter chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ScatterChart extends XYChartViewer {

    /**
     * This constructor creates the chart.
     *
     * @param parent parent composite
     * @param dataSeries configured data series for the chart
     * @param model chart model to use
     */
    public ScatterChart(Composite parent, ChartData dataSeries, ChartModel model) {
        super(parent, dataSeries, model);
    }

    @Override
    public void createSeries() {
        ISeriesSet set = getChart().getSeriesSet();

        for(DataDescriptor descriptor : getData().getYData()) {
            String title = descriptor.getAspect().getLabel();

            ILineSeries series = (ILineSeries) set.createSeries(SeriesType.LINE, title);
            series.setLineStyle(LineStyle.NONE);
        }
    }

    @Override
    public void setSeriesColor() {
        // TODO Auto-generated method stub
    }

    @Override
    protected List<double[]> generateYData(List<DataDescriptor> descriptors) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<double[]> generateXData(List<DataDescriptor> descriptors) {
        // TODO Auto-generated method stub
        return null;
    }

//    @Override
//    public double[] generateYData(DataDescriptor descriptor) {
//        double[] data;
//
//        if(descriptor.getAspect().isContinuous()) {
//            // generate data if the aspect is continuous
//            data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
//                    .mapToDouble(num -> num.doubleValue())
//                    .toArray();
//        } else {
//            // generate unique position for each string
//            generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), getYMap());
//
//            data = ((IStringSource) descriptor.getSource()).getStreamString()
//                    .map(str -> getYMap().get(str))
//                    .map(num -> checkNotNull(num))
//                    .mapToDouble(num -> (double) num)
//                    .toArray();
//        }
//
//        return data;
//    }
//
//    @Override
//    public double[] generateXData(DataDescriptor descriptor) {
//        double[] data;
//
//        if(descriptor.getAspect().isContinuous()) {
//            // generate data if the aspect is continuous
//            data = ((INumericalSource) descriptor.getSource()).getStreamNumber()
//                    .mapToDouble(num -> num.doubleValue())
//                    .toArray();
//        } else {
//            // generate unique position for each string
//            generateLabelMap(((IStringSource) descriptor.getSource()).getStreamString(), getXMap());
//
//            data = ((IStringSource) descriptor.getSource()).getStreamString()
//                    .map(str -> getXMap().get(str))
//                    .map(num -> checkNotNull(num))
//                    .mapToDouble(num -> (double) num)
//                    .toArray();
//        }
//
//        return data;
//    }
}
