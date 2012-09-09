/*
 * Copyright (c) 2006-2013 Shane Mc Cormack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package uk.org.dataforce.libs.logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Create and maintain loggers.
 */
public class LogFactory {
    /** Current default log level. */
    private static LogLevel defaultLogLevel = LogLevel.DEBUG2;

    private final static List<Logger> loggers = new LinkedList<Logger>();

    public static synchronized Logger getLogger() {
        return getLogger(defaultLogLevel);
    }

    public static synchronized Logger getLogger(final LogLevel level) {
        final Logger l = new Logger(level);
        loggers.add(l);
        return l;
    }

    /**
     * Get the default log level.
     *
     * @return The current LogLevel.
     */
    public static LogLevel getDefaultLevel() {
        return defaultLogLevel;
    }

    /**
     * Set the default log level.
     *
     * @param level The new LogLevel.
     */
    public static synchronized void setDefaultLevel(final LogLevel level) {
        defaultLogLevel = level;
    }

    /**
     * Set the default log level and update all loggers.
     *
     * @param level The new LogLevel.
     */
    public static synchronized  void setAllLevel(final LogLevel level) {
        defaultLogLevel = level;
        for (final Logger l : loggers) {
            l.setLevel(level);
        }
    }
}
