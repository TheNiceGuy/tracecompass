/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.type;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;

/**
 * Scatter chart implementation of the a chart type.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ScatterChartTypeDefinition implements IChartTypeDefinition {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Icons used in the chart maker
     */
    private static final String ICONS = "icons/scatterchart.png"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public ChartType getType() {
        return ChartType.SCATTER_CHART;
    }

    @Override
    public ImageData getImageData() {
        return new ImageData(getClass().getClassLoader().getResourceAsStream(ICONS));
    }

    @Override
    public boolean checkIfXDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        return IChartTypeDefinition.filterSameDescriptor(desc, filter);
    }

    @Override
    public boolean checkIfYDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        return IChartTypeDefinition.filterSameDescriptor(desc, filter);
    }

    @Override
    public boolean checkIfXLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter) {
        if (filter == null) {
            return true;
        }

        return IChartTypeDefinition.checkIfNumerical(filter);
    }

    @Override
    public boolean checkIfYLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter) {
        if (filter == null) {
            return true;
        }

        return IChartTypeDefinition.checkIfNumerical(filter);
    }

}
