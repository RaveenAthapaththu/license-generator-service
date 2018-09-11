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
 * SQL queries
 */
public class SqlConstants {

    /**
     * Private constructor to hide public constructor
     */
    private SqlConstants() {

    }

    public static final String INSERT_PRODUCT = "INSERT INTO product (prod_Name, prod_Version) VALUES (?,?)";
    public static final String INSERT_LIBRARY = "INSERT INTO  library (lib_Name, lib_Version, lib_Type) VALUES (?,?,?)";
    public static final String INSERT_LIBRARY_LICENSE = "UPDATE library SET lib_lic_ID = ? WHERE lib_ID = ?";
    public static final String SELECT_ALL_LICENSES = "SELECT * FROM license";
    public static final String SELECT_PRODUCT = "SELECT prod_ID FROM product WHERE prod_Name=? AND " + "prod_Version=?";
    public static final String SELECT_LIBRARY_PRODUCT_ID = "SELECT lp_lib_ID FROM library_product WHERE lp_prod_ID=?";
    public static final String SELECT_LIBRARY = "SELECT LIB_ID FROM library WHERE lib_Name=? AND lib_Version=? AND" +
            " lib_Type=?";
    public static final String SELECT_LIBRARY_BYID = "SELECT * FROM library WHERE lib_ID=?";
    public static final String INSERT_INTO_PRODUCT_LICENSE = "INSERT IGNORE INTO library_product (lp_lib_ID, " +
            "lp_prod_ID) VALUES (?,?)";
    public static final String SELECT_LICENSE = "SELECT lic_ID FROM license WHERE lic_key=?";
    public static final String SELECT_LICENSE_KEY = "SELECT lic_Key FROM license WHERE lic_ID=?";
    public static final String SELECT_LIBRARY_LICENSE = "SELECT * FROM library WHERE lib_Name=? AND lib_Version=? AND" +
            " lib_Type=?";
    public static final String SELECT_ALL_LIBRARIES = "SELECT * FROM LIBRARY";
    public static final String SELECT_LICENSE_FOR_ANY_COMP = "SELECT LICENSE_KEY FROM LM_COMPONENT_LICENSE WHERE " +
            "COMP_KEY = (SELECT COMP_KEY FROM LM_COMPONENT WHERE COMP_NAME=? LIMIT 1)";
    public static final String SELECT_LICENSE_FOR_ANY_LIB = "SELECT LICENSE_KEY FROM LM_LIBRARY_LICENSE WHERE LIB_ID " +
            "= (SELECT LIB_ID FROM LM_LIBRARY WHERE LIB_NAME=? LIMIT 1)";
    public static final String SELECT_LICENSE_FOR_KEY = "SELECT * FROM license WHERE lic_Key=?";

    public static final String PRIMARY_KEY_LIBRARY = "LIB_ID";
    public static final String PRIMARY_KEY_PRODUCT = "PROD_ID";
    public static final String PRIMARY_KEY_LICENSE = "lic_Key";
    public static final String LICENSE_ID = "lic_ID";
    public static final String LICENSE_NAME = "lic_Name";
    public static final String LICENSE_URL = "lic_URL";
    public static final String COMPONENT_KEY = "COMP_KEY";
    public static final String COMPONENT_TYPE = "COMP_TYPE";

}
