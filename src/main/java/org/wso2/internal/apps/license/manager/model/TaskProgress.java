/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.internal.apps.license.manager.model;

/**
 * Java object to store the progress of a task.
 */
public class TaskProgress {

    private String username;
    private int stepNumber;
    private long executingThreadId;
    private String taskId;
    private String status;
    private String message;
    private PackDetails data;
    private String packName;

    public TaskProgress(String username, String taskId, String status, String packName) {

        this.username = username;
        this.taskId = taskId;
        this.status = status;
        this.packName = packName;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getTaskId() {

        return taskId;
    }

    public void setTaskId(String taskId) {

        this.taskId = taskId;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public PackDetails getData() {

        return data;
    }

    public void setData(PackDetails data) {

        this.data = data;
    }

    public int getStepNumber() {

        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {

        this.stepNumber = stepNumber;
    }

    public long getExecutingThreadId() {

        return executingThreadId;
    }

    public void setExecutingThreadId(long executingThreadId) {

        this.executingThreadId = executingThreadId;
    }

    public String getPackName() {

        return packName;
    }

    public void setPackName(String packName) {

        this.packName = packName;
    }
}
