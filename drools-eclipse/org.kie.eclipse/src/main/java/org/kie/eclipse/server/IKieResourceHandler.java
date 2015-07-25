/*******************************************************************************
 * Copyright (c) 2011, 2012, 2013, 2014 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 * @author Bob Brodt
 ******************************************************************************/

package org.kie.eclipse.server;

import java.util.List;

import org.eclipse.wst.server.core.IServer;
import org.osgi.service.prefs.Preferences;

import com.eclipsesource.json.JsonObject;

/**
 *
 */
public interface IKieResourceHandler {
	String getName();
	IServer getServer();
	IKieResourceHandler getParent();
	void setParent(IKieResourceHandler parent);
	IKieResourceHandler getRoot();
	Object getResource();
	void dispose();
	IKieServiceDelegate getDelegate();
	Object load();
	boolean isLoaded();
	public void setProperties(JsonObject properties);
	public JsonObject getProperties();
	List<? extends IKieResourceHandler> getChildren() throws Exception;
	String getRuntimeId();
	String getPreferenceName(String name);
	Preferences getPreferences();
	// convenience methods
	String getPreference(String name, String def);
	boolean getPreference(String name, boolean def);
	int getPreference(String name, int def);
	void putPreference(String name, String value);
	void putPreference(String name, boolean value);
	void putPreference(String name, int value);
}
