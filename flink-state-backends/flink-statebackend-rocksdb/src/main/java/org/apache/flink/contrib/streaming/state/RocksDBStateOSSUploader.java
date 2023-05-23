/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.contrib.streaming.state;

import org.apache.flink.core.fs.CloseableRegistry;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.PluginFileSystemFactory;
import org.apache.flink.runtime.state.CheckpointStreamFactory;
import org.apache.flink.runtime.state.StreamStateHandle;
import org.apache.flink.runtime.state.filesystem.FileStateHandle;
import org.apache.flink.runtime.state.filesystem.FsCheckpointStorageLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.UUID;

/** Help class for uploading RocksDB state files. */
public class RocksDBStateOSSUploader extends RocksDBStateUploader {
    private static final Logger LOG = LoggerFactory.getLogger(RocksDBStateOSSUploader.class);

    public RocksDBStateOSSUploader(int numberOfSnapshottingThreads) {
        super(numberOfSnapshottingThreads);
    }

    @Override
    protected StreamStateHandle uploadLocalFileToCheckpointFs(
            Path filePath,
            CheckpointStreamFactory checkpointStreamFactory,
            CloseableRegistry closeableRegistry)
            throws IOException {
        LOG.info("file path {}", filePath);
        if (checkpointStreamFactory instanceof FsCheckpointStorageLocation) {
            org.apache.flink.core.fs.Path sharedPath = ((FsCheckpointStorageLocation) checkpointStreamFactory).getSharedStateDirectory();
            LOG.info("sharedPath is {}", sharedPath);
            if (!sharedPath.toUri().getScheme().equals("oss")) {
                LOG.error("sharedPath schema is {}", sharedPath.toUri().getScheme());
                throw new RuntimeException(
                        sharedPath + " sharedPath schema is not oss");
            }

            PluginFileSystemFactory.ClassLoaderFixingFileSystem classLoaderFixingFileSystem = (PluginFileSystemFactory.ClassLoaderFixingFileSystem) FileSystem.getUnguardedFileSystem(
                    sharedPath.toUri());
            FileSystem fileSystem = classLoaderFixingFileSystem.getWrappedDelegate();
            try {
                Method fsMethod = fileSystem.getClass().getMethod("getHadoopFileSystem", null);
                Object ossFileSystem = fsMethod.invoke(
                        fileSystem);

                Method storeMethod = ossFileSystem.getClass().getMethod("getStore", null);

                Object store = storeMethod.invoke(ossFileSystem);

                final String fileName = UUID.randomUUID().toString();
                org.apache.flink.core.fs.Path target =
                        new org.apache.flink.core.fs.Path(sharedPath, fileName);

                String key = target.toUri().getPath().substring(1);

                File file = filePath.toFile();

                LOG.info("upload local file {} to remote oss {}", filePath.toUri().getPath(), key);
                Method uploadMethod = store
                        .getClass()
                        .getMethod("uploadObject", String.class, File.class);
                uploadMethod.invoke(store, key, file);

                StreamStateHandle result = new FileStateHandle(
                        target,
                        file.length());

                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("checkpointStreamFactory " + checkpointStreamFactory
                    .getClass()
                    .getCanonicalName() + "is not FsCheckpointStorageLocation");
        }


    }
}
