/*
 * ******************************************************************************
 *   Copyright (c) 2014-2015 Ivan Gaglioti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */
package org.symptomcheck.capstone.model;

import com.activeandroid.serializer.TypeSerializer;


/**
 * Created by igaglioti on 03/11/2014.
 */
public class StringArraySerializer  extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return String[].class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public Object serialize(Object data) {
        if (data == null) {
            return null;
        }
        return toString((String[])data);
    }
    @Override
    public String[] deserialize(Object data) {
        if (data == null) {
            return null;
        }
        return toArray((String)data);
    }

    public String[] toArray(String value) {
        String[] values = value.split(",");
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }

    private String toString(String[] values) {
        String result = "";
        for (int i = 0; i < values.length; i++) {
            result += values[i];
            if (i < values.length - 1) {
                result += ",";
            }
        }
        return result;
    }
}
