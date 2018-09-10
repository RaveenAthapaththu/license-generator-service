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

package org.wso2.internal.apps.license.manager.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.data.LicenseDAOImpl;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.model.License;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GenerateLicenseFileServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(GenerateLicenseFileServiceImpl.class);

    private String licenseHeader = "\n" +
            "This product is licensed by WSO2 Inc. under Apache License 2.0. The license\n" +
            "can be downloaded from the following locations:\n" +
            "\thttp://www.apache.org/licenses/LICENSE-2.0.html\n" +
            "\thttp://www.apache.org/licenses/LICENSE-2.0.txt\n\n" +

            "This product also contains software under different licenses. This table below\n" +
            "all the contained libraries (jar files) and the license under which they are \n" +
            "provided to you.\n\n" +

            "At the bottom of this licenseText is a table that shows what each license indicated\n" +
            "below is and where the actual text of the license can be found.\n\n";

    public void generateLicenseFile(String product, String version, String packPath)
            throws LicenseManagerDataException, IOException {

        ArrayList<LibraryDetails> jarsWithLicense;
        try (LicenseDAOImpl licenseDAO = new LicenseDAOImpl()) {
            String licenseText = licenseHeader;

            jarsWithLicense = licenseDAO.getAllLicense(product, version);
            Set<String> keys = new HashSet<String>();

            String formatString = String.format("%-80s%-15s%-10s\n", "Name", "Type", "License");
            licenseText += formatString;
            licenseText +=
                    "----------------------------------------------------------------------------------" +
                            "-----------------------\n";
            for (LibraryDetails libraryDetails : jarsWithLicense) {
                formatString = String.format("%-80s%-15s%-10s\n",
                        libraryDetails.getName(),
                        libraryDetails.getType(),
                        libraryDetails.getLicenseKey());
                licenseText += formatString;
                keys.add(libraryDetails.getLicenseKey());
            }
            licenseText += "\n\n\nThe license types used by the above libraries and their information is given " +
                    "below:\n\n";
            for (String key : keys) {
                License licenseDetail = licenseDAO.getLicense(key);
                if (licenseDetail != null) {
                    formatString = String.format("%-15s%s\n%-15s%s\n",
                            licenseDetail.getKey(),
                            licenseDetail.getName(),
                            "",
                            licenseDetail.getUrl());
                }

                licenseText += formatString;
            }
            FileWriter fw = new FileWriter(packPath + File.separator + "LICENSE(" + product + "-" + version + ").TXT");
                log.info("license text" + licenseText);
                fw.write(licenseText);
                fw.close();
        } catch (SQLException e) {
            throw new LicenseManagerDataException("Failed to retrieve licenses from database.", e);
        }
    }

}
