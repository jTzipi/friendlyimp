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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Describe the possibility to launch an os native command.
 *
 * @param <T> result type
 * @author jTzipi
 */
@FunctionalInterface
public interface INativeCommand<T extends INativeCommandResult> {

    /**
     * Default timeout in seconds (no timeout).
     */
    long DEFAULT_TIMEOUT_SECONDS = -1L;

    /**
     * Launch a native command with no timeout.
     *
     * @return the result
     * @throws UnsupportedOperationException if the command is not supported by the OS
     * @throws IOException                   if some I/O related error
     * @throws InterruptedException          if the command was interrupted
     */
    default T launch() throws UnsupportedOperationException, IOException, InterruptedException {

        return launch(null);
    }

    /**
     * Launch a native command with no timeout.
     *
     * @param cmdDirectory command dir (can be {@code null})
     * @return the result
     * @throws UnsupportedOperationException if the command is not supported by the OS
     * @throws IOException                   if some I/O related error
     * @throws InterruptedException          if the command was interrupted
     *
     */
    default T launch(final java.io.File cmdDirectory) throws UnsupportedOperationException, IOException, InterruptedException {

        return launch(cmdDirectory, DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Launch the native command.
     *
     * @param cmdDirectory workdir of command (can be {@code null})
     * @param timeout      timeout value (can be negative for no timeout)
     * @param timeUnit     Timeunit
     * @return the result
     * @throws UnsupportedOperationException if the command is not supported by the OS
     * @throws IOException                   if some I/O related error
     * @throws InterruptedException          if the command was interrupted
     */
    T launch(java.io.File cmdDirectory, long timeout, TimeUnit timeUnit) throws UnsupportedOperationException, IOException, InterruptedException;
}