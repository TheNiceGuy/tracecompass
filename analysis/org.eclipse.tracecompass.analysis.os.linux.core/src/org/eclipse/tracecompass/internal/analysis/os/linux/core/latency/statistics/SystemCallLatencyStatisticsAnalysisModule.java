/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatisticsDataModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Analysis module to calculate statistics of a latency analysis
 *
 * @author Bernd Hufmann
 */
public class SystemCallLatencyStatisticsAnalysisModule extends AbstractSegmentStatisticsAnalysis<String> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     *  The analysis module ID
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.latency.statistics.syscall"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private @Nullable SegmentStoreStatisticsDataModel fModel;

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    protected @Nullable String getSegmentType(@NonNull ISegment segment) {
        if (segment instanceof SystemCall) {
            SystemCall syscall = (SystemCall) segment;
            return syscall.getName();
        }
        return null;
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(@NonNull ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, SystemCallLatencyAnalysis.class, SystemCallLatencyAnalysis.ID);
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        boolean ret = super.executeAnalysis(monitor);
        Map<String, SegmentStoreStatistics> segments = getPerSegmentTypeStats();

        if(ret && segments != null) {
            fModel = new SegmentStoreStatisticsDataModel("System Call", segments); //$NON-NLS-1$
        }

        return ret;
    }

    @Override
    public @NonNull Stream<@NonNull String> getSource() {
        return checkNotNull(getPerSegmentTypeStats()).keySet().stream()
                .filter(Objects::nonNull);
    }

    @Override
    public @NonNull List<IDataChartDescriptor<String,?>> getDataDescriptors() {
        return checkNotNull(fModel).getDataDescriptors();
    }

}
