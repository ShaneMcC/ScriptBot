/*
 *  Copyright 2013 Shane Mc Cormack <shanemcc@gmail.com>.
 *  See LICENSE.txt for licensing details.
 */
package uk.org.dataforce.scriptbot.scripts.irc;

import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import java.util.Date;
import uk.org.dataforce.scriptbot.scripts.BoundMethod;

/**
 * This object binds to parser events so that scripts can't call the parser
 * callback methods.
 *
 * @author Shane Mc Cormack <shanemcc@gmail.com>
 */
public class EventHandler implements ChannelMessageListener {

    /** The IRCScripter that we handle events for. */
    private IRCScripter ircscripter;

    /**
     * Create an EventHandler for the given IRCScripter
     * @param aThis
     */
    public EventHandler(final IRCScripter ircscripter) {
        this.ircscripter = ircscripter;
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelMessage(final Parser parser, final Date date, final ChannelInfo channel, final ChannelClientInfo client, final String message, final String host) {
        // A message, holy crap a message!
        final String[] bits = message.split(" ", 2);

        final BoundMethod bm = ircscripter.__getBoundMethod(bits[0]);
        if (bm != null) {
            ircscripter.__getScript().call(bm.object, bm.method, parser, date, channel, client, bits[0], (bits.length > 1 ? bits[1] : ""));
        }
    }

}
