package simplestream;
import java.util.Scanner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class SimpleStreamer{

    public final static int LOCAL_MODE = 0;
    public final static int REMOTE_MODE = 1;
    public static int mode;

    private int sport = 6262; //sport is port for serving connection requests
	private String hostName = null;
	private int rport = 6262;
	private int sleepTime = 100;
	boolean appLive = false;

    private Scanner keyboard = null;

	private SimpleStreamer(String[] args){

        CommandLineValues values = new CommandLineValues(args);
        CmdLineParser parser = new CmdLineParser(values);

        // Parse command line arguments
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.exit(1);
        }

        try {
            if (values.getSport() != 0)
                sport = values.getSport();
            if (values.getHostName() != null)
                hostName = values.getHostName();
            if (values.getRport() != 0)
                rport = values.getRport();
            if (values.getRate() != 0)
                sleepTime = values.getRate();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        appLive = true;
        Viewer myViewer = new Viewer();
        // uncomment line below prior to final projected delivery. commented out currently to ease load during testing.
        // LocalViewer localViewer = new LocalViewer(myViewer);
        Server server = new Server(sport, appLive);
        keyboard = new Scanner(System.in);

        if (hostName != null) {
            // example command line argument for remote mode
            // -sport 6263 -remote localhost -rport 6262 -rate 400
            mode = REMOTE_MODE;
            SetImgThread setImgThread = new SetImgThread();
            Client client = new Client(sport, hostName, rport, sleepTime, keyboard, appLive);
            // having 6 parameters is less than ideal but other option is nesting
        }
        else {
            mode = LOCAL_MODE;
            SetImgThread setImgThread = new SetImgThread();
        }
	}

	public static void main(String[] args){

		SimpleStreamer app = new SimpleStreamer(args);

	}

    // CommandLineValues employs the args4j library to extract command line arguments
    private static class CommandLineValues {

        @Option(name = "-rate", required = false,
                usage = "sleep time")
        private int rate;

        @Option(name = "-rport", required = false,
                usage = "remote server port")
        private int rport;

        @Option(name = "-remote", required = false,
                usage = "remote host name")
        private String hostName;

        @Option(name = "-sport", required = false,
                usage = "server port")
        private int sport;


        private boolean errorFree = false;

        public CommandLineValues(String... args) {
            CmdLineParser parser = new CmdLineParser(this);
            parser.setUsageWidth(80);
            try {
                parser.parseArgument(args);
                errorFree = true;
            } catch (CmdLineException e) {
                System.err.println(e.getMessage());
                parser.printUsage(System.err);
            }
        }

        // getters for command line arguments
        public int getSport() {
            return sport;
        }

        public String getHostName() {
            return hostName;
        }

        public int getRport() {
            return rport;
        }

        public int getRate() {
            return rate;
        }

    }

}