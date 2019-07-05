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

import org.wso2.internal.apps.license.manager.model.LibraryDetails;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Library DAO
 */
public interface LibraryDAO {

    void insertLib(LibraryDetails libraryDetails);
    List<LibraryDetails> getAllLibraries();
    LibraryDetails getLibraryFromID(int libID);
    List<LibraryDetails> getAllLibrariesForLicense(ArrayList<Integer> libIDs);
    int getLibraryID(LibraryDetails libraryDetails) throws SQLException;



}
