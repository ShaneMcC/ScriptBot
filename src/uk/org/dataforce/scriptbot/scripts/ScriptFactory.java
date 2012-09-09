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

import javax.script.ScriptEngineManager;
import uk.org.dataforce.scriptbot.scripts.rhinosandbox.RhinoScriptEngine;

/**
 * Script Factory. This hands out ScriptEngines
 */
public class ScriptFactory {

    /** Static ScriptEngineManager. */
    private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    /**
     * Get a ScriptEngine based on a file extension
     *
     * @param extension Extension that engine must support.
     */
    public static ScriptBotEngine getEngineByExtension(final String extension) {
        if (extension.equalsIgnoreCase("js")) {
            // We need to emulate a proper ScriptEngine/Factory.. but untill then...
            return new RhinoScriptEngine();
        } else {
            return new ScriptEngineWrapper(scriptEngineManager.getEngineByExtension(extension));
        }
    }

    /**
     * Get a ScriptEngine based on a type
     *
     * @param type Type of engine to get.
     */
    public static ScriptBotEngine getEngineByType(final String type) {
        if (type.equalsIgnoreCase("javascript")) {
            // We need to emulate a proper ScriptEngine/Factory.. but untill then...
            return new RhinoScriptEngine();
        } else {
            return new ScriptEngineWrapper(scriptEngineManager.getEngineByName(type));
        }
    }
}