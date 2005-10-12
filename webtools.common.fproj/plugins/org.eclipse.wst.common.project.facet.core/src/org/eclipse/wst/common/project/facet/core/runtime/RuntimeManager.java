/******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial API and implementation
 ******************************************************************************/

package org.eclipse.wst.common.project.facet.core.runtime;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.common.project.facet.core.runtime.internal.RuntimeManagerImpl;

/**
 * @author <a href="mailto:kosta@bea.com">Konstantin Komissarchik</a>
 */

public final class RuntimeManager 
{
    private RuntimeManager() {}
    
    /**
     * Returns all of the available runtime component types.
     * 
     * @return all of the available runtime component types (element type: 
     *   {@link IRuntimeComponentType})
     */
    
    public static Set getRuntimeComponentTypes()
    {
        return RuntimeManagerImpl.getRuntimeComponentTypes();
    }
    
    /**
     * Determines whether the specified runtime component type exists.
     * 
     * @param id the runtime component type id
     * @return <code>true</code> if the specified runtime component type exists,
     *   <code>false</code> otherwise
     */
    
    public static boolean isRuntimeComponentTypeDefined( final String id )
    {
        return RuntimeManagerImpl.isRuntimeComponentTypeDefined( id );
    }
    
    /**
     * Returns the {@see IRuntimeComponentType} object corresponding to the
     * specified runtime component type id.
     * 
     * @param id the runtime componenet type id
     * @return the {@see IRuntimeComponentType} object corresponding to the
     *   specified runtime componenet type id
     * @throws IllegalArgumentException if the runtime component type id is not
     *   recognized
     */
    
    public static IRuntimeComponentType getRuntimeComponentType( final String id )
    {
        return RuntimeManagerImpl.getRuntimeComponentType( id );
    }
    
    /**
     * Returns all of the defined runtimes.
     * 
     * @return all of the defined runtimes (element type: {@link IRuntime})
     */
    
    public static Set getRuntimes()
    {
        return RuntimeManagerImpl.getRuntimes();
    }
    
    /**
     * Determines whether the specified runtime has been defined.
     * 
     * @param name the runtime name
     * @return <code>true</code> if the specified runtime is defined, 
     *   <code>false</code> otherwise
     */
    
    public static boolean isRuntimeDefined( final String name )
    {
        return RuntimeManagerImpl.isRuntimeDefined( name );
    }
    
    /**
     * Returns the runtime corresponding to the specified name.
     * 
     * @param name the runtime name
     * @return the runtime corresponding to the specified name
     * @throws IllegalArgumentException if the runtime name is not recognized
     */
    
    public static IRuntime getRuntime( final String name )
    {
        return RuntimeManagerImpl.getRuntime( name );
    }
    
    /**
     * Defines a new runtime.
     * 
     * @param name the runtime name
     * @param components the list of runtime componenets (element type: 
     *   {@see IRuntimeComponent})
     * @param properties the runtime properties (key type: {@see String}, value
     *   type: {@see String})
     * @return the new runtime
     */
    
    public static IRuntime defineRuntime( final String name,
                                          final List components,
                                          final Map properties )
    {
        return RuntimeManagerImpl.defineRuntime( name, components, properties );
    }
    
    /**
     * Deletes the runtime from the registry.
     * 
     * @param runtime the runtime to delete
     */
    
    public static void deleteRuntime( final IRuntime runtime )
    {
        RuntimeManagerImpl.deleteRuntime( runtime );
    }
    
    /**
     * Creates a new runtime componenet. This method is intended to be used in
     * conjunction with the {@see defineRuntime(String,List,Map)} method.
     * 
     * @param rcv the runtime component version
     * @param properties the runtime component properties (key type:
     *   {@see String}, value type: {@see String})
     * @return the new runtime component
     */
    
    public static IRuntimeComponent createRuntimeComponent( final IRuntimeComponentVersion rcv,
                                                            final Map properties )
    {
        return RuntimeManagerImpl.createRuntimeComponent( rcv, properties );
    }
    
    /**
     * Caution: experimental.
     */
    
    public static void bridge()
    {
        RuntimeManagerImpl.bridge();
    }
    
}
