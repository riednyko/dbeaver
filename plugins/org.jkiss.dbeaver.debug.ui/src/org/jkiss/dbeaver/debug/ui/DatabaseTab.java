/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2017 Alexander Fedorov (alexander.fedorov@jkiss.org)
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
package org.jkiss.dbeaver.debug.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.debug.core.DebugCore;
import org.jkiss.dbeaver.debug.internal.ui.DebugUIMessages;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.registry.DataSourceProviderRegistry;
import org.jkiss.dbeaver.registry.driver.DriverDescriptor;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIUtils;

public class DatabaseTab extends AbstractLaunchConfigurationTab {

	private static final String GLOBAL = "Global";
	private static final String LOCAL = "Local";
	private Text driverText;
	private Text datasourceText;
	private Text databaseText;
	private Text schemaText;
	private Text oidText;
	private Text nameText;
	private Text callText;
	// private GridData labelsGD;
	// private GridData fieldsGD;
	// private GridData multiFieldsGD;

	/**
	 * Modify listener that simply updates the owning launch configuration dialog.
	 */
	protected ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent evt) {
			scheduleUpdateJob();
		}
	};

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
		comp.setLayout(new GridLayout(1, true));
		comp.setFont(parent.getFont());

		createComponents(comp);
	}

	protected void createComponents(Composite comp) {

		createConnectionSettingsGroup(comp);
		createDatabaseSettingsGroup(comp);
		createAdditionalSettingsGroup(comp);

	}

	protected void createConnectionSettingsGroup(Composite comp) {
		Group connectionSettingsGroup = UIUtils.createControlGroup(comp, DebugUIMessages.DatabaseTab_driver_group_text,
				2, GridData.FILL_HORIZONTAL, SWT.DEFAULT);

		// Driver
		createLabel(connectionSettingsGroup, DebugUIMessages.DatabaseTab_driver_label_text,
				GridFactory.createLayoutLabelData());
		driverText = createTextField(driverText, SWT.BORDER, connectionSettingsGroup, DebugCore.ATTR_DRIVER_ID_DEFAULT,
				GridFactory.createLayoutTextData(), false);

		// DataSource
		createLabel(connectionSettingsGroup, DebugUIMessages.DatabaseTab_datasource_group_text,
				GridFactory.createLayoutLabelData());
		datasourceText = createTextField(datasourceText, SWT.BORDER, connectionSettingsGroup,
				DebugCore.ATTR_DRIVER_ID_DEFAULT, GridFactory.createLayoutTextData(), false);

	}

	protected void createDatabaseSettingsGroup(Composite comp) {
		Group databaseSettingsGroup = UIUtils.createControlGroup(comp, DebugUIMessages.DatabaseTab_database_group_text,
				2, GridData.FILL_HORIZONTAL, SWT.DEFAULT);

		// Database
		createLabel(databaseSettingsGroup, DebugUIMessages.DatabaseTab_database_label_text,
				GridFactory.createLayoutLabelData());
		databaseText = createTextField(databaseText, SWT.BORDER, databaseSettingsGroup,
				DebugCore.ATTR_DATABASE_NAME_DEFAULT, GridFactory.createLayoutTextData(), false);

		// Schema
		createLabel(databaseSettingsGroup, DebugUIMessages.DatabaseTab_schema_label_text,
				GridFactory.createLayoutLabelData());
		schemaText = createTextField(schemaText, SWT.BORDER, databaseSettingsGroup, DebugCore.ATTR_SCHEMA_NAME_DEFAULT,
				GridFactory.createLayoutTextData(), false);

		// Oid
		createLabel(databaseSettingsGroup, DebugUIMessages.DatabaseTab_oid_label_text,
				GridFactory.createLayoutLabelData());
		oidText = createTextField(oidText, SWT.BORDER, databaseSettingsGroup, DebugCore.ATTR_PROCEDURE_OID_DEFAULT,
				GridFactory.createLayoutTextData(), false);

	}

	protected void createAdditionalSettingsGroup(Composite comp) {

		Group additionalSettings = new Group(comp, SWT.BORDER);
		additionalSettings.setLayout(new GridLayout(2, false));
		additionalSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		additionalSettings.setText(DebugUIMessages.DatabaseTab_name_group_text);

		// Name
		createLabel(additionalSettings, DebugUIMessages.DatabaseTab_name_label_text,
				GridFactory.createLayoutLabelData());
		nameText = createTextField(nameText, SWT.BORDER, additionalSettings, DebugCore.ATTR_PROCEDURE_NAME_DEFAULT,
				GridFactory.createLayoutTextData(), false);

		SelectionListener selectionListenerLocal = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button button = ((Button) event.widget);
				if (button != null && !button.isDisposed()) {
					callText.setEnabled(false);
				}
			};
		};

		SelectionListener selectionListenerGlobal = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button button = ((Button) event.widget);
				if (button != null && !button.isDisposed()) {
					callText.setEnabled(true);
				}
			};
		};

		// space
		createLabel(additionalSettings, "", GridFactory.createLayoutLabelData());

		Group groupCall = new Group(additionalSettings, SWT.SHADOW_IN);
		groupCall.setLayout(new GridLayout(2, false));
		groupCall.setLayoutData(GridFactory.createLayoutRadioData());

		Button btnLocal = new Button(groupCall, SWT.RADIO);
		btnLocal.setText(LOCAL);
		btnLocal.addSelectionListener(selectionListenerLocal);
		btnLocal.setLayoutData(GridFactory.createLayoutTextData());

		Button btnGlobal = new Button(groupCall, SWT.RADIO);
		btnGlobal.setText(GLOBAL);
		btnGlobal.addSelectionListener(selectionListenerGlobal);
		btnGlobal.setLayoutData(GridFactory.createLayoutTextData());

		// Call
		createLabel(additionalSettings, DebugUIMessages.DatabaseTab_call_label_text,
				GridFactory.createLayoutLabelData());
		callText = createTextField(callText, SWT.BORDER | SWT.MULTI, additionalSettings,
				DebugCore.ATTR_PROCEDURE_CALL_DEFAULT, GridFactory.createLayoutMultiLineData(), true);

		btnGlobal.setSelection(true);

	}

	private Text createTextField(Text textFiled, int style, Group connectionSettingsGroup, String text,
			GridData layoutData, boolean isEditable) {
		textFiled = new Text(connectionSettingsGroup, style);
		textFiled.setLayoutData(layoutData);
		textFiled.setText(text);
		textFiled.addModifyListener(modifyListener);
		textFiled.setEditable(isEditable);
		return textFiled;
	}

	private void createLabel(Group connectionSettingsGroup, String text, GridData layoutData) {
		Label lblDriverText = new Label(connectionSettingsGroup, SWT.BORDER);
		lblDriverText.setText(text);
		lblDriverText.setLayoutData(layoutData);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(DebugCore.ATTR_DRIVER_ID, DebugCore.ATTR_DRIVER_ID);
		configuration.setAttribute(DebugCore.ATTR_DATASOURCE_ID, DebugCore.ATTR_DATASOURCE_ID_DEFAULT);
		configuration.setAttribute(DebugCore.ATTR_DATABASE_NAME, DebugCore.ATTR_DATABASE_NAME_DEFAULT);
		configuration.setAttribute(DebugCore.ATTR_SCHEMA_NAME, DebugCore.ATTR_SCHEMA_NAME_DEFAULT);
		configuration.setAttribute(DebugCore.ATTR_PROCEDURE_OID, DebugCore.ATTR_PROCEDURE_OID_DEFAULT);
		configuration.setAttribute(DebugCore.ATTR_PROCEDURE_NAME, DebugCore.ATTR_PROCEDURE_NAME_DEFAULT);
		configuration.setAttribute(DebugCore.ATTR_PROCEDURE_CALL, DebugCore.ATTR_PROCEDURE_CALL_DEFAULT);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		initializeDriver(configuration);
		initializeDatasource(configuration);
		initializeDatabase(configuration);
		initializeSchema(configuration);
		initializeOid(configuration);
		initializeName(configuration);
		initializeCall(configuration);
	}

	protected void initializeDriver(ILaunchConfiguration configuration) {
		String extracted = DebugCore.extractDriverId(configuration);
		driverText.setText(extracted);
	}

	protected void initializeDatasource(ILaunchConfiguration configuration) {
		String extracted = DebugCore.extractDatasourceId(configuration);
		datasourceText.setText(extracted);
	}

	protected void initializeDatabase(ILaunchConfiguration configuration) {
		String extracted = DebugCore.extractDatabaseName(configuration);
		databaseText.setText(extracted);
	}

	protected void initializeSchema(ILaunchConfiguration configuration) {
		String extracted = DebugCore.extractSchemaName(configuration);
		schemaText.setText(extracted);
	}

	protected void initializeOid(ILaunchConfiguration configuration) {
		String extracted = DebugCore.extractProcedureOid(configuration);
		oidText.setText(extracted);
	}

	protected void initializeName(ILaunchConfiguration configuration) {
		String extracted = DebugCore.extractProcedureName(configuration);
		nameText.setText(extracted);
	}

	protected void initializeCall(ILaunchConfiguration configuration) {
		String extracted = DebugCore.extractProcedureCall(configuration);
		callText.setText(extracted);
	}

	@Override
	public Image getImage() {
		DBPImage image = extractDriverImage();
		if (image == null) {
			image = DBIcon.TREE_DATABASE;
		}
		return DBeaverIcons.getImage(image);
	}

	protected DBPImage extractDriverImage() {
		if (driverText == null || driverText.isDisposed()) {
			return null;
		}
		String driverName = driverText.getText();
		DriverDescriptor driver = DataSourceProviderRegistry.getInstance().findDriver(driverName);
		if (driver == null) {
			return null;
		}
		return driver.getIcon();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(DebugCore.ATTR_DRIVER_ID, driverText.getText());
		configuration.setAttribute(DebugCore.ATTR_DATASOURCE_ID, datasourceText.getText());
		configuration.setAttribute(DebugCore.ATTR_DATABASE_NAME, databaseText.getText());
		configuration.setAttribute(DebugCore.ATTR_SCHEMA_NAME, schemaText.getText());
		configuration.setAttribute(DebugCore.ATTR_PROCEDURE_OID, oidText.getText());
		configuration.setAttribute(DebugCore.ATTR_PROCEDURE_NAME, nameText.getText());
		configuration.setAttribute(DebugCore.ATTR_PROCEDURE_CALL, callText.getText());
	}

	@Override
	public String getName() {
		return DebugUIMessages.DatabaseTab_name;
	}

	static class GridFactory {
		private static final int DEFAULT_WIDTH = 80;
		private static final int DEFAULT_MULTI_HEIGHT = 60;
		private static final int DEFAULT_HEIGHT = 20;

		public static GridData createLayoutLabelData() {
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
			layoutData.widthHint = DEFAULT_WIDTH;
			return layoutData;
		}

		public static GridData createLayoutTextData() {
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			layoutData.minimumHeight = DEFAULT_HEIGHT;
			return layoutData;
		}

		public static GridData createLayoutMultiLineData() {
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			return layoutData;
		}

		public static GridData createLayoutRadioData() {
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			return layoutData;
		}

	}

}
