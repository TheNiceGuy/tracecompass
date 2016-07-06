/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

/**
 * Visitor used for determining the type of multiple descriptors. In order to
 * get the type of a set of descriptors, it must visit all of them. It uses a
 * bitfield for determining if all visited descriptors share the same type.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DescriptorTypeVisitor implements IDescriptorVisitor {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Enumeration for determining what kind of descriptors is inside.
     */
    public class DescriptorType {
        /**
         * Value when no descriptor has been set.
         */
        public static final int NONE = 1 << 0;
        /**
         * Value when the descriptors share multiple types.
         */
        public static final int MULTIPLE = 1 << 1;
        /**
         * Value when the descriptors are numerical.
         */
        public static final int NUMERICAL = 1 << 2;
        /**
         * Value when the descriptors are durations.
         */
        public static final int DURATION = 1 << 3;
        /**
         * Value when the descriptors are timestamps.
         */
        public static final int TIMESTAMP = 1 << 4;
        /**
         * Value when the descriptors are strings.
         */
        public static final int STRING = 1 << 5;
    }

    private int fType = DescriptorType.NONE;

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void visit(DataChartStringDescriptor<?> desc) {
        setType(DescriptorType.STRING);
    }

    @Override
    public void visit(DataChartNumericalDescriptor<?, ?> desc) {
        setType(DescriptorType.NUMERICAL);
    }

    @Override
    public void visit(DataChartDurationDescriptor<?, ?> desc) {
        setType(DescriptorType.NUMERICAL | DescriptorType.DURATION);
    }

    @Override
    public void visit(DataChartTimestampDescriptor<?, ?> desc) {
        setType(DescriptorType.NUMERICAL | DescriptorType.TIMESTAMP);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    private void setType(int type) {
        if (checkIfType(DescriptorType.NONE)) {
            fType = type;
        } else if (fType != type) {
            fType = DescriptorType.MULTIPLE;
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Method for checking if a certain type is present in the bitfield.
     *
     * @param type
     *            The descriptor type's bitfield
     * @return {@code true} if the type is present, else {@code false}
     * @see DescriptorType
     */
    public boolean checkIfType(int type) {
        return (fType & type) == type;
    }

}
