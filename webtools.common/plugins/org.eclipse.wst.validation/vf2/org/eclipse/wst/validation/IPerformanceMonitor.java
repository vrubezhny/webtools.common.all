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
package org.eclipse.wst.validation;

import java.util.List;

/**
 * A service that collects performance information on validation operations.
 * @author karasiuk
 *
 */
public interface IPerformanceMonitor {
	
	public enum CollectionLevel {None, Default}
	
	/** 
	 * Add this performance counter to the monitor. The monitor will decide what to 
	 * do with the results, it may log them to a file, or it may simply hold on to them.
	 * 
	 * @param counters
	 */
	public void add(PerformanceCounters counters);
	
	/**
	 * Answer true if we have been asked to collect performance events.
	 */
	public boolean isCollecting();
	
	/** Answer true if only summary information is wanted. */
	public boolean isSummaryOnly();
	
	/** 
	 * Set the performance event collection level. This controls which performance events are
	 * collected, including none of them.
	 * 
	 * @param level the level to collect. The default is to not collect anything.
	 */
	public void setCollectionLevel(CollectionLevel level);
	
	/**
	 * Answer the level of performance events that are being collected by the validation framework.
	 */
	public CollectionLevel getCollectionLevel();
	
	/**
	 * Answer the performance counters that have been collected so far. Some monitors do
	 * not save counters, and they will always return an empty list.
	 * 
	 * @param asSummary if true only answer a summary of the counters
	 */
	public List<PerformanceCounters> getPerformanceCounters(boolean asSummary);
	
	/**
	 * Delete the collected performance counters.
	 */
	public void resetPerformanceCounters();

}
