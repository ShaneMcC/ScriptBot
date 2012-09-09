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

    public class BoundMethod {
        final Object object;
        final Object method;
        public BoundMethod(final Object object, final Object method) {
            this.object = object;
            this.method = method;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof BoundMethod) {
                final BoundMethod bm = (BoundMethod)o;
                if (this.object == null && bm.object != null) { return false; }
                if (this.method == null && bm.method != null) { return false; }
                return this.object.equals(bm.object) && this.method.equals(bm.method);
            }
            return false;
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.object != null ? this.object.hashCode() : 0);
            hash = 29 * hash + (this.method != null ? this.method.hashCode() : 0);
            return hash;
        }
    }

    /** The script that owns this bridge. */
    private Script myScript;

    private final Map<String, Set<BoundMethod>> bindings = new HashMap<String, Set<BoundMethod>>();

    /** Create a new script bridge. */
    public ScriptBridge(final Script script) {
        myScript = script;
    }

    /** Used by scripts to write a line to the logger. */
    public void log(final String line) {
        myScript.getLogger().info(line);
    }

    /** Used by scripts to bind to a parser event. */
    public void bindEvent(final String event, final Object method) {
        bindEvent(event, null, method);
    }

    /** Used by scripts to bind to a parser event. */
    public void bindEvent(final String event, final Object object, final Object method) {
        synchronized (bindings) {
            if (!bindings.containsKey(event)) {
                bindings.put(event, new LinkedHashSet<BoundMethod>());
            }
            final Set<BoundMethod> items = bindings.get(event);
            items.add(new BoundMethod(object, method));
        }
    }

    /** Used by scripts to bind to a parser event. */
    public void unbindEvent(final String event, final Object method) {
        unbindEvent(event, null, method);
    }

    /** Used by scripts to bind to a parser event. */
    public void unbindEvent(final String event, final Object object, final Object method) {
        synchronized (bindings) {
            if (!bindings.containsKey(event)) {
                return;
            }
            final Set<BoundMethod> items = bindings.get(event);
            items.remove(new BoundMethod(object, method));
        }
    }

    /** Used by bot to find scripts bound to a parser event. */
    public Set<BoundMethod> __getBindings(final String event) {
        synchronized (bindings) {
            final Set<BoundMethod> s = bindings.get(event);
            return (s == null) ? new LinkedHashSet<BoundMethod>() :new LinkedHashSet<BoundMethod>(s);
        }
    }
}
