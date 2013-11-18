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

import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import uk.org.dataforce.scriptbot.Server;

/**
 * This class handles calling all callbacks as script events.
 */
public class ParserBridge {

    /** My Server */
    private Server myServer;

    /** Map of callbacks we have created. */
    @SuppressWarnings("rawtypes")
    final Map<Class, CallbackInterface> callbacks = new HashMap<Class, CallbackInterface>();

    /**
     * Create a new ParserBridge for the given server.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ParserBridge(final Server server) {
        try {
            myServer = server;

            final CallbackManager cbm = myServer.getParser().getCallbackManager();
            final Field field = cbm.getClass().getDeclaredField("CLASSES");
            field.setAccessible(true);
            final Class[] classes = (Class[])field.get(null);

            for (final Class clazz : classes) {
                if (CallbackInterface.class.isAssignableFrom(clazz)) {
                    final CallbackInterface i = ScriptInvocationHandler.getProxy(clazz, this);

                    callbacks.put(clazz, i);
                    cbm.addCallback(clazz, i);
                }
            }
        } catch (final IllegalArgumentException ex) {
        } catch (final IllegalAccessException ex) {
        } catch (final NoSuchFieldException ex) {
        } catch (final SecurityException ex) {
        }
    }

    /**
     * Clear all callbacks.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void clear() {
        for (Map.Entry<Class, CallbackInterface> entry : callbacks.entrySet()) {
            final CallbackManager cbm = myServer.getParser().getCallbackManager();
            cbm.delCallback(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get the server that owns us.
     *
     * @return The server that owns us.
     */
    public Server getServer() {
        return myServer;
    }
}
