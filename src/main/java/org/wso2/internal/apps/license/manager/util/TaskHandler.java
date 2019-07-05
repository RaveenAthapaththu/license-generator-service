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

import org.wso2.internal.apps.license.manager.model.TaskProgress;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handles the operation related to progress of each running tasks
 */
public class TaskHandler {

    private static final ReadWriteLock progressTrackerLock = new ReentrantReadWriteLock();
    private static Map<String, TaskProgress> taskProgressMapByPackName = new HashMap<>();

    /**
     * Create a new Task and track it
     * @param username
     * @param packName Name of the selected pack to extract
     * @return a TaskProgress object
     */
    public static TaskProgress createNewTaskProgress(String username, String packName) {

        //Initiate write lock on this block of code
        progressTrackerLock.writeLock().lock();
        try {

            String userID = UUID.randomUUID().toString();
            TaskProgress taskProgress = new TaskProgress(username, userID, Constants.RUNNING, packName);

            //Store and map each task progress by pack name
            //concurrent map?
            taskProgressMapByPackName.put(packName, taskProgress);
            return taskProgress;
        } finally {
            progressTrackerLock.writeLock().unlock();
        }
    }


    /**
     * Get an existing task by packname. If such a task does not exist null will returned.
     *
     * @param packname The email of the user who requested the task
     * @return The task progress for the specified task
     */
    public static TaskProgress getTaskByPackName(String packname) {

        TaskProgress taskProgress;
        progressTrackerLock.readLock().lock();
        try {
            taskProgress = taskProgressMapByPackName.get(packname);
        } finally {
            progressTrackerLock.readLock().unlock();
        }
        return taskProgress;
    }

    /**
     * Delete a task identified by pack name
     * @param packName
     */
    private void endAnyExistingTasksByPackname(String packName) {
        // Create a new object to track the progress.
        TaskProgress taskProgressByPackName = getTaskByPackName(packName);

        if (taskProgressByPackName != null) {
            long previousThreadId = taskProgressByPackName.getExecutingThreadId();
            //Take the set of running threads
            Set<Thread> setOfThread = Thread.getAllStackTraces().keySet();

            //Iterate over set to the relevant thread
            for (Thread thread : setOfThread) {
                if (thread.getId() == previousThreadId) {
                    thread.interrupt();
                }
            }
            deleteTaskByPackName(packName);
        }
    }


    /**
     * Delete a task progress being tracked.
     *
     * @param packName pack name
     */
    public static void deleteTaskByPackName(String packName) {

        progressTrackerLock.writeLock().lock();
        try {
            taskProgressMapByPackName.remove(packName);
        } finally {
            progressTrackerLock.writeLock().unlock();
        }
    }

    public static boolean checkForAlreadyRunningTask(String packname) {

        TaskProgress taskProgress = getTaskByPackName(packname);
        return (taskProgress != null);
    }


}
