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
import com.dmdirc.parser.interfaces.ClientInfo;
import java.util.HashMap;
import java.util.Map;
import uk.org.dataforce.scriptbot.scripts.BoundMethod;
import uk.org.dataforce.scriptbot.scripts.Script;
import uk.org.dataforce.scriptbot.scripts.ScriptObject;

/**
 * The IRC Scripter simplifies certain common irc interactions such as binding
 * on commands (rather than parsing "onChannelMessage" events) and dealing with
 * user levels.
 *
 * @author Shane Mc Cormack <shanemcc@gmail.com>
 */
public class IRCScripter implements ScriptObject {
    /** The script that owns this IRCScripter. */
    private Script myScript;

    /** The event handler for this IRCScripter. */
    private EventHandler myEventHandler = new EventHandler(this);

    /** List storing bindings. */
    private final Map<String, BoundMethod> bindings = new HashMap<String, BoundMethod>();

    /**
     * Create a new IRCScripter.
     *
     * @param script Script that owns this IRCScripter.
     */
    public IRCScripter(final Script script) {
        myScript = script;
    }

    /**
     * Get the EventHandler object for this IRCScripter.
     *
     * @return the EventHandler object for this IRCScripter.
     */
    public EventHandler __getEventHandler() {
        return myEventHandler;
    }

    /**
     * Get the script object that owns us.
     *
     * @return the script object that owns us.
     */
    public Script __getScript() {
        return myScript;
    }

    /**
     * Get the command bindings we currently know about.
     *
     * @return the script object that owns us.
     */
    protected BoundMethod __getBoundMethod(final String command) {
        synchronized (bindings) {
            return bindings.get(command.toLowerCase());
        }
    }

    /**
     * Used by scripts to bind to a command.
     *
     * @param command Command to bind to.
     * @param flags Flags for the binding, currently unused.
     * @param method Method to bind.
     */
    public BoundMethod bindCommand(final String command, final String flags, final Object method) {
        return bindCommand(command, flags, null, method);
    }

    /**
     * Used by scripts to bind to a parser command.
     *
     * @param command Command to bind to.
     * @param flags Flags for the binding, currently unused.
     * @param object Object to bind on.
     * @param method Method to bind.
     * @return BoundMethod.
     */
    public BoundMethod bindCommand(final String command, final String flags, final Object object, final Object method) {
        synchronized (bindings) {
            if (bindings.containsKey(command.toLowerCase())) { bindings.remove(command); }
            final BoundMethod boundMethod = new BoundMethod(object, method);
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
     * Check if the given user has the given global flag.
     *
     * @param cci ChannelClientInfo object.
     * @praam flag Flag to check.
     * @Deprecated This function is experimental, and may be removed at any time.
     */
    @Deprecated
    public boolean hasBotFlag(final ChannelClientInfo cci, final char flag) {
        return hasBotFlag(cci.getClient(), flag);
    }

    /**
     * Check if the given user has the given global flag.
     *
     * @param ci ClientInfo object.
     * @praam flag Flag to check.
     * @Deprecated This function is experimental, and may be removed at any time.
     */
    @Deprecated
    public boolean hasBotFlag(final ClientInfo ci, final char flag) {
        // TODO: Actually check this...
        return true;
    }
}
