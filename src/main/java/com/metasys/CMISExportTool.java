package com.metasys;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kbryd on 1/16/15.
 */
public class CMISExportTool {
    private static final Logger logger = Logger.getLogger(CMISExportTool.class.toString());

    public static void main(String[] a) {

        CommandLineParser parser = new BasicParser();

        Options options = new Options();
        options.addOption("h", false, "Print help for this application");
        options.addOption("u", true, "User login");
        options.addOption("p", true, "Password");
        options.addOption("f", true, "Destination folder location");
        options.addOption("url", true, "CMIS Endpoint URL (AtomPub)");
        options.addOption("s", "starting-path", true, "Start path");
        options.addOption(OptionBuilder.withArgName("max-levels")
                .hasArg()
                .withDescription("Number of levels")
                .withArgName("number of levels")
                .create("levels"));

        try {
            CommandLine line = parser.parse(options, a);

            if (line.hasOption("h")) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp(CMISExportTool.class.getCanonicalName(), options);
            }

            processExport(line.getOptionValue("url"), line.getOptionValue("u"), line.getOptionValue("p"), line);
        } catch (ParseException exp) {
            logger.log(Level.SEVERE, "Unexpected exception:" + exp.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unexpected exception:" + e.getMessage());
        }
    }

    private static void processExport(String url, String login, String password, CommandLine extraArgs) throws IOException {
        String startingPath = extraArgs.getOptionValue("starting-path");
        Integer maxLevels = -1;
        if (extraArgs.hasOption("levels")) {
            maxLevels = Integer.parseInt(extraArgs.getOptionValue("levels"));
        }

        logger.log(Level.INFO, "Connecting to " + url + " as " + login);
        logger.log(Level.INFO, "Starting export from folder: " + startingPath + " to " + extraArgs.getOptionValue("f"));
        logger.log(Level.INFO, "Number of levels: " + ((maxLevels == -1) ? "Unlimited" : maxLevels));
        CMISSession session = new CMISSession(login, password, url);
        session.connect();

        Exporter.export(session, startingPath, extraArgs.getOptionValue("f"), maxLevels);
    }
}
