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
package uk.org.dataforce.scriptbot.scripts;

import com.dmdirc.parser.interfaces.Parser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.script.ScriptException;
import uk.org.dataforce.libs.logger.Logger;
import uk.org.dataforce.scriptbot.Server;

/**
 *
 * @author shane
 */
public class ScriptHandler {
    /** My Server. */
    private final Server myServer;

    /** Store Script State Name,Engine */
    private final Map<String, Script> scripts = new HashMap<String,Script>();

    /**
     * Create a new ScriptHandler.
     *
     * @param server Server that owns this handler.
     */
    public ScriptHandler(final Server server) {
        myServer = server;
    }

    /**
     * Get the server that owns this handler.
     *
     * @return The server that owns this handler.
     */
    public Server getServer() {
        return myServer;
    }

    /**
     * Load a script and guess the type using the file extension.
     *
     * @param file File to load.
     * @return True if script loaded.
     */
    public boolean loadScript(final File file) {
        return loadScript(file, null);
    }

    /**
     * Load a script using the given type.
     *
     * @param file File to load.
     * @param type Script type.
     * @return True if script loaded.
     */
    public boolean loadScript(final File file, final String type) {
        if (file.exists() && getScript(file) == null) {
            try {
                final Script script = new Script(this, file, type, myServer.getConfig());
                if (script.load()) {
                    synchronized (scripts) {
                        scripts.put(getFilePath(file), script);
                    }
                    return true;
                }
            } catch (final ScriptException se) {
                getLogger().error(se.getMessage());
            }
        }
        return false;
    }

    /**
     * Load a script and guess the type using the file extension.
     *
     * @param file File to load.
     * @return True if script loaded.
     */
    public Script getScript(final File file) {
        final String filename = getFilePath(file);
        if (scripts.containsKey(filename)) {
            return scripts.get(filename);
        }

        return null;
    }

    /**
     * Get the full path of the given file.
     * This will try for the Canonical path first, or fallback to the
     * absolute path if there is an error.
     *
     * @param file File to get path for.
     * @return The path.
     */
    private String getFilePath(final File file) {
        try {
            return file.getCanonicalPath();
        } catch (final IOException ioe) {
            return file.getAbsolutePath();
        }
    }

    /**
     * Call a bound event in all scripts in this script handler.
     *
     * @param function Function name
     * @param args Arguments for functions.
     */
    public void callBound(final String function, final Object... args) {
        for (final Script script : new ArrayList<Script>(scripts.values())) {
            final Set<BoundMethod> bound = script.getBridge().__getBindings(function);
            for (BoundMethod b : bound) {
                script.call(b.object, b.method, args);
            }
        }
    }

    /**
     * Get the Logger for this ScriptHandler
     *
     * @return The Logger for this ScriptHandler
     */
    public Logger getLogger() {
        return myServer.getLogger();
    }

    /**
     * Reload all scripts.
     */
    public void reloadAll() {
        synchronized (scripts) {
            for (final Script script : new ArrayList<Script>(scripts.values())) {
                try {
                    script.reload();
                } catch (final ScriptException se) { /* Ignore, it will have been logged */ }
            }
        }
    }

    /**
     * Unload all scripts.
     */
    public void unload() {
        synchronized (scripts) {
            final List<Script> oldScripts = new ArrayList<Script>(scripts.values());
            scripts.clear();
            for (final Script script : oldScripts) {
                script.unload();
            }
        }
    }


    /**
     * Temporary method to bind IRCScripters...
     *
     * @deprecated This may change.
     */
    @Deprecated
    public void bindIRCScripters(final Parser parser) {
        synchronized (scripts) {
            for (final Script script : new ArrayList<Script>(scripts.values())) {
                parser.getCallbackManager().addAllCallback(script.getIRCScripter().__getEventHandler());
            }
        }
    }
}
