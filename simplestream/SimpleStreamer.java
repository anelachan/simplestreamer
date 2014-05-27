package simplestream;
import java.util.Scanner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class SimpleStreamer{

    /* for command line parsing */
    private CommandLineValues values = null;
    private CmdLineParser parser = null;

    /* specifications */
    private final static int LOCAL_MODE = 0;
    private final static int REMOTE_MODE = 1;
    private int mode;
    // for serving connection requests as SERVER
    private int sport = 6262; 
    // for making a connection request as CLIENT
	private String hostName = null;
	private int rport = 6262;
	private int sleepTime = 100;

    /* utilities for running the app */
	boolean appLive = false;
    private Scanner keyboard = null;
    private Viewer myViewer = null;
    private LocalViewer localViewer = null;
    public static String img = null; // either WebCam or GetImgThread will write, LocalViewer reads

	private SimpleStreamer(String[] args){

        this.setSpecifications(args);
        appLive = true;
        myViewer = new Viewer();
        // localViewer = new LocalViewer(myViewer);
        keyboard = new Scanner(System.in);
        Server server = new Server(sport, appLive);
        

        // example command line argument for remote mode:
        // -sport 6263 -remote localhost -rport 6262 -rate 400
        if (mode == REMOTE_MODE) {
            Client client = new Client(sport, hostName, rport, sleepTime, keyboard, appLive);
            // img will be set in GetImgThread
        }
        else if (mode == LOCAL_MODE){
            WebCam myWebCam = new WebCam();
            // sets img from local webcam
        } else
            assert false;
	}

	public static void main(String[] args){

		SimpleStreamer app = new SimpleStreamer(args);

	}

    private void setSpecifications(String[] args){
        // Parse command line arguments
        values = new CommandLineValues(args);
        parser = new CmdLineParser(values);

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

            this.setMode();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void setMode(){
        if (hostName != null)
            mode = REMOTE_MODE;
        else
            mode = LOCAL_MODE;
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