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

import java.util.List;

/**
 * This class controls what classes scripts in the VM have access to.
 *
 * From http://codeutopia.net/blog/2009/01/02/sandboxing-rhino-in-java/
 */
public class ClassShutter implements org.mozilla.javascript.ClassShutter {
    /** Additional classes to allow apart from the defaults. */
    private List<String> goodClasses;

    /** Classes to specifically deny. */
    private List<String> badClasses;


    public ClassShutter(final List<String> goodClasses, final List<String> badClasses) {
        this.goodClasses = goodClasses;
        this.badClasses = badClasses;
    }

    /** {@inheritDoc} */
    @Override
    public boolean visibleToScripts(final String className) {
        for (final String clazz : badClasses) {
            if (className.startsWith(clazz)) {
                return false;
            }
        }

        if (className.startsWith("adapter") ||
            className.startsWith("com.dmdirc.parser.") ||
            className.startsWith("java.util.") ||
            className.startsWith("java.lang.") ||
            className.startsWith("org.mozilla.javascript.") || // Unsure about this one, but allow it for now.
            className.startsWith("uk.org.dataforce.scriptbot.scripts.ScriptBridge") ||
            className.startsWith("uk.org.dataforce.scriptbot.scripts.irc.IRCScripter") ||
            className.startsWith("uk.org.dataforce.scriptbot.scripts.BoundMethod")) {
            return true;
        } else if (className.startsWith("uk.org.dataforce.scriptbot") && !className.startsWith("uk.org.dataforce.scriptbot.scripts")) {
            return true;
        } else {
            for (final String clazz : goodClasses) {
                if (className.startsWith(clazz)) {
                    return true;
                }
            }
        }

        System.out.println("Denied Class: "+ className);

        return false;
    }
}