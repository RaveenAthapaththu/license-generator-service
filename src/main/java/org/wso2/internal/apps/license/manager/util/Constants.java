/*
 *
 *  * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.internal.apps.license.manager.util;

/**
 * Constants
 */
public class Constants {

    // Environment variables for database access.
    public static final String DATABASE_DRIVER = "LM_DATABASE_DRIVER";
    public static final String DATABASE_URL = "LM_DATABASE_URL";
    public static final String DATABASE_USERNAME = "LM_DATABASE_USERNAME";
    public static final String DATABASE_PASSWORD = "LM_DATABASE_PASSWORD";
    public static final String DATABASE_CONNECTIONS_MAX_NUMBER = "LM_DATABASE_CONNECTIONS_MAX_NUMBER";

    // Environment variables for authentication.
    public static final String LICENSE_MANAGER_APP_USERNAME = "LICENSE_MANAGER_APP_USERNAME";
    public static final String LICENSE_MANAGER_APP_PASSWORD = "LICENSE_MANAGER_APP_PASSWORD";

    // Constants related to response
    public static final String SUCCESS = "done";
    public static final String ERROR = "error";

    // Constants in extracting licenses.
    public static final String JAR_TYPE_WSO2 = "wso2";
    public static final String JAR_TYPE_BUNDLE = "bundle";
    public static final String JAR_TYPE_JAR_IN_BUNDLE = "jarinbundle";
    public static final String JAR_TYPE_JAR = "jar";

    // Progress Status.
    public static final String COMPLETE = "complete";
    public static final String FAILED = "failed";
    public static final String RUNNING = "running";

    // Response json object parameter names.
    public static final String RESPONSE_TYPE = "responseType";
    public static final String RESPONSE_MESSAGE = "responseMessage";
    public static final String RESPONSE_DATA = "responseData";
    public static final String RESPONSE_STATUS = "responseStatus";
    public static final String TASK_STATUS = "taskStatus";
    public static final String LICENSE_MISSING_COMPONENTS = "component";
    public static final String LICENSE_MISSING_LIBRARIES = "library";

    // Environment variables for FTP server access.
    public static final String FILE_DOWNLOAD_PATH = "LICENSE_MANAGER_FILE_DOWNLOAD_PATH";
    public static final String FTP_HOST = "LICENSE_MANAGER_FTP_HOST";
    public static final String FTP_PORT = "LICENSE_MANAGER_FTP_PORT";
    public static final String FTP_USERNAME = "LICENSE_MANAGER_FTP_USERNAME";
    public static final String FTP_PASSWORD = "LICENSE_MANAGER_FTP_PASSWORD";
    public static final String FTP_FILE_LOCATION = "LICENSE_MANAGER_FTP_FILE_LOCATION";
    // Environment variables for sending emails.
    public static final String EMAIL_USERNAME = "LICENSE_MANAGER_EMAIL_USERNAME";
    public static final String EMAIL_PASSWORD = "LICENSE_MANAGER_EMAIL_PASSWORD";
    public static final String LICENSE_MANAGER_ADMINS = "LICENSE_MANAGER_ADMINS";

    //
    public static final int PACK_EXTRACTION_STEP_ID = 1;
    public static final int UPDATE_DB_STEP_ID = 2;
    public static final int INSERT_LICENSE_STEP_ID = 3;

    /**
     * hide public constructor
     */
    private Constants() {
        // Do nothing.
    }

}
