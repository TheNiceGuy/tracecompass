/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.provisional.tmf.chart.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the chart package
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.provisional.tmf.chart.ui.messages"; //$NON-NLS-1$
    /**
     * Default title for an axis
     */
    public static String XYChartViewer_DefaultAxisTitle;
    /**
     * Keyword used for the title
     */
    public static String XYChartViewer_TitleVersus;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
