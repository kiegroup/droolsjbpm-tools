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

import java.util.Set;

import org.drools.process.core.datatype.DataTypeFactory;

/**
 * A registry of datatypes.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public interface DataTypeRegistry {
    
	/**
	 * Returns all registered data types.
	 */
    Set getDataTypes();
    
    void registerDataType(Class type, DataTypeFactory dataTypeFactory, String name,
        Class valueEditorClass, Class dataTypeEditorClass);
    
    /**
     * Returns the data type info of the data type with the given type
     * @param type
     * @return the data type info
     * @throws IllegalArgumentException if the data type info of this type cannot be found
     */
    IDataTypeInfo getDataTypeInfo(Class type);
    
    interface IDataTypeInfo {
        Class getType();
        DataTypeFactory getDataTypeFactory();
        String getName();
        Class getDataTypeEditorClass();
        Class getValueEditorClass();
    }
}
