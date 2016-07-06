/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics;

import org.eclipse.osgi.util.NLS;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Messages for the segment store statistics
 * @since 1.1.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.messages"; //$NON-NLS-1$
    /**
     * Title of the average
     */
    public static @Nullable String SegStoreStatisticsDataModel_Average;
    /**
     * Title of the count
     */
    public static @Nullable String SegStoreStatisticsDataModel_Count;
    /**
     * Title of the maximum latencies
     */
    public static @Nullable String SegStoreStatisticsDataModel_Maximum;
    /**
     * Title of the minimum latencies
     */
    public static @Nullable String SegStoreStatisticsDataModel_Minimum;
    /**
     * Title of the standard deviation
     */
    public static @Nullable String SegStoreStatisticsDataModel_StandardDeviation;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
