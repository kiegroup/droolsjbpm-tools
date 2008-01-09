package org.drools.eclipse.flow.common.datatype.impl;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.drools.eclipse.flow.common.datatype.DataTypeRegistry;
import org.drools.process.core.datatype.DataTypeFactory;

/**
 * Default implementation of a datatype registry.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DataTypeRegistryImpl implements DataTypeRegistry {

    private Map dataTypes = new HashMap();
    
    public Set getDataTypes() {
        return new HashSet(dataTypes.values());
    }

    public void registerDataType(Class type, DataTypeFactory dataTypeFactory, String name,
            Class valueEditorClass, Class dataTypeEditorClass) {
        if (dataTypeFactory == null) {
            throw new NullPointerException("Data type factory may not be null");
        }
        if (name == null) {
            throw new NullPointerException("Name may not be null");
        }
        if (valueEditorClass == null) {
            throw new NullPointerException("valueEditorClass may not be null");
        }
        if (dataTypeEditorClass == null) {
            throw new NullPointerException("dataTypeEditorClass may not be null");
        }
        dataTypes.put(type, new DataTypeInfo(
            type, dataTypeFactory, name, valueEditorClass, dataTypeEditorClass));
    }
    
    public IDataTypeInfo getDataTypeInfo(Class type) {
        IDataTypeInfo dataTypeInfo = (IDataTypeInfo) dataTypes.get(type);
        if (dataTypeInfo == null) {
            throw new IllegalArgumentException("Cannot find data type info with type " + type);
        }
        return dataTypeInfo;
    }
    
    public class DataTypeInfo implements IDataTypeInfo {
        private Class type;
        private DataTypeFactory dataTypeFactory;
        private String name;
        private Class valueEditorClass;
        private Class dataTypeEditorClass;
        private DataTypeInfo(Class type, DataTypeFactory dataTypeFactory, String name,
                Class valueEditorClass, Class dataTypeEditorClass) {
            this.type = type;
            this.dataTypeFactory = dataTypeFactory;
            this.name = name;
            this.valueEditorClass = valueEditorClass;
            this.dataTypeEditorClass = dataTypeEditorClass;
        }
        public Class getType() {
            return type;
        }
        public DataTypeFactory getDataTypeFactory() {
            return dataTypeFactory;
        }
        public String getName() {
            return name;
        }
        public Class getValueEditorClass() {
            return valueEditorClass;
        }
        public Class getDataTypeEditorClass() {
            return dataTypeEditorClass;
        }
    }
}
