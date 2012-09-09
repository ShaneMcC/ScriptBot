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
import javax.script.Invocable;
import javax.script.ScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import uk.org.dataforce.scriptbot.scripts.ScriptBotEngine;

/**
 * Rhino Script Bot Engine.
 */
public class RhinoScriptEngine implements ScriptBotEngine, Invocable {
    private Scriptable engineScope;

    public RhinoScriptEngine() {
        final Context cx = enterContext();
        engineScope = cx.initStandardObjects();
        engineScope.setParentScope(null);
        Context.exit();
    }

    private Context enterContext() {
        final Context cx = Context.enter();
        cx.setWrapFactory(new WrapFactory());
        cx.setClassShutter(new ClassShutter());
        return cx;
    }

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

    @Override
    public void put(final String string, final Object object) {
        final Context cx = enterContext();
        try {
            engineScope.put(string, engineScope, object);
        } finally {
            Context.exit();
        }
    }


    @Override
    public Object invokeFunction(final String name, final Object... args) throws ScriptException, NoSuchMethodException {
        return invokeMethod(null, name, args);
    }

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

    private Object[] wrap(final Object[] args) {
        if (args == null) { return Context.emptyArgs; }

        final Object[] res = new Object[args.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = Context.javaToJS(args[i], engineScope);
        }

        return res;
    }

    private Object unwrap(final Object result) {
        final Object res = (result instanceof Wrapper) ? ((Wrapper)result).unwrap() : result;
        return (res instanceof Undefined) ? null : res;
    }

    @Override
    public <T> T getInterface(final Class<T> clasz) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <T> T getInterface(final Object thiz, final Class<T> clasz) {
        throw new UnsupportedOperationException("Not supported.");
    }

}