/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.data;

import java.util.Collection;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor.DescriptorType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;

/**
 * This class keeps informations about a group of descriptors.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DescriptorsInformation {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final boolean fAreDescriptorsNumerical;
    private final boolean fAreDescriptorsDuration;
    private final boolean fAreDescriptorsTimestamp;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param descriptors
     *            A stream of descriptors to check
     */
    public DescriptorsInformation(Collection<IDataChartDescriptor<?, ?>> descriptors) {
        /* Visit each descriptor for checking if they share the same type */
        DescriptorTypeVisitor visitor = new DescriptorTypeVisitor();
        descriptors.forEach(desc -> desc.accept(visitor));

        /* Make sure there was at least one descriptor */
        if (visitor.checkIfType(DescriptorType.NONE)) {
            throw new IllegalArgumentException("No descriptor were given."); //$NON-NLS-1$
        }

        /* Make sure each descriptor have the same type */
        if (visitor.checkIfType(DescriptorType.MULTIPLE)) {
            throw new IllegalArgumentException("Each descriptor must be the same type."); //$NON-NLS-1$
        }

        /* Check what are the type of the descriptors */
        if (visitor.checkIfType(DescriptorType.NUMERICAL)) {
            fAreDescriptorsNumerical = true;

            if (visitor.checkIfType(DescriptorType.DURATION)) {
                fAreDescriptorsDuration = true;
                fAreDescriptorsTimestamp = false;
            } else if (visitor.checkIfType(DescriptorType.TIMESTAMP)) {
                fAreDescriptorsDuration = false;
                fAreDescriptorsTimestamp = true;
            } else {
                fAreDescriptorsDuration = false;
                fAreDescriptorsTimestamp = false;
            }
        } else {
            fAreDescriptorsNumerical = false;
            fAreDescriptorsDuration = false;
            fAreDescriptorsTimestamp = false;
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns whether all descriptors are numericals or not.
     *
     * @return A boolean
     */
    public boolean areNumerical() {
        return fAreDescriptorsNumerical;
    }

    /**
     * Accessor that returns whether all descriptors are time durations or not.
     *
     * @return A boolean
     */
    public boolean areDuration() {
        return fAreDescriptorsDuration;
    }

    /**
     * Accessor that returns whether all descriptors are timestamps or not.
     *
     * @return A boolean
     */
    public boolean areTimestamp() {
        return fAreDescriptorsTimestamp;
    }

}
