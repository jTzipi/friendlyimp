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

import java.time.Duration;
import java.util.Optional;

/**
 * Defines the common values of the result of an {@linkplain INativeCommand}.
 * <p>
 *     <ul>
 *         <li>exit code</li>
 *         <li>elapsed time</li>
 *         <li>(optional) error</li>
 *     </ul>
 * </p>
 *
 * @author jTzipi
 */
public interface INativeCommandResult {


    /**
     * Optional error text if the command failed duo to user error.
     *
     * @return Optional error text
     */
    Optional<String> error();

    /**
     * Duration of this command.
     *
     * @return duration
     */
    Duration elapsed();

    /**
     * The exit code of the command.
     * Hint: copied from {@link Process} "By convention, the value 0 indicates normal termination"
     *
     * @return exit code (0 == normal termination)
     */
    int exitCode();
}