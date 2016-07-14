/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui.type;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartType;


/**
 * Scatter chart implementation of the a chart type.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ScatterChartType implements IChartType {

    /**
     * Icons used in the chart maker
     */
    private static final String ICONS = "icons/scatterchart.png"; //$NON-NLS-1$

    @Override
    public ChartType getType() {
        return ChartType.SCATTER_CHART;
    }

    @Override
    public ImageData getImageData() {
        return new ImageData(getClass().getClassLoader().getResourceAsStream(ICONS));
    }

    @Override
    public boolean checkIfXAspectValid(IDataChartAspect aspect, @Nullable IDataChartAspect filter) {
        return IChartType.filterSameAspect(aspect, filter);
    }

    @Override
    public boolean checkIfYAspectValid(IDataChartAspect aspect, @Nullable IDataChartAspect filter) {
        return IChartType.filterSameAspect(aspect, filter);
    }

    @Override
    public boolean checkIfXLogscalePossible(@Nullable IDataChartAspect filter) {
        if(filter == null) {
            return true;
        }

        return IChartType.checkIfNumerical(filter);
    }

    @Override
    public boolean checkIfYLogscalePossible(@Nullable IDataChartAspect filter) {
        if(filter == null) {
            return true;
        }

        return IChartType.checkIfNumerical(filter);
    }

}
