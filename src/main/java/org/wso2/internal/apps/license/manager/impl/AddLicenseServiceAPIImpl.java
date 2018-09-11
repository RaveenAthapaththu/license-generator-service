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

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.data.LibraryDAOImpl;
import org.wso2.internal.apps.license.manager.data.LicenseDAOImpl;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.model.License;
import org.wso2.internal.apps.license.manager.model.PackDetails;
import org.wso2.internal.apps.license.manager.model.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.JsonUtils;
import org.wso2.internal.apps.license.manager.util.TaskHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

public class AddLicenseServiceAPIImpl {

    private static final Logger log = LoggerFactory.getLogger(AddLicenseServiceAPIImpl.class);

    public TaskProgress startInsertingNewLicenses(String payload, String packName) {

        TaskProgress taskProgress = TaskHandler.getTaskByPackName(packName);
        taskProgress.setStatus(Constants.RUNNING);
        taskProgress.setMessage("Start inserting new licenses");
        taskProgress.setStepNumber(Constants.INSERT_LICENSE_STEP_ID);
        new Thread(() -> {
            try {
                taskProgress.setExecutingThreadId(Thread.currentThread().getId());
                PackDetails packDetails = taskProgress.getData();
                updateLicenses(packDetails, payload, packName);
                taskProgress.setStatus(Constants.COMPLETE);
            } catch (LicenseManagerDataException e) {
                taskProgress.setStatus(Constants.FAILED);
                taskProgress.setMessage("Failed to add jar information into the database.");
                log.error("Failed to add jar information into the database.", e);
            } catch (MessagingException e) {
                taskProgress.setStatus(Constants.COMPLETE);
                taskProgress.setMessage("Failed to send the email to admin.");
                log.error("Failed to send the email to admin.", e);
            }
        }).start();
        return taskProgress;
    }

    private void updateLicenses(PackDetails packDetails, String payload, String username) throws
            LicenseManagerDataException, MessagingException {

        JsonArray componentsJson = JsonUtils.getAttributesFromRequestBody(payload, "components");
        JsonArray librariesJson = JsonUtils.getAttributesFromRequestBody(payload, "libraries");
        updateLicensesOfLicenseMissingJars(packDetails.getLicenseMissingComponentLibraries(), componentsJson);
        updateLicensesOfLicenseMissingJars(packDetails.getLicenseMissingLibraries(), librariesJson);
        insertNewLicensesToDb(packDetails);
    }

    /**
     * Update the licenses of the license missing jars with the user input.
     *
     * @param licenseMissingJarList list of license missing jars
     * @param licenseDefinedJars    user inputs for the licenses
     */
    private void updateLicensesOfLicenseMissingJars(List<LibraryDetails> licenseMissingJarList,
                                                    JsonArray licenseDefinedJars) {

        for (int i = 0; i < licenseDefinedJars.size(); i++) {
            int index = licenseDefinedJars.get(i).getAsJsonObject().get("index").getAsInt();
            String licenseKey = licenseDefinedJars.get(i).getAsJsonObject().get("licenseKey").getAsString();
            licenseMissingJarList.get(index).setLicenseKey(licenseKey);
        }
    }

    /**
     * Insert new licenses for the jars into the database and send mail to the admin.
     *
     * @param packDetails PackDetails obj
     * @throws LicenseManagerDataException if the data insertion fails
     * @throws MessagingException          if sending mail fails
     */
    private void insertNewLicensesToDb(PackDetails packDetails)
            throws LicenseManagerDataException, MessagingException {

        Boolean isInsertionSuccess = false;

        List<License> newLicenseEntryComponentList = null;
        List<License> newLicenseEntryLibraryList = null;
        try (LicenseDAOImpl licenseDAO = new LicenseDAOImpl()) {
            newLicenseEntryComponentList = insertComponentLicenses(packDetails.getLicenseMissingComponentLibraries(),
                    packDetails.getPackId(), licenseDAO);
            newLicenseEntryLibraryList = insertLibraryLicenses(packDetails.getLicenseMissingLibraries(),
                    packDetails.getPackId(), licenseDAO);
            isInsertionSuccess = true;
        } catch (SQLException e) {
            throw new LicenseManagerDataException("Failed to add licenses.", e);
        } catch (IOException e) {
            log.error("Failed to close the database connection while adding new licenses for the jars. " +
                    e.getMessage(), e);
        } finally {
            //TODO send email
        }
    }

    private List<License> insertComponentLicenses(List<LibraryDetails> componentList, int productId,
                                                  LicenseDAOImpl licenseDAO) throws
            SQLException {

        List<License> newLicenseEntryComponentList = new ArrayList<>();

        for (LibraryDetails licenseMissingJar : componentList) {
            String name = licenseMissingJar.getProduct();
            String componentName = licenseMissingJar.getJarContent().getName();
            String licenseKey = licenseMissingJar.getLicenseKey();
            String version = licenseMissingJar.getVersion();
            try (LibraryDAOImpl libraryDAO = new LibraryDAOImpl()) {
                int libId = libraryDAO.getLibraryID(licenseMissingJar);

                licenseDAO.insertLibraryLicense(licenseKey, libId);

            } catch (IOException e) {
                log.info("IO Exception", e);
            }

            License newEntry = new License();
            newEntry.setName(componentName);
            newEntry.setKey(licenseKey);
            newLicenseEntryComponentList.add(newEntry);
        }
        return newLicenseEntryComponentList;
    }

    private List<License> insertLibraryLicenses(List<LibraryDetails> libraryList, int productId,
                                                LicenseDAOImpl licenseDAO) throws SQLException {

        List<License> newLicenseEntryLibraryList = new ArrayList<>();
        for (LibraryDetails licenseMissingJar : libraryList) {
            String name = licenseMissingJar.getProduct();
            String libraryFileName = licenseMissingJar.getJarContent().getName();
            String licenseKey = licenseMissingJar.getLicenseKey();
            String version = licenseMissingJar.getVersion();
            String type = licenseMissingJar.getType();
            String componentKey = null;
            LibraryDetails parent = null;

            if (licenseMissingJar.getJarContent().getParent() != null) {
                parent = licenseMissingJar.getParent();
            }

            try (LibraryDAOImpl libraryDAO = new LibraryDAOImpl()) {
                int libId = libraryDAO.getLibraryID(licenseMissingJar);

                licenseDAO.insertLibraryLicense(licenseKey, libId);

            } catch (IOException e) {
                log.info("IO Exception", e);
            }

            License newEntry = new License();
            newEntry.setName(libraryFileName);
            newEntry.setKey(licenseKey);
            newLicenseEntryLibraryList.add(newEntry);
        }
        return newLicenseEntryLibraryList;
    }

}
