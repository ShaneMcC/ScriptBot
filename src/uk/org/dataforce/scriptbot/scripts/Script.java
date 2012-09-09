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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.script.Invocable;
import javax.script.ScriptException;
import uk.org.dataforce.libs.logger.LogFactory;
import uk.org.dataforce.libs.logger.Logger;
import uk.org.dataforce.scriptbot.scripts.rhinosandbox.RhinoScriptEngine;

/**
 * This class represents a script.
 * @author shane
 */
public class Script {

    /** My Handler. */
    private ScriptHandler myHandler;

    /** My Logger. */
    private Logger myLogger = LogFactory.getLogger();

    /** My current script engine. */
    private ScriptBotEngine myEngine;

    /** My script bridge. */
    private ScriptBridge myScriptBridge = new ScriptBridge(this);

    /** My file. */
    private final File myFile;

    /**
     * Load a script using the given type, or guess based on the file extension
     * if null type is given.
     *
     * @param handler ScriptHandler that created this script.
     * @param file File to load.
     * @param type Script type.
     * @return True if script loaded.
     */
    public Script(final ScriptHandler handler, final File file, final String type) throws ScriptException {
        myHandler = handler;
        myFile = file;
        myLogger.setTag(myHandler.getServer().getName() + ">" + file.getName());

        if (type == null) {
            final String filename = file.getName();
            if (filename.lastIndexOf('.') == -1 || filename.lastIndexOf('.') >= filename.length()) {
                myHandler.getLogger().error("Unable to find ScriptEngine for: '" + file + "' (No file extension found)");
            }
            final String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            myEngine = ScriptFactory.getEngineByExtension(extension);
        } else {
            myEngine = ScriptFactory.getEngineByType(type);
        }
        if (myEngine == null) {
            throw new ScriptException("Unable to find ScriptEngine for: '" + file + "' (Type: '" + type + "')");
        }
    }


    /**
     * Load a script into the given engine.
     *
     * @param file File to load.
     * @param engine Engine to load script into.
     * @return True if script loaded.
     */
    public boolean load() {
        try {
            myEngine.put("bot", myScriptBridge);
            myEngine.eval(new FileReader(myFile));
            getLogger().info("Loaded script '" + getFilePath(myFile) + "'");
            call("onScriptLoaded");
            return true;
        } catch (final FileNotFoundException ex) {
            getLogger().error("Error loading script '" + getFilePath(myFile) + "': " + ex.getMessage());
        } catch (final ScriptException ex) {
            getLogger().error("Error loading script '" + getFilePath(myFile) + "': " + ex.getMessage());
        }
        return false;
    }

    /**
     * Call a function in this script.
     *
     * @param function Function name
     * @param args Arguments for functions.
     */
    public void call(final String function, final Object... args) {
        call(null, function, args);
    }

    /**
     * Call a function of an object in this script.
     *
     * @param obj Object
     * @param function Function name
     * @param args Arguments for functions.
     */
    public void call(final Object obj, final Object function, final Object... args) {
        if (myEngine instanceof Invocable) {
            final Invocable iEngine = (Invocable)myEngine;
            try {
                if (function instanceof String) {
                    if (obj != null) {
                        iEngine.invokeMethod(obj, (String)function, args);
                    } else {
                        iEngine.invokeFunction((String)function, args);
                    }
                } else if (myEngine instanceof RhinoScriptEngine) {
                    ((RhinoScriptEngine)myEngine).invokeMethod(obj, function, args);
                }
            } catch (final NoSuchMethodException ex) {
                /** Ignore. */
            } catch (final ScriptException se) {
                getLogger().error("Error in script:");
                getLogger().error(String.format("\t%s %s<%s>: %s", se.getFileName(), se.getLineNumber(), se.getColumnNumber(), se.getMessage()));
            }
        }
    }

    /**
     * Get the logger related to this script.
     *
     * @return Logger for this script.
     */
    public Logger getLogger() {
        return myLogger;
    }

    /**
     * Get the ScriptBridge related to this script.
     *
     * @return ScriptBridge for this script.
     */
    public ScriptBridge getBridge() {
        return myScriptBridge;
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
}
