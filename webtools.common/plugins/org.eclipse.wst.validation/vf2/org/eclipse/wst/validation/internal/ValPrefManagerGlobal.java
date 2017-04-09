/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.validation.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.wst.validation.MessageSeveritySetting;
import org.eclipse.wst.validation.ValidationFramework;
import org.eclipse.wst.validation.Validator;
import org.eclipse.wst.validation.Validator.V2;
import org.eclipse.wst.validation.internal.model.FilterGroup;
import org.eclipse.wst.validation.internal.model.GlobalPreferences;
import org.eclipse.wst.validation.internal.plugin.ValidationPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * A class that knows how to manage the global persisted validation settings.
 * @author karasiuk
 */
public class ValPrefManagerGlobal {
	
	/** 
	 * Version of the framework properties.
	 * <ul>
	 * <li>Version 2 - used the filter approach
	 * <li>Version 3 - switched to a difference based approach. (See Bugzilla 224179)
	 * </ul>
	 * 
	 */
	public final static int frameworkVersion = 3;
	
	private List<IValChangedListener> _listeners = new LinkedList<IValChangedListener>();
	private static ValPrefManagerGlobal _me;
	
	private List<Validator> _validators;
	
	private ValPrefManagerGlobal(){}
	
	public static ValPrefManagerGlobal getDefault(){
		if (_me == null)_me = new ValPrefManagerGlobal();
		return _me;
	}
	
	public void addListener(IValChangedListener listener){
		if (_listeners.contains(listener))return;
		_listeners.add(listener);
	}
	
	public void removeListener(IValChangedListener listener){
		_listeners.remove(listener);
	}
	
	private void updateListeners(boolean validationSettingChanged){
		for (IValChangedListener cl : _listeners)cl.validatorsForProjectChanged(null, validationSettingChanged); 
	}
			
	/**
	 * Update the validator filters from the preference store.
	 *  
	 * @param val
	 * 
	 * @return false if there are no preferences, that means that the user has never changed any
	 * of the default settings. Also answer false if there was some sort of error, which essentially
	 * means that the preferences aren't valid for whatever reason.   
	 * 
	 * @deprecated
	 */
//	public boolean loadPreferences(Validator[] val) {
//	
//		try {
//			IEclipsePreferences pref = ValidationFramework.getDefault().getPreferenceStore();
//			if (!pref.nodeExists(PrefConstants.filters))return false;
//		
//			Preferences filters = pref.node(PrefConstants.filters);
//			for (Validator v : val){
//				String id = v.getId();
//				if (filters.nodeExists(id)){
//					Preferences vp = filters.node(id);
//					loadPreferences(v, vp);
//				}
//			}			
//		}
//		catch (Exception e){
//			ValidationPlugin.getPlugin().handleException(e);
//			return false;
//		}
//		
//		return true;
//	}
	
	/**
	 * Answer the v2 validators that have been overridden by the global preferences.
	 */
	public List<Validator> getValidators() throws BackingStoreException {
		List<Validator> vals = _validators;
		if (vals == null){
			vals = loadValidators();
			_validators = vals;
		}
		return vals;
	}
	
	/**
	 * Load the validators from the preference store.
	 * @return the validators that have been overridden by the global references.
	 */
	private List<Validator> loadValidators() throws BackingStoreException {
		LinkedList<Validator> list = new LinkedList<Validator>();
		IEclipsePreferences pref = ValidationFramework.getDefault().getPreferenceStore();
		if (pref.nodeExists(PrefConstants.vals)){
			Preferences vals = pref.node(PrefConstants.vals);
			for (String id : vals.childrenNames()){
				Validator base = ExtensionValidators.instance().getMapV2().get(id);
				Validator v = loadValidator(id, vals, base);
				if (v != null){
					V2 v2 = v.asV2Validator();
					if (v2 != null)v2.setLevel(Validator.Level.Global);					
					list.add(v);
				}
			}
		}
		return list;
	}

	/**
	 * Answer a copy of the validator that has been updated with the given preferences.
	 * 
	 * @param id
	 *            Validator id.
	 * @param valsNode
	 *            The /vals node in the preference store.
	 * @param base
	 *            The base validator that is being customized. This can be null,
	 *            in which case null will be returned.
	 * 
	 * @return A new validator that is a copy of the extension point validator
	 *         with the updates from the preference store.
	 */
	static Validator loadValidator(String id, Preferences valsNode, Validator base) {
		if (base == null)return null;
		
		Preferences vp = valsNode.node(id);
		base = base.copy();
		V2 v2 = base.asV2Validator();

		String global = vp.get(PrefConstants.global, ""); //$NON-NLS-1$
		if (global.length() > 0){
		Global g = new Global(global);
			base.setBuildValidation(g.isBuild());
			base.setManualValidation(g.isManual());
			base.setDelegatingId(g.getDelegating());
		}
		
		if (v2 != null){
			String groups = vp.get(PrefConstants.groups, ""); //$NON-NLS-1$
			if (groups.length() > 0){
				List<FilterGroup> list = new LinkedList<FilterGroup>();
				Deserializer des = new Deserializer(groups);
				while(des.hasNext())list.add(FilterGroup.create(des));
				v2.setGroups(list);
			}
		}					
		return base;
	}

	/**
	 * The only valid way to get the global preferences is through the ValManager.
	 * 
	 * @see ValManager#getGlobalPreferences()
	 */
	public void loadGlobalPreferences(GlobalPreferences gp) {
		IEclipsePreferences pref = ValidationFramework.getDefault().getPreferenceStore();
		gp.setSaveAutomatically(pref.getBoolean(PrefConstants.saveAuto, GlobalPreferences.DefaultAutoSave));
		gp.setDisableAllValidation(pref.getBoolean(PrefConstants.suspend, GlobalPreferences.DefaultSuspend));
		gp.setConfirmDialog(pref.getBoolean(PrefConstants.confirmDialog, GlobalPreferences.DefaultConfirm));
		gp.setOverride(pref.getBoolean(PrefConstants.override, GlobalPreferences.DefaultOverride));
		gp.setVersion(pref.getInt(PrefConstants.frameworkVersion, GlobalPreferences.DefaultFrameworkVersion));
		gp.setStateTimeStamp(pref.getLong(PrefConstants.stateTS, 0));
		
		if (gp.getVersion() != frameworkVersion)migrate(gp.getVersion(), pref);
	}
	
	/**
	 * If necessary migrate the preferences.
	 * @param version The incoming version of the preferences.
	 * @param pref the root of the preference store
	 */
	static void migrate(int version, IEclipsePreferences pref) {
		try {
			boolean update = false;
			if (version == 2){
				if (pref.nodeExists(PrefConstants.filters)){
					pref.node(PrefConstants.filters).removeNode();
					update = true;
				}
				if (pref.nodeExists(PrefConstants.msgs)){
					pref.node(PrefConstants.msgs).removeNode();
					update = true;
				}
			}
			if (update){
				pref.putInt(PrefConstants.frameworkVersion, frameworkVersion);
				pref.flush();
			}
		}
		catch (BackingStoreException e){
			ValidationPlugin.getPlugin().handleException(e);
		}		
	}

	/**
	 * Load the preferences for a validator.
	 * 
	 * @param v the validator that is being built up
	 * @param p the node in the preference tree for the validator, 
	 * 	e.g. /instance/validator-framework-id/filters/validator-id
	 * 
	 * @deprecated
	 */
//	static void loadPreferences(Validator v, Preferences p) throws BackingStoreException {
//		v.setBuildValidation(p.getBoolean(PrefConstants.build, true));
//		v.setManualValidation(p.getBoolean(PrefConstants.manual, true));
//		v.setVersion(p.getInt(PrefConstants.version, 1));
//		v.setDelegatingId(p.get(PrefConstants.delegate, null));
//		
//		Validator.V2 v2 = v.asV2Validator();
//		if (v2 == null)return;
//		if (!p.nodeExists(PrefConstants.groups))return;
//		
//		Preferences groupNode = p.node(PrefConstants.groups);
//		for (String groupName : groupNode.childrenNames()){
//			Preferences group = groupNode.node(groupName);
//			String type = group.get(PrefConstants.type, null);
//			if (type == null)throw new IllegalStateException(ValMessages.ErrGroupNoType);
//			FilterGroup fg = FilterGroup.create(type);
//			if (fg == null)throw new IllegalStateException(NLS.bind(ValMessages.ErrGroupInvalidType, type));
//			v2.add(fg);
//			
//			if (group.nodeExists(PrefConstants.rules)){
//				Preferences ruleNode = group.node(PrefConstants.rules);
//				for (String ruleName : ruleNode.childrenNames()){
//					Preferences rule = ruleNode.node(ruleName);
//					FilterRule fr = FilterRule.create(rule.get(PrefConstants.ruleType, null));
//					if (fr != null){
//						fr.load(rule);
//						fg.add(fr);
//					}
//				}
//			}
//		}		
//	}
	
	/**
	 * Save the validator into the preference store.
	 * 
	 * @param validator
	 *            The validator being saved.
	 * 
	 * @param root
	 *            The top of the preference tree for validators, i.e.
	 *            /instance/validator-framework-id/vals for workspace validators
	 *            and /vals for project validators.
	 *            
	 * @param baseValidators
	 *            A map of the validators that are one level higher in the
	 *            storage hierarchy. So if we are updating the preference page
	 *            validators, then this map would be the extension point
	 *            validators. If we are updating a project's validators, then
	 *            this map would be the preference page validators.
	 */
	static void save(Validator validator, Preferences root, Map<String, Validator> baseValidators) throws BackingStoreException {
		Validator.V2 v2 = validator.asV2Validator();
		if (v2 == null)return;
		
		Preferences vp = root.node(validator.getId());
		if (validator.sameConfig(baseValidators.get(validator.getId()))){
			vp.removeNode();
			return;
		}
		if (!validator.isChanged())return;
		if (validator.getChangeCountGlobal() > 0){
			Global g = new Global(validator.isManualValidation(), validator.isBuildValidation(), validator.getVersion(),
				validator.getDelegatingId());
			vp.put(PrefConstants.global, g.serialize());
		}
		
		if (validator.getChangeCountMessages() > 0){
			Collection<MessageSeveritySetting> msgs = validator.getMessageSettings().values();
			if (msgs.size() > 0){
				vp.put(PrefConstants.msgs, Msgs.serialize(msgs));
			}
		}
		
		if (v2.getChangeCountGroups() > 0){
			FilterGroup[] groups = v2.getGroups();
			if (groups.length > 0){
				Serializer ser = new Serializer(500);
				for (FilterGroup group : groups)group.save(ser);
				vp.put(PrefConstants.groups, ser.toString());
			}
		}
	}
	
	public void saveAsPrefs(Validator[] val) {
		try {
			IEclipsePreferences pref = ValidationFramework.getDefault().getPreferenceStore();
			Preferences vals = pref.node(PrefConstants.vals);
			Map<String, Validator> base = ExtensionValidators.instance().getMapV2();
			for (Validator v : val)save(v, vals, base);
			pref.flush();
			_validators = null;
			updateListeners(true);
		}
		catch (BackingStoreException e){
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * Save the global preferences and the validators.
	 */
	public synchronized void savePreferences(GlobalPreferences gp, Validator[] validators){
		try {
			IEclipsePreferences prefs = ValidationFramework.getDefault().getPreferenceStore();
			savePreferences(prefs, gp);
			Preferences vals = prefs.node(PrefConstants.vals);

			Map<String, Validator> base = ExtensionValidators.instance().getMapV2();
			for (Validator v : validators)save(v, vals, base);
			prefs.flush();
			_validators = null;
			updateListeners(true);
		}
		catch (BackingStoreException e){
			ValidationPlugin.getPlugin().handleException(e);
		}
	}
	
	/**
	 * Save the global preferences and the validators.
	 */
	public synchronized void savePreferences(GlobalPreferences gp){
		try {
			IEclipsePreferences prefs = ValidationFramework.getDefault().getPreferenceStore();
			boolean isConfigChange = gp.isConfigChange();
			savePreferences(prefs, gp);
			prefs.flush();
			updateListeners(isConfigChange);
		}
		catch (BackingStoreException e){
			ValidationPlugin.getPlugin().handleException(e);
		}
	}
	
	/**
	 * Save the global preferences and the validators.
	 */
	private void savePreferences(IEclipsePreferences prefs, GlobalPreferences gp){
		prefs.putBoolean(PrefConstants.saveAuto, gp.getSaveAutomatically());
		prefs.putBoolean(PrefConstants.suspend, gp.getDisableAllValidation());
		prefs.putLong(PrefConstants.stateTS, gp.getStateTimeStamp());
		prefs.putBoolean(PrefConstants.confirmDialog, gp.getConfirmDialog());
		prefs.putBoolean(PrefConstants.override, gp.getOverride());
		prefs.putInt(PrefConstants.frameworkVersion, ValPrefManagerGlobal.frameworkVersion);
	}

	/**
	 * Update any message preferences in the map.
	 * @param validator
	 * @param settings
	 */
	public void loadMessages(Validator validator, Map<String, MessageSeveritySetting> settings) {
		IEclipsePreferences pref = ValidationFramework.getDefault().getPreferenceStore();
		try {
			loadMessageSettings(validator, settings, pref);
		}
		catch (BackingStoreException e){
			ValidationPlugin.getPlugin().handleException(e);
		}
	}
		
	/**
	 * Load the message preferences for the validator into the map.
	 * 
	 * @param val
	 * @param settings
	 * @param root the root of the preference store
	 */
	static void loadMessageSettings(Validator val, Map<String, MessageSeveritySetting> settings, Preferences root) 
		throws BackingStoreException {
		if (!root.nodeExists(PrefConstants.vals))return;
		
		Preferences vals = root.node(PrefConstants.vals); 
		if (!vals.nodeExists(val.getId()))return;
		
		Preferences valPrefs = vals.node(val.getId());
		String msgs = valPrefs.get(PrefConstants.msgs, ""); //$NON-NLS-1$
		if (msgs.length() == 0)return;
		
		Map<String, MessageSeveritySetting.Severity> map = Msgs.deserialize(msgs);
		
		for (Map.Entry<String, MessageSeveritySetting.Severity> me : map.entrySet()){
			MessageSeveritySetting ms = settings.get(me.getKey());
			if (ms != null)ms.setCurrent(me.getValue());
		}		
	}

	/**
	 * Save whether the validator is enabled or not. 
	 * @param validator
	 * @param prefs up to the filter part of the preference tree
	 */
//	private void saveShallowPreference(Validator validator, Preferences prefs) {
//		if (validator.asV2Validator() == null)return;
//		Preferences val = prefs.node(validator.getId());
//		val.putBoolean(PrefConstants.build, validator.isBuildValidation());
//		val.putBoolean(PrefConstants.manual, validator.isManualValidation());
//		val.putInt(PrefConstants.version, validator.getVersion());
//	}
	
//	/**
//	 * Load the customized message settings from the preference store.
//	 * @param messageSettings
//	 */
//	public void loadMessageSettings(Validator val, MessageCategory[] messageSettings) {
//		try {
//			loadMessageSettings(val, messageSettings, ValidationFramework.getDefault().getPreferenceStore());
//		}
//		catch (Exception e){
//			ValidationPlugin.getPlugin().handleException(e);
//		}
//	}
	
	private static class Global {
		private boolean _manual;
		private boolean _build;
		private int		_version;
		private String	_delegating;
		
		public Global(String value){
			Deserializer d = new Deserializer(value);
			_manual = d.getBoolean();
			_build = d.getBoolean();
			_version = d.getInt();
			if (d.hasNext())_delegating = d.getString();
		}
		
		public Global(boolean manual, boolean build, int version, String delegating){
			_manual = manual;
			_build = build;
			_version = version;
			_delegating = delegating;
		}
		
		public String serialize(){
			Serializer s = new Serializer(50);
			s.put(_manual);
			s.put(_build);
			s.put(_version);
			if (_delegating != null)s.put(_delegating);
			return s.toString();
		}

		public boolean isManual() {
			return _manual;
		}

		public boolean isBuild() {
			return _build;
		}

		public int getVersion() {
			return _version;
		}

		public String getDelegating() {
			return _delegating;
		}
	}
	
	private static class Msgs {
		public static String serialize(Collection<MessageSeveritySetting> messages){
			Serializer s = new Serializer(100);
			for (MessageSeveritySetting ms : messages){
				s.put(ms.getId());
				s.put(ms.getCurrent().ordinal());
			}
			return s.toString();	
		}
		
		/**
		 * Answer a map for all the messages.
		 * The key is the message id and the value is the current setting for that message
		 * @param v
		 * @return
		 */
		public static Map<String, MessageSeveritySetting.Severity> deserialize(String v){
			Map<String, MessageSeveritySetting.Severity> map = new HashMap<String, MessageSeveritySetting.Severity>(10);
			Deserializer d = new Deserializer(v);
			while(d.hasNext()){
				String id = d.getString();
				int sev = d.getInt();
				map.put(id, MessageSeveritySetting.Severity.values()[sev]);
			}
			return map;
		}
	}
}
