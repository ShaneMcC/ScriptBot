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

import java.io.BufferedWriter;
import uk.org.dataforce.scriptbot.config.BlackHoleConfig;
import uk.org.dataforce.scriptbot.config.Config;
import uk.org.dataforce.scriptbot.config.InvalidConfigFileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import uk.org.dataforce.libs.cliparser.BooleanParam;
import uk.org.dataforce.libs.cliparser.CLIParam;
import uk.org.dataforce.libs.cliparser.CLIParser;
import uk.org.dataforce.libs.cliparser.StringParam;
import uk.org.dataforce.libs.logger.LogLevel;
import uk.org.dataforce.libs.logger.Logger;

/**
 * Main Bot Class.
 */
public class ScriptBot {
    /** Me */
    private static ScriptBot me = null;

    /** Version Config File */
    private static Config versionConfig = BlackHoleConfig.createInstance();

    /** The CLIParser */
    private static CLIParser cli = CLIParser.getCLIParser();

    /** The config directory file name */
    private static String configDirectory = "ScriptBot";

    /** The config file name */
    private static String configFile = "ScriptBot.conf";

    /** The time that the bot was started at */
    public static final Long startTime = System.currentTimeMillis();

    /** Global config. */
    private Config config;

    /** Shutdown hook. */
    private ShutdownHook shutdownHook;

    /** Daemon. */
    public final static ScriptBotDaemon daemon = new ScriptBotDaemon();

    /** PID File name. */
    static String pidFile = "";

    /**
     * Create the bot.
     */
    private ScriptBot() { }

    /**
     * Init the application.
     * Parses CLI Arguments, loads config file, and sets up the listen sockets.
     *
     * @param args CLI Arguments passed to application
     */
    private void init(final String[] args) {
        Logger.setLevel(LogLevel.INFO);
        loadVersionInfo();
        if (ScriptBotDaemon.canFork() && daemon.isDaemonized()) {
            Logger.setTag("(" + ScriptBotDaemon.getPID() + ") Child");
        } else {
            Logger.info("Starting ScriptBot (Version: " + getVersion() + ")..");
        }

        setupCLIParser();
        if (cli.wantsHelp(args)) {
            cli.showHelp("ScriptBot Help", "ScriptBot [options]");
            System.exit(0);
        }

        Logger.info("Adding shutdown hook");
        shutdownHook = new ShutdownHook(this);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        cli.parseArgs(args, true);

        setupLogging();

        if (ScriptBotDaemon.canFork() && daemon.isDaemonized()) {
            try {
                final CLIParam pidFileCLI = cli.getParam("-pidfile");
                pidFile = pidFileCLI.getStringValue().isEmpty() ? "scriptbot.pid" : pidFileCLI.getStringValue();
                Logger.info("Using pid file: " + pidFile);

                daemon.init(pidFile);
            } catch (final Exception e) {
                Logger.error("Daemon init failed. Exiting: " + e);
                e.printStackTrace();
                System.exit(1);
            }
        } else if (!ScriptBotDaemon.canFork() && cli.getParamNumber("-background") > 0) {
            Logger.error("Forking is not possible on the current OS (" +  System.getProperty("os.name") + ").");
        } else if (ScriptBotDaemon.canFork() && cli.getParamNumber("-background") > 0) {
            try {
                Logger.info("Forking to background...");
                Logger.info(null);

                // Before forking, close any sockets and files.
                Logger.setLevel(LogLevel.SILENT);
                shutdownHook.inactivate();
                this.shutdown(true);

                // Daemonise.
                daemon.daemonize();

                // Wait a short while for child to start so that user can see
                // its output without a shell prompt appearing!
                Thread.sleep(2000);

                // Exit the parent.
                System.exit(0);
            } catch (Throwable t) {
                Logger.error("Forking failed: " + t);
                t.printStackTrace();
                System.exit(1);
            }
        }

        if (cli.getParamNumber("-silent") > 0) {
            Logger.setLevel(LogLevel.SILENT);
        } else if (cli.getParamNumber("-debug") >= 9) {
            Logger.info("Enabling Stupidly Advanced Debugging Information (DEBUG9).");
            Logger.setLevel(LogLevel.DEBUG9);
        } else if (cli.getParamNumber("-debug") == 8) {
            Logger.info("Enabling Spammy Advanced Debugging Information (DEBUG8).");
            Logger.setLevel(LogLevel.DEBUG8);
        } else if (cli.getParamNumber("-debug") == 7) {
            Logger.info("Enabling Stupid Amounts of Advanced Debugging Information (DEBUG7).");
            Logger.setLevel(LogLevel.DEBUG7);
        } else if (cli.getParamNumber("-debug") == 6) {
            Logger.info("Enabling Loads More Advanced Debugging Information (DEBUG6).");
            Logger.setLevel(LogLevel.DEBUG6);
        } else if (cli.getParamNumber("-debug") == 5) {
            Logger.info("Enabling Yet More Advanced Debugging Information (DEBUG5).");
            Logger.setLevel(LogLevel.DEBUG5);
        } else if (cli.getParamNumber("-debug") == 4) {
            Logger.info("Enabling Even More Advanced Debugging Information (DEBUG4).");
            Logger.setLevel(LogLevel.DEBUG4);
        } else if (cli.getParamNumber("-debug") == 3) {
            Logger.info("Enabling More Advanced Debugging Information (DEBUG3).");
            Logger.setLevel(LogLevel.DEBUG3);
        } else if (cli.getParamNumber("-debug") == 2) {
            Logger.info("Enabling Advanced Debugging Information (DEBUG2).");
            Logger.setLevel(LogLevel.DEBUG2);
        } else if (cli.getParamNumber("-debug") == 1) {
            Logger.info("Enabling Debugging Information (DEBUG).");
            Logger.setLevel(LogLevel.DEBUG);
        }

        if (cli.getParamNumber("-config") > 0) { configDirectory = cli.getParam("-config").getStringValue(); }
        Logger.info("Loading Config..");

        try {
            config = createDefaultConfig();
            if (cli.getParamNumber("-configonly") == 1) {
                Logger.info("Config created. Exiting");
                System.exit(0);
            }
        } catch (IOException ex) {
            Logger.error("Error loading config: " + configDirectory + " (" + ex.getMessage() + "). Exiting");
            System.exit(1);
        } catch (InvalidConfigFileException ex) {
            Logger.error("Error loading config (" + ex.getMessage() + "). Exiting");
            System.exit(1);
        }

        // By now, we will have forked if required.

        if (ScriptBotDaemon.canFork() && daemon.isDaemonized()) {
            Logger.info("Forked and running! (PID: " + ScriptBotDaemon.getPID() +")");
            try {
                daemon.closeDescriptors();
            } catch (final IOException ioe) {
                Logger.error("Error closing file descriptors: " + ioe);
                ioe.printStackTrace();
            }
        } else {
            Logger.info("Running!");
        }
    }

    /**
     * Set up the log file.
     */
    public void setupLogging() {
        final CLIParam logFile = cli.getParam("-logfile");
        if (!logFile.getStringValue().isEmpty()) {
            final File file = new File(logFile.getStringValue());
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (final IOException ex) {
                    Logger.error("Unable to create log file: " + ex);
                }
            }

            try {
                final BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (!Logger.getTag().isEmpty()) {
                    bw.append("[");
                    bw.append(Logger.getTag());
                    bw.append("] ");
                }
                bw.append("Log file opened at: " + sdf.format(new Date(System.currentTimeMillis())));
                bw.append("\n");
                bw.flush();
                // We will never get to setting it here if it failed to write above!
                Logger.setWriter(bw);
                Logger.info("Using log file: " + file);
            } catch (final IOException ex) {
                Logger.error("Unable to write to log file: " + ex);
            }
        }
    }

    /**
     * Load the version info from the jar file if present.
     */
    public static void loadVersionInfo() {
        final InputStream version = ScriptBot.class.getResourceAsStream("/uk/org/dataforce/scriptbot/version.config");
        if (version != null) {
            try {
                versionConfig = new Config(version);
            } catch (final Exception e) { /** Oh well, default it is. */ }
        }
    }

    /**
     * Get the ScriptBot Version if possible.
     *
     * @return ScriptBot Version.
     */
    public static String getVersion() {
        return getVersion("scriptbot");
    }

    /**
     * Get the Version of a given component if possible.
     *
     * @param component Component to get version for.
     * @return Component Version.
     */
    public static String getVersion(final String component) {
        return versionConfig.getOption("versions", component, "Unknown");
    }

    /**
     * Get the versions of all known components.
     *
     * @return Component Versions Map.
     */
    public static Map<String,String> getVersions() {
        return versionConfig.getOptionDomain("versions");
    }

    /**
     * Get the start time
     *
     * @return the start time.
     */
    public static long getStartTime() {
        return startTime;
    }

    /**
     * Handle shutdown
     */
    public void shutdown() {
        shutdown(false);
    }

    /**
     * Handle shutdown
     * @param shuttingDown are we already shutting down?
     */
    public void shutdown(final boolean shuttingDown) {
        Logger.info("---------------------");
        Logger.info("Shuting down.");

        if (config != null) {
            Logger.info("Saving config to '"+configFile+"'");
            config.save();
        }

        if (ScriptBotDaemon.canFork() && daemon.isDaemonized()) {
            if (!pidFile.isEmpty()) {
                Logger.info("Removing pid file");
                final File pid = new File(pidFile);
                if (pid.exists()) { pid.delete(); }
            }
        }

        final BufferedWriter bw = Logger.getWriter();
        if (bw != null) {
            Logger.info("Closing log file");
            Logger.setWriter(null);
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (!Logger.getTag().isEmpty()) {
                    bw.append("[");
                    bw.append(Logger.getTag());
                    bw.append("] ");
                }
                bw.append("Log file closed at: " + sdf.format(new Date(System.currentTimeMillis())));
                bw.append("\n");
                bw.flush();
                bw.close();
            } catch (final IOException ioe) { /** Oh well. */ }
        }

        Logger.info("Deactivating shutdown hook.");
        if (shutdownHook != null) { shutdownHook.inactivate(); }
        if (!shuttingDown) {
            Logger.info("Exiting.");
            System.exit(0);
        }
    }

    /**
     * Get the name of the configfile
     *
     * @return The name of the configfile
     */
    public static String getConfigDirName() {
        return configDirectory;
    }

    /**
     * Get the name of the configfile
     *
     * @return The name of the configfile
     */
    public static String getConfigFileName() {
        return configFile;
    }

    /**
     * Setup the cli parser.
     * This clears the current CLIParser params and creates new ones.
     *
     * @return the CLIParser.
     */
    private static void setupCLIParser() {
        cli.clear();
        cli.add(new BooleanParam('h', "help", "Show Help"));
        cli.add(new BooleanParam('d', "debug", "Enable extra debugging. (Use multiple times for more)"));
        cli.add(new BooleanParam('s', "silent", "Disable all output"));
        cli.add(new BooleanParam((char)0, "configonly", "Generate default config file then exit."));
        cli.add(new StringParam('c', "config", "Alternative config directory to use"));
        cli.add(new BooleanParam((char)0, "enableDebugOptions", "Enable 'debugging.*' config settings"));
        if (ScriptBotDaemon.canFork()) {
            cli.add(new BooleanParam((char)0, "background", "Fork into background (EXPERIMENTAL)"));
            cli.add(new StringParam((char)0, "pidfile", "Change pidfile location (Default: ./scriptbot.pid)"));
        } else {
            cli.add(new BooleanParam((char)0, "background", "Fork into background (EXPERIMENTAL) [UNSUPPORTED ON THIS OS]"));
            cli.add(new StringParam((char)0, "pidfile", "Change pidfile location (Default: ./scriptbot.pid) [UNSUPPORTED ON THIS OS]"));
        }
        cli.add(new StringParam((char)0, "logfile", "Log file to use for console output. (Default: none)"));
        cli.setHelp(cli.getParam("-help"));
    }

    /**
     * Get the ScriptBot instance.
     *
     * @return the ScriptBot instance.
     */
    public static ScriptBot getBot() {
        return me;
    }

    /**
     * Get the default settings.
     *
     * @return Defaults config settings
     *
     * @throws IOException If an error occurred loading the config
     * @throws InvalidConfigFileException If the config was invalid
     */
    public static Config createDefaultConfig() throws IOException, InvalidConfigFileException {
        final File directory = new File(configDirectory);
        final File file = new File(directory, configFile);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Unable to create config directory.");
            }
        }
        final Config defaults = new Config(file);
        if (!defaults.hasOption("server", "host")) { defaults.setOption("server", "host", "irc.quakenet.org"); }
        if (!defaults.hasOption("server", "port")) { defaults.setOption("server", "port", "6667"); }
        if (!defaults.hasOption("server", "nickname")) { defaults.setOption("server", "nickname", "scriptbot"); }
        if (!defaults.hasOption("server", "altnickname")) { defaults.setOption("server", "altnickname", "scriptbot`"); }
        if (!defaults.hasOption("server", "username")) { defaults.setOption("server", "username", "scriptbot"); }
        if (!defaults.hasOption("server", "realname")) { defaults.setOption("server", "realname", "ScriptBot"); }

        if (!defaults.hasOption("channels", "autojoin")) {
            final List<String> autojoinchannels = new ArrayList<String>();
            autojoinchannels.add("#dataforce");
            autojoinchannels.add("#mdbot");
            defaults.setListOption("channels", "autojoin", autojoinchannels);
        }


        return defaults;
    }

    /**
     * Returns the global config.
     *
     * @return Global config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Start the application.
     *
     * @param args CLI Arguments passed to application
     */
    public static void main(String[] args) {
        me = new ScriptBot();
        me.init(args);
    }
}
