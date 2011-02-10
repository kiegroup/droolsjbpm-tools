/*
 * Copyright 2010 JBoss Inc
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

package org.drools.eclipse.builder;

public class DroolsBuildMarker {

    private String text;
    private int line = -1;
    private int offset = -1;
    private int length = -1;

    public DroolsBuildMarker(String text) {
        this.text = text;
    }

    public DroolsBuildMarker(String text, int line) {
        this.text = text;
        this.line = line;
    }

    public DroolsBuildMarker(String text, int offset, int length) {
        this.text = text;
        this.offset = offset;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getLine() {
        return line;
    }

    public int getOffset() {
        return offset;
    }

    public String getText() {
        return text;
    }
}
