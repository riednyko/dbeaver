/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
 * Copyright (C) 2011-2012 Eugene Fradkin (eugene.fradkin@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jkiss.dbeaver.erd.model.internal;

import org.eclipse.osgi.util.NLS;

public class ERDMessages extends NLS {
	static final String BUNDLE_NAME = "org.jkiss.dbeaver.erd.model.internal.ERDMessages"; //$NON-NLS-1$

    static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ERDMessages.class);
	}

	public static String erd_attribute_visibility_selection_item_all;
	public static String erd_attribute_visibility_selection_item_any_keys;
	public static String erd_attribute_visibility_selection_item_none;
	public static String erd_attribute_visibility_selection_item_primary_key;

	private ERDMessages() {
	}
}
