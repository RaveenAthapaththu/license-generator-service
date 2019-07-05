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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.connector.DatabaseConnectionPool;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.util.SqlConstants;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Library related DB operations
 */
public class LibraryDAOImpl implements LibraryDAO, Closeable {

    private static final Logger log = LoggerFactory.getLogger(LibraryDAOImpl.class);
    private Connection connection;

    public LibraryDAOImpl() throws SQLException {

        DatabaseConnectionPool databaseConnectionPool = DatabaseConnectionPool.getDbConnectionPool();
        connection = databaseConnectionPool.getConnection();
    }

    @Override
    public void close() throws IOException {
        //close the connection and release system resources associated
        try {
            connection.close();
        } catch (SQLException e) {
            log.info("Exception occured while closing connection", e);
        }
    }

    @Override
    public void insertLib(LibraryDetails libraryDetails) {

        //insert Library details to the DB
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.INSERT_LIBRARY,
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, libraryDetails.getName());
            preparedStatement.setString(2, libraryDetails.getVersion());
            preparedStatement.setString(3, libraryDetails.getType());
            preparedStatement.setString(4, libraryDetails.getFileName());

            if (!(libraryExist(libraryDetails))) {
                preparedStatement.execute();
            }

        } catch (SQLException e) {
            log.info("SQL exception occurred while adding a library", e);
        }
    }

    @Override
    public LibraryDetails getLibraryFromID(int libID) {

        LibraryDetails libraryDetails = new LibraryDetails();

        //Get all library details from DB
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.SELECT_LIBRARY_BYID)) {
            preparedStatement.setInt(1, libID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    //Final name is full file name. hence setName is set to lib_FileName
                    libraryDetails.setName(resultSet.getString("lib_FileName"));
                    libraryDetails.setType(resultSet.getString("lib_Type"));
                    int libLicID = (resultSet.getInt("lib_lic_ID"));
                    try (LicenseDAOImpl licenseDAO = new LicenseDAOImpl()) {
                        String licenceKey = licenseDAO.getLicenseKeyByID(libLicID);
                        libraryDetails.setLicenseKey(licenceKey);

                    } catch (IOException e) {
                        log.info("ERROR occurred", e);
                    }

                }
            }
            //returning a list of libraries
            return libraryDetails;
        } catch (SQLException e) {
            log.info("SQL exception occurred while getting data from db.library", e);
        }
        return null;
    }

    @Override
    public List<LibraryDetails> getAllLibraries() {

        List<LibraryDetails> listOfAllLibrariesFromDB = new ArrayList<>();
        LibraryDetails libraryDetails = new LibraryDetails();

        //Get all library details from DB
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.SELECT_ALL_LIBRARIES)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    libraryDetails.setName(resultSet.getString("lib_Name"));
                    libraryDetails.setType(resultSet.getString("lib_Type"));
                    libraryDetails.setVersion(resultSet.getString("lib_Version"));
                    libraryDetails.setFileName(resultSet.getString("lib_Name"));
                    listOfAllLibrariesFromDB.add(libraryDetails);
                }
            }
            //returning a list of libraries
            return listOfAllLibrariesFromDB;
        } catch (SQLException e) {
            log.info("SQL exception occurred while getting data from db.library", e);
        }
        //returning an empty list to avoid null
        return Collections.emptyList();
    }

    private boolean libraryExist(LibraryDetails libraryDetails) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.SELECT_LIBRARY)) {
            preparedStatement.setString(1, libraryDetails.getName());
            preparedStatement.setString(2, libraryDetails.getVersion());
            preparedStatement.setString(3, libraryDetails.getType());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.first();
            }
        }
    }

    @Override
    public int getLibraryID(LibraryDetails libraryDetails) throws SQLException {

        int libID;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.SELECT_LIBRARY)) {
            preparedStatement.setString(1, libraryDetails.getName());
            preparedStatement.setString(2, libraryDetails.getVersion());
            preparedStatement.setString(3, libraryDetails.getType());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    libID = resultSet.getInt(1);
                    return libID;
                } else {
                    insertLib(libraryDetails);
                    getLibraryID(libraryDetails);
                }
            }
        }
        return 0;
    }

    @Override
    public List<LibraryDetails> getAllLibrariesForLicense(ArrayList<Integer> libIDs) {

        return Collections.emptyList();
    }
}
