/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.segmentstore.core;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages for the segmentstore
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.segmentstore.core.messages"; //$NON-NLS-1$
    /**
     * The aspect name for the start of the segment
     */
    public static String SegmentStoreDataModel_StartAspect;
    /**
     * The aspect name for the end of the segment
     */
    public static String SegmentStoreDataModel_EndAspect;
    /**
     * The aspect name for the duration of the segment
     */
    public static String SegmentStoreDataModel_DurationAspect;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}