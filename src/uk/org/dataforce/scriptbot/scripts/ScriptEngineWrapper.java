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

import java.io.FileReader;
import java.io.Reader;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import uk.org.dataforce.scriptbot.config.Config;

/**
 * ScriptBotEngine that Wraps the standard java ScriptEngine
 * @author shane
 */
class ScriptEngineWrapper implements ScriptBotEngine, Compilable, Invocable {
    /** My Engine */
    final ScriptEngine myEngine;

    /** My Config file */
    final Config myConfig;

    /**
     * Create a new ScriptEngineWrapper that Wraps the given ScriptEngine
     *
     * @param engine ScriptEngine to wrap.
     */
    public ScriptEngineWrapper(final ScriptEngine engine, final Config config) {
        myEngine = engine;
        myConfig = config;
    }

    /** {@inheritDoc} */
    @Override
    public void eval(final FileReader fileReader) throws ScriptException {
        myEngine.eval(fileReader);
    }

    /** {@inheritDoc} */
    @Override
    public void put(final String string, final Object object) {
        myEngine.put(string, object);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMethod(final Object thiz, final String name, final Object... args) throws ScriptException, NoSuchMethodException {
        return ((Invocable)myEngine).invokeMethod(thiz, name, args);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeFunction(final String name, final Object... args) throws ScriptException, NoSuchMethodException {
        return ((Invocable)myEngine).invokeFunction(name, args);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getInterface(final Class<T> clasz) {
        return ((Invocable)myEngine).getInterface(clasz);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getInterface(final Object thiz, final Class<T> clasz) {
        return ((Invocable)myEngine).getInterface(thiz, clasz);
    }

    /** {@inheritDoc} */
    @Override
    public CompiledScript compile(String script) throws ScriptException {
        return ((Compilable)myEngine).compile(script);
    }

    /** {@inheritDoc} */
    @Override
    public CompiledScript compile(Reader script) throws ScriptException {
        return ((Compilable)myEngine).compile(script);
    }
}
