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
import java.util.Properties;
import javax.script.Invocable;
import javax.script.ScriptException;
import uk.org.dataforce.libs.logger.LogFactory;
import uk.org.dataforce.libs.logger.Logger;
import uk.org.dataforce.scriptbot.config.Config;
import uk.org.dataforce.scriptbot.scripts.irc.IRCScripter;
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

    /** IRC Scripter. */
    private IRCScripter myIRCScripter;

    /** My config file. */
    private Config myConfig;

    /** My file. */
    private final File myFile;

    /** My file type. */
    private final String myType;

    /**
     * Load a script using the given type, or guess based on the file extension
     * if null type is given.
     *
     * @param handler ScriptHandler that created this script.
     * @param file File to load.
     * @param type Script type.
     * @return True if script loaded.
     */
    public Script(final ScriptHandler handler, final File file, final String type, final Config config) throws ScriptException {
        myHandler = handler;
        myIRCScripter = new IRCScripter(myHandler.getIRCScripter(), this);
        myFile = file;
        myLogger.setTag(myHandler.getServer().getName() + ">" + file.getName());
        myType = type;
        myConfig = config;

        myEngine = initEngine();
    }

    private ScriptBotEngine initEngine() throws ScriptException {
        ScriptBotEngine newEngine;
        if (myType == null) {
            final String filename = myFile.getName();
            if (filename.lastIndexOf('.') == -1 || filename.lastIndexOf('.') >= filename.length()) {
                myHandler.getLogger().error("Unable to find ScriptEngine for: '" + myFile + "' (No file extension found)");
            }
            final String extension = myFile.getName().substring(myFile.getName().lastIndexOf('.') + 1);
            newEngine = ScriptFactory.getEngineByExtension(extension, myConfig);
            if (newEngine == null) {
                throw new ScriptException("Unable to find ScriptEngine for: '" + myFile + "' (Extension: '" + extension + "')");
            }
        } else {
            newEngine = ScriptFactory.getEngineByType(myType, myConfig);
            if (newEngine == null) {
                throw new ScriptException("Unable to find ScriptEngine for: '" + myFile + "' (Type: '" + myType + "')");
            }
        }
        return newEngine;
    }


    /**
     * Load the script into the engine.
     *
     * @return True if script loaded.
     */
    public boolean load() {
        return load(myEngine, myScriptBridge, myIRCScripter);
    }

    /**
     * Get the ScriptHandler for this script.
     *
     * @return ScriptHandler for this script.
     */
    public ScriptHandler getHandler() {
        return myHandler;
    }

    /**
     * Load the script into the given engine with the given bridge.
     *
     * @param engine Engine to use.
     * @param bridge Bridge to use.
     * @param ircscripter IRCScripter to use.
     * @return True if script loaded.
     */
    private boolean load(final ScriptBotEngine engine, final ScriptBridge bridge, final IRCScripter ircscripter) {
        try {
            engine.put("bot", bridge);
            engine.put("irc", ircscripter);
            engine.eval(new FileReader(myFile));
            getLogger().info("Loaded script '" + getFilePath(myFile) + "'");
            // call("onScriptLoaded");
            return true;
        } catch (final FileNotFoundException ex) {
            getLogger().error("Error loading script '" + getFilePath(myFile) + "': " + ex.getMessage());
        } catch (final ScriptException ex) {
            getLogger().error("Error loading script '" + getFilePath(myFile) + "': " + ex.getMessage());
        }
        return false;
    }

    /**
     * Reload this script.
     */
    public void reload() throws ScriptException {
        // call("onScriptUnloaded");
        getLogger().info("Reloading script '" + getFilePath(myFile) + "'...");
        final ScriptBotEngine engine = initEngine();
        if (engine != null) {
            final ScriptBridge bridge = new ScriptBridge(this);
            final IRCScripter ircscripter = new IRCScripter(myHandler.getIRCScripter(), this);

            if (load(engine, bridge, ircscripter)) {
                myHandler.getIRCScripter().unbindScripter(myIRCScripter);
                myScriptBridge.unbindAll();
                myScriptBridge = bridge;
                myIRCScripter = ircscripter;
                myEngine.put("bot", null);
                myEngine.put("irc", null);
                myEngine = engine;
                getLogger().info("Reloading script '" + getFilePath(myFile) + "' was successful, new script is active.");
            } else {
                getLogger().info("Reloading script '" + getFilePath(myFile) + "' failed, keeping old script.");
            }
        }
    }

    /**
     * Unload this script.
     *
     * Once this is called, this script object can not be used again.
     */
    public void unload() {
        // call("onScriptUnloaded");
        getLogger().info("Unloaded script '" + getFilePath(myFile) + "'");
        myScriptBridge.unbindAll();
        myHandler.getIRCScripter().unbindScripter(myIRCScripter);
        myEngine.put("bot", null);
        myEngine.put("irc", null);
        myEngine = null;
        myScriptBridge = new ScriptBridge(this);
        myLogger = null;
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
     * Get the IRCScripter related to this script.
     *
     * @return IRCScripter for this script.
     */
    public IRCScripter getIRCScripter() {
        return myIRCScripter;
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
