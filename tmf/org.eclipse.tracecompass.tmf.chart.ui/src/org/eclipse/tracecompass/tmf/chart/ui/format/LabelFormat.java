/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.BiMap;

/**
 * Format label based on a given Map<String, Integer>
 *
 * @author Jonathan Rajotte-Julien
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class LabelFormat extends Format {

    private static final long serialVersionUID = 4939553034329681316L;

    private static final String SWTCHART_EMPTY_LABEL = " "; //$NON-NLS-1$
    private static final String UNKNOWN_REPRESENTATION = "?"; //$NON-NLS-1$

    private final BiMap<@Nullable String, Integer> fMap;

    /**
     * Constructor
     *
     * @param map
     *              Map of indices to labels
     */
    public LabelFormat(BiMap<@Nullable String, Integer> map) {
        super();

        fMap = map;
    }

    @Override
    public @Nullable StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        if (obj == null || toAppendTo == null) {
            return new StringBuffer(SWTCHART_EMPTY_LABEL);
        }

        Double doubleObj = (Double) obj;

        /*
         * Return a string buffer with a space in it since SWT does not like to
         * draw empty strings.
         */
        if ((doubleObj % 1 != 0) || !fMap.containsValue((doubleObj.intValue()))) {
            return new StringBuffer(SWTCHART_EMPTY_LABEL);
        }

        String key = fMap.inverse().get(doubleObj.intValue());
        if(key == null) {
            return new StringBuffer(UNKNOWN_REPRESENTATION);
        }

        return toAppendTo.append(key);
    }

    @Override
    public @Nullable Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return fMap.get(source);
    }

}

