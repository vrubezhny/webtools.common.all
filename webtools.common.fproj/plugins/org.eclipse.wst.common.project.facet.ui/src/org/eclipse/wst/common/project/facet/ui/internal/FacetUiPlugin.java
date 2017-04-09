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

package org.eclipse.wst.common.project.facet.ui.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author <a href="mailto:kosta@bea.com">Konstantin Komissarchik</a>
 */

public final class FacetUiPlugin 

    extends AbstractUIPlugin 
    
{
    public static final String PLUGIN_ID 
        = "org.eclipse.wst.common.project.facet.ui";
    
    private static FacetUiPlugin plugin;
    
    public FacetUiPlugin() 
    {
        super();
        plugin = this;
    }
    
    public static FacetUiPlugin getInstance()
    {
        return plugin;
    }
    
    public static void log( final Exception e )
    {
        final ILog log = getInstance().getLog();
        final String msg = e.getMessage();
        
        log.log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e ) );
    }
    
}
