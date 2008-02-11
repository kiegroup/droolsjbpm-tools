package org.drools.eclipse.flow.common.datatype;
/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.drools.eclipse.flow.common.datatype.impl.DataTypeRegistryImpl;
import org.drools.eclipse.flow.common.view.datatype.editor.impl.BooleanEditor;
import org.drools.eclipse.flow.common.view.datatype.editor.impl.EmptyEditor;
import org.drools.eclipse.flow.common.view.datatype.editor.impl.FloatEditor;
import org.drools.eclipse.flow.common.view.datatype.editor.impl.IntegerEditor;
import org.drools.eclipse.flow.common.view.datatype.editor.impl.StringEditor;
import org.drools.process.core.datatype.DataTypeFactory;
import org.drools.process.core.datatype.impl.InstanceDataTypeFactory;
import org.drools.process.core.datatype.impl.type.BooleanDataType;
import org.drools.process.core.datatype.impl.type.FloatDataType;
import org.drools.process.core.datatype.impl.type.IntegerDataType;
import org.drools.process.core.datatype.impl.type.StringDataType;
import org.drools.process.core.datatype.impl.type.UndefinedDataType;

//import sun.beans.editors.FloatEditor;
//import sun.beans.editors.StringEditor;

/**
 * Default datatype registry containing default datatypes.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DefaultDataTypeRegistry {

	private static final DataTypeRegistry variableDataTypeRegistry = new DataTypeRegistryImpl();

	private DefaultDataTypeRegistry() {
	}

	static {
		registerVariableDataType(UndefinedDataType.class,
				new InstanceDataTypeFactory(UndefinedDataType.class),
				"Undefined", EmptyEditor.class, EmptyEditor.class);
		registerVariableDataType(BooleanDataType.class,
				new InstanceDataTypeFactory(BooleanDataType.class), "Boolean",
				BooleanEditor.class, EmptyEditor.class);
		registerVariableDataType(IntegerDataType.class,
				new InstanceDataTypeFactory(IntegerDataType.class), "Integer",
				IntegerEditor.class, EmptyEditor.class);
		registerVariableDataType(FloatDataType.class,
				new InstanceDataTypeFactory(FloatDataType.class), "Float",
				FloatEditor.class, EmptyEditor.class);
		registerVariableDataType(StringDataType.class,
				new InstanceDataTypeFactory(StringDataType.class), "String",
				StringEditor.class, EmptyEditor.class);
	}

	public static void registerVariableDataType(Class type,
			DataTypeFactory dataTypeFactory, String name,
			Class valueEditorClass, Class dataTypeEditorClass) {
		variableDataTypeRegistry.registerDataType(type, dataTypeFactory, name,
				valueEditorClass, dataTypeEditorClass);
	}

	public static DataTypeRegistry getInstance() {
		return variableDataTypeRegistry;
	}

}
