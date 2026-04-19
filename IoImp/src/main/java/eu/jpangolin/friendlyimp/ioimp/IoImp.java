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

package eu.jpangolin.friendlyimp.ioimp;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Utils class for IO related things.
 *
 * @author jTzpi
 */
public final class IoImp {

    // LOG
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(IoImp.class);

    private IoImp() {
        throw new AssertionError("__No__");
    }

    /**
     * Return the current working path of the running app.
     *
     * @return Path of current working dir
     * @implNote we use the system property {@code user.dir}
     */
    public static Path getWorkingDir() {
        String path = System.getProperty("user.dir", ".");
        return Path.of(path);
    }
    

    /**
     * Read  resource bundle.
     *
     * @param cls             class from which location the resource loaded
     * @param resourceFileStr name of resource
     * @return resource bundle
     * @throws IOException           if ioe
     * @throws NullPointerException  if {@code cls}|{@code resourceFileStr} is {@code null}
     * @throws IllegalStateException if resource is not readable
     */
    public static ResourceBundle loadResourceBundle(final Class<?> cls, final String resourceFileStr) throws IOException {
        Objects.requireNonNull(cls, "class is null");
        Objects.requireNonNull(resourceFileStr, "resource file is null!");
        LOG.info("<<loadResourceBundle>> Try to load resource '{}' for class '{}'", resourceFileStr, cls.toGenericString());
        ResourceBundle resBu;
        // ARM
        try (final InputStream resIS = cls.getResourceAsStream(resourceFileStr)) {
            if (null == resIS) {
                throw new IllegalStateException("ResourceBundle[='" + resourceFileStr + "'] not readable");
            }
            resBu = new PropertyResourceBundle(resIS);

        }
        return resBu;
    }
}