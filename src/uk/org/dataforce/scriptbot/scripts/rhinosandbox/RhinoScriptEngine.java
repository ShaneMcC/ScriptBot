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
package uk.org.dataforce.scriptbot.scripts.rhinosandbox;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import uk.org.dataforce.scriptbot.config.Config;
import uk.org.dataforce.scriptbot.scripts.ScriptBotEngine;

/**
 * Rhino Script Bot Engine.
 *
 * This class is used to interact between the bot and scripts.
 */
public class RhinoScriptEngine implements ScriptBotEngine, Invocable {
    /** Engine variable scope. */
    private Scriptable engineScope;

    /** Additional classes to allow apart from the defaults in the sandbox. */
    private List<String> goodClasses;

    /** Classes to specifically deny in the sandbox. */
    private List<String> badClasses;

    /**
     * Create a new RhinoeScriptEngine and set the default scope.
     */
    public RhinoScriptEngine(final Config config) {
        this.goodClasses = config.hasFlatDomain("rhino.goodClasses") ? config.getFlatDomain("rhino.goodClasses") : Collections.<String>emptyList();
        this.badClasses = config.hasFlatDomain("rhino.badClasses") ? config.getFlatDomain("rhino.badClasses") : Collections.<String>emptyList();
        final Context cx = enterContext();
        engineScope = cx.initStandardObjects();
        engineScope.setParentScope(null);
        Context.exit();
    }

    /**
     * Enter a context and make sure we use the right wrapper and shutter.
     *
     * @return Context that we are now in.
     */
    private Context enterContext() {
        final Context cx = Context.enter();
        try {
            cx.setClassShutter(new ClassShutter(this.goodClasses, this.badClasses));
            cx.setWrapFactory(new WrapFactory());
        } catch (final SecurityException se) {
            // ClassShutter is already set, so we might be already in a context
        }
        return cx;
    }

    /** {@inheritDoc} */
    @Override
    public void eval(final FileReader fileReader) throws ScriptException {
        try {
            final Context cx = enterContext();
            cx.evaluateReader(engineScope, fileReader, "<UnkownSource>", 1, null);
        } catch (final RhinoException re) {
            throw new ScriptException(re.getMessage(), re.sourceName(), re.lineNumber(), re.columnNumber());
        } catch (final IOException ex) {
            throw new ScriptException(ex);
        } finally {
            Context.exit();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void put(final String string, final Object object) {
        final Context cx = enterContext();
        try {
            engineScope.put(string, engineScope, object);
        } finally {
            Context.exit();
        }
    }


    /** {@inheritDoc} */
    @Override
    public Object invokeFunction(final String name, final Object... args) throws ScriptException, NoSuchMethodException {
        return invokeMethod(null, name, args);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMethod(Object thiz, final String name, final Object... args) throws ScriptException, NoSuchMethodException {
        if (thiz != null && !(thiz instanceof Scriptable)) {
            final Context cx = enterContext();
            if (thiz instanceof String) {
                thiz = ScriptableObject.getProperty(engineScope, (String)thiz);
            } else {
                thiz = Context.toObject(thiz, engineScope);
            }
            Context.exit();
        }

        final Scriptable localScope = (thiz != null) ? (Scriptable)thiz : engineScope;

        final Object obj = ScriptableObject.getProperty(localScope, name);
        if (!(obj instanceof Function)) {
            System.out.println(name + " is undefined or not a function in " + thiz + ".");
            throw new NoSuchMethodException(name + " is undefined or not a function.");
        } else {
            return invokeMethod(thiz, (Function)obj, args);
        }
    }

    /**
     * Invoke the given method on the given object, with the given args.
     *
     * @param thiz Object to invoke method on (or null for global method)
     * @param obj Object to invoke (Should be some kind of Function Object)
     * @param args Arguments for the function.
     * @return Object result from function call.
     */
    public Object invokeMethod(Object thiz, final Object obj, final Object... args) throws ScriptException, NoSuchMethodException {
        if (thiz != null && !(thiz instanceof Scriptable)) {
            final Context cx = enterContext();
            if (thiz instanceof String) {
                thiz = ScriptableObject.getProperty(engineScope, (String)thiz);
            } else {
                thiz = Context.toObject(thiz, engineScope);
            }
            Context.exit();
        }

        final Scriptable localScope = (thiz != null) ? (Scriptable)thiz : engineScope;

         if (!(obj instanceof Function)) {
            throw new NoSuchMethodException("obj is undefined or not a function.");
        } else {
            final Function f = (Function)obj;
            final Scriptable callerScope = f.getParentScope() == null ? localScope : f.getParentScope();
            Context cx = enterContext();
            try {
                final Object result = f.call(cx, callerScope, localScope, wrap(args));
                return unwrap(result);
            } catch (final RhinoException re) {
                throw new ScriptException(re.getMessage(), re.sourceName(), re.lineNumber(), re.columnNumber());
            } finally {
                Context.exit();
            }
         }
    }

    /**
     * Wrap the given arguments in the correct wrapper classes to pass to
     * scripts
     *
     * @param args Arguments to wrap.
     * @return Wrapps args.
     */
    private Object[] wrap(final Object[] args) {
        if (args == null) { return Context.emptyArgs; }

        final Object[] res = new Object[args.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = Context.javaToJS(args[i], engineScope);
        }

        return res;
    }

    /**
     * Unwrap the given object result
     *
     * @param result Result to unwrap
     * @return Unwrapped result
     */
    private Object unwrap(final Object result) {
        final Object res = (result instanceof Wrapper) ? ((Wrapper)result).unwrap() : result;
        return (res instanceof Undefined) ? null : res;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getInterface(final Class<T> clasz) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getInterface(final Object thiz, final Class<T> clasz) {
        throw new UnsupportedOperationException("Not supported.");
    }

}