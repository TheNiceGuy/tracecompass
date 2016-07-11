/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.data;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;

/**
 * Simple class that maps two {@link ChartRange} together. The first one is the
 * internal range: it represents the range of the chart in which the data will
 * be plotted. The second one is the external range: it represents the range of
 * the incoming data that will be plotted.
 * <p>
 * This map is used for mapping values that might be too big for a chart. Values
 * that are in the external range are mapped to fit the internal range.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartRangeMap {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int BIG_DECIMAL_DIVISION_SCALE = 22;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private ChartRange fInternal;
    private ChartRange fExternal;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public ChartRangeMap() {
        fInternal = new ChartRange();
        fExternal = new ChartRange();
    }

    /**
     * Surcharged constructor with the external range.
     *
     * @param external
     *            The external range of the map
     */
    public ChartRangeMap(ChartRange external) {
        fInternal = new ChartRange();
        fExternal = external;
    }

    /**
     * Surcharged constructor with the internal and external ranges.
     *
     * @param internal
     *            The internal range of the map
     * @param external
     *            The external range of the map
     */
    public ChartRangeMap(ChartRange internal, ChartRange external) {
        fInternal = internal;
        fExternal = external;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the internal range of the map.
     *
     * @return The internal range
     */
    public ChartRange getInternal() {
        return fInternal;
    }

    /**
     * Accessor that returns the external range of the map.
     *
     * @return The external range
     */
    public ChartRange getExternal() {
        return fExternal;
    }

    // ------------------------------------------------------------------------
    // Mutators
    // ------------------------------------------------------------------------

    /**
     * Mutator that sets the internal range of the map.
     *
     * @param internal
     *            The new internal range
     */
    public void setInternal(ChartRange internal) {
        fInternal = internal;
    }

    /**
     * Mutator that sets the external range of the map.
     *
     * @param external
     *            The new external range
     */
    public void setExternal(ChartRange external) {
        fExternal = external;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This method transforms an external value into an internal value.
     * <p>
     * Incoming numbers from the data might be bigger than the numbers that the
     * chart library supports (e.g. SWT supports Double and we can pass Long).
     * While processing the numbers, a loss of precision might occur. In order
     * to minimize this, we transform the raw values to an internal
     * representation based on a linear transformation.
     * <p>
     * Let <i>e_val</i>, <i>e_min</i>, <i>e_Δ</i> be the external value,
     * external minimum and external delta respectively and <i>i_min</i>,
     * <i>i_Δ</i> be the internal minimum and the internal delta. The internal
     * value <i>i_val</i> is given by the formula:
     * <p>
     * i_val=(e_val-e_min)*(i_Δ/e_Δ)+i_min
     *
     * @param number
     *            A number to transform
     * @return The transformed value
     */
    public Number getInternalValue(Number number) {
        BigDecimal value = new BigDecimal(number.toString());
        ChartRange internal = getInternal();
        ChartRange external = getExternal();

        /* Apply the formula */
        BigDecimal internalValue = value
                .subtract(external.getMinimum())
                .multiply(internal.getDelta())
                .divide(external.getDelta(), BIG_DECIMAL_DIVISION_SCALE, BigDecimal.ROUND_DOWN)
                .add(internal.getMinimum());

        return checkNotNull(internalValue);
    }

    /**
     * Util method that transforms an internal value into an external
     * {@link BigDecimal} value.
     * <p>
     * It is very similar to {@link #getInternalValue(Number)}, except that is
     * apply the formula in reverse in order to obtain the original value while
     * minimizing lost in precision.
     *
     * @param number
     *            A number to transform
     * @return A BigDecimal representation of the external value
     */
    public BigDecimal getExternalValue(Number number) {
        ChartRange internal = getInternal();
        ChartRange external = getExternal();

        /* Apply the formula in reverse */
        BigDecimal externalValue = (new BigDecimal(number.toString()))
                .subtract(internal.getMinimum())
                .multiply(external.getDelta())
                .divide(internal.getDelta(), BIG_DECIMAL_DIVISION_SCALE, BigDecimal.ROUND_DOWN)
                .add(external.getMinimum());

        return checkNotNull(externalValue);
    }

}
