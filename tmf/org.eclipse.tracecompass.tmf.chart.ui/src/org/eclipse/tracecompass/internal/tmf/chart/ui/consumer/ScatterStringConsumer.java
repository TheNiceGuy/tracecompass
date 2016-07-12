/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IDataResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * This class processes string values in order to create valid data for a
 * scatter chart. It takes a {@link IStringResolver} for mapping values.
 * <p>
 * The current implementation of the scatter chart maps each unique string to an
 * int. It is different than the bar chart because the other one cannot allow
 * multiple Y values on an X value.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ScatterStringConsumer implements IDataConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IStringResolver<Object> fResolver;
    private final BiMap<@Nullable String, Integer> fMap;
    private final List<@Nullable String> fList = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param resolver
     *            The resolver that consumes values
     */
    public ScatterStringConsumer(IDataResolver<?, ?> resolver) {
        fResolver = checkNotNull(IStringResolver.class.cast(resolver));
        fMap = checkNotNull(HashBiMap.create());
    }

    /**
     * Surcharged constructor with a bimap provided.
     *
     * @param resolver
     *            The resolver that consumes values
     * @param map
     *            The bimap to store values
     */
    public ScatterStringConsumer(IDataResolver<?, ?> resolver, BiMap<@Nullable String, Integer> map) {
        fResolver = checkNotNull(IStringResolver.class.cast(resolver));
        fMap = map;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public boolean test(Object obj) {
        return true;
    }

    @Override
    public void accept(@NonNull Object obj) {
        String str = fResolver.getMapper().apply(obj);

        fList.add(str);
        fMap.putIfAbsent(str, fMap.size());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the list of string.
     *
     * @return The list of string
     */
    public List<@Nullable String> getList() {
        return fList;
    }

    /**
     * Accessor that returns the map between strings and numbers used in a
     * scatter chart.
     *
     * @return The map of string
     */
    public BiMap<@Nullable String, Integer> getMap() {
        return fMap;
    }

}
