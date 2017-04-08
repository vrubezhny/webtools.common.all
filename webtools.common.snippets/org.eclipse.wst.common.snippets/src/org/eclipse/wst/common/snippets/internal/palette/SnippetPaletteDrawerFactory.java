/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.common.snippets.internal.palette;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteTemplateEntry;
import org.eclipse.gef.ui.palette.customize.PaletteDrawerFactory;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.common.snippets.internal.SnippetsPlugin;
import org.eclipse.wst.common.snippets.internal.SnippetsPluginImageHelper;
import org.eclipse.wst.common.snippets.internal.SnippetsPluginImages;

public class SnippetPaletteDrawerFactory extends PaletteDrawerFactory {

	public SnippetPaletteDrawerFactory() {
		super();
		setLabel(SnippetsPlugin.getResourceString("%New_Category_Title")); //$NON-NLS-1$
		setImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_CLCL_NEW_CATEGORY));
	}

	/**
	 * @see org.eclipse.gef.ui.palette.customize.PaletteEntryFactory#createNewEntry(org.eclipse.swt.widgets.Shell)
	 */
	protected PaletteEntry createNewEntry(Shell shell) {
		SnippetPaletteDrawer drawer = new SnippetPaletteDrawer(SnippetsPlugin.getResourceString("%Unnamed_Category")); //$NON-NLS-1$
		drawer.setType(PaletteDrawer.PALETTE_TYPE_DRAWER);
		drawer.setDrawerType(PaletteTemplateEntry.PALETTE_TYPE_TEMPLATE);
		drawer.setId(SnippetsPlugin.getResourceString("%category") + "_" + System.currentTimeMillis()); //$NON-NLS-1$ //$NON-NLS-2$
		drawer.setUserModificationPermission(PaletteEntry.PERMISSION_FULL_MODIFICATION);
		drawer.setFilters(getDefaultFilters());
		return drawer;
	}

	protected String[] getDefaultFilters() {
		return new String[]{"*"}; //$NON-NLS-1$
	}
}