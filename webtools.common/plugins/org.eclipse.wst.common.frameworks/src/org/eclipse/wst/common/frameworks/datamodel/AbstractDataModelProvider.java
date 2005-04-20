/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.common.frameworks.datamodel;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * A default implementation of <code>IDataModelProvider</code>.
 * </p>
 * 
 * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider
 * @since 1.0
 */
public abstract class AbstractDataModelProvider implements IDataModelProvider {

	/**
	 * <p>
	 * The IDataModel for this provider.
	 * </p>
	 */
	protected IDataModel model = null;

	/**
	 * <p>
	 * A utility method for combining property arrays.
	 * </p>
	 * 
	 * @param props1
	 *            a first property array
	 * @param props2
	 *            a second property array
	 * @return a new property array containing the contents of the first and second property arrays.
	 */
	protected static String[] combineProperties(String[] props1, String[] props2) {
		String[] properties = new String[props1.length + props2.length];
		System.arraycopy(props1, 0, properties, 0, props1.length);
		System.arraycopy(props2, 0, properties, props1.length, props2.length);
		return properties;
	}

	/**
	 * @see IDataModelProvider#init()
	 */
	public void init() {
	}

	/**
	 * @see IDataModelProvider#setDataModel(IDataModel)
	 */
	public final void setDataModel(IDataModel dataModel) {
		this.model = dataModel;
	}

	/**
	 * @see IDataModelProvider#getDataModel()
	 */
	public final IDataModel getDataModel() {
		return model;
	}


	/**
	 * @see IDataModelProvider#propertySet(String, Object)
	 */
	public boolean propertySet(String propertyName, Object propertyValue) {
		return true;
	}

	/**
	 * @see IDataModelProvider#getDefaultProperty(String)
	 */
	public Object getDefaultProperty(String propertyName) {
		return null;
	}

	/**
	 * @see IDataModelProvider#isPropertyEnabled(String)
	 */
	public boolean isPropertyEnabled(String propertyName) {
		return true;
	}

	/**
	 * @see IDataModelProvider#validate(String)
	 */
	public IStatus validate(String name) {
		return null;
	}

	/**
	 * @see IDataModelProvider#getPropertyDescriptor(String)
	 */
	public DataModelPropertyDescriptor getPropertyDescriptor(String propertyName) {
		return null;
	}

	/**
	 * @see IDataModelProvider#getValidPropertyDescriptors(String)
	 */
	public DataModelPropertyDescriptor[] getValidPropertyDescriptors(String propertyName) {
		return null;
	}

	/**
	 * @see IDataModelProvider#getExtendedContext()
	 */
	public List getExtendedContext() {
		return null;
	}

	/**
	 * @see IDataModelProvider#getDefaultOperation()
	 */
	public IDataModelOperation getDefaultOperation() {
		return null;
	}

	/**
	 * @see IDataModelProvider#getID()
	 */
	public String getID() {
		return this.getClass().getName();
	}

	/**
	 * @see IDataModelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * <p>
	 * Convenience method for getting a property from the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @return the property value
	 * 
	 * @see IDataModel#getProperty(String)
	 */
	protected final Object getProperty(String propertyName) {
		return model.getProperty(propertyName);
	}

	/**
	 * <p>
	 * Convenience method for setting a property on the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @param propertyValue
	 *            the property value
	 * 
	 * @see IDataModel#setProperty(String, Object)
	 */
	protected final void setProperty(String propertyName, Object propertyValue) {
		model.setProperty(propertyName, propertyValue);
	}

	/**
	 * <p>
	 * Convenience method for getting a boolean property from the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @return the boolean value of the property
	 * 
	 * @see IDataModel#getBooleanProperty(String)
	 */
	protected final boolean getBooleanProperty(String propertyName) {
		return model.getBooleanProperty(propertyName);
	}

	/**
	 * <p>
	 * Convenience method for setting a boolean property on the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @param propertyValue
	 *            the boolean property value
	 * 
	 * @see IDataModel#setBooleanProperty(String, boolean)
	 */
	protected final void setBooleanProperty(String propertyName, boolean propertyValue) {
		model.setBooleanProperty(propertyName, propertyValue);
	}

	/**
	 * <p>
	 * Convenience method for getting an int property from the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @return the int value of the property
	 * 
	 * @see IDataModel#getIntProperty(String)
	 */
	protected final int getIntProperty(String propertyName) {
		return model.getIntProperty(propertyName);
	}

	/**
	 * <p>
	 * Convenience method for setting an int property on the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @param propertyValue
	 *            the int property value
	 * 
	 * @see IDataModel#setIntProperty(String, int)
	 */
	protected final void setIntProperty(String propertyName, int propertyValue) {
		model.setIntProperty(propertyName, propertyValue);
	}

	/**
	 * <p>
	 * Convenience method for getting a String property from the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @return the String value of the property
	 * 
	 * @see IDataModel#getStringProperty(String)
	 */
	public final String getStringProperty(String propertyName) {
		return model.getStringProperty(propertyName);
	}

	/**
	 * <p>
	 * Convenience method for checking if a property is set from the backing IDataModel.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name
	 * @return <code>true</code> if the property is set, <code>false</code> otherwise.
	 */
	protected final boolean isPropertySet(String propertyName) {
		return model.isPropertySet(propertyName);
	}
}
