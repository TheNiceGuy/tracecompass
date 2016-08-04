/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;

/**
 * Generic descriptor that describes a time range duration.
 *
 * @param <T>
 *            The type of object it understands
 * @param <R>
 *            The type of durations it describes
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataChartDurationDescriptor<T, R extends Number> extends DataChartNumericalDescriptor<T, R> {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping durations
     */
    public DataChartDurationDescriptor(String name, INumericalResolver<T, R> resolver) {
        super(name, resolver);
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(IDescriptorVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public @Nullable String getUnit() {
        return "ns"; //$NON-NLS-1$
    }

}
