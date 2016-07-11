/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.format;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRange;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * Formatter for timestamps.
 *
 * @author Michael Jeanson
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartTimeStampFormat extends Format {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 8102026791684954897L;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final TmfTimestampFormat fFormat;
    private @Nullable ChartRangeMap fRangeMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public ChartTimeStampFormat() {
        fFormat = checkNotNull(TmfTimestampFormat.getDefaulTimeFormat());
        fRangeMap = null;
    }

    /**
     * Constructor with a range map supplied.
     *
     * @param map
     *            A internal and external ranges map
     */
    public ChartTimeStampFormat(ChartRangeMap map) {
        fFormat = checkNotNull(TmfTimestampFormat.getDefaulTimeFormat());
        fRangeMap = map;
    }

    /**
     * Constructor with a pattern supplied.
     *
     * @param pattern
     *            The format pattern
     */
    public ChartTimeStampFormat(String pattern) {
        fFormat = new TmfTimestampFormat(pattern);
        fRangeMap = null;
    }

    /**
     * Constructor with both a pattern and a range map supplied.
     *
     * @param pattern
     *            The format pattern
     * @param map
     *            A internal and external ranges map
     */
    public ChartTimeStampFormat(String pattern, ChartRangeMap map) {
        fFormat = new TmfTimestampFormat(pattern);
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

        /* If no range was provided, format with the number unchanged */
        if (fRangeMap == null) {
            long time = ((Number) obj).longValue();
            return checkNotNull(toAppendTo.append(fFormat.format(time)));
        }

        ChartRange internalRange = checkNotNull(fRangeMap).getInternal();
        ChartRange externalRange = checkNotNull(fRangeMap).getExternal();

        /* If any range's delta is null, format with the external bounds */
        if (internalRange.isDeltaNull() || externalRange.isDeltaNull()) {
            return checkNotNull(toAppendTo.append(fFormat.format(externalRange.getMinimum().doubleValue())));
        }

        /* Find external value before formatting */
        BigDecimal externalValue = checkNotNull(fRangeMap).getExternalValue(number);
        return checkNotNull(toAppendTo.append(fFormat.format(externalValue.longValue())));
    }

    @Override
    public @Nullable Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return null;
    }

}
