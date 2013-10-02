/*
 * Copyright 2013 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gieseckedevrient.android.servicelayertester;

/**
*
* Class used to identify the different types of API Provider Tests.
*
* @author openmobileapis
*
*/
public final class ProviderType {

    /** Id used for Discovery Provider API Test. */
    public static final int DISCOVERY = 0;
    /** Id used for Authentication Provider API Test. */
    public static final int AUTHENTICATION = 1;
    /** Id used for File View Provider API Test. */
    public static final int FILE_MANAGEMENT = 2;
    /** Id used for PKCS15 Provider API Test. */
    public static final int PKCS15 = 3;
    /** Id used for Secure Storage Provider API Test. */
    public static final int SECURE_STORAGE = 4;

    /**
    *
    * Constructor of the class.
    *
    */
    private ProviderType() {
    }
}
