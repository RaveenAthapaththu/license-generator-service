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

import org.apache.commons.lang.StringUtils;
import org.op4j.Op;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.model.LibraryDetails;
import org.wso2.internal.apps.license.manager.model.PackDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles JAR files
 */
public class JarFileHandler {

    private static final Logger log = LoggerFactory.getLogger(JarFileHandler.class);

    /**
     * Recursively check all the jars in the product.
     *
     * @param file path to the pack.
     * @throws LicenseManagerRuntimeException If file unzipping or extraction fails.
     */
    public PackDetails extractJarsRecursively(String file) throws LicenseManagerRuntimeException {

        //check for the file
        if (StringUtils.isEmpty(file) || !new File(file).exists() || !new File(file).isDirectory()) {
            throw new LicenseManagerRuntimeException("Folder is not found in the location");
        }

        //create a folder to extract content
        PackDetails packDetails = new PackDetails();
        String targetFolder = new File(file).getName();
        String uuid = UUID.randomUUID().toString();
        String tempFolderToHoldJars = new File(file).getParent() + File.separator + uuid;

        //Get all the JARs inside the zip
        List<LibraryDetails> jarFilesInPack = findDirectJars(file);

        //Get all the JARs with faulty name from All the JARs found
        List<LibraryDetails> faultyNamedJars = findAllJars(tempFolderToHoldJars, jarFilesInPack);

        packDetails.setPackName(getName(targetFolder));
        packDetails.setPackVersion(getVersion(targetFolder));
        packDetails.setLibFilesInPack(jarFilesInPack);
        packDetails.setFaultyNamedLibs(faultyNamedJars);
        return packDetails;
    }

    /**
     * Obtain the direct jars contained in the pack.
     *
     * @param path path to the pack file
     */
    private List<LibraryDetails> findDirectJars(String path) {

        List<File> directZips = find(path);
        List<LibraryDetails> listOfDirectJarsInPack = new ArrayList<>();

        for (File directZip : directZips) {
            LibraryDetails currentJarFile = createJarObjectFromFile(directZip, null);
            listOfDirectJarsInPack.add(currentJarFile);
        }
        return listOfDirectJarsInPack;
    }

    /**
     * @param file Jar File
     * @return boolean is file JAR / MAR
     */
    private static boolean accept(File file) {
        //is a file a JAR or MAR ?
        return file.getName().endsWith(".jar") || file.getName().endsWith(".mar");
    }

    /**
     * @param path Folder Path
     * @return List of jars found
     */
    private List<File> find(String path) {

        List<File> files = new ArrayList<>();
        Stack<File> directories = new Stack<>();
        directories.add(new File(path));
        while (!directories.empty()) {
            File next = directories.pop();
            directories.addAll(Op.onArray(next.listFiles(File::isDirectory)).toList().get());

            files.addAll(Op.onArray(next.listFiles(JarFileHandler::accept)).toList().get());
        }
        return files;
    }

    /**
     * Set the values for the attributes of the Jar object.
     *
     * @param fileContainingJar jar file to create a JarFile.java object
     * @param parent            parent jar of the corresponding jar
     * @return JarFile object
     */
    private LibraryDetails createJarObjectFromFile(File fileContainingJar, LibraryDetails parent) {

        LibraryDetails jar = new LibraryDetails();
        setNameAndVersionOfJar(fileContainingJar, jar);
        jar.setJarContent(fileContainingJar);
        jar.setParent(parent);
        return jar;
    }

    /**
     * Set the values for the attributes of the Jar object.
     *
     * @param fileContainingJar jar file to create a JarFile.java object
     * @param jar               JarFile java object
     */
    private void setNameAndVersionOfJar(File fileContainingJar, LibraryDetails jar) {

        String jarName = getName(fileContainingJar.getName());
        String jarVersion = getVersion(fileContainingJar.getName());

        if (StringUtils.isEmpty(jarName) || StringUtils.isEmpty(jarVersion)) {
            jar.setValidName(false);
            //jar.setProduct(getDefaultName(fileContainingJar.getName()));
            jar.setName(getDefaultName(fileContainingJar.getName()));
            jar.setFileName(fileContainingJar.getName());
            jar.setVersion("1.0.0");
        } else {
            jar.setValidName(true);
            jar.setName(jarName);
            jar.setFileName(fileContainingJar.getName());
            log.info("FILE NAME :" + fileContainingJar.getName());
            //jar.setProduct(jarName);
            jar.setVersion(jarVersion);
        }

    }

    /**
     * Extract the name of the jar from the file name.
     *
     * @param name file name of the jar
     * @return name of the jar
     */
    private static String getName(String name) {

        String extractedName = null;

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' || name.charAt(i) == '_') && (Character.isDigit(name.charAt(i + 1)) || name.charAt(i + 1) == 'S' || name.charAt(i + 1) == 'r')) {

                extractedName = name.substring(0, i);
            }
        }
        return extractedName;
    }

    /**
     * Extract the version of the jar from the file name.
     *
     * @param name file name of the jar
     * @return version of the jar
     */
    private static String getVersion(String name) {

        String extractedVersion = null;

        name = name.replace(".jar", "");
        name = name.replace(".mar", "");

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' || name.charAt(i) == '_') && (Character.isDigit(name.charAt(i + 1)) || name.charAt(i + 1) == 'S' || name.charAt(i + 1) == 'r')) {
                extractedVersion = name.substring(i + 1, name.length());
            }
        }
        return extractedVersion;
    }

    /**
     * @param filename d
     * @return d
     */
    private String getDefaultName(String filename) {

        if (filename.endsWith(".jar") || filename.endsWith(".mar")) {
            filename = filename.replace(".jar", "");
            filename = filename.replace(".mar", "");
        }

        return filename;
    }

    /**
     * Find all the jars including inner jars which are inside another jar.
     *
     * @param tempFolderToHoldJars File path to extract the jars.
     * @throws LicenseManagerRuntimeException if the jar extraction fails.
     */
    private List<LibraryDetails> findAllJars(String tempFolderToHoldJars, List<LibraryDetails> jarFilesInPack) throws LicenseManagerRuntimeException {

        boolean check = new File(tempFolderToHoldJars).mkdir();

        Stack<LibraryDetails> zipStack = new Stack<>();
        List<LibraryDetails> faultyNamedJars = new ArrayList<>();

        zipStack.addAll(jarFilesInPack);
        jarFilesInPack.clear();
        tempFolderToHoldJars = tempFolderToHoldJars + File.separator;

        while (!zipStack.empty()) {
            LibraryDetails jarFile = zipStack.pop();
            File fileToBeExtracted = jarFile.getJarContent();
            File extractTo;

            // Get information from the Manifest file.
            Manifest manifest;
            try (java.util.jar.JarFile jarFile1 = new java.util.jar.JarFile(fileToBeExtracted)) {
                manifest = jarFile1.getManifest();
            } catch (IOException e) {
                throw new LicenseManagerRuntimeException(e.getMessage());
            }
            if (manifest != null) {
                setNameAndVersionOfJar(jarFile.getJarContent(), jarFile);
                jarFile.setisBundle(getIsBundle(manifest));
                jarFile.setType(getType(manifest, jarFile));
                jarFile.setVendor(setVendor(manifest, jarFile));
                if (!jarFile.isValidName()) {
                    faultyNamedJars.add(jarFile);
                } else {
                    jarFilesInPack.add(jarFile);
                }
            }

            // If a jarFile contains jars inside, extract the parent jarFile.
            if (checkInnerJars(fileToBeExtracted.getAbsolutePath())) {
                extractTo = new File(tempFolderToHoldJars + fileToBeExtracted.getName());
                extractTo.mkdir();
                ZipHandler.unzip(fileToBeExtracted.getAbsolutePath(), extractTo.getAbsolutePath());
                List<File> listOfInnerFiles =
                        Op.onArray(extractTo.listFiles(file -> (file.getName().endsWith(".jar") || file.getName().endsWith(".mar")))).toList().get();
                for (File nextFile : listOfInnerFiles) {
                    zipStack.add(createJarObjectFromFile(nextFile, jarFile));
                }
            }
        }
        return faultyNamedJars;
    }

    /**
     * Returns the type of the jarFile by evaluating the Manifest file.
     *
     * @param man     Manifest of the jarFile
     * @param jarFile jarFile for which the type is needed
     * @return type of the jarFile
     */
    private String getType(Manifest man, LibraryDetails jarFile) {

        if (jarFile.getParent() == null)
            return (jarFile.isBundle()) ? Constants.JAR_TYPE_BUNDLE : Constants.JAR_TYPE_JAR;
        else return Constants.JAR_TYPE_JAR_IN_BUNDLE;

    }

    /**
     * Returns whether a given jar is a bundle or not
     *
     * @param manifest Manifest of the jar file
     * @return true/false
     */
    private boolean getIsBundle(Manifest manifest) {

        Attributes map = manifest.getMainAttributes();
        String bundleManifest = map.getValue("Bundle-ManifestVersion");
        return bundleManifest != null;
    }

    /**
     * Checks whether a jar file contains other jar files inside it.
     *
     * @param filePath absolute path to the jar
     * @return true/false
     * @throws LicenseManagerRuntimeException if file input stream fails.
     */
    private boolean checkInnerJars(String filePath) throws LicenseManagerRuntimeException {

        boolean containsJars = false;

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(filePath))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().endsWith(".jar") || entry.getName().endsWith(".mar")) {
                    containsJars = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new LicenseManagerRuntimeException("Failed to check the inner jars. ", e);
        }

        return containsJars;
    }

    /**
     * Remove the duplicates in the faulty named jars.
     *
     * @param faultyNamedJars list of jars in which the names and version are missing
     * @return unique list of jars with faulty names.
     */
    public static List<LibraryDetails> removeDuplicates(List<LibraryDetails> faultyNamedJars) {

        List<LibraryDetails> faultyNamedUniqueJarFiles = new ArrayList<>();
        for (LibraryDetails jarFile : faultyNamedJars) {
            boolean newJar = true;
            for (LibraryDetails uniqueJarFile : faultyNamedUniqueJarFiles) {
                if (jarFile.getName().equals(uniqueJarFile.getName())) {
                    newJar = false;
                }
            }
            if (newJar) {
                faultyNamedUniqueJarFiles.add(jarFile);
            }
        }
        return faultyNamedUniqueJarFiles;
    }

    private String setVendor(Manifest man, LibraryDetails jarFile) {

        Attributes map = man.getMainAttributes();
        String name = map.getValue("Bundle-Name");
        if ((name != null && name.startsWith("org.wso2")) || (jarFile.getJarContent().getName().startsWith("org.wso2")) || jarFile.getVersion().contains("wso2")) {
            return "wso2";
        } else {
            return map.getValue("Bundle-Vendor");
        }

    }
}
