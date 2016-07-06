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
 * Abstract class for describing timestamps from a stream of values it
 * understands.
 *
 * @param <T>
 *            The type of object it understands
 * @param <R>
 *            The type of timestamps it describes
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataChartTimestampDescriptor<T, R extends Number> extends DataChartNumericalDescriptor<T, R> {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final @Nullable String fUnit;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping timestamps
     * @param unit
     *            The unit of the timestamps
     */
    public DataChartTimestampDescriptor(String name, INumericalResolver<T, R> resolver, @Nullable String unit) {
        super(name, resolver);

        fUnit = unit;
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
        return fUnit;
    }

}
