/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.wst.common.componentcore.internal;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.wst.common.componentcore.StructureEdit;
import org.eclipse.wst.common.componentcore.UnresolveableURIException;
import org.eclipse.wst.common.componentcore.internal.impl.PlatformURLModuleConnection;
import org.eclipse.wst.common.componentcore.internal.resources.ComponentHandle;
import org.eclipse.wst.common.internal.emfworkbench.EMFWorkbenchContext;
import org.eclipse.wst.common.internal.emfworkbench.integration.EditModel;

/**
 * 
 * Provides resource life cycle management between an EditModel and a WTP flexible module pattern.
 * <p>
 * ArtifactEditModel provides a framework for managing a set of EMF resources within a unit of work.
 * Management of these resources includes basic services such as loads, saves, and synchronization
 * for the managed resources. ArtifactEditModels are reference counted so that a single instance can
 * be shared by more than one operation or editor.
 * </p>
 * <p>
 * AritfactEditModel utilizes
 * {@see org.eclipse.wst.common.internal.emfworkbench.EMFWorkbenchContext}&nbsp; to manage the life
 * cycle of an EMF {@see org.eclipse.emf.ecore.resource.Resource}&nbsp; in a given EMF
 * {@see org.eclipse.emf.ecore.resource.ResourceSet}. There is one ArtifactEditModel per
 * {@see org.eclipse.wst.common.modulecore.WorkbenchComponent}&nbsp;in each project.
 * </p>
 * 
 * @see org.eclipse.wst.common.internal.emfworkbench.EMFWorkbenchContext
 * @see org.eclipse.emf.ecore.resource.Resource
 * @see org.eclipse.emf.ecore.resource.ResourceSet
 * @see org.eclipse.wst.common.componentcore.StructureEdit
 * @see org.eclipse.wst.common.componentcore.internal.WorkbenchComponent
 * @see org.eclipse.wst.common.componentcore.internal.ComponentResource
 */

public class ArtifactEditModel extends EditModel implements IAdaptable {

	private final URI moduleURI;
	private final IPath modulePath;

	/**
	 * <p>
	 * Creates a ArtifactEditModel instance that uses information from the
	 * <b>org.eclipse.wst.common.emfworkbench.integration.editModelFactory </b> extension point
	 * associated with anEditModelId attached to the project managed by aContext for a specific
	 * module referenced by aModuleURI. Resoures that are not recognized as defined Resources via
	 * the appropriate EditModel extension points will be accessed as read-only.
	 * </p>
	 * <p>
	 * This method is functionally equivalent to:
	 * </p>
	 * <p>
	 * <code>ArtifactEditModel(anEditModelId, aContext, toMakeReadOnly, true, aModuleURI)</code>
	 * </p>
	 * 
	 * @param anEditModelId
	 *            A unique identifier for the EditModel defined by the appropriate
	 *            <b>org.eclipse.wst.common.emfworkbench.integration.editModelFactory </b> extension
	 *            point.
	 * @param aContext
	 *            A valid EMFWorkbenchContext which helps manage the lifecycle of EMF resources for
	 *            a given project.
	 * @param toMakeReadOnly
	 *            True indicates that Resources loaded by the EditModel will not allow
	 *            modifications.
	 * @param aModuleURI
	 *            A fully-qualified URI that conforms to the "module:" format.
	 */

	public ArtifactEditModel(String anEditModelId, EMFWorkbenchContext aContext, boolean toMakeReadOnly, URI aModuleURI) {
		this(anEditModelId, aContext, toMakeReadOnly, true, aModuleURI);
	}

	/**
	 * 
	 * <p>
	 * Creates a ArtifactEditModel instance that uses information from the
	 * <b>org.eclipse.wst.common.emfworkbench.integration.editModelFactory </b> extension point
	 * associated with anEditModelId attached to the project managed by aContext for a specific
	 * module referenced by aModuleURI. Resoures that are not recognized as defined
	 * </p>*
	 * 
	 * @param anEditModelId
	 *            A unique identifier for the EditModel defined by the appropriate
	 *            <b>org.eclipse.wst.common.emfworkbench.integration.editModelFactory </b> extension
	 *            point.
	 * @param aContext
	 *            A valid EMFWorkbenchContext which helps manage the lifecycle of EMF resources for
	 *            a given project.
	 * @param toMakeReadOnly
	 *            True indicates that Resources loaded by the EditModel will not allow
	 *            modifications.
	 * @param toAccessUnknownResourcesAsReadOnly
	 *            True indicates that Resources not recognized by the EditModel be loaded as
	 *            read-only - such those loaded via {@see #getResource(URI)}.
	 * @param aModuleURI
	 *            A fully-qualified URI that conforms to the "module:" format.
	 *  
	 */

	public ArtifactEditModel(String anEditModelId, EMFWorkbenchContext aContext, boolean toMakeReadOnly, boolean toAccessUnknownResourcesAsReadOnly, URI aModuleURI) {
		super(anEditModelId, aContext, toMakeReadOnly, toAccessUnknownResourcesAsReadOnly);
		moduleURI = aModuleURI;
		modulePath = new Path(moduleURI.path());
		processLoadedResources(moduleURI);
	}

	/**
	 * <p>
	 * Accesses resources within the underlying resource set. Takes a standard URI attaches module
	 * protocol and information. This data can be used to determine if the resource is part of a
	 * flexible module structure. If the resource does not exist in the resource set it will try and
	 * load the resource.
	 * </p>
	 * 
	 * @param aUri -
	 *            location of resource
	 * 
	 * @return Resource (@see Resource)
	 */
	public Resource getResource(URI aUri) {
		IPath requestPath = modulePath.append(new Path(aUri.path()));
		URI resourceURI = URI.createURI(PlatformURLModuleConnection.MODULE_PROTOCOL + requestPath.toString());
		return super.getResource(resourceURI);
	}

	/**
	 * <p>
	 * Uses the cached moduleURI to query ModuleCore (@see ModuleCore) for the module type this
	 * ArtifactEditModel represents. Module types can be found in IModuleConstants (@see
	 * IModuleConstants).
	 * </p>
	 * 
	 * <@return string reprentation of a module type i.e. "wst.web">
	 */


	public String getModuleType() {
		String type = null;
		WorkbenchComponent wbModule;
		StructureEdit moduleCore = null;
		try {
			moduleCore = StructureEdit.getStructureEditForRead(StructureEdit.getContainingProject(moduleURI));
			wbModule = moduleCore.findComponentByURI(moduleURI);
			type = wbModule.getComponentType().getComponentTypeId();
		} catch (UnresolveableURIException e) {
			e.printStackTrace();
		} finally {
			if (moduleCore != null)
				moduleCore.dispose();
		}
		return type;
	}
	public ComponentHandle getComponentHandle() {
		ComponentHandle handle = null;
		try {
			handle = ComponentHandle.create(StructureEdit.getContainingProject(moduleURI),moduleURI);
		} catch (UnresolveableURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return handle;
	}

	public URI getModuleURI() {
		return moduleURI;
	}

	/**
	 * <p>
	 * Accesses resoureces within the underlying resource set. Takes a starndard URI attaches module
	 * information. This data can be used to determine if the resource is part of a flexible module
	 * structure.
	 * </p>
	 * 
	 * @param aUri -
	 *            location of resource
	 * 
	 * @return Resource (@see Resource)
	 */

	public Resource getOrCreateResource(URI aUri) {

		IPath requestPath = modulePath.append(new Path(aUri.path()));
		URI resourceURI = URI.createURI(requestPath.toString());
		return super.getOrCreateResource(resourceURI);
	}

	/**
	 * <p>
	 * Overridden to prevent super() implementation, processLoadedResources(aModuleURI) used
	 * instead. (@link processLoadedResources(URI aModuleURI)
	 * </p>
	 * 
	 * @param aUri -
	 *            location of resource
	 */



	protected void processLoadedResources() {
	}

	/**
	 * <p>
	 * Gathers resources from the underlying resource set, determines if interested (@link
	 * processLoadedResources(URI aModuleURI))and request access (@link access(ReferencedResource
	 * aResource))to each resource incrementing the write/read count.
	 * </p>
	 * 
	 * @param aUri -
	 *            location of resource
	 */


	protected void processLoadedResources(URI aModuleURI) {
		List loadedResources = getResourceSet().getResources();
		if (!loadedResources.isEmpty()) {
			processResourcesIfInterrested(loadedResources);
		}
	}

	/**
	 * <p>
	 * Checks to make sure a flexible module structure exist for the resources in the resource set.
	 * This is achieved by querying ModuleCore(@see ModuleCore) for existing
	 * WorkbenchModuleResources (@see WorkbenchModuleResources). If the resource exist it processes
	 * the resource (@link processResource(Resource aResource)). Processing the resource increments
	 * the read/write count.
	 * </p>*
	 * 
	 * @param theResources -
	 *            list of resources to verify flexible module status, and process
	 * @throws UnresolveableURIException
	 *             could not WorkbenchResource with the corresponding URI.
	 */



	protected boolean processResourcesIfInterrested(List theResources) {
		int size = theResources.size();
		Resource resourceToProcess;
		boolean processed = false;
		StructureEdit moduleCore = null;
		try {
			moduleCore = StructureEdit.getStructureEditForRead(StructureEdit.getContainingProject(moduleURI));

			ComponentResource[] relevantModuleResources = null;
			URI aResourceURI = null;
			for (int i = 0; i < size; i++) {
				try {
					resourceToProcess = (Resource) theResources.get(i);
					aResourceURI = resourceToProcess.getURI();
					relevantModuleResources = moduleCore.findResourcesBySourcePath(aResourceURI);
					for (int resourcesIndex = 0; resourcesIndex < relevantModuleResources.length; resourcesIndex++) {
						if (moduleURI.equals(relevantModuleResources[resourcesIndex].getComponent().getHandle())) {
							processResource(resourceToProcess);
							processed = true;
						}
					}

				} catch (UnresolveableURIException uurie) {
				}
			}
		} catch (UnresolveableURIException uurie) {
		} finally {
			if (moduleCore != null)
				moduleCore.dispose();
		}
		return processed;
	}

	/**
	 * 
	 * <p>
	 * Generic way to retrieve containing information, within the platform.
	 * </p>
	 * 
	 * @return instance of the adapterType for this adapter class from adapter factory stored in the
	 *         adapter manager (@see AdapterManager)
	 *  
	 */

	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}
}