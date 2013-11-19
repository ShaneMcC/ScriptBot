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
package uk.org.dataforce.scriptbot;

import uk.org.dataforce.scriptbot.scripts.ScriptHandler;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DataInListener;
import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.parser.interfaces.callbacks.NickInUseListener;
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import com.dmdirc.parser.interfaces.callbacks.NumericListener;
import com.dmdirc.parser.irc.IRCParser;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import uk.org.dataforce.libs.logger.LogFactory;
import uk.org.dataforce.libs.logger.Logger;
import uk.org.dataforce.scriptbot.config.Config;
import uk.org.dataforce.scriptbot.scripts.ParserBridge;

/**
 * Class that represents an individual server.
 */
public class Server implements ServerReadyListener, DataInListener, DataOutListener, DebugInfoListener, NickInUseListener, NumericListener {

    /** My Manager. */
    private final ServerManager manager;

    /** My Name. */
    private final String name;

    /** My Config File. */
    private final Config configFile;

    /** My Parser */
    private Parser myParser;

    /** My Script Handler */
    private ScriptHandler myScriptHandler;

    /** My Parser Bridge */
    private ParserBridge myParserBridge;

    /** My logger. */
    private Logger logger;

    /** Have we had the 001 from our server? (Used for nickinuse) */
    private boolean got001 = false;

    /** Have we tried our altnick? (Used for nickinuse) */
    private boolean triedAlt = false;

    /** What nickname do we think we have? (Used for nickinuse) */
    private String thinkNickname;

    /**
     * Create a new Server
     *
     * @param name Server name
     * @param configFile Server config file.
     */
    public Server(final ServerManager manager, final String name, final Config configFile) {
        this.manager = manager;
        this.name = name;
        this.configFile = configFile;
        logger = LogFactory.getLogger();
        logger.setTag(name);
    }

    /**
     * Get the logger for this server.
     *
     * @return Server logger.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Return the manager of this server.
     *
     * @return The manager of this server.
     */
    public ServerManager getManager() {
        return manager;
    }

    /**
     * Return the name of this server.
     *
     * @return The name of this server.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the config for this server.
     *
     * @return The config for this server.
     */
    public Config getConfig() {
        return configFile;
    }

    /**
     * Get the current parser for this Server
     *
     * @return The current parser for this Server
     */
    public Parser getParser() {
        return myParser;
    }

    /**
     * Get the script handler for this Server
     *
     * @return The script handler for this Server
     */
    public ScriptHandler getScriptHandler() {
        return myScriptHandler;
    }

    /**
     * Create a new parser based on the config file.
     *
     * @return New PARSER instance based on the config file.
     * @throw URISyntaxException if the settings for the parser are invalid.
     */
    private Parser createParser() throws URISyntaxException {
        Parser parser = null;
        final String parserType = configFile.getOption("server", "type", "").toLowerCase();

        if (parserType.equals("irc")) {
            final String scheme = configFile.getBoolOption("server", "ssl", false) ? "ircs" : "irc";
            final URI uri = new URI(scheme, "", configFile.getOption("server", "host", "irc.quakenet.org"), configFile.getIntOption("server", "port", 6667), "", "", "");

            final MyInfo myDetails = new MyInfo();
            final String nickname = configFile.getOption("server", "nickname", "ScriptBot");
            myDetails.setNickname(nickname);
            myDetails.setAltNickname(configFile.getOption("server", "altnickname", nickname));
            myDetails.setUsername(configFile.getOption("server", "username", nickname));
            myDetails.setRealname(configFile.getOption("server", "realname", nickname));

            IRCParser ircparser = new IRCParser(myDetails, uri);
            parser = ircparser;
            thinkNickname = nickname;

            if (configFile.hasOption("server", "bindip")) {
                parser.setBindIP(configFile.getOption("server", "bindip", ""));
            }

            ircparser.setNickname(configFile.getOption("server", "nickname", ""));

        }

        return parser;
    }

    /**
     * Run this server.
     *
     * @return True if the server started running.
     */
    public boolean begin() {
        if (myParser != null) { return false; }
        if (!configFile.getBoolOption("server", "enabled", false)) {
            logger.error("Server not enabled, aborting.");
            return false;
        }

        try {
            myParser = createParser();
        } catch (final URISyntaxException use) {
            logger.error("Error creating parser: " + use);
            return false;
        }
        myScriptHandler = new ScriptHandler(this);
        if (configFile.hasFlatDomain("scripts")) {
            for (final String script : configFile.getFlatDomain("scripts")) {
                final File scriptFile = new File(manager.getBot().getScriptsDirectory(), script);
                logger.info("Loading script: " + scriptFile.toString());
                if (scriptFile.exists()) {
                    myScriptHandler.loadScript(scriptFile);
                }
            }
        }

        got001 = false;
        triedAlt = false;
        myParser.getCallbackManager().addAllCallback(this);
        myParserBridge = new ParserBridge(this);

        myParser.connect();

        return true;
    }

    /**
     * Stop this server.
     *
     * @return True if the server stopped running.
     */
    public boolean stop(final String reason) {
        if (myParser == null) { return false; }

        if (myParser instanceof IRCParser) {
            ((IRCParser)myParser).quit("Shutting Down");
        } else {
            myParser.disconnect(reason);
        }
        if (myParserBridge != null) { myParserBridge.clear(); }
        if (myScriptHandler != null) { myScriptHandler.unload(); }
        myParserBridge = null;
        myScriptHandler = null;
        myParser = null;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void onServerReady(final Parser parser, final Date date) {
         if (configFile.hasFlatDomain("autojoin")) {
            for (final String channel : configFile.getFlatDomain("autojoin")) {
                parser.joinChannel(channel);
            }
        }
    }



    /** {@inheritDoc} */
    @Override
    public void onNickInUse(final Parser parser, final Date date, final String nickname) {
        if (parser instanceof IRCParser && !got001) {
            final IRCParser ircParser = (IRCParser)parser;
            // If this is before 001 we will try and get a nickname, else we will leave the nick as-is
            if (triedAlt) {
                final MyInfo myInfo = ircParser.getMyInfo();
                final String magicAltNick = "_" + myInfo.getNickname();
                if (parser.getStringConverter().equalsIgnoreCase(thinkNickname, myInfo.getAltNickname()) && !myInfo.getAltNickname().equalsIgnoreCase(magicAltNick)) {
                    thinkNickname = myInfo.getNickname();
                }
                parser.getLocalClient().setNickname(myInfo.getPrependChar() + thinkNickname);
            } else {
                parser.getLocalClient().setNickname(ircParser.getMyInfo().getAltNickname());
                triedAlt = true;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onNumeric(final Parser parser, final Date date, final int numeric, final String[] token) {
        if (numeric == 1) { got001 = true; }
    }

    /** {@inheritDoc} */
    @Override
    public void onDataIn(final Parser parser, final Date date, final String data) {
        logger.debug3(String.format("[ IN] %s", data));
    }

    /** {@inheritDoc} */
    @Override
    public void onDataOut(final Parser parser, final Date date, final String data, final boolean fromParser) {
        logger.debug3(String.format("[OUT] %s", data));
    }

    /** {@inheritDoc} */
    @Override
    public void onDebugInfo(final Parser parser, final Date date, final int level, final String data) {
        logger.debug4(String.format("%d: %s", level, data));
    }

}
