/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.BiMap;

/**
 * Format label based on a given Map<String, Integer>.
 *
 * @author Jonathan Rajotte-Julien
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class LabelFormat extends Format {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = -7046922375212377647L;
    private static final String CHART_EMPTY_LABEL = " "; //$NON-NLS-1$
    private static final String UNKNOWN_REPRESENTATION = "?"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final BiMap<@Nullable String, Integer> fMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param map
     *            Map of labels and indices
     */
    public LabelFormat(BiMap<@Nullable String, Integer> map) {
        super();

        fMap = map;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public @Nullable StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        if (obj == null || toAppendTo == null) {
            return new StringBuffer(CHART_EMPTY_LABEL);
        }

        Double doubleObj = (Double) obj;

        /* If the key is not contained in the map, format a empty label */
        if ((doubleObj % 1 != 0) || !fMap.containsValue((doubleObj.intValue()))) {
            return new StringBuffer(CHART_EMPTY_LABEL);
        }

        /* If the value is null in the map, format an unknown label */
        String key = fMap.inverse().get(doubleObj.intValue());
        if (key == null) {
            return new StringBuffer(UNKNOWN_REPRESENTATION);
        }

        return toAppendTo.append(key);
    }

    @Override
    public @Nullable Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return fMap.get(source);
    }

}
