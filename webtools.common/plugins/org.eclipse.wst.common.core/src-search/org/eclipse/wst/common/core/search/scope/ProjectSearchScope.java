/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.common.core.search.scope;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class ProjectSearchScope extends SearchScopeImpl
{

	/**
	 * Creates a scope that ecloses workspace path and eclosing project
	 * 
	 * @param workspacePath -
	 *            path to the resource in the workspace, e.g.
	 *            /MyProject/MyFile.xml
	 */
	public ProjectSearchScope(IPath workspacePath)
	{
		super();
		initialize(workspacePath);

	}

	protected void initialize(IPath workspacePath)
	{
		IResource resource = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(workspacePath);
		if (resource != null)
		{
			IProject project = resource.getProject();
			traverseContainer(project);
		}
	}

}
