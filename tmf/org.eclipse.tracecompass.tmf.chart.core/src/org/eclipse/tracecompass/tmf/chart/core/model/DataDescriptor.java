/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.model;

import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.source.IDataSource;

/**
 * Class that links an aspect to a data source.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataDescriptor {

    /**
     * Aspect describing the source
     */
    private final IDataChartAspect fAspect;
    /**
     * Data source itself
     */
    private final IDataSource fSource;

    /**
     * Constructor.
     *
     * @param aspect
     *            Aspect of the source
     * @param source
     *            Source of data
     */
    public DataDescriptor(IDataChartAspect aspect, IDataSource source) {
        fAspect = aspect;
        fSource = source;
    }

    /**
     * Surcharged constructor.
     *
     * @param descriptor
     *            Descriptor to copy
     */
    public DataDescriptor(DataDescriptor descriptor) {
        fAspect = descriptor.getAspect();
        fSource = descriptor.getSource();
    }

    /**
     * Get the aspect that describes the content of the source.
     *
     * @return The aspect of the source
     */
    public IDataChartAspect getAspect() {
        return fAspect;
    }

    /**
     * Get the stream of data.
     *
     * @return The source of data
     */
    public IDataSource getSource() {
        return fSource;
    }

}
