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

package org.wso2.internal.apps.license.manager.model;

import java.util.List;

/**
 * * This class contains properties and operations related to a extracted pack
 */
public class PackDetails {

    //all libs.
    private List<LibraryDetails> libFilesInPack;

    //all libs with faulty name.
    private List<LibraryDetails> faultyNamedLibs;

    //all libs with unidentified license.
    private List<LibraryDetails> licenseMissingLibraries;

    //all wso2 libs with unidentified license
    private List<LibraryDetails> licenseMissingComponentLibraries;

    private int packId;
    private String packName;
    private String packVersion;

    public List<LibraryDetails> getLibFilesInPack() {

        return libFilesInPack;
    }

    public void setLibFilesInPack(List<LibraryDetails> libFilesInPack) {

        this.libFilesInPack = libFilesInPack;
    }

    public List<LibraryDetails> getFaultyNamedLibs() {

        return faultyNamedLibs;
    }

    public void setFaultyNamedLibs(List<LibraryDetails> faultyNamedLibs) {

        this.faultyNamedLibs = faultyNamedLibs;
    }

    public List<LibraryDetails> getLicenseMissingLibraries() {

        return licenseMissingLibraries;
    }

    public void setLicenseMissingLibraries(List<LibraryDetails> licenseMissingLibraries) {

        this.licenseMissingLibraries = licenseMissingLibraries;
    }

    public int getPackId() {

        return packId;
    }

    public void setPackId(int packId) {

        this.packId = packId;
    }

    public String getPackName() {

        return packName;
    }

    public void setPackName(String packName) {

        this.packName = packName;
    }

    public String getPackVersion() {

        return packVersion;
    }

    public void setPackVersion(String packVersion) {

        this.packVersion = packVersion;
    }

    public List<LibraryDetails> getLicenseMissingComponentLibraries() {

        return licenseMissingComponentLibraries;
    }

    public void setLicenseMissingComponentLibraries(List<LibraryDetails> licenseMissingComponentLibraries) {

        this.licenseMissingComponentLibraries = licenseMissingComponentLibraries;
    }
}
