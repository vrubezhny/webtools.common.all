/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.validation.internal;

import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.validation.core.IMessage;
import org.eclipse.wst.validation.plugin.ValidationPlugin;

import com.ibm.wtp.common.logger.LogEntry;
import com.ibm.wtp.common.logger.proxy.Logger;

/**
 * This class must be called only by the validation framework.
 * 
 * This singleton interacts with the eclipse workbench's Task list. TaskListUtility adds and removes
 * tasks from the list.
 * 
 * This class must not be called outside of an IWorkspaceRunnable or IRunnableWithProgress. Many
 * resource deltas can be generated by the methods in this class.
 */
public class TaskListUtility implements ConfigurationConstants {
	protected static final int DEPTH_INFINITE = IResource.DEPTH_INFINITE;
	protected static final int DEPTH_ZERO = IResource.DEPTH_ZERO;
	protected static final String VALIDATION_MARKER_TARGETOBJECT = "targetObject"; //$NON-NLS-1$
	private final static IMarker[] NO_MARKERS = new IMarker[0];

	public static IWorkspaceRoot getRoot() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root;
	}

	/**
	 * This method is here for use by the SABER validator's reporter instance ONLY. Do not use. See
	 * defect 260144 for details.
	 */
	public static IMarker setPriority(IMarker item, int priority) throws CoreException {
		Map attrib = item.getAttributes();
		attrib.put(IMarker.PRIORITY, new Integer(priority));
		item.setAttributes(attrib);
		return item;
	}
	
	/**
	 * This method adds a message to a resource in the task list.
	 */
	public static IMarker addTask(String pluginId, IResource resource, String location, String messageId, String message, int markerType, String markerName, String targetObjectName, String groupName, int offset, int length) throws CoreException {
		if ((message == null) || (resource == null)) {
			return null;
		}

		int severity = getSeverity(markerType);

		// Allow duplicate entries in the task list.
		// Prior to a full validation, the validation framework will remove all messages owned
		// by a validator before it is executed.
		// Prior to an incremental validation, the validation framework will remove all messages,
		// on each of the changed resources, owned by a validator before it is invoked.
		// 
		// It is up to the validator to make sure that it is not adding the same message
		// in more than one place, and also to clear out any old messages which are not cleared
		// by the validation framework.
		IMarker item = null;
		if(markerName != null && markerName.length() >0 )
			 item = resource.createMarker(markerName); // add a validation marker
		else
		     item = resource.createMarker(VALIDATION_MARKER); // add a validation marker

		// For performance reasons, replace the multiple setAttribute
		// calls above with a single setAttributes call.
		boolean offsetSet = ((offset != IMessage.OFFSET_UNSET) && (length != IMessage.OFFSET_UNSET));
		int size = (offsetSet) ? 10 : 8; // add CHAR_START, CHAR_END only if the offset is set. If
		// the offset is set, it takes precendence over the line
		// number. (eclipse's rule, not mine.)
		String[] attribNames = new String[size];
		Object[] attribValues = new Object[size];

		// Very first thing, add the owner. That way, if the code dies
		// before things are persisted, hopefully this marker will be persisted.
		// Hopefully, eclipse WILL persist this field, as requested.
		attribNames[0] = VALIDATION_MARKER_OWNER;
		attribValues[0] = pluginId;
		attribNames[1] = VALIDATION_MARKER_SEVERITY; // this validation severity is stored, in
		// addition to the marker severity, to enable
		// more than one severity of message to be
		// displayed. e.g. ERROR | WARNING (using
		// binary OR). The IMarker constants are
		// regular decimal constants.
		attribValues[1] = new Integer(markerType);
		attribNames[2] = VALIDATION_MARKER_TARGETOBJECT; // to distinguish between messages which
		// are registered on an IResource, but
		// against different target objects
		attribValues[2] = ((targetObjectName == null) ? "" : targetObjectName); //$NON-NLS-1$
		attribNames[3] = VALIDATION_MARKER_GROUP;
		attribValues[3] = ((groupName == null) ? "" : groupName); //$NON-NLS-1$
		attribNames[4] = IMarker.MESSAGE;
		attribValues[4] = message;
		attribNames[5] = VALIDATION_MARKER_MESSAGEID;
		attribValues[5] = messageId;

		attribNames[6] = IMarker.SEVERITY; // IMarker.SEVERITY_ERROR, IMarker.SEVERITY_WARNING,
		// IMarker.SEVERITY_INFO
		attribValues[6] = new Integer(severity);
		try {
			// If the location is a line number, store it as a line number
			Integer lineNumber = Integer.valueOf(location);
			attribNames[7] = IMarker.LINE_NUMBER;
			attribValues[7] = lineNumber;
		} catch (NumberFormatException exc) {
			// Otherwise, store it as a text location
			attribNames[7] = IMarker.LOCATION;
			attribValues[7] = location;
		}

		if (offsetSet) {
			attribNames[8] = IMarker.CHAR_START;
			attribValues[8] = new Integer(offset);
			attribNames[9] = IMarker.CHAR_END;
			attribValues[9] = new Integer(offset + length);
		}

		item.setAttributes(attribNames, attribValues);

		return item;
	}

	/**
	 * This method adds a message to a resource in the task list.
	 */
	public static IMarker addTask(String pluginId, IResource resource, String location, String messageId, String message, int markerType, String targetObjectName, String groupName, int offset, int length) throws CoreException {
		if ((message == null) || (resource == null)) {
			return null;
		}

		int severity = getSeverity(markerType);

		// Allow duplicate entries in the task list.
		// Prior to a full validation, the validation framework will remove all messages owned
		// by a validator before it is executed.
		// Prior to an incremental validation, the validation framework will remove all messages,
		// on each of the changed resources, owned by a validator before it is invoked.
		// 
		// It is up to the validator to make sure that it is not adding the same message
		// in more than one place, and also to clear out any old messages which are not cleared
		// by the validation framework.
		IMarker item = resource.createMarker(VALIDATION_MARKER); // add a validation marker

		// For performance reasons, replace the multiple setAttribute
		// calls above with a single setAttributes call.
		boolean offsetSet = ((offset != IMessage.OFFSET_UNSET) && (length != IMessage.OFFSET_UNSET));
		int size = (offsetSet) ? 10 : 8; // add CHAR_START, CHAR_END only if the offset is set. If
		// the offset is set, it takes precendence over the line
		// number. (eclipse's rule, not mine.)
		String[] attribNames = new String[size];
		Object[] attribValues = new Object[size];

		// Very first thing, add the owner. That way, if the code dies
		// before things are persisted, hopefully this marker will be persisted.
		// Hopefully, eclipse WILL persist this field, as requested.
		attribNames[0] = VALIDATION_MARKER_OWNER;
		attribValues[0] = pluginId;
		attribNames[1] = VALIDATION_MARKER_SEVERITY; // this validation severity is stored, in
		// addition to the marker severity, to enable
		// more than one severity of message to be
		// displayed. e.g. ERROR | WARNING (using
		// binary OR). The IMarker constants are
		// regular decimal constants.
		attribValues[1] = new Integer(markerType);
		attribNames[2] = VALIDATION_MARKER_TARGETOBJECT; // to distinguish between messages which
		// are registered on an IResource, but
		// against different target objects
		attribValues[2] = ((targetObjectName == null) ? "" : targetObjectName); //$NON-NLS-1$
		attribNames[3] = VALIDATION_MARKER_GROUP;
		attribValues[3] = ((groupName == null) ? "" : groupName); //$NON-NLS-1$
		attribNames[4] = IMarker.MESSAGE;
		attribValues[4] = message;
		attribNames[5] = VALIDATION_MARKER_MESSAGEID;
		attribValues[5] = messageId;

		attribNames[6] = IMarker.SEVERITY; // IMarker.SEVERITY_ERROR, IMarker.SEVERITY_WARNING,
		// IMarker.SEVERITY_INFO
		attribValues[6] = new Integer(severity);
		try {
			// If the location is a line number, store it as a line number
			Integer lineNumber = Integer.valueOf(location);
			attribNames[7] = IMarker.LINE_NUMBER;
			attribValues[7] = lineNumber;
		} catch (NumberFormatException exc) {
			// Otherwise, store it as a text location
			attribNames[7] = IMarker.LOCATION;
			attribValues[7] = location;
		}

		if (offsetSet) {
			attribNames[8] = IMarker.CHAR_START;
			attribValues[8] = new Integer(offset);
			attribNames[9] = IMarker.CHAR_END;
			attribValues[9] = new Integer(offset + length);
		}

		item.setAttributes(attribNames, attribValues);

		return item;
	}

	/**
	 * Given one of the SeverityEnum severities, return the IMarker severity int that is its
	 * equivalent.
	 * 
	 * This method was made public for the SaberReporter. No one other than TaskListUtility, or the
	 * SaberReporter, should use this method!
	 *  
	 */
	private static int getSeverity(int severityEnumValue) {
		switch (severityEnumValue) {
			case (IMessage.HIGH_SEVERITY) : {
				return IMarker.SEVERITY_ERROR;
			}

			case (IMessage.LOW_SEVERITY) : {
				return IMarker.SEVERITY_INFO;
			}

			case (IMessage.NORMAL_SEVERITY) : {
				return IMarker.SEVERITY_WARNING;
			}

			case (IMessage.ALL_MESSAGES) :
			case (IMessage.ERROR_AND_WARNING) :
			default : {
				// assume it's a warning.
				return IMarker.SEVERITY_WARNING;
			}
		}
	}

	private static int getDepth(IResource resource) {
		if (resource instanceof IProject) {
			return DEPTH_INFINITE; // DEPTH_INFINITE means get this project's markers, and the
			// markers belonging to the project's children.
		} else if (resource instanceof IWorkspaceRoot) {
			// Needed for the ValidationMigrator when it checks for orphan tasks.
			return DEPTH_INFINITE; // DEPTH_INFINITE means get all of the markers in the workspace
		}

		return DEPTH_ZERO; // DEPTH_ZERO means just this resource, not its children
	}

	public static IMarker[] getValidationTasks(int severity, IProject project) {
		// DEPTH_INFINITE means get this project's markers, and the markers
		// belonging to the project's children.
		return getValidationTasks(project, severity);
	}

	public static IMarker[] getValidationTasks(IResource resource, int severity) {
		return getValidationTasks(resource, severity, getDepth(resource));
	}

	/**
	 * Return true if the marker is owned by the ownerId.
	 */
	public static boolean isOwner(IMarker marker, String ownerId) {
		try {
			Object owner = marker.getAttribute(VALIDATION_MARKER_OWNER);
			if ((owner == null) || !(owner instanceof String)) {
				// The ValidationMigrator will remove any "unowned" validation markers.
				return false;
			}

			return ((String) owner).equals(ownerId);
		} catch (CoreException exc) {
			Logger logger = ValidationPlugin.getPlugin().getMsgLogger();
			if (logger.isLoggingLevel(Level.SEVERE)) {
				LogEntry entry = ValidationPlugin.getLogEntry();
				entry.setSourceID("TaskListUtility.isOwner(IMarker, ownerId)"); //$NON-NLS-1$
				entry.setTargetException(exc);
				logger.write(Level.SEVERE, entry);
			}
			return false;
		}
	}

	private static IMarker[] getValidationTasks(IResource resource, int severity, int depth) {
		IMarker[] tempMarkers = null;
		int validCount = 0;
		try {
			IMarker[] allMarkers = null;
			try {
				allMarkers = resource.findMarkers(VALIDATION_MARKER, true, depth); // false means
				// only consider
				// PROBLEM_MARKER,
				// not variants
				// of
				// PROBLEM_MARKER.
				// Since addTask
				// only adds
				// PROBLEM_MARKER,
				// we don't need
				// to consider
				// its subtypes.
			} catch (CoreException exc) {
				Logger logger = ValidationPlugin.getPlugin().getMsgLogger();
				if (logger.isLoggingLevel(Level.SEVERE)) {
					LogEntry entry = ValidationPlugin.getLogEntry();
					entry.setSourceID("TaskListUtility.getValidationTasks(IResource, int)"); //$NON-NLS-1$
					entry.setTargetException(exc);
					logger.write(Level.SEVERE, entry);
				}
				return NO_MARKERS;
			}

			// Now filter in the markers, based on severity type.
			if (allMarkers.length != 0) {
				tempMarkers = new IMarker[allMarkers.length];
				for (int i = 0; i < allMarkers.length; i++) {
					IMarker marker = allMarkers[i];
					Integer filterSeverity = (Integer) marker.getAttribute(VALIDATION_MARKER_SEVERITY);
					if (filterSeverity == null) {
						// odd...marker wasn't created correctly. How could this happen?
						// Default to the current severity and add it to the list.
						try {
							marker.setAttribute(IMarker.SEVERITY, getSeverity(severity));
						} catch (CoreException exc) {
							Logger logger = ValidationPlugin.getPlugin().getMsgLogger();
							if (logger.isLoggingLevel(Level.SEVERE)) {
								LogEntry entry = ValidationPlugin.getLogEntry();
								entry.setSourceID("TaskListUtility.getValidationTasks(int, IResource, int)"); //$NON-NLS-1$
								entry.setTargetException(exc);
								logger.write(Level.SEVERE, entry);
							}
							continue;
						} catch (Throwable exc) {
							Logger logger = ValidationPlugin.getPlugin().getMsgLogger();
							if (logger.isLoggingLevel(Level.SEVERE)) {
								LogEntry entry = ValidationPlugin.getLogEntry();
								entry.setSourceID("TaskListUtility.getValidationTasks(int, IResource, int)"); //$NON-NLS-1$
								entry.setTargetException(exc);
								logger.write(Level.SEVERE, entry);
							}
							continue;
						}
					} else if ((severity & filterSeverity.intValue()) == 0) {
						continue;
					}
					tempMarkers[validCount++] = marker;
				}
			}
		} catch (CoreException exc) {
			Logger logger = ValidationPlugin.getPlugin().getMsgLogger();
			if (logger.isLoggingLevel(Level.SEVERE)) {
				LogEntry entry = ValidationPlugin.getLogEntry();
				entry.setSourceID("TaskListUtility.getValidationTasks(int, IResource, int)"); //$NON-NLS-1$
				entry.setTargetException(exc);
				logger.write(Level.SEVERE, entry);
			}
		}

		if (validCount == 0) {
			return NO_MARKERS;
		}

		IMarker[] validMarkers = new IMarker[validCount];
		System.arraycopy(tempMarkers, 0, validMarkers, 0, validCount);
		return validMarkers;
	}

	public static IMarker[] getValidationTasks(IResource resource, String messageOwner) {
		return getValidationTasks(resource, new String[]{messageOwner}, getDepth(resource));
	}

	public static IMarker[] getValidationTasks(IResource resource, String[] messageOwners) {
		return getValidationTasks(resource, messageOwners, getDepth(resource));
	}

	private static IMarker[] getValidationTasks(IResource resource, String[] messageOwners, int depth) {
		IMarker[] markers = getValidationTasks(resource, IMessage.ALL_MESSAGES, depth);
		if (markers.length == 0) {
			return NO_MARKERS;
		}

		IMarker[] temp = new IMarker[markers.length];
		int validCount = 0;
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];

			try {
				Object owner = marker.getAttribute(VALIDATION_MARKER_OWNER);
				if ((owner == null) || !(owner instanceof String)) {
					// The ValidationMigrator will remove any "unowned" validation markers.
					continue;
				}

				for (int j = 0; j < messageOwners.length; j++) {
					String messageOwner = messageOwners[j];
					if (((String) owner).equals(messageOwner)) {
						temp[validCount++] = marker;
						break;
					}
				}
			} catch (CoreException exc) {
				Logger logger = ValidationPlugin.getPlugin().getMsgLogger();
				if (logger.isLoggingLevel(Level.SEVERE)) {
					LogEntry entry = ValidationPlugin.getLogEntry();
					entry.setSourceID("TaskListUtility.getValidationTasks(project, String[])"); //$NON-NLS-1$
					entry.setTargetException(exc);
					logger.write(Level.SEVERE, entry);
				}
				return NO_MARKERS;
			}
		}

		IMarker[] result = new IMarker[validCount];
		System.arraycopy(temp, 0, result, 0, validCount);
		return result;
	}

	/**
	 * This method retrieves all validation tasks from the resource. If depth is INFINITE, child
	 * tasks are returned as well. Only the tasks which are owned by the specified messageOwner, and
	 * apply to the named IMessage's target object (objectName) will be returned.
	 */
	private static IMarker[] getValidationTasks(IResource resource, String[] messageOwner, String objectName, String groupName, int depth) throws CoreException {
		if ((messageOwner == null) || (resource == null)) {
			return NO_MARKERS;
		}

		int validCount = 0;
		IMarker[] validList = null;
		IMarker[] markers = getValidationTasks(resource, messageOwner, depth);
		if (markers != null) {
			validList = new IMarker[markers.length];
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];

				// If more than one target object resolves to the same resource, removing one
				// target's
				// messages should not remove the other target object's messages.
				if (objectName != null) {
					Object targetObject = marker.getAttribute(VALIDATION_MARKER_TARGETOBJECT);
					if ((targetObject == null) || !(targetObject instanceof String) || !(((String) targetObject).equals(objectName))) {
						continue;
					}
				}

				if (groupName != null) {
					Object group = marker.getAttribute(VALIDATION_MARKER_GROUP);
					if ((group == null) || !(group instanceof String) || !(((String) group).equals(groupName))) {
						continue;
					}
				}

				validList[validCount++] = marker;
			}
		}

		if (validCount == 0) {
			return NO_MARKERS;
		}

		IMarker[] result = new IMarker[validCount];
		System.arraycopy(validList, 0, result, 0, validCount);
		return result;
	}

	/**
	 * Remove all validation messages from the resource and its children.
	 */
	public static void removeAllTasks(IResource resource) {
		if (resource == null) {
			return;
		}

		try {
			IMarker[] markers = getValidationTasks(resource, IMessage.ALL_MESSAGES);
			ResourcesPlugin.getWorkspace().deleteMarkers(markers);
		} catch (CoreException exc) {
			// Couldn't remove the task from the task list for some reason...
			Logger logger = ValidationPlugin.getPlugin().getMsgLogger();
			if (logger.isLoggingLevel(Level.SEVERE)) {
				LogEntry entry = ValidationPlugin.getLogEntry();
				entry.setSourceID("WorkbenchMonitor.removeAllMessages(String[], IResource, String)"); //$NON-NLS-1$
				entry.setTargetException(exc);
				logger.write(Level.SEVERE, entry);
			}
		}
	}

	/**
	 * This method removes all tasks from the resource. If the resource is an IProject, all tasks
	 * are also removed from the project's children.
	 */
	public static void removeAllTasks(IResource resource, String[] owners) throws CoreException {
		removeAllTasks(resource, owners, null); // null means remove messages from all target
		// objects
	}

	/**
	 * This method removes all messages from a resource in the task list.
	 */
	public static void removeAllTasks(IResource resource, String owner, String objectName) throws CoreException {
		removeAllTasks(resource, new String[]{owner}, objectName);
	}

	public static void removeAllTasks(IResource resource, String[] owners, String objectName) throws CoreException {
		removeAllTasks(resource, owners, objectName, getDepth(resource));
	}

	protected static void removeAllTasks(IResource resource, String[] owners, String objectName, int depth) throws CoreException {
		removeTaskSubset(resource, owners, objectName, null, depth); // null means no group name
	}

	/**
	 * This method removes a subset of tasks from the project, including child tasks. Every task
	 * which belongs to the group, identified by groupName, will be removed.
	 */
	public static void removeTaskSubset(IResource resource, String[] owners, String objectName, String groupName) throws CoreException {
		removeTaskSubset(resource, owners, objectName, groupName, getDepth(resource));
	}

	/**
	 * This method removes a subset of tasks from the project, including child tasks. Every task
	 * which belongs to the group, identified by groupName, will be removed.
	 */
	protected static void removeTaskSubset(IResource resource, String[] owners, String objectName, String groupName, int depth) throws CoreException {
		if ((owners == null) || (resource == null)) {
			return;
		}

		IMarker[] allTasks = getValidationTasks(resource, owners, objectName, groupName, depth);
		if (allTasks.length > 0) {
			ResourcesPlugin.getWorkspace().deleteMarkers(allTasks);
		}
	}

	/**
	 * This method changes all validator markers which are owned by "from" to make their owner "to".
	 */
	public static void updateOwner(String from, String to) throws CoreException {
		updateOwner(from, to, getRoot());
	}

	/**
	 * This method changes all validator markers on the IResource and its children. All markers
	 * owned by "from" have their owner reassigned to "to".
	 */
	public static void updateOwner(String from, String to, IResource resource) throws CoreException {
		IMarker[] ownedMarkers = getValidationTasks(resource, from);
		if (ownedMarkers == null) {
			return;
		}

		for (int i = 0; i < ownedMarkers.length; i++) {
			IMarker marker = ownedMarkers[i];
			marker.setAttribute(VALIDATION_MARKER_OWNER, to);
		}
	}
}