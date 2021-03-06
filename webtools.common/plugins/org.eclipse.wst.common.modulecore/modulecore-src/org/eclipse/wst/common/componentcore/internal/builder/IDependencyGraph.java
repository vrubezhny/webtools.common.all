/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.common.componentcore.internal.builder;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

/**
 * This graph provides a backward mapping of project component dependencies. It
 * provides a project limited inverse of
 * {@link IVirtualComponent#getReferences()}.
 * 
 * <p>For example:
 * <ul>
 * <li>if the IVirtualComponent for project A has a dependency on the
 * IVirtualComponent for project B, then calling
 * {@link #getReferencingComponents(IProject)} on project B will return project
 * A. </li>
 * <li>if the IVirtualComponent for project A has a dependency on on the
 * IVirtualComponent for a jar in project B, then calling
 * {@link #getReferencingComponents(IProject)} for project B will return project
 * A. This is true even if project B is not defined as an IVirtualComponent.
 * </li>
 * </ul>
 * 
 * Any call to {@link #getReferencingComponents(IProject)} is always expected to
 * be up to date. The only case where a client may need to force an update is if
 * that client is also defining dynamic IVirtualComponent dependencies, i.e. the
 * client is using the org.eclipse.wst.common.modulecore.componentimpl extension
 * point. Only in this case should a client be calling any of
 * {@link #preUpdate()}, {@link #postUpdate()}, or {@link #update(IProject)}
 * 
 */
public interface IDependencyGraph {

	/**
	 * Flag used by {@link #update(IProject, int)} to specify that something has been
	 * modified in a project which has changed the component dependencies.
	 */
	int MODIFIED = 0;

	/**
	 * Flag used by {@link #update(IProject, int)} to specify a project has been
	 * added or opened. This flag should be used as sparingly as possible
	 * because there are performance implications.
	 */
	int ADDED = 1;

	/**
	 * Flag used by {@link #update(IProject, int)} to specify a project has been
	 * removed or closed.
	 */
	int REMOVED = 2;

	/**
	 * The static instance of this graph.
	 */
	IDependencyGraph INSTANCE = DependencyGraphImpl.getInstance();

	/**
	 * Returns the set of component projects referencing the specified target
	 * project.
	 * 
	 * @param targetProject
	 * @return
	 */
	Set<IProject> getReferencingComponents(IProject targetProject);

	/**
	 * If <code>waitForAllUpdates</code> is <code>true</code> this method is
	 * equivalent to {@link #getReferencingComponents(IProject)}. Otherwise this
	 * method will return immediately without waiting for all updates to occur
	 * and potentially returning stale data. This a safer way to call
	 * getReferences to avoid deadlocks. If stale data is returned, then a
	 * subsequent on the same thread can be made, but there may be deadlock risk
	 * depending on what ISchedulingRule is currently held.  A safer reaction
	 * to stale data would be to schedule another job to run in future to try again.
	 * 
	 * @param targetProject
	 * @param waitForAllUpdates
	 * @return
	 */
	IDependencyGraphReferences getReferencingComponents(IProject targetProject, boolean waitForAllUpdates);
		
	/**
	 * Returns <code>true</code> if there are any pending updates.
	 * @return
	 */
	boolean isStale();
	
	/**
	 * Returns a modification stamp. This modification stamp will be different
	 * if the project dependencies ever change.
	 */
	long getModStamp();
	
	void addListener(IDependencyGraphListener listener);
	
	void removeListener(IDependencyGraphListener listener);

	/**
	 * WARNING: this should only be called by implementors of the
	 * org.eclipse.wst.common.modulecore.componentimpl extension point.
	 * 
	 * <p>This method is part of the update API.
	 * 
	 * @see {@link #update(IProject)}
	 */
	void preUpdate();

	/**
	 * WARNING: this should only be called by implementors of the
	 * org.eclipse.wst.common.modulecore.componentimpl extension point.
	 * 
	 * <p>This method is part of the update API.
	 * 
	 * @see {@link #update(IProject)}
	 */
	void postUpdate();

	/**
	 * @deprecated use {@link #update(IProject, int) using the #MODIFIED flag.
	 */
	void update(IProject sourceProject);

	/**
	 * WARNING: this should only be called by implementors of the
	 * org.eclipse.wst.common.modulecore.componentimpl extension point.
	 * 
	 * <p>This method must be called when a resource change is detected which will
	 * affect how dependencies behave. For example, the core IVirtualComponent
	 * framework updates when changes are made to the
	 * .settings/org.eclipse.wst.common.component file changes, and also when
	 * IProjects are added or removed from the workspace. In the case for J2EE,
	 * this occurs when changes are made to the META-INF/MANIFEST.MF file. In
	 * general a call to update should only be made from a fast
	 * {@link IResourceDeltaVisitor}.
	 * 
	 * <p>In order to improve efficiency and avoid unnecessary update processing,
	 * it is necessary to always proceed calls to update() with a call to
	 * preUpdate() and follow with a call to postUpdate() using a try finally
	 * block as follows: 
	 * <pre>
	 * try {
	 *     preUpdate();
	 *     // perform 0 or more update() calls here
	 * } finally {
	 *     IDependencyGraph.INSTANCE.postUpdate();
	 * }    
	 * </pre>
	 * 
	 * Valid updateType flags are {@link #MODIFIED}, {@link #ADDED}, and
	 * {@link #REMOVED}
	 * 
	 */
	void update(IProject sourceProject, int updateType);

}
