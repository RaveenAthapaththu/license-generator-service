/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.internal.apps.license.manager.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.impl.AddLicenseServiceAPIImpl;
import org.wso2.internal.apps.license.manager.impl.ExtractPackServiceImpl;
import org.wso2.internal.apps.license.manager.impl.GenerateLicenseFileServiceImpl;
import org.wso2.internal.apps.license.manager.impl.GetLicenseServiceImpl;
import org.wso2.internal.apps.license.manager.impl.GetUploadedPacksApiServiceImpl;
import org.wso2.internal.apps.license.manager.impl.UpdateLibDetailsInDBAPIServiceImpl;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.model.PackDetails;
import org.wso2.internal.apps.license.manager.model.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.JarFileHandler;
import org.wso2.internal.apps.license.manager.util.JsonUtils;
import org.wso2.internal.apps.license.manager.util.TaskHandler;
import org.wso2.internal.apps.license.manager.util.ZipHandler;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Main Service class which contains all the micro service endpoints.
 */

@Path("/")
public class LicenseManagerServiceEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LicenseManagerServiceEndpoint.class);
    private GetUploadedPacksApiServiceImpl getUploadedPacksService = new GetUploadedPacksApiServiceImpl();
    private UpdateLibDetailsInDBAPIServiceImpl updateLibDetails = new UpdateLibDetailsInDBAPIServiceImpl();
    private ExtractPackServiceImpl extractPackService = new ExtractPackServiceImpl();
    private AddLicenseServiceAPIImpl licenseServiceAPI = new AddLicenseServiceAPIImpl();
    private GetLicenseServiceImpl getLicenseService = new GetLicenseServiceImpl();
    private GenerateLicenseFileServiceImpl generateLicenseFile = new GenerateLicenseFileServiceImpl();
    private static final String ACCESS_CONTROL_HEADER = "Access-Control-Allow-Credentials";

    @GET
    @Path("/pack/uploadedPacks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUploadedPacks() {

        JsonObject responseJson = new JsonObject();
        log.info("receving uploaded packs.");

        try {
            // Obtain the list of the available zip files.
            JsonArray responseData = getUploadedPacksService.getListOfPacksName();
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "List of uploaded packs were retrieved.");
            responseJson.add(Constants.RESPONSE_DATA, responseData);
        } catch (LicenseManagerConfigurationException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, e.getMessage());
            log.error("Failed to get the list of uploaded packs. ", e);
        }

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header(ACCESS_CONTROL_HEADER, true)
                .build();
    }

    /**
     * Start the downloading and extracting the selected pack in a new thread.
     *
     * @param request      Post request
     * @param username     logged user
     * @param payload selected pack
     * @return success/failure of starting thread
     */
    @POST
    @Path("/pack/selectedPack")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractJarsForSelectedPack(@Context Request request,
                                               @QueryParam("username") String username,
                                               String payload) {

        log.info("in: extractJarsForSelectedPack");


        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(payload);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String selectedPack = jsonObject.get("packName").getAsString();

        log.info("extracted pack name : " + selectedPack);

        if (TaskHandler.checkForAlreadyRunningTask(selectedPack)) {
            log.info("selected pack failed, already running pack");
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.FAILED);

            return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                    .header(ACCESS_CONTROL_HEADER, true)
                    .build();
        } else {

            log.info("starting extraction");
            log.info("User and packname : =====" + username + "pack " + selectedPack);
            TaskProgress taskProgress = extractPackService.startPackExtractionProcess(username, selectedPack);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.RUNNING);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, taskProgress.getMessage());

            log.info("Extraction in progress?");
            return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                    .header(ACCESS_CONTROL_HEADER, true)
                    .build();
        }
    }

    /**
     * Get the jars with name and version unidentified.
     *
     * @param request  HTTP request object.
     * @param username Username of the user.
     * @return The API response
     */
    @GET
    @Path("/pack/faultyNamedJars/{packName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFaultyNamedJars(@Context Request request,
                                       @QueryParam("username") String username,
                                       @PathParam("packName") String packName) {

        log.info("get faulty named jars");
        log.info("User and packname : =====" + username + "pack " + packName);
        TaskProgress taskProgress = TaskHandler.getTaskByPackName(packName);
        JsonObject responseJson = new JsonObject();
        JsonArray faultyNamedJars;
        String statusMessage = taskProgress.getMessage();

        if (taskProgress.getStatus().equals(Constants.COMPLETE)) {
            log.info("removing duplicates ");
            List<LibraryDetails> errorJarFileList = JarFileHandler.removeDuplicates(taskProgress.getData().getFaultyNamedLibs());
            log.info("Getting faulty named jars");
            faultyNamedJars = JsonUtils.getFaultyNamedJarsAsJsonArray(errorJarFileList);
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
            responseJson.add(Constants.RESPONSE_DATA, faultyNamedJars);
        } else {
            log.info("Not complete?");
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Failed to get data");
        }

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header(ACCESS_CONTROL_HEADER, true)
                .build();
    }

    /**
     * Submit the names and versions for the faulty named jars and identifies the license missing jars.
     *
     * @param request       POST request
     * @param username      logged user
     * @param payload list of jars with new names and version
     * @return list of jars in which the licenses are missing
     */
    @POST
    @Path("/pack/nameDefinedJars")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateNameAndVersionOfJars(@Context Request request,
                                               @QueryParam("username") String username, String payload) {

        log.info("Update name and version in");
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(payload);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String selectedPack = jsonObject.get("packName").getAsString();

        log.info("Update name and Version started with" + selectedPack);
        TaskProgress taskProgress = updateLibDetails.startUpdatingDatabase(selectedPack, JsonUtils
                .getAttributesFromRequestBody(payload, "jars"));
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
        responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.RUNNING);
        responseJson.addProperty(Constants.RESPONSE_MESSAGE, taskProgress.getMessage());

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header(ACCESS_CONTROL_HEADER, true)
                .build();
    }

    /**
     * Get the long running task progress.
     *
     * @param request  HTTP request object.
     * @param packName  of the pack.
     * @return The API response
     */
    @GET
    @Path("/longRunningTask/progress/{packName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatusOfLongRunningTasks(@Context Request request,
                                                @PathParam("packName") String packName) {

        log.info("pack name from long tasks =======" +  packName);
        TaskProgress taskProgress = TaskHandler.getTaskByPackName(packName);
        JsonObject responseJson = new JsonObject();
        String statusMessage = taskProgress.getMessage();

        log.info("checking Longer running tasks");
        // Build the response based on the status of the task.
        log.info("status " + taskProgress.getStatus());
        switch (taskProgress.getStatus()) {

            case Constants.COMPLETE:
                responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
                responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.COMPLETE);
                responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
                break;

            case Constants.RUNNING:
                responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
                responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.RUNNING);
                responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
                break;

            default:
                responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
                responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.FAILED);
                responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
                TaskHandler.deleteTaskByPackName(packName);
                break;
        }

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header(ACCESS_CONTROL_HEADER, true)
                .build();
    }

    /**
     * Get the jars for which the licenses are undefined.
     *
     * @param request  HTTP request object.
     * @param packName packname.
     * @return The API response
     */
    @GET
    @Path("/pack/licenseMissingJars/{packName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicenseMissingJars(@Context Request request,
                                          @QueryParam("username") String username,
                                          @PathParam("packName") String packName) {

        log.info("IN license missing jar");
        TaskProgress taskProgress = TaskHandler.getTaskByPackName(packName);
        JsonObject responseJson = new JsonObject();
        PackDetails packDetails = taskProgress.getData();

        if (taskProgress.getStatus().equals(Constants.COMPLETE)) {
            // Create the response if success
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "License missing jars were identified.");
            responseJson.add(Constants.LICENSE_MISSING_COMPONENTS, JsonUtils.getLicenseMissingJarsAsJsonArray
                    (packDetails.getLicenseMissingComponentLibraries()));
            responseJson.add(Constants.LICENSE_MISSING_LIBRARIES, JsonUtils.getLicenseMissingJarsAsJsonArray
                    (packDetails.getLicenseMissingLibraries()));
            log.info("Misiing jars got");
        } else {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Failed to get data");
        }

        log.info("returning L missing jars");
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Add licenses for the jars which did not have licenses.
     *
     * @param request       POST request
     * @param username      logged user
     * @param stringPayload licenses for the jar
     * @return success/failure of adding licenses
     */
    @POST
    @Path("license/newLicenses")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewLicenseForJars(@Context Request request,
                                         @QueryParam("username") String username,
                                         String stringPayload) {

        log.info("NEW license IN");
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(stringPayload);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String selectedPack = jsonObject.get("packName").getAsString();

        TaskProgress taskProgress = licenseServiceAPI.startInsertingNewLicenses(stringPayload, selectedPack);

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
        responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.RUNNING);
        responseJson.addProperty(Constants.RESPONSE_MESSAGE, taskProgress.getMessage());

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Return the list of available set of licenses in the database.
     *
     * @param request Http request.
     * @return response with licenses.
     */
    @GET
    @Path("/license/availableLicenses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLicenseInformation(@Context Request request) {

        log.info("IN available license");
        JsonObject responseJson = new JsonObject();

        try {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "All the licenses were extracted.");
            responseJson.add(Constants.RESPONSE_DATA, getLicenseService.getListOfAllLicenses());
        } catch (LicenseManagerDataException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, e.getMessage());
            log.error("Failed to retrieve data from the database. " + e.getMessage(), e);
        }

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Request to generate the license text to previously selected pack.
     *
     * @param request  POST request
     * @param username logged user
     * @return success/failure of generating the license text
     */
    @POST
    @Path("/license/text")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateLicenseText(@Context Request request,
                                        @QueryParam("username") String username,
                                        String stringPayload) {

        log.info("NEW license IN");
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(stringPayload);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String selectedPack = jsonObject.get("packName").getAsString();

        JsonObject responseJson = new JsonObject();

        String fileUploadPath = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);
        String productName = null;
        String productVersion = null;

        try {
            PackDetails packDetails = TaskHandler.getTaskByPackName(selectedPack).getData();
            productName = packDetails.getPackName();
            productVersion = packDetails.getPackVersion();
            generateLicenseFile.generateLicenseFile(productName, productVersion, fileUploadPath);
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Done");
        } catch (IOException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Could not generate the license text.");
            log.error("Failed to generate licenses for the product " + productName + "-" + productVersion, e);
        } catch (LicenseManagerDataException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, e.getMessage());
            log.error("Failed to generate licenses for the product " + productName + "-" + productVersion, e);
        }

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Request to download the license text file.
     *
     * @param request  GET request
     * @param username logged user
     * @return the license text file
     */
    @GET
    @Path("/license/textToDownload/{packName}")
    public Response getLicenseTextFile(@Context Request request,
                                       @QueryParam("username") String username,
                                       @PathParam("packName") String packName) {

        log.info("DOWNLOADING TEXT file");

        String mountPath = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);
        PackDetails packDetails = TaskHandler.getTaskByPackName(packName).getData();
        String productName = packDetails.getPackName();
        String productVersion = packDetails.getPackVersion();
        String fileName = "LICENSE(" + productName + "-" + productVersion + ").TXT";
        File file = Paths.get(mountPath, fileName).toFile();
        if (file.exists()) {
            // Clean the storage.
           ZipHandler.cleanFileStorage(productName + "-" + productVersion, mountPath);
            TaskHandler.deleteTaskByPackName(packName);

            log.info("FILE " + file );
            return Response.ok(file)
                    .header("Access-Control-Allow-Credentials", true)
                    .build();
        } else {
            log.error("License file does not exist");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
