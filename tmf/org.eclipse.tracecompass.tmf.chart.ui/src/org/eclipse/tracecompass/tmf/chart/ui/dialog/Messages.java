/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui.dialog;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages for the chart package
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.chart.ui.messages"; //$NON-NLS-1$
    /**
     * Title for the selection of data
     */
    public static @Nullable String ChartMakerDialog_AvailableData;
    /**
     * Title for the selection of the chart type
     */
    public static @Nullable String ChartMakerDialog_ChartType;
    /**
     * Title for the enabling logarithmic scale for the X axis
     */
    public static @Nullable String ChartMakerDialog_LogScaleX;
    /**
     * Title for the enabling logarithmic scale for the Y axis
     */
    public static @Nullable String ChartMakerDialog_LogScaleY;
    /**
     * Title for the options group
     */
    public static @Nullable String ChartMakerDialog_Options;
    /**
     * Title for the series creator group
     */
    public static @Nullable String ChartMakerDialog_SeriesCreator;
    /**
     * Title for @Nullable choosing data source for the X axis
     */
    public static @Nullable String ChartMakerDialog_XAxis;
    /**
     * Title for choosing data source for the Y axis
     */
    public static @Nullable String ChartMakerDialog_YAxis;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
