/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.internal.apps.license.manager.exception;

/**
 * Exceptions that might occur while communicating with the database.
 */
public class LicenseManagerDataException extends Exception {

    /**
     * Constructor with only the message of the exception
     *
     * @param message string message about the exception
     */
    public LicenseManagerDataException(String message) {

        super(message);
    }

    /**
     * Constructor with both message and cause
     *
     * @param message String message about caused exception
     * @param cause   caught exception
     */
    public LicenseManagerDataException(String message, Throwable cause) {

        super(message, cause);
    }
}
