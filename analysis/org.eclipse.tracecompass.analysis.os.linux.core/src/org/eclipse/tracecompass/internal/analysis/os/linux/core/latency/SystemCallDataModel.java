/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartStringDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;

/**
 * This class provides a data model for the system call analysis.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 * @since 2.0
 */
public class SystemCallDataModel extends SegmentStoreDataModel<SystemCall> {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public SystemCallDataModel() {
        super();

        String syscall = checkNotNull(Messages.SegmentAspectName_SystemCall);
        getDataDescriptors().add(new DataChartStringDescriptor<>(syscall, new NameSource()));
    }

    // ------------------------------------------------------------------------
    // Data resolvers
    // ------------------------------------------------------------------------

    private final class NameSource implements IStringResolver<SystemCall> {
        @Override
        public Function<SystemCall, @Nullable String> getMapper() {
            return o -> o.getName();
        }
    }

}
