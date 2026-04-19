/*
 * Copyright (c) 2026. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.jpangolin.friendlyimp.ioimp.cmd;

import eu.jpangolin.friendlyimp.ioimp.cmd.linux.LsblkCommand;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Provide some examples for native commands.
 *
 * @author jTzipi
 */
public final class NativeCommandExample {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NativeCommandExample.class);

    private NativeCommandExample() {
    }

    static void main(String[] args) {

        LOG.warn("Launch :: LSBLK");
        LsblkCommand lsblkCommand = LsblkCommand.ofDefault();
        try {
            INativeCommandResult ncr = lsblkCommand.launch();
            LOG.warn("Result - '{}'", ncr);
        } catch (IOException e) {

            LOG.error("Failed to run LSBLK ->", e);
        } catch (InterruptedException e) {
            LOG.warn("LSBLK interrupted!", e);
        }

    }


}