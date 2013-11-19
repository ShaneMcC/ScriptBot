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
package uk.org.dataforce.scriptbot.scripts.irc;

import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;
import java.util.HashMap;
import java.util.Map;
import uk.org.dataforce.scriptbot.scripts.BoundMethod;
import uk.org.dataforce.scriptbot.scripts.ScriptObject;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * The Global IRCScripter deals with things that need to be handled across the
 * whole server, and not just the current script instance. (For example,
 * dealing with parser events)
 *
 * @author Shane Mc Cormack <shanemcc@gmail.com>
 */
public class GlobalIRCScripter implements ScriptObject, ChannelMessageListener {
    /** List storing bindings. */
    private final Map<String, BoundMethod> bindings = new HashMap<String, BoundMethod>();

    /**
     * Create a new IRCScripter.
     */
    public GlobalIRCScripter() { }

    /**
     * Get the command bindings we currently know about.
     *
     * @return the command bindings we know about.
     */
    protected BoundMethod getBoundMethod(final String command) {
        synchronized (bindings) {
            return bindings.get(command.toLowerCase());
        }
    }

    /**
     * Used by scripts to bind to a channel command.
     *
     * @param scripter IRCScripter that wanted the binding.
     * @param command Command to bind to.
     * @param flags Flags for the binding, currently unused.
     * @param object Object to bind on.
     * @param method Method to bind.
     * @return BoundMethod.
     */
    public BoundMethod bindCommand(final IRCScripter scripter, final String command, final String flags, final Object object, final Object method) {
        synchronized (bindings) {
            if (bindings.containsKey(command.toLowerCase())) { bindings.remove(command); }
            final BoundMethod boundMethod = new BoundMethod(scripter, object, method);
            bindings.put(command.toLowerCase(), boundMethod);
            return boundMethod;
        }
    }

    /**
     * Used by scripts to unbind a command.
     *
     * @param command Command to unbind.
     */
    public void unbindCommand(final String command) {
        synchronized (bindings) {
            bindings.remove(command.toLowerCase());
        }
    }

    /**
     * Remove any bindings related to the given script.
     *
     * @param command Command to unbind.
     */
    public void unbindScripter(final IRCScripter scripter) {
        synchronized (bindings) {
            final List<String> ks = new LinkedList(bindings.keySet());
            for (final String k : ks) {
                final BoundMethod bm = bindings.get(k);
                if (bm.scripter == scripter) {
                    bindings.remove(k);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelMessage(final Parser parser, final Date date, final ChannelInfo channel, final ChannelClientInfo client, final String message, final String host) {
        // A message, holy crap a message!
        final String[] bits = message.split(" ", 2);

        final BoundMethod bm = getBoundMethod(bits[0]);
        if (bm != null && bm.scripter != null) {
            bm.scripter.__getScript().call(bm.object, bm.method, parser, date, channel, client, bits[0], (bits.length > 1 ? bits[1] : ""));
        }
    }
}
