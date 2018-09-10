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
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.data.LibraryDAOImpl;
import org.wso2.internal.apps.license.manager.data.LicenseDAOImpl;
import org.wso2.internal.apps.license.manager.data.ProductDAOImpl;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.model.PackDetails;
import org.wso2.internal.apps.license.manager.model.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.TaskHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * class update db
 */
public class UpdateLibDetailsInDBAPIServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(UpdateLibDetailsInDBAPIServiceImpl.class);

    /**
     * @param packName             package name
     * @param jarsWithDefinedNames jars with proper names
     * @return task
     */
    public TaskProgress startUpdatingDatabase(String packName, JsonArray jarsWithDefinedNames) {

        TaskProgress taskProgress = TaskHandler.getTaskByPackName(packName);
        taskProgress.setStatus(Constants.RUNNING);
        taskProgress.setStepNumber(Constants.UPDATE_DB_STEP_ID);
        taskProgress.setMessage("Start updating the database");
        log.info("Start updating the database");
        new Thread(() -> {
            try {
                taskProgress.setExecutingThreadId(Thread.currentThread().getId());
                updateJarInfo(jarsWithDefinedNames, taskProgress);
                taskProgress.setStatus(Constants.COMPLETE);
            } catch (LicenseManagerDataException e) {
                taskProgress.setStatus(Constants.FAILED);
                taskProgress.setMessage("Failed to add jar information into the database.");
                log.error("Failed to add jar information into the database.", e);
            }
        }).start();
        return taskProgress;

    }

    /**
     *
     * @param jarsWithDefinedNames d
     * @param taskProgress s
     * @throws LicenseManagerDataException w
     */
    private void updateJarInfo(JsonArray jarsWithDefinedNames, TaskProgress taskProgress) throws
            LicenseManagerDataException {

        log.info("update faulty named list");
        updateFaultyNamedListOfJars(taskProgress.getData(), jarsWithDefinedNames);
        log.info("enters to DB");
        enterJarsIntoDB(taskProgress);

    }

    /**
     * Update the name and version of the list of jars which name and version is undefined --with user input-- .
     *
     * @param packDetails          java object which contains jar object details
     * @param jarsWithDefinedNames json array which hols the user inputs
     */
    private void updateFaultyNamedListOfJars(PackDetails packDetails, JsonArray jarsWithDefinedNames) {

        // Define the name and the version from the user input

        for (int i = 0; i < jarsWithDefinedNames.size(); i++) {
            JsonObject jar = jarsWithDefinedNames.get(i).getAsJsonObject();
            int index = jar.get("index").getAsInt();
            packDetails.getFaultyNamedLibs().get(index).setProduct(jar.get("name").getAsString());
            packDetails.getFaultyNamedLibs().get(index).setVersion(jar.get("version").getAsString());
        }

        // Add name defined jars into the jar list of the jar holder.
        for (LibraryDetails jarFile : packDetails.getFaultyNamedLibs()) {
            packDetails.getLibFilesInPack().add(jarFile);
        }
    }

    /**
     * Recursively insert the information of all jars extracted from the pack into the database.
     *
     * @throws LicenseManagerDataException if the data insertion fails
     */
    private void enterJarsIntoDB(TaskProgress taskProgress) {

        int productID = 0;
        log.info("enter to db in");
        PackDetails packDetails = taskProgress.getData();

        try(ProductDAOImpl productDAO = new ProductDAOImpl()){
            productID = productDAO.getProductID(packDetails.getPackName(), packDetails.getPackVersion());

            if (productID < 0){
                log.info("product id " + productID);
               productID = productDAO.insertProduct(packDetails.getPackName(), packDetails.getPackVersion());
            }
        } catch (SQLException | IOException e){
            log.info("Error occured while adding Product", e);
        }

        List<LibraryDetails> licenseMissingLibraries = new ArrayList<>();
        List<LibraryDetails> licenseMissingComponents = new ArrayList<>();

        //check if the license available for the jar file x
        //jar x -> unq(name , ver, type)
        for (LibraryDetails libraryDetails : packDetails.getLibFilesInPack()){
            String name = libraryDetails.getJarContent().getName();
            String version = libraryDetails.getVersion();
            String type = libraryDetails.getType();

            try(ProductDAOImpl productDAO = new ProductDAOImpl()) {



                try (LibraryDAOImpl libraryDAO = new LibraryDAOImpl()) {
                    //insert to db if not present
                    libraryDAO.insertLib(libraryDetails);
                    int prodID = productDAO.getProductID(packDetails.getPackName(), packDetails.getPackVersion());
                    int libID = libraryDAO.getLibraryID(libraryDetails);

                    log.info("product id & Library ID" + productID + libID);
                    productDAO.insertLibraryProduct(libID,prodID);

                    //update libs to map
                }

                try(LicenseDAOImpl licenseDAO = new LicenseDAOImpl()){
                    //check if license available
                    boolean isLicenseExist = licenseDAO.checkLicense(libraryDetails);

                    if (!isLicenseExist && libraryDetails.getType().equals("wso2")){
                        licenseMissingComponents.add(libraryDetails);
                    } else if(!isLicenseExist){
                        licenseMissingLibraries.add(libraryDetails);
                    }

                }

            }catch (SQLException e){
                log.info("SQL error occurred", e);
            } catch (IOException e){
                log.info("IO error occurred", e);
            }
        }

        log.info("pack licensce missing details are set");
        packDetails.setPackId(productID);
        log.info("LM libraries are set");
        packDetails.setLicenseMissingComponentLibraries(licenseMissingComponents);
        packDetails.setLicenseMissingLibraries(licenseMissingLibraries);

    }

}
