/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.segmentstore.core;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages for the segment store core
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.segmentstore.core.messages"; //$NON-NLS-1$
    /**
     * Title for the end of a segment
     */
    public static @Nullable String SegmentStoreDataModel_End;
    /**
     * Title for the length of a segment
     */
    public static @Nullable String SegmentStoreDataModel_Length;
    /**
     * Title for the start of a segment
     */
    public static @Nullable String SegmentStoreDataModel_Start;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
