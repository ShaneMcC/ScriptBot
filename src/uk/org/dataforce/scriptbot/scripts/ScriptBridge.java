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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is used by Script's to interact with the bot.
 *
 * @author shane
 */
public class ScriptBridge {

    /** The script that owns this bridge. */
    private Script myScript;

    /** List storing bindings. */
    private final Map<String, Set<BoundMethod>> bindings = new HashMap<String, Set<BoundMethod>>();

    /**
     * Create a new script bridge.
     *
     * @param script Script that owns this bridge.
     */
    public ScriptBridge(final Script script) {
        myScript = script;
    }

    /**
     * Used by scripts to write a line to the logger.
     *
     * @param line Line to log.
     */
    public void log(final String line) {
        myScript.getLogger().info(line);
    }

    /**
     * Used by scripts to bind to a parser event.
     *
     * @param event Event Name
     * @param method Method to bind.
     */
    public BoundMethod bindEvent(final String event, final Object method) {
        return bindEvent(event, null, method);
    }

    /**
     * Used by scripts to bind to a parser event.
     *
     * @param event Event Name
     * @param object Object to bind on.
     * @param method Method to bind.
     * @return BoundMethod.
     */
    public BoundMethod bindEvent(final String event, final Object object, final Object method) {
        synchronized (bindings) {
            if (!bindings.containsKey(event)) {
                bindings.put(event, new LinkedHashSet<BoundMethod>());
            }
            final Set<BoundMethod> items = bindings.get(event);
            final BoundMethod boundMethod = new BoundMethod(object, method);
            items.add(boundMethod);
            return boundMethod;
        }
    }

    /**
     * Used by scripts to remove a previous binding.
     *
     * @param event Event Name
     * @param method Method to bind.
     */
    public void unbindEvent(final String event, final Object method) {
        unbindEvent(event, null, method);
    }

    /**
     * Used by scripts to unbind a previous binding.
     * Will remove all bindings if both method and object are null or empty.
     *
     * @param event Event Name
     * @param object Object to bind on.
     * @param method Method to bind.
     */
    public void unbindEvent(final String event, final Object object, final Object method) {
        synchronized (bindings) {
            if (!bindings.containsKey(event)) {
                return;
            }
            if ((object == null || object.toString().isEmpty()) && (method == null || method.toString().isEmpty())) {
                bindings.remove(event);
            } else {
                final Set<BoundMethod> items = bindings.get(event);
                items.remove(new BoundMethod(object, method));
            }
        }
    }

    /**
     * Used by scripts to unbind a previous binding.
     *
     * @param event Event Name
     * @param boundMethod BoundMethod to remove.
     */
    public void unbindEvent(final String event, final BoundMethod boundMethod) {
        synchronized (bindings) {
            if (!bindings.containsKey(event)) {
                return;
            }
            final Set<BoundMethod> items = bindings.get(event);
            items.remove(boundMethod);
        }
    }

    /**
     * Used by bot to find scripts bound to a parser event.
     * Begins with __ so can't be accessed by script.
     *
     * @param event Event to get bindings for
     * @return All bindings for the given event.
     */
    public Set<BoundMethod> __getBindings(final String event) {
        synchronized (bindings) {
            final Set<BoundMethod> s = bindings.get(event);
            return (s == null) ? new LinkedHashSet<BoundMethod>() :new LinkedHashSet<BoundMethod>(s);
        }
    }

    /**
     * Remove all bindings.
     */
    public void unbindAll() {
        bindings.clear();
    }

    /**
     * Rehash the bot.
     */
    public void rehash() {
        myScript.getHandler().reloadAll();
    }
}
