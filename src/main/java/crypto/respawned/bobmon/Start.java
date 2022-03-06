package crypto.respawned.bobmon;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crypto.forestfish.utils.NotificationUtils;
import crypto.forestfish.utils.SystemUtils;
import crypto.forestfish.utils.TCPIPUtils;
import crypto.forestfish.utils.URLUtils;
import net.pushover.client.MessagePriority;

public class Start {

    private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) { 
        LOGGER.info("init()");

        boolean debug = false;

        BobcatSettings settings = parseCliArgs(args);
        settings.sanityCheck();

        /**
         * Sample replies from bobcat: 
         * 
           http://bobcatminer/status.json
           {
              "status": "",
              "gap": "",
              "miner_height": "pings.",
              "blockchain_height": "1007663",
              "epoch": "Node"
            }

            {
              "status": "synced",
              "gap": "1",
              "miner_height": "1007786",
              "blockchain_height": "1007787",
              "epoch": "26033"
            }

            http://bobcatminer/speed.json
            {
              "DownloadSpeed": "57 Mbit/s",
              "UploadSpeed": "16 Mbit/s",
              "Latency": "20.329312ms"
            }

            http://bobcatminer/temp.json
            {
              "timestamp": "2021-09-12 09:03:24 +0000 UTC",
              "temp0": 45,
              "temp1": 44,
              "unit": "°C"
            }
            
            http://bobcatminer/miner.json
            {
            <<full output>>
            }

         */

        int gapIncreaseCounter = 0;
        int stuckCounter = 0;
        int previousGAP = 0;
        int connectIssueCounter = 0;
        int offsetOver1kCounter = 0;
        while (true) {
            if (debug) LOGGER.info("httpGETContent() ..");
            String jsonSTR = TCPIPUtils.httpGETContent(settings.getBobcatStatusURL(), 10);
            if (null != jsonSTR) {
                BobcatStatus bcStatus = BobcatStatus.parseBobcatStatus(jsonSTR);
                if ((null != bcStatus) && !"".equals(bcStatus.getStatus())) {
                    if ("synced".equals(bcStatus.getStatus().toLowerCase())) {
                        LOGGER.info("All good, bobcat miner is SYNCED");
                        connectIssueCounter = 0;
                    } else if ("alert".equals(bcStatus.getStatus())) {
                        LOGGER.warn("We are in alert status ... stuckCounter=" + stuckCounter);
                        stuckCounter++;
                    } else if ("Helium API Timeout".equals(bcStatus.getStatus())) {
                        LOGGER.warn("We got an API timeout ... stuckCounter=" + stuckCounter);
                        stuckCounter++;
                    } else if ("Loading".equals(bcStatus.getStatus())) {
                        LOGGER.warn("We got are in loading state ... stuckCounter=" + stuckCounter);
                        stuckCounter++;
                    } else if ("syncing".equals(bcStatus.getStatus().toLowerCase())) {
                        Integer currentGAP = Integer.parseInt(bcStatus.getGap());
                        if (0 == previousGAP)  {
                            previousGAP = currentGAP;
                        } else {
                            Integer catchingInOnGAPDIFF = previousGAP - currentGAP;
                            boolean badState = false;
                            if (catchingInOnGAPDIFF > 0) {
                                LOGGER.info("Recovering, we are catching up .. catchingInOnGAPDIFF=" + catchingInOnGAPDIFF + ": " + bcStatus.toString());
                                stuckCounter = 0;
                                gapIncreaseCounter = 0;
                            } else if (catchingInOnGAPDIFF == 0) {
                                LOGGER.info("OK, we aint moving ... stuckCounter=" + stuckCounter + ": " + bcStatus.toString());
                                stuckCounter++;
                                if (stuckCounter > settings.getStuckThreshold()) badState = true;
                            } else {
                                gapIncreaseCounter++;
                                if (gapIncreaseCounter >= 3) badState = true;
                            } 
                            
                            if (currentGAP >= 1000) {
                            	LOGGER.warn("Current gap is over 1k");
                            	offsetOver1kCounter++;
                            }

                            if (offsetOver1kCounter >= 5) {
                            	LOGGER.warn("We are stuck in a bad state, over 1k offset 5 times in a row");
                            	badState = true;
                            }
                            
                            if (badState) {
                                LOGGER.warn("bcStatus.toString(): " + bcStatus.toString() + " currentGAP=" + currentGAP + ", previousGAP=" + previousGAP + ", catchingInOnGAPDIFF=" + catchingInOnGAPDIFF);
                                NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Bobcat Miner GAP issue", "gap is currently at " + currentGAP + ", catchingInOnGAPDIFF=" + catchingInOnGAPDIFF + ", previousGAP=" + previousGAP, MessagePriority.HIGH, settings.getBobcatStatusURL(), "Bobcat URL", "siren");
                                String bobcatHostname = URLUtils.extractURLHostnameFromURL(settings.getBobcatStatusURL());
                                LOGGER.warn("credit: https://www.reddit.com/r/BobcatMiner300/comments/pjokmu/fast_sync_for_users_stuck_syncing/");
                                LOGGER.warn("Run curl --user bobcat:miner --request POST http://" + bobcatHostname + "/admin/fastsync");
                                LOGGER.warn("Note! FASTSYNC. Not RESYNC.");
                                SystemUtils.halt();
                            }
                            previousGAP = currentGAP;
                        }
                    } else {
                        LOGGER.warn("bcStatus.toString(): " + bcStatus.toString());
                        NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Bobcat Miner UKNOWN state issue", "bcStatus.toString(): " + bcStatus.toString(), MessagePriority.HIGH, settings.getBobcatStatusURL(), "Bobcat URL", "siren");
                        LOGGER.error("Not sure what this state is: " + bcStatus.getStatus());
                        SystemUtils.halt();
                    }
                } else {
                    connectIssueCounter++;
                    LOGGER.warn("Connection issue when attempting to access " + settings.getBobcatStatusURL() + ", connectIssueCounter=" + connectIssueCounter);
                    if (connectIssueCounter >= settings.getStuckThreshold()) {
                        LOGGER.warn("bcStatus.toString(): " + bcStatus.toString());
                        NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Bobcat Miner UNREACHABLE", "connectIssueCounter: " + connectIssueCounter, MessagePriority.HIGH, settings.getBobcatStatusURL(), "Bobcat URL", "siren");
                        LOGGER.error("We seem to be stuck, bailing. connectIssueCounter=" + connectIssueCounter);
                        SystemUtils.halt();
                    }
                }
            } else {
                connectIssueCounter++;
                LOGGER.warn("Connection issue when attempting to access " + settings.getBobcatStatusURL() + ", connectIssueCounter=" + connectIssueCounter);
                if (connectIssueCounter >= settings.getStuckThreshold()) {
                    NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Bobcat Miner UNREACHABLE", "connectIssueCounter: " + connectIssueCounter, MessagePriority.HIGH, settings.getBobcatStatusURL(), "Bobcat URL", "siren");
                    LOGGER.error("We seem to be stuck, bailing. connectIssueCounter=" + connectIssueCounter);
                    SystemUtils.halt();
                }
            }

            // [60,600] secs results in {"message":"rate limit exceeded"}
            SystemUtils.sleepInSeconds(settings.getSleepTimeInSeconds());
        }

    }

    private static BobcatSettings parseCliArgs(String[] args) {

        BobcatSettings settings = new BobcatSettings();
        Options options = new Options();

        // API token app ID
        Option apiTokenApp = new Option("a", "apitokenappid", true, "API token app ID");
        options.addOption(apiTokenApp);

        // API token user ID
        Option apiTokenUser = new Option("u", "apitokenuserid", true, "API token user ID");
        options.addOption(apiTokenUser);

        // BobcatStatusURL
        Option bobcatStatusURL = new Option("b", "bobcalstatusurl", true, "bobcat status URL");
        options.addOption(bobcatStatusURL);
        
        // sleepTimeInSeconds
        Option sleepTimeInSeconds = new Option("s", "sleeptimeinseconds", true, "sleeptime in seconds");
        options.addOption(sleepTimeInSeconds);
        
        // stuckThreshold
        Option stuckThreshold = new Option("t", "stuckthreshold", true, "threshold for being considered stuck");
        options.addOption(stuckThreshold);

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("a")) settings.setApiTokenApp(cmd.getOptionValue("apitokenappid"));
            if (cmd.hasOption("u")) settings.setApiTokenUser(cmd.getOptionValue("apitokenuserid"));
            if (cmd.hasOption("b")) settings.setBobcatStatusURL(cmd.getOptionValue("bobcalstatusurl"));
            if (cmd.hasOption("s")) settings.setSleepTimeInSeconds(Integer.parseInt(cmd.getOptionValue("sleeptimeinseconds")));
            if (cmd.hasOption("t")) settings.setStuckThreshold(Integer.parseInt(cmd.getOptionValue("stuckthreshold")));

            settings.print();

        } catch (ParseException e) {
            LOGGER.error("ParseException: " + e.getMessage());
            formatter.printHelp(" ", options);
            SystemUtils.halt();
        }

        return settings;
    }

}
