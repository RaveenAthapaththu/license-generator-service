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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.connector.DatabaseConnectionPool;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.model.License;
import org.wso2.internal.apps.license.manager.util.JsonUtils;
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

public class LicenseDAOImpl implements LicenseDAO, Closeable {

    private static final Logger log = LoggerFactory.getLogger(LibraryDAOImpl.class);
    private Connection connection;

    public LicenseDAOImpl() throws SQLException {

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
    public License getLicense(String key) throws SQLException {

        License license = new License();
        String query = SqlConstants.SELECT_LICENSE_FOR_KEY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    license.setName(resultSet.getString(SqlConstants.PRIMARY_KEY_LICENSE));
                    license.setKey(resultSet.getString(SqlConstants.LICENSE_NAME));
                    license.setUrl(resultSet.getString(SqlConstants.LICENSE_URL));
                }
                return license;
            }catch (SQLException e){
                log.info("Error occured while getting license details", e);
            }
        }
        return null;

    }

    @Override
    public ArrayList<LibraryDetails> getAllLicense(String product, String version) {

        try(ProductDAOImpl productDAO = new ProductDAOImpl()){

            int prodID = productDAO.getProductID(product, version);

            ArrayList<Integer> libArray = productDAO.getLibIDFromLibraryProduct(prodID);

            try(LibraryDAOImpl libraryDAO = new LibraryDAOImpl()) {

                ArrayList<LibraryDetails> listWithLicenseInfo = new ArrayList<>();
                for (int i : libArray) {
                    LibraryDetails libraryDetails = libraryDAO.getLibraryFromID(i);
                    listWithLicenseInfo.add(libraryDetails);
                }
                return listWithLicenseInfo;
            }
        } catch (SQLException | IOException e){
            log.info("Error occured while getting license", e);
        }
        return null;
    }

    @Override
    public String getLicenseKeyByID(int licID) throws SQLException {

        log.info("getLicenseKeyByID");
        String key;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.SELECT_LICENSE_KEY)) {
            preparedStatement.setInt(1, licID);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    key = resultSet.getString(1);
                    return key;
                }else {
                    return "";
                }
            }
        }
    }


    @Override
    public boolean checkLicense(LibraryDetails libraryDetails) throws SQLException {

        boolean isExist = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.SELECT_LIBRARY_LICENSE)) {
            preparedStatement.setString(1, libraryDetails.getJarContent().getName());
            preparedStatement.setString(2, libraryDetails.getVersion());
            preparedStatement.setString(3, libraryDetails.getType());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getString(9) != null) {
                        isExist = true;
                    }
                }
                return isExist;

            }
        }
    }

    @Override
    public JsonArray getAllLicenseAsJson() throws SQLException {

        String query = SqlConstants.SELECT_ALL_LICENSES;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                JsonArray licenseArray = JsonUtils.createJsonArrayFromLicenseResultSet(resultSet);
                log.info("Licenses are retrieved from the LICENSE table.");
                return licenseArray;
            }
        }
    }

    @Override
    public void insertLibraryLicense(String key, int libID) throws SQLException {

        log.info("UPDATE lib with license");
        int licID = getLicenseKey(key);
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.INSERT_LIBRARY_LICENSE,
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(2, libID);
            preparedStatement.setInt(1, licID);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            log.info("SQL exception occurred while adding a libraryLicense", e);
        }
    }

    //return licence_ID from lic_key
    private int getLicenseKey(String key) throws SQLException{
        int licID;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SqlConstants.SELECT_LICENSE)) {
            preparedStatement.setString(1, key);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    licID = resultSet.getInt(1);
                    return licID;
                }else {
                    return 0;
                }
            }
        }
    }
}
