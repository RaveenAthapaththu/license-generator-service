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

package org.wso2.internal.apps.license.manager.data;

import com.google.gson.JsonArray;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.model.License;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface LicenseDAO {

    ArrayList<LibraryDetails> getAllLicense(String product, String License) throws SQLException;
    JsonArray getAllLicenseAsJson() throws SQLException;
    License getLicense(String key) throws SQLException;
    //License getLicenseID(LibraryDetails libraryDetails) throws SQLException;
    boolean checkLicense(LibraryDetails libraryDetails) throws SQLException;
    void insertLibraryLicense(String key, int libID) throws SQLException;
    String getLicenseKeyByID(int licID) throws SQLException;

}
