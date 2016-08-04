/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.format;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.text.FieldPosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRange;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;

/**
 * Decimal formatter for graph
 *
 * Since the graph use normalized internal value the initial (external)
 * representation needs to be obtained. Subsequent formatting is done based on a
 * Double. Loss of precision could occurs based on the size. For now, loss of
 * precision for decimal values is not a big concern. If it ever become one the
 * use of Long while formatting might come in handy.
 *
 * @author Jonathan Rajotte-Julien
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartDecimalUnitFormat extends DecimalUnitFormat {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = -4288059349658845257L;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private @Nullable ChartRangeMap fRangeMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public ChartDecimalUnitFormat() {
        super();
        fRangeMap = null;
    }

    /**
     * Constructor with a range map supplied.
     *
     * @param map
     *            A internal and external ranges map
     */
    public ChartDecimalUnitFormat(ChartRangeMap map) {
        super();
        fRangeMap = map;
    }

    /**
     * Constructor with a multiplication factor.
     *
     * @param factor
     *            A multiplication factor to apply to the value
     */
    public ChartDecimalUnitFormat(double factor) {
        super(factor);
        fRangeMap = null;
    }

    /**
     * Constructor with a multiplication factor and range map.
     *
     * @param factor
     *            Multiplication factor to apply to the value
     * @param map
     *            A internal and external ranges map
     */
    public ChartDecimalUnitFormat(double factor, ChartRangeMap map) {
        super(factor);
        fRangeMap = map;
    }

    // ------------------------------------------------------------------------
    // Mutators
    // ------------------------------------------------------------------------

    /**
     * Mutators that sets the chart range map of this formatter.
     *
     * @param map
     *            The new chart range map
     */
    public void setRangeMap(ChartRangeMap map) {
        fRangeMap = map;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        if (!(obj instanceof Number) || toAppendTo == null) {
            throw new IllegalArgumentException("Cannot format given Object as a Number: " + obj); //$NON-NLS-1$
        }

        Number number = (Number) obj;

        /* If no map was provided, format with the number unchanged */
        if (fRangeMap == null) {
            StringBuffer buffer = super.format(number, toAppendTo, pos);
            return (buffer == null ? new StringBuffer() : buffer);
        }

        ChartRange internalRange = checkNotNull(fRangeMap).getInternal();
        ChartRange externalRange = checkNotNull(fRangeMap).getExternal();

        /* If any range's delta is null, format with the external bounds */
        if (internalRange.isDeltaNull() || externalRange.isDeltaNull()) {
            StringBuffer buffer = super.format(externalRange.getMinimum().doubleValue(), toAppendTo, pos);
            return (buffer == null ? new StringBuffer() : buffer);
        }

        /* Find external value before formatting */
        Double externalValue = checkNotNull(fRangeMap).getExternalValue(number).doubleValue();
        StringBuffer buffer = super.format(externalValue, toAppendTo, pos);
        return (buffer == null ? new StringBuffer() : buffer);
    }

}
