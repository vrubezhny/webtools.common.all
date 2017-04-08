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
package org.eclipse.wst.common.snippets.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.DefaultPaletteViewerPreferences;
import org.eclipse.gef.ui.palette.PaletteContextMenuProvider;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.gef.ui.palette.customize.PaletteCustomizerDialog;
import org.eclipse.gef.ui.parts.PaletteViewerKeyHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.common.snippets.internal.Debug;
import org.eclipse.wst.common.snippets.internal.IHelpContextIds;
import org.eclipse.wst.common.snippets.internal.ISnippetCategory;
import org.eclipse.wst.common.snippets.internal.Logger;
import org.eclipse.wst.common.snippets.internal.PluginRecord;
import org.eclipse.wst.common.snippets.internal.SnippetDefinitions;
import org.eclipse.wst.common.snippets.internal.SnippetTransfer;
import org.eclipse.wst.common.snippets.internal.SnippetsMessages;
import org.eclipse.wst.common.snippets.internal.SnippetsPlugin;
import org.eclipse.wst.common.snippets.internal.SnippetsPluginImageHelper;
import org.eclipse.wst.common.snippets.internal.SnippetsPluginImages;
import org.eclipse.wst.common.snippets.internal.actions.DeleteCategoryAction;
import org.eclipse.wst.common.snippets.internal.actions.DeleteItemAction;
import org.eclipse.wst.common.snippets.internal.model.SnippetManager;
import org.eclipse.wst.common.snippets.internal.palette.SnippetPaletteDrawer;
import org.eclipse.wst.common.snippets.internal.palette.SnippetPaletteItem;
import org.eclipse.wst.common.snippets.internal.palette.SnippetPaletteItemFactory;
import org.eclipse.wst.common.snippets.internal.palette.SnippetPaletteRoot;
import org.eclipse.wst.common.snippets.internal.palette.SnippetViewer;
import org.eclipse.wst.common.snippets.internal.provisional.ISnippetItem;
import org.eclipse.wst.common.snippets.internal.provisional.ISnippetsEntry;
import org.eclipse.wst.common.snippets.internal.provisional.ISnippetsInsertion;
import org.eclipse.wst.common.snippets.internal.provisional.insertions.VariableInsertion;
import org.eclipse.wst.common.snippets.internal.util.UserDrawerSelector;
import org.eclipse.wst.common.snippets.internal.util.VisibilityUtil;
import org.eclipse.wst.sse.ui.internal.actions.ActiveEditorActionHandler;
import org.osgi.framework.Bundle;

public class SnippetsView extends ViewPart {

	protected class CopyAction extends Action {
		public CopyAction() {
			super(SnippetsMessages.Copy_2); //$NON-NLS-1$
			setImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_ELCL_COPY));
			setHoverImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_CLCL_COPY));
			setDisabledImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_DLCL_COPY));
			setToolTipText(getText());
		}

		public void run() {
			super.run();
			byte[] stringData = EntrySerializer.getInstance().toXML(getSelectedEntry());
			getClipboard().setContents(new Object[]{stringData, getSelectedEntry().getLabel()}, new Transfer[]{SnippetTransfer.getTransferInstance(), TextTransfer.getInstance()});
		}
	}

	protected class CutAction extends Action {
		public CutAction() {
			super(SnippetsMessages.Cut_2); //$NON-NLS-1$
			setImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_ELCL_CUT));
			setHoverImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_CLCL_CUT));
			setDisabledImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_DLCL_CUT));
			setToolTipText(getText());
		}

		public void run() {
			super.run();
			getCopyAction().run();
			getDeleteAction().run();
			SnippetsView.this.save();
		}
	}

	protected class DeleteAction extends Action {
		public DeleteAction() {
			super(SnippetsMessages.Delete_1); //$NON-NLS-1$
			setImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_ELCL_DELETE));
			setHoverImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_CLCL_DELETE));
			setDisabledImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_DLCL_DELETE));
			setToolTipText(getText());
		}

		public void run() {
			super.run();
			if (getSelectedEntry() instanceof PaletteContainer)
				new DeleteCategoryAction(SnippetsView.this, (PaletteContainer) getSelectedEntry()).run();
			else if (getSelectedEntry() instanceof PaletteEntry)
				new DeleteItemAction(getViewer(), (PaletteEntry) getSelectedEntry()).run();
			SnippetsView.this.save();
		}
	}

	protected class InsertAction extends Action {
		public InsertAction() {
			super(SnippetsMessages.Insert___); 
			setImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_ELCL_INSERT));
			setHoverImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_CLCL_INSERT));
			setDisabledImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_DLCL_INSERT));
			setToolTipText(getText());
		}

		public void run() {
			super.run();
			insert();
		}
	}

	protected class InsertionHelper {
		protected InsertionHelper() {
			super();
		}

		protected ISnippetsInsertion getInsertion(ISnippetItem item) {
			ISnippetsInsertion insertion = null;
			String className = item.getClassName();

			PluginRecord record = null;
			if (item.getSourceType() == ISnippetsEntry.SNIPPET_SOURCE_PLUGINS)
				record = (PluginRecord) item.getSourceDescriptor();
			// ignore the version
			if (record != null && record.getPluginName() != null && className != null) {
				Class theClass = null;
				Bundle bundle = Platform.getBundle(record.getPluginName());
				try {
					if (className != null && className.length() > 0)
						theClass = bundle.loadClass(className);
				}
				catch (ClassNotFoundException e) {
					try { // maybe it's local???
						theClass = Class.forName(className);
					}
					catch (ClassNotFoundException f) {
						Logger.logException("Could not load Insertion class", e); //$NON-NLS-1$
					}
					if (theClass == null)
						Logger.logException("Could not load Insertion class", e); //$NON-NLS-1$
				}
				if (theClass != null) {
					try {
						insertion = (ISnippetsInsertion) theClass.newInstance();
					}
					catch (IllegalAccessException e) {
						Logger.logException("Could not access Insertion class", e); //$NON-NLS-1$
					}
					catch (InstantiationException e) {
						Logger.logException("Could not instantiate Insertion class", e); //$NON-NLS-1$
					}
				}
			}
			if (insertion == null) {
				try {
					insertion = (ISnippetsInsertion) DEFAULT_INSERTION_CLASS.newInstance();
				}
				catch (IllegalAccessException e) {
					Logger.logException("Could not access Insertion class", e); //$NON-NLS-1$
				}
				catch (InstantiationException e) {
					Logger.logException("Could not instantiate Insertion class", e); //$NON-NLS-1$
				}
			}
			return insertion;
		}

		public boolean insert(ISnippetItem item, IEditorPart editorPart) {
			ISnippetsInsertion insertion = getInsertion(item);
			if (insertion != null) {
				insertion.setItem(item);
				insertion.setActiveEditorPart(editorPart);
				insertion.insert(editorPart);
				return true;
			}
			return false;
		}
	}

	protected class InsertMouseListener extends MouseAdapter {
		public void mouseDoubleClick(MouseEvent e) {
			// doesn't detect if hitpoint is on internal scrollbars
			if (e.button == 1) {
				EditPart part = SnippetsView.this.getViewer().findObjectAt(new Point(e.x, e.y));
				ISnippetItem item = null;
				if (part != null) {
					if (part.getModel() instanceof ISnippetItem)
						item = (ISnippetItem) part.getModel();
				}
				if (item != null)
					insert(item);
			}
		}

	}

	private final class PartActivationListener implements IPartListener {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				IEditorPart editorPart = (IEditorPart) part;
				IEditorInput input = editorPart.getEditorInput();
				if (input.getName() != null) {
					SnippetDefinitions snippetDefinitions = SnippetManager.getInstance().getDefinitions();
					List categories = snippetDefinitions.getCategories();
					for (Iterator iter = categories.iterator(); iter.hasNext();) {
						SnippetPaletteDrawer drawer = (SnippetPaletteDrawer) iter.next();
						String[] filters = drawer.getFilters();
						boolean drawerVisibility = VisibilityUtil.isContentType(input, filters);
						drawer.setVisible(drawerVisibility);
					}
				}
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			// do nothing
		}

		public void partClosed(IWorkbenchPart part) {
			updateActions(getSelectedEntry());
		}

		public void partDeactivated(IWorkbenchPart part) {
			// do nothing
		}

		public void partOpened(IWorkbenchPart part) {
			updateActions(getSelectedEntry());
		}
	}

	protected class PasteAction extends Action {

		public PasteAction() {
			super(SnippetsMessages.Paste_4); //$NON-NLS-1$
			setImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_ELCL_PASTE));
			setHoverImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_CLCL_PASTE));
			setDisabledImageDescriptor(SnippetsPluginImageHelper.getInstance().getImageDescriptor(SnippetsPluginImages.IMG_DLCL_PASTE));
			setToolTipText(getText());
		}

		protected String getFirstLine(String text) {
			if (text == null || text.length() < 1) {
				return text;
			}
			IDocument doc = new Document(text);
			try {
				int lineNumber = doc.getLineOfOffset(0);
				IRegion line = doc.getLineInformation(lineNumber);
				return doc.get(line.getOffset(), line.getLength());
			}
			catch (BadLocationException e) {
				// do nothing
			}
			return text;
		}

		protected void launchCustomizer(PaletteEntry entry) {
			PaletteCustomizerDialog dialog = getViewer().getCustomizerDialog();
			if (!(entry instanceof PaletteRoot)) {
				dialog.setDefaultSelection(entry);
			}
			dialog.open();
		}

		public void run() {
			super.run();
			byte[] itemDefinition = (byte[]) getClipboard().getContents(SnippetTransfer.getTransferInstance());
			if (itemDefinition != null) {
				ISnippetsEntry entry = EntryDeserializer.getInstance().fromXML(itemDefinition);
				ISnippetsEntry destination = getSelectedEntry();
				// if the selected entry isn't user owned
				if (destination == null || destination.getSourceType() == ISnippetsEntry.SNIPPET_SOURCE_PLUGINS) {
					UserDrawerSelector selector = new UserDrawerSelector(getSite().getShell());
					selector.setSelectionPrompt(SnippetsMessages.Cant_add_to_this); //$NON-NLS-1$
					SnippetPaletteDrawer drawer = (SnippetPaletteDrawer) selector.getUserDrawer();
					if (drawer != null)
						destination = drawer;
					else
						return;
				}
				if (entry instanceof ISnippetItem) {
					ISnippetItem item = (ISnippetItem) entry;
					if (destination instanceof ISnippetCategory) {
						item.setId(destination.getId() + System.currentTimeMillis());
						((ISnippetCategory) destination).add(item);
						EditPart container = (EditPart) getViewer().getEditPartRegistry().get(destination);
						container.refresh();
					}
					else if (destination instanceof ISnippetItem) {
						ISnippetCategory category = ((ISnippetItem) destination).getCategory();
						item.setId("item_" + System.currentTimeMillis()); //$NON-NLS-1$
						category.add(item);
						EditPart container = (EditPart) getViewer().getEditPartRegistry().get(category);
						container.refresh();
					}
					EditPart part = (EditPart) getViewer().getEditPartRegistry().get(item);
					if (part != null)
						getViewer().select(part);
				}
				SnippetsView.this.save();
			}
			else {
				String itemText = (String) getClipboard().getContents(TextTransfer.getInstance());
				if (itemText == null)
					itemText = ""; //$NON-NLS-1$

				ISnippetCategory category = null;
				ISnippetsEntry entry = SnippetsView.this.getSelectedEntry();
				if (entry == null || entry.getSourceType() == ISnippetsEntry.SNIPPET_SOURCE_PLUGINS) {
					UserDrawerSelector selector = new UserDrawerSelector(getSite().getShell());
					selector.setSelectionPrompt(SnippetsMessages.Cant_add_to_this); //$NON-NLS-1$
					SnippetPaletteDrawer drawer = (SnippetPaletteDrawer) selector.getUserDrawer();
					if (drawer != null)
						entry = drawer;
					else
						return;
				}
				if (entry instanceof ISnippetItem)
					category = ((ISnippetItem) SnippetsView.this.getSelectedEntry()).getCategory();
				else if (entry instanceof ISnippetCategory)
					category = (ISnippetCategory) entry;
				if (category == null)
					return;
				SnippetPaletteItem item = (SnippetPaletteItem) new SnippetPaletteItemFactory().createNewEntry(getViewer().getControl().getShell(), (PaletteEntry) category);
				item.setDescription(getFirstLine(itemText));
				item.setContentString(itemText);
				launchCustomizer(item);
			}
		}

		public void update() {
			if (getClipboard().getContents(SnippetTransfer.getTransferInstance()) == null && getClipboard().getContents(TextTransfer.getInstance()) != null) {
				setText(SnippetsMessages.Paste_as_Snippet); //$NON-NLS-1$
			}
			else {
				setText(SnippetsMessages.Paste_4); //$NON-NLS-1$
			}
			setToolTipText(getText());
		}
	}

	protected class SnippetsContextMenuListener implements IMenuListener {
		protected PaletteContextMenuProvider baseProvider = null;
		protected IMenuManager manager = null;

		public SnippetsContextMenuListener(MenuManager mgr) {
			this.manager = mgr;
			baseProvider = new PaletteContextMenuProvider(SnippetsView.this.getViewer());
		}

		public void menuAboutToShow(IMenuManager menuManager) {
			SnippetsView.this.fillContextMenu(menuManager);
			baseProvider.buildContextMenu(menuManager);
		}
	}

	protected class TransferDragSourceListenerImpl implements TransferDragSourceListener {
		private Transfer fTransfer;

		public TransferDragSourceListenerImpl(Transfer xfer) {
			fTransfer = xfer;
		}

		public void dragFinished(DragSourceEvent event) {
			if (Debug.debugDragAndDrop)
				System.out.println("drag finished"); //$NON-NLS-1$
		}

		public void dragSetData(DragSourceEvent event) {
			if (getSelectedEntry() == null)
				return;
			if (getSelectedEntry() instanceof ISnippetItem) {
				ISnippetsInsertion insertion = insertionHelper.getInsertion((ISnippetItem) getSelectedEntry());
				insertion.setActiveEditorPart(getSite().getPage().getActiveEditor());
				insertion.dragSetData(event, (ISnippetItem) getSelectedEntry());
			}
			else {
				if (getSelectedEntry() != null)
					event.data = getSelectedEntry().getLabel();
				else
					event.data = EMPTY_STRING;
			}
		}

		public void dragStart(DragSourceEvent event) {
			if (Debug.debugDragAndDrop)
				System.out.println("drag begun"); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.util.TransferDragSourceListener#getTransfer()
		 */
		public Transfer getTransfer() {
			return fTransfer;
		}
	}

	public static final Class DEFAULT_INSERTION_CLASS = VariableInsertion.class;

	protected Clipboard clipboard = null;

	protected Action copyAction = null;
	protected Action cutAction = null;
	private Action deleteAction;

	private final String EMPTY_STRING = ""; //$NON-NLS-1$
	private PartActivationListener fPartActionUpdateListener = null;
	protected SnippetPaletteRoot fRoot = null;
	protected ISnippetsEntry fSelectedEntry;
	protected ISelectionChangedListener fSelectionChangedListener = null;

	protected List fTransferDragSourceListeners = null;

	protected PaletteViewer fViewer = null;
	protected Action insertAction = null;

	protected InsertionHelper insertionHelper = null;

	protected MouseListener insertListener = new InsertMouseListener();
	protected PasteAction pasteAction = null;

	/**
	 * Constructor for SnippetsView.
	 */
	public SnippetsView() {
		super();
		insertionHelper = new InsertionHelper();
	}

	protected void addTextTransferListener() {
		TransferDragSourceListener listener = new TransferDragSourceListenerImpl(TextTransfer.getInstance());
		getViewer().addDragSourceListener(listener);
		getTransferDragSourceListeners().add(listener);
	}

	/*
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		fViewer = new SnippetViewer();
		fViewer.enableVerticalScrollbar(true);

		fViewer.createControl(parent);

		fViewer.setKeyHandler(new PaletteViewerKeyHandler(fViewer));

		DefaultEditDomain domain = new DefaultEditDomain(null);
		fViewer.setEditDomain(domain);

		fillLocalMenu(getViewSite().getActionBars().getMenuManager());
		fillToolBar(getViewSite().getActionBars().getToolBarManager());

		getCopyAction().setEnabled(false);
		getCutAction().setEnabled(false);
		getPasteAction().setEnabled(false);
		getInsertAction().setEnabled(false);

		if (fViewer.getContextMenu() == null)
			fViewer.setContextMenu(new MenuManager("#popup", getClass().getName())); //$NON-NLS-1$
		fViewer.getContextMenu().addMenuListener(new SnippetsContextMenuListener(fViewer.getContextMenu()));
		fViewer.getContextMenu().setRemoveAllWhenShown(true);

		fViewer.setCustomizer(new SnippetsCustomizer());
		PaletteViewerPreferences libraryPreferences = new DefaultPaletteViewerPreferences(SnippetsPlugin.getDefault().getPreferenceStore());
		SnippetsPlugin.getDefault().getPreferenceStore().setDefault(PaletteViewerPreferences.PREFERENCE_AUTO_COLLAPSE, PaletteViewerPreferences.COLLAPSE_ALWAYS);
		fViewer.setPaletteViewerPreferences(libraryPreferences);

		fViewer.setPaletteRoot(getRoot());

		getViewer().addSelectionChangedListener(getSelectionChangedListener());

		// drawers initially collapsed through model setup
		fViewer.getControl().addMouseListener(insertListener);

		WorkbenchHelp.setHelp(getViewer().getControl(), IHelpContextIds.MAIN_VIEW_GENERAL);
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.UNDO.getId(), new ActiveEditorActionHandler(getViewSite(), ActionFactory.UNDO.getId()));
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REDO.getId(), new ActiveEditorActionHandler(getViewSite(), ActionFactory.REDO.getId()));

		fPartActionUpdateListener = new PartActivationListener();
		getViewSite().getPage().addPartListener(fPartActionUpdateListener);
	}

	protected PaletteRoot createRoot(SnippetDefinitions defs) {
		fRoot = new SnippetPaletteRoot(defs);
		fRoot.connect();
		return fRoot;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fRoot != null)
			fRoot.disconnect();
		if (clipboard != null)
			clipboard.dispose();
		if (fPartActionUpdateListener != null)
			getViewSite().getPage().removePartListener(fPartActionUpdateListener);
		save();
	}

	protected void fillContextMenu(IMenuManager mgr) {
		ISnippetsEntry entry = null;
		EditPart part = getViewer().getFocusEditPart();
		if (part != null && part.getModel() instanceof ISnippetsEntry)
			entry = (ISnippetsEntry) part.getModel();

		if (entry != null) {
			if (getInsertAction().isEnabled()) {
				mgr.add(getInsertAction());
				mgr.add(new Separator());
			}
			// if (getCutAction().isEnabled())
			mgr.add(getCutAction());
			// if (getCopyAction().isEnabled())
			mgr.add(getCopyAction());
			// if (getPasteAction().isEnabled())
			mgr.add(getPasteAction());
		}
	}

	protected void fillLocalMenu(IMenuManager manager) {
		manager.add(getInsertAction());
		manager.add(new Separator());
		manager.add(getCutAction());
		manager.add(getCopyAction());
		manager.add(getPasteAction());
	}

	protected void fillToolBar(IToolBarManager manager) {
		manager.add(getInsertAction());
	}

	public Object getAdapter(Class adapter) {
		return super.getAdapter(adapter);
	}

	/**
	 * Returns the clipboard.
	 * 
	 * @return Clipboard
	 */
	protected Clipboard getClipboard() {
		if (clipboard == null && getViewer() != null && getViewer().getControl() != null && !getViewer().getControl().isDisposed())
			clipboard = new Clipboard(getViewer().getControl().getDisplay());
		return clipboard;
	}

	// protected CollapseCategoriesAction getCollapseAction() {
	// if(collapseCategoriesAction == null)
	// collapseCategoriesAction = new CollapseCategoriesAction();
	// return collapseCategoriesAction;
	// }

	/**
	 * Returns the copyAction.
	 * 
	 * @return Action
	 */
	public Action getCopyAction() {
		if (copyAction == null) {
			copyAction = new CopyAction();
			WorkbenchHelp.setHelp(copyAction, IHelpContextIds.MENU_COPY_SNIPPET);
		}
		return copyAction;
	}

	/**
	 * Returns the cutAction.
	 * 
	 * @return Action
	 */
	public Action getCutAction() {
		if (cutAction == null) {
			cutAction = new CutAction();
			WorkbenchHelp.setHelp(cutAction, IHelpContextIds.MENU_CUT_SNIPPET);
		}
		return cutAction;
	}

	/**
	 * Returns the deleteAction.
	 * 
	 * @return Action
	 */
	protected Action getDeleteAction() {
		if (deleteAction == null)
			deleteAction = new DeleteAction();
		return deleteAction;
	}

	// public void collapseCategories() {
	// Iterator categories =
	// getRoot().getDefinitions().getCategories().iterator();
	// while (categories.hasNext()) {
	// ISnippetCategory cat = (ISnippetCategory) categories.next();
	// if (!cat.isVisible())
	// continue;
	// GraphicalEditPart catPart = (GraphicalEditPart)
	// fViewer.getEditPartRegistry().get(cat);
	// if (catPart != null && catPart instanceof DrawerEditPart)
	// ((DrawerEditPart) catPart).setExpanded(false);
	// }
	// }

	protected ISnippetsEntry getEntryFromSelection(ISelection selection) {
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof EditPart) {
					if (((EditPart) obj).getModel() instanceof ISnippetsEntry)
						return (ISnippetsEntry) ((EditPart) obj).getModel();
				}
				else if (obj instanceof ISnippetsEntry)
					return (ISnippetsEntry) obj;
			}
		}
		return null;
	}

	/**
	 * Returns the insertAction.
	 * 
	 * @return Action
	 */
	public Action getInsertAction() {
		if (insertAction == null)
			insertAction = new InsertAction();
		return insertAction;
	}

	/**
	 * Returns the pasteAction.
	 * 
	 * @return Action
	 */
	public PasteAction getPasteAction() {
		if (pasteAction == null) {
			pasteAction = new PasteAction();
			WorkbenchHelp.setHelp(pasteAction, IHelpContextIds.MENU_PASTE_SNIPPET);
		}
		return pasteAction;
	}

	/**
	 * Gets the model root -- should be identical across all instances
	 * 
	 * @return Returns a SnippetPaletteRoot
	 */
	public SnippetPaletteRoot getRoot() {
		return SnippetManager.getInstance().getPaletteRoot();
	}

	/**
	 * Returns the selected ISnippetsEntry.
	 * 
	 * @return ISnippetsEntry
	 */
	public ISnippetsEntry getSelectedEntry() {
		return fSelectedEntry;
	}

	private ISelectionChangedListener getSelectionChangedListener() {
		if (fSelectionChangedListener == null)
			fSelectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					setSelectedEntry(getEntryFromSelection(event.getSelection()));
				}
			};
		return fSelectionChangedListener;
	}

	/**
	 * Returns the transferDragSourceListeners last set on the
	 * GraphicalViewer.
	 * 
	 * @return List
	 */
	public List getTransferDragSourceListeners() {
		if (fTransferDragSourceListeners == null)
			fTransferDragSourceListeners = new ArrayList();
		return fTransferDragSourceListeners;
	}

	/**
	 * Returns the PaletteViewer
	 * 
	 * @return PaletteViewer
	 */
	public PaletteViewer getViewer() {
		return fViewer;
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
	}

	public void insert() {
		if (getSelectedEntry() != null && getSelectedEntry() instanceof ISnippetItem) {
			ISnippetItem item = (ISnippetItem) getSelectedEntry();
			insert(item);
		}
	}

	public void insert(ISnippetItem item) {
		insertionHelper.insert(item, getSite().getPage().getActiveEditor());
	}

	public void save() {
		SnippetManager.getInstance().saveDefinitions();
	}

	public void saveState(IMemento memento) {
		// not done when view is closed so hooked dispose instead
	}

	public void setFocus() {
		if (getViewer() != null && getViewer().getControl() != null && !getViewer().getControl().isDisposed())
			getViewer().getControl().setFocus();
	}

	/**
	 * Sets the root.
	 * 
	 * @param root
	 *            The root to set
	 */
	public void setRoot(SnippetPaletteRoot root) {
		fRoot = root;
		fViewer.setPaletteRoot(root);
	}

	public void setSelectedEntry(ISnippetsEntry entry) {
		if (Debug.debugPaletteSelection) {
			if (fSelectedEntry == entry)
				System.out.println("selection notification: " + entry + " (duplicated)"); //$NON-NLS-1$ //$NON-NLS-2$
			else
				System.out.println("selection notification: " + entry); //$NON-NLS-1$
		}
		if (fSelectedEntry == entry) {
			return;
		}
		if (Debug.debugPaletteSelection)
			System.out.println("processing selection: " + entry); //$NON-NLS-1$
		fSelectedEntry = entry;
		updateDragSource();
		updateActions(entry);
	}

	protected void updateActions(ISnippetsEntry entry) {
		boolean isItem = entry instanceof ISnippetItem;
		boolean isFromPlugin = entry != null && entry.getSourceType() == ISnippetsEntry.SNIPPET_SOURCE_PLUGINS;

		getCopyAction().setEnabled(entry != null && isItem);
		getCutAction().setEnabled(entry != null && isItem && !isFromPlugin);
		getPasteAction().setEnabled(getClipboard().getContents(TextTransfer.getInstance()) != null);
		getPasteAction().update();
		getInsertAction().setEnabled(entry != null && isItem && getSite().getPage().getEditorReferences().length > 0);
	}

	protected void updateDragSource() {
		Transfer[] supportedTypes = null;
		if (getSelectedEntry() != null && getSelectedEntry() instanceof ISnippetItem) {
			ISnippetItem item = (ISnippetItem) getSelectedEntry();
			ISnippetsInsertion insertion = insertionHelper.getInsertion(item);
			if (insertion != null) {
				insertion.setItem(item);
				insertion.setActiveEditorPart(getSite().getPage().getActiveEditor());
				supportedTypes = insertion.getTransfers();
			}
		}
		else {
			// Keep a transfer registered so that GEF doesn't unhook the drag
			// source
			// from the control. Not unhooking it would make it look like
			// conditions
			// where drag isn't supported would be (the cursor looks like it's
			// dragging)
			// when they shouldn't be draggable at all.
			supportedTypes = new Transfer[]{TextTransfer.getInstance()};
		}

		// TRH suggested use of the event's doit field by the fListeners, but
		// there's
		// no other way to guarantee that TextTransfer is considered last
		Iterator iterator = getTransferDragSourceListeners().iterator();
		ArrayList oldListeners = new ArrayList();
		while (iterator.hasNext()) {
			TransferDragSourceListener listener = (TransferDragSourceListener) iterator.next();
			oldListeners.add(listener);
			iterator.remove();
		}

		boolean addTextTransfer = false;
		for (int i = 0; i < supportedTypes.length; i++) {
			if (TextTransfer.class.equals(supportedTypes[i].getClass())) {
				addTextTransfer = true;
			}
			else {
				TransferDragSourceListener listener = new TransferDragSourceListenerImpl(supportedTypes[i]);
				getViewer().addDragSourceListener(listener);
				getTransferDragSourceListeners().add(listener);
			}
		}
		iterator = oldListeners.iterator();
		while (iterator.hasNext()) {
			TransferDragSourceListener listener = (TransferDragSourceListener) iterator.next();
			getViewer().removeDragSourceListener(listener);
			iterator.remove();
		}
		if (addTextTransfer) {
			addTextTransferListener();
		}
		if (Debug.debugDragAndDrop)
			System.out.println("" + getTransferDragSourceListeners().size() + " transfer types"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}