/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.type;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Base class for data types allowed in LAMI analysis scripts JSON output.
 *
 * @author Alexandre Montplaisir
 * @author Philippe Proulx
 */
public abstract class AbstractData {

    /**
     * Enum of all the valid data types
     */
    @SuppressWarnings("javadoc")
    public enum DataType {

        /* Generic JSON types */
        STRING("string", "Value", false, null, DataString.class), //$NON-NLS-1$ //$NON-NLS-2$
        INT("int", "Value", true, null, DataInteger.class); //$NON-NLS-1$ //$NON-NLS-2$
/*        FLOAT("float", "Value", true, null, LamiNumber.class), //$NON-NLS-1$ //$NON-NLS-2$
        NUMBER("number", "Value", true, null, LamiNumber.class), //$NON-NLS-1$ //$NON-NLS-2$
        BOOL("bool", "Value", false, null, LamiBoolean.class), //$NON-NLS-1$ //$NON-NLS-2$
*/
        /* Lami-specific data types */
/*        RATIO("ratio", "Ratio", true, "%", LamiRatio.class), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        TIMESTAMP("timestamp", "Timestamp", true, "ns", LamiTimestamp.class), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        TIME_RANGE("time-range", "Time range", true, null, LamiTimeRange.class), //$NON-NLS-1$ //$NON-NLS-2$
        DURATION("duration", "Duration", true, "ns", LamiDuration.class), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SIZE("size", "Size", true, Messages.LamiData_UnitBytes, LamiSize.class), //$NON-NLS-1$ //$NON-NLS-2$
        BITRATE("bitrate", "Bitrate", true, Messages.LamiData_UnitBitsPerSecond, LamiBitrate.class), //$NON-NLS-1$ //$NON-NLS-2$
        SYSCALL("syscall", "System call", false, null, LamiSystemCall.class), //$NON-NLS-1$ //$NON-NLS-2$
        PROCESS("process", "Process", false, null, LamiProcess.class), //$NON-NLS-1$ //$NON-NLS-2$
        PATH("path", "Path", false, null, LamiPath.class), //$NON-NLS-1$ //$NON-NLS-2$
        FD("fd", "File descriptor", false, null, LamiFileDescriptor.class), //$NON-NLS-1$ //$NON-NLS-2$
        IRQ("irq", "IRQ", false, null, LamiIRQ.class), //$NON-NLS-1$ //$NON-NLS-2$
        CPU("cpu", "CPU", false, null, LamiCPU.class), //$NON-NLS-1$ //$NON-NLS-2$
        DISK("disk", "Disk", false, null, LamiDisk.class), //$NON-NLS-1$ //$NON-NLS-2$
        PART("part", "Disk partition", false, null, LamiDiskPartition.class), //$NON-NLS-1$ //$NON-NLS-2$
        NETIF("netif", "Network interface", false, null, LamiNetworkInterface.class), //$NON-NLS-1$ //$NON-NLS-2$
        UNKNOWN("unknown", "Value", false, null, LamiUnknown.class), //$NON-NLS-1$ //$NON-NLS-2$
        MIXED("mixed", "Value", false, null, null); //$NON-NLS-1$ //$NON-NLS-2$
*/
        private final String fName;
        private final String fTitle;
        private final boolean fIsContinuous;
        private final @Nullable String fUnits;
        private final @Nullable Class<?> fClass;

        private DataType(String name, String title, boolean isContinous, @Nullable String units, @Nullable Class<?> cls) {
            fName = name;
            fTitle = title;
            fIsContinuous = isContinous;
            fUnits = units;
            fClass = cls;
        }

        /**
         * Indicates if this data type represents a continuous numerical value.
         *
         * For example, time or bitrates are continuous values, but CPU or IRQ
         * numbers are not (you can't have CPU 1.5!)
         *
         * @return If this aspect is continuous
         */
        public boolean isContinuous() {
            return fIsContinuous;
        }

        /**
         * Get the units of this data type, if any.
         *
         * @return The units, or <code>null</code> if there are no units
         */
        public @Nullable String getUnits() {
            return fUnits;
        }

        /**
         * Get the name of this data type.
         *
         * @return The data type's name
         */
        public String getName() {
            return fName;
        }

        /**
         * The default title for columns containing these units.
         *
         * @return The data type's column title
         */
        public String getTitle() {
            return fTitle;
        }

        /**
         * Get the date type enum element from its implementation Class.
         *
         * @param cls
         *            The data type class
         * @return The data type
         */
        public static @Nullable DataType fromClass(Class<? extends AbstractData> cls) {
            for (DataType type : DataType.values()) {
                if (cls.equals(type.fClass)) {
                    return type;
                }
            }

            return null;
        }
    }

    @Override
    public abstract @Nullable String toString();
}
