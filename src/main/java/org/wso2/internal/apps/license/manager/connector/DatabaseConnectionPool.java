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

package org.wso2.internal.apps.license.manager.connector;

import org.apache.commons.dbcp2.BasicDataSource;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A singleton class to create a database connection pool of 5 connections.
 */
public class DatabaseConnectionPool {

    private static BasicDataSource dataSource = null;
    private static DatabaseConnectionPool databaseConnectionPool = null;

    private DatabaseConnectionPool() {

        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        int maximumNumberOfConnections = Integer.parseInt(SystemVariableUtil.getValue(Constants
                .DATABASE_CONNECTIONS_MAX_NUMBER, "3"));

        dataSource = new BasicDataSource();
        dataSource.setUrl(databaseUrl);
        dataSource.setUsername(databaseUsername);
        dataSource.setPassword(databasePassword);
        dataSource.setInitialSize(maximumNumberOfConnections);
    }

    public static synchronized DatabaseConnectionPool getDbConnectionPool() {

        if (databaseConnectionPool == null) {
            databaseConnectionPool = new DatabaseConnectionPool();
        }
        return databaseConnectionPool;

    }

    public synchronized Connection getConnection() throws SQLException {

        return dataSource.getConnection();

    }

}
