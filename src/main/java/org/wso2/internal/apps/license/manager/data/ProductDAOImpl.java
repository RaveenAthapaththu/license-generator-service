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
import org.wso2.internal.apps.license.manager.model.Product;
import org.wso2.internal.apps.license.manager.util.SqlConstants;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ProductDAOImpl implements ProductDAO, Closeable {

    private static final Logger log = LoggerFactory.getLogger(ProductDAOImpl.class);
    private Connection connection;

    public ProductDAOImpl() throws SQLException {
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
    public int getProductID(String product, String version) throws SQLException {

        int productId = -1;

        String query = SqlConstants.SELECT_PRODUCT;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, product);
            preparedStatement.setString(2, version);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    productId = rs.getInt(SqlConstants.PRIMARY_KEY_PRODUCT);
                }
                return productId;
            }
        }
    }

    @Override
    public void insertLibraryProduct(int libID, int prodID) throws SQLException {

        String insertProductLibrary = SqlConstants.INSERT_INTO_PRODUCT_LICENSE;
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertProductLibrary)) {
            preparedStatement.setInt(1, libID);
            preparedStatement.setInt(2, prodID);
            preparedStatement.executeUpdate();
            log.info("Successfully inserted the product - library relationship for the product id " +
                        prodID +
                        " and library id " + libID + " into LM_LIBRARY_PRODUCT table.");
        }
    }

    @Override
    public int insertProduct(String product, String version) throws SQLException {

        String insertProduct = SqlConstants.INSERT_PRODUCT;
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertProduct, Statement
                .RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, product);
            preparedStatement.setString(2, version);
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                int id = -1;
                while (resultSet.next()) {
                    id = resultSet.getInt("GENERATED_KEY");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Successfully inserted the product " + product + " into LM_PRODUCT table.");
                }
                return id;
            }
        }
    }

    @Override
    public ArrayList<Integer> getLibIDFromLibraryProduct(int prodID) throws SQLException {

        log.info("getLibIDFromLibraryProduct");
        ArrayList<Integer> libArray = new ArrayList<>();
        int libProductId = 0;

        String query = SqlConstants.SELECT_LIBRARY_PRODUCT_ID;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, prodID);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    libProductId = rs.getInt("lp_lib_ID");
                    libArray.add(libProductId);
                }
                return libArray;
            }
        }
    }
}
