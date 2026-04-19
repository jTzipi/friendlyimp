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

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract implementation of a native command.
 *
 * @param <T> command result type
 * @author jTzipi
 */
public abstract class AbstractNativeCommand<T extends INativeCommandResult> implements INativeCommand<T> {

    // LOG
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractNativeCommand.class);

    private final String baseCommand;
    // The command with all of its arguments
    private final List<String> cmdArgList;

    ///
    /// AbstractNativeCommand constructor.
    ///
    /// @param baseCommandStr      Base command name like `ls -l`
    /// @param commandArgumentList Arguments/Option List (optional)
    ///
    protected AbstractNativeCommand(final String baseCommandStr, final List<String> commandArgumentList) {
        this.baseCommand = baseCommandStr;
        this.cmdArgList = Objects.requireNonNullElseGet(commandArgumentList, ArrayList::new);

        // add base command on pos 0
        cmdArgList.addFirst(baseCommandStr);
    }

    ///
    /// AbstractNativeCommand constructor.
    ///
    /// @param baseCommandStr Base command name like `ls -l`
    /// @param arguments      optional option/arguments of the command
    ///
    protected AbstractNativeCommand(String baseCommandStr, String... arguments) {
        this(baseCommandStr, Stream.of(arguments).map(Objects::requireNonNull).collect(Collectors.toCollection(ArrayList::new)));
    }

    /**
     * Parse the std output/error from the native command.
     *
     * @param commandStdOutputStr String read from the command from standard out
     * @param commandStdErrorStr  String read from the command from standard error
     * @param exitCode            Process exitCode
     * @param duration            Optional of duration of the command
     * @return The result
     */
    protected abstract T parse(String commandStdOutputStr, String commandStdErrorStr, int exitCode, Duration duration);

    @Override
    public T launch(final java.io.File cmdDirectory, final long timeout, final TimeUnit timeUnit) throws UnsupportedOperationException, IOException, InterruptedException {
        // Create the process builder with the always non nun nonempty command arguments
        ProcessBuilder pb = new ProcessBuilder(cmdArgList);
        LOG.info("<<launch>> command '{}'", cmdArgList);

        // user set working dir
        if (null != cmdDirectory) {
            LOG.info("<<launch>> work dir set -> '{}'", cmdDirectory);
            pb.directory(cmdDirectory);
        }

        final Instant cmdStart = Instant.now();
        // throws Unsupported OpEx, IOEx and more
        Process p = pb.start();

        // The std out and str error streamed
        String rawInput, rawError;

        // Use ARM for Executors
        // we start a single thread to read the input and error stream
        try (var sts = StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {

            // start two separate threads for each input stream
            StructuredTaskScope.Subtask<String> rawInputF = sts.fork(new SimpleReader(p.inputReader()));
            StructuredTaskScope.Subtask<String> rawErrorF = sts.fork(new SimpleReader(p.errorReader()));
            LOG.warn("<<launch>> Wait for output read ...");
            // wait for all input read
            sts.join();

            rawInput = rawInputF.get();
            rawError = rawErrorF.get();
        }

        int exitValue;
        //
        if (0 < timeout) {
            // timeout waiting...
            boolean completed = p.waitFor(timeout, timeUnit);
            if (completed) {
                LOG.info("<<launch>> Command exit normal! :-)");
            } else {
                LOG.error("<<launch>> Command did not finish until timeout... isAlive? -> '{}'", p.isAlive());
            }
            exitValue = p.exitValue();
        } else {
            // wait for the end of command...
            exitValue = p.waitFor();
        }

        LOG.warn("<<launch>> Exit value is -> '{}' raw value -> '{}', error -> '{}'", exitValue, rawInput, rawError);

        // duration of cmd
        Duration cmdDuration = Duration.between(cmdStart, Instant.now());

        return parse(rawInput, rawError, exitValue, cmdDuration);
    }

    ///
    /// SimpleReader.
    ///
    /// This is a simple approach to read all input from a command std input/error stream via {@link BufferedReader}.
    ///
    private record SimpleReader(BufferedReader bufferedReader) implements Callable<String> {

        @Override
        public String call() throws IOException {
            String result;
            // ARM
            try (bufferedReader) {
                result = bufferedReader.lines().collect(Collectors.joining("\n"));
            }
            return result;
        }

    }

    /// Return the base command part which is the command name.
    ///
    /// @return the command name like `ls`
    public final String getBaseCommand() {
        return baseCommand;
    }

    /**
     * Return the full command.
     *
     * @return full command line like {@code ls -l}
     */
    public final String getFullCommand() {
        return "`%s %s`".formatted(baseCommand, cmdArgList);
    }
}