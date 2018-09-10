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

/**
 *  * This class contains properties and operations related to a license
 */
public class License {

    private String name;
    private String version;
    private String key;
    private int id;
    private String url;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }
}
