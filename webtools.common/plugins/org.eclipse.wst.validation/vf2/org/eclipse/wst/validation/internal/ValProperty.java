package org.eclipse.wst.validation.internal;

import java.util.BitSet;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.wst.validation.internal.plugin.ValidationPlugin;

/**
 * A resource session property, that is used to improve the performance of the validation framework. This is placed
 * on each resource and it enables the framework to quickly determine if the resource needs to be processed.
 * @author karasiuk
 *
 */
public class ValProperty {
/*
 * I did some performance measurements on the IResource#setSessionProperty() and IResource#getSessionProperty()
 * methods, and they were very fast. I used a very large workspace (over 17,000) resources, and you could set (or get)
 * a property on all the resources in under 100ms. 
 */
	
	public static final QualifiedName Key = new QualifiedName(ValidationPlugin.PLUGIN_ID, "status"); //$NON-NLS-1$
	
	private int 	_configNumber;
	private BitSet	_configSet;
	
	private int		_validationNumber;
	private BitSet	_validationSet;
	
	public int getConfigNumber() {
		return _configNumber;
	}
	public void setConfigNumber(int configNumber) {
		_configNumber = configNumber;
	}
	public BitSet getConfigSet() {
		return _configSet;
	}
	public void setConfigSet(BitSet configSet) {
		_configSet = configSet;
	}
	public int getValidationNumber() {
		return _validationNumber;
	}
	public void setValidationNumber(int validationNumber) {
		_validationNumber = validationNumber;
	}
	public BitSet getValidationSet() {
		return _validationSet;
	}
	public void setValidationSet(BitSet validationSet) {
		_validationSet = validationSet;
	}
	
}
