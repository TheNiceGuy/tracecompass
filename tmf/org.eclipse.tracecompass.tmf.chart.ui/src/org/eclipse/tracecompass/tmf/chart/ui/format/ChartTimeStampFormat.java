/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.ui.format;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * Formatter for time stamps
 */
public class ChartTimeStampFormat extends Format {

    private static final int BIG_DECIMAL_DIVISION_SCALE = 22;

    private static final long serialVersionUID = 4285447886537779762L;

    private final TmfTimestampFormat fFormat;

    private @Nullable ChartRange fInternalRange = null;
    private @Nullable ChartRange fExternalRange = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public ChartTimeStampFormat() {
        fFormat = checkNotNull(TmfTimestampFormat.getDefaulTimeFormat());
    }

    /**
     * The base constructor.
     *
     * @param internalRange
     *            The internal range used for graph representation
     * @param externalRange
     *            The external (real value) range shown to the user
     */
    public ChartTimeStampFormat(ChartRange internalRange, ChartRange externalRange) {
        fFormat = checkNotNull(TmfTimestampFormat.getDefaulTimeFormat());
        fInternalRange = internalRange;
        fExternalRange = externalRange;
    }

    /**
     * Constructor.
     *
     * @param pattern
     *            The format pattern
     */
    public ChartTimeStampFormat(String pattern) {
        fFormat = new TmfTimestampFormat(pattern);
    }

    /**
     * Constructor.
     *
     * @param pattern
     *            The format pattern
     * @param internalRange
     *            The internal range used for graph representation
     * @param externalRange
     *            The external (real value) range shown to the user
     */
    public ChartTimeStampFormat(String pattern, ChartRange internalRange, ChartRange externalRange) {
        fFormat = new TmfTimestampFormat(pattern);
        fInternalRange = internalRange;
        fExternalRange = externalRange;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @return The internal range definition
     */
    public @Nullable ChartRange getInternalRange() {
        return fInternalRange;
    }

    /**
     * @param internalRange
     *            The internal range definition to be used by the formatter
     */
    public void setInternalRange(@Nullable ChartRange internalRange) {
        fInternalRange = internalRange;
    }

    /**
     * @return The external range definition
     */
    public @Nullable ChartRange getExternalRange() {
        return fExternalRange;
    }

    /**
     * @param externalRange
     *            The external range definition to be used by the formatter
     */
    public void setExternalRange(@Nullable ChartRange externalRange) {
        fExternalRange = externalRange;
    }

    @Override
    public StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        if (obj != null && obj instanceof Number && toAppendTo != null) {
            @Nullable
            ChartRange internalRange = fInternalRange;
            @Nullable
            ChartRange externalRange = fExternalRange;
            if (internalRange == null || externalRange == null) {
                long time = ((Number) obj).longValue();
                return checkNotNull(toAppendTo.append(fFormat.format(time)));
            }

            if (internalRange.getDelta().compareTo(BigDecimal.ZERO) == 0 ||
                    externalRange.getDelta().compareTo(BigDecimal.ZERO) == 0) {
                return checkNotNull(toAppendTo.append(fFormat.format(externalRange.getMinimum().doubleValue())));
            }

            /* Find external value before formatting */
            BigDecimal externalValue = (new BigDecimal(obj.toString()))
                    .subtract(internalRange.getMinimum())
                    .multiply(externalRange.getDelta())
                    .divide(internalRange.getDelta(), BIG_DECIMAL_DIVISION_SCALE, BigDecimal.ROUND_DOWN)
                    .add(externalRange.getMinimum());

            return checkNotNull(toAppendTo.append(fFormat.format(externalValue.longValue())));
        }
        return new StringBuffer();
    }

    @Override
    public @Nullable Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return null;
    }

}
