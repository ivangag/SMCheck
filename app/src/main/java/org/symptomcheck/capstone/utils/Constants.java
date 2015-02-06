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
package org.symptomcheck.capstone.utils;

/**
 * Created by igaglioti on 17/11/2014.
 */
public class Constants {

    public final static class HTTP_STATUS_CODES {
        public static final int OK = 200;
        public static final int UNAUTHORIZED = 401;
        public static final int INTERNAL_ERROR = 500;
    }

    public final static class STRINGS{
        public static final String EMPTY = "";
        public static final String YES = "YES";
        public static final String NO = "NO";
    }

    public final static class TIME {
        public static final String GMT00 = "GMT+00";
        public static final String DEFAULT_FORMAT = STRINGS.EMPTY;
    }
}
