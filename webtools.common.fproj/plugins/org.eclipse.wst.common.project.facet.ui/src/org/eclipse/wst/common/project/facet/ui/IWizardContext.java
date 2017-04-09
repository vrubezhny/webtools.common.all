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

package org.eclipse.wst.common.project.facet.ui;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action.Type;

/**
 * @author <a href="mailto:kosta@bea.com">Konstantin Komissarchik</a>
 */

public interface IWizardContext 
{
    String getProjectName();
    
    Set getSelectedProjectFacets();
    
    boolean isProjectFacetSelected( IProjectFacetVersion fv );
    
    Set getActions();
    
    Action getAction( Action.Type type,
                      IProjectFacetVersion f );
    
	Object getConfig(IProjectFacetVersion fv, Type type, String pjname) throws CoreException;
    
}
