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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * This class makes sure that all java objects are wrapped with the correct
 * Wrapper Object.
 *
 * From http://codeutopia.net/blog/2009/01/02/sandboxing-rhino-in-java/
 */
public class WrapFactory extends org.mozilla.javascript.WrapFactory {
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("rawtypes")
    public Scriptable wrapAsJavaObject(final Context cx, final Scriptable scope, final Object javaObject, final Class staticType) {
        return new NativeJavaObject(scope, javaObject, staticType);
    }
}