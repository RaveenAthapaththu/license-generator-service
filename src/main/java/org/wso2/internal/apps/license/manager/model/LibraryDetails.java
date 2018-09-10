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

import java.io.File;

/**
 *  * This class contains properties and operations related to a Library (Jar)
 */
public class LibraryDetails {

    private String name;
    private String type;
    private String version;
    private String product;
    private String vendor;
    private String fileName;
    private String licenseKey;
    private File jarContent;
    private LibraryDetails parent;
    private boolean isBundle = false;
    private boolean isValidName = false;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getProduct() {

        return product;
    }

    public void setProduct(String product) {

        this.product = product;
    }

    public String getVendor() {

        return vendor;
    }

    public void setVendor(String vendor) {

        this.vendor = vendor;
    }

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
    }

    public File getJarContent() {

        return jarContent;
    }

    public void setJarContent(File jarContent) {

        this.jarContent = jarContent;
    }

    public LibraryDetails getParent() {

        return parent;
    }

    public void setParent(LibraryDetails parent) {

        this.parent = parent;
    }

    public boolean isBundle() {

        return isBundle;
    }

    public void setisBundle(boolean bundle) {

        isBundle = bundle;
    }

    public boolean isValidName() {

        return isValidName;
    }

    public void setValidName(boolean validName) {

        isValidName = validName;
    }

    public String getLicenseKey() {

        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {

        this.licenseKey = licenseKey;
    }

    public void setBundle(boolean bundle) {

        isBundle = bundle;
    }
}
