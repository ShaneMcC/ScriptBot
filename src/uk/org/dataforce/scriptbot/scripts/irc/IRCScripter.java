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
 * The non-global IRCScripter (this class) is the actual interface that scripts
 * have access to, and hands off to the GlobalIRCScripter where required.
 *
 * @author Shane Mc Cormack <shanemcc@gmail.com>
 */
public class IRCScripter implements ScriptObject {
    /** The script that owns this this IRCScripter. */
    private Script myScript;

    /** The Global IRCScripter. */
    private GlobalIRCScripter myGlobalIRCScripter;

    /** List storing bindings. */
    private final Map<String, BoundMethod> bindings = new HashMap<String, BoundMethod>();

    /**
     * Create a new IRCScripter.
     */
    public IRCScripter(final GlobalIRCScripter globalIRCScripter, final Script script) {
        myGlobalIRCScripter = globalIRCScripter;
        myScript = script;
    }

    /**
     * Get the Script object for this IRCScripter.
     *
     * @return the Script object for this IRCScripter.
     */
    public Script __getScript() {
        return myScript;
    }

    /**
     * Used by scripts to bind to a channel command.
     *
     * @param command Command to bind to.
     * @param flags Flags for the binding, currently unused.
     * @param object Object to bind on.
     * @param method Method to bind.
     * @return BoundMethod.
     */
    public BoundMethod bindCommand(final String command, final String flags, final Object method) {
        return this.bindCommand(command, flags, null, method);
    }

    /**
     * Used by scripts to bind to a channel command.
     *
     * @param command Command to bind to.
     * @param flags Flags for the binding, currently unused.
     * @param object Object to bind on.
     * @param method Method to bind.
     * @return BoundMethod.
     */
    public BoundMethod bindCommand(final String command, final String flags, final Object object, final Object method) {
        return myGlobalIRCScripter.bindCommand(this, command, flags, object, method);
    }

    /**
     * Used by scripts to unbind a command.
     *
     * @param command Command to unbind.
     */
    public void unbindCommand(final String command) {
        myGlobalIRCScripter.unbindCommand(command);
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
        return this.hasBotFlag(cci.getClient(), flag);
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
