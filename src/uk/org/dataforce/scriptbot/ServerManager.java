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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.org.dataforce.libs.logger.Logger;
import uk.org.dataforce.scriptbot.config.Config;
import uk.org.dataforce.scriptbot.config.InvalidConfigFileException;

/**
 * This class is responsible for servers. Yay.
 */
public class ServerManager {

    /** Map of servers. */
    final Map<String, Server> serverList = new HashMap<String, Server>();

    /** My Bot. */
    final ScriptBot myBot;

    /**
     * Create a server manager.
     *
     * @param bot Bot that owns this ServerManager
     */
    public ServerManager(final ScriptBot bot) {
        myBot = bot;
    }

    /**
     * Get the bot that owns this ServerManager
     *
     * @return the bot that owns this ServerManager
     */
    public ScriptBot getBot() {
        return myBot;
    }

    /**
     * Get the server with the given name.
     *
     * @param serverName Server name to get
     * @return Server if it exists, else null.
     */
    public Server getServer(final String serverName) {
        return serverList.get(serverName);
    }

    /**
     * Get a list of all servers.
     *
     * @return A list of all servers.
     */
    public List<Server> getServers() {
        return new ArrayList<Server>(serverList.values());
    }

    /**
     * Load a server.
     *
     * @param serverName Name of server
     * @param configFileName Config file for server
     * @return Server if loaded, else null.
     */
    public Server loadServer(final String serverName, final String configFileName) {
        if (!serverList.containsKey(serverName)) {
            try {
                final Server server = new Server(this, serverName, createDefaultConfig(configFileName));
                serverList.put(serverName, server);
                ScriptBot.getBot().getLogger().info("Loaded server '" + serverName + "'");
                return server;
            } catch (final IOException ioe) {
                ScriptBot.getBot().getLogger().error("Unable to load server '" + serverName + "': " + ioe.getMessage());
            } catch (final InvalidConfigFileException icfe) {
                ScriptBot.getBot().getLogger().error("Unable to load server '" + serverName + "': " + icfe.getMessage());
            }
        }
        return null;
    }

    /**
     * Get the default settings.
     *
     * @return Defaults config settings
     *
     * @throws IOException If an error occurred loading the config
     * @throws InvalidConfigFileException If the config was invalid
     */
    public static Config createDefaultConfig(final String configFile) throws IOException, InvalidConfigFileException {
        final File file = new File(configFile);
        final Config defaults = new Config(file);
        if (!defaults.hasOption("server", "type")) { defaults.setOption("server", "type", "irc"); }
        if (!defaults.hasOption("server", "host")) { defaults.setOption("server", "host", "irc.quakenet.org"); }
        if (!defaults.hasOption("server", "port")) { defaults.setIntOption("server", "port", 6667); }
        if (!defaults.hasOption("server", "nickname")) { defaults.setOption("server", "nickname", "scriptbot"); }
        // if (!defaults.hasOption("server", "altnickname")) { defaults.setOption("server", "altnickname", "scriptbot`"); }
        // if (!defaults.hasOption("server", "username")) { defaults.setOption("server", "username", "scriptbot"); }
        // if (!defaults.hasOption("server", "realname")) { defaults.setOption("server", "realname", "ScriptBot"); }
        if (!defaults.hasOption("server", "enabled")) { defaults.setBoolOption("server", "enabled", false); }

        defaults.save();
        return defaults;
    }
}
