/* SimpleStreamer.java
* Author: Anela Chan and King Chan
* Date: 29 May 2014
* Description: Runs a streaming application with a viewer. 
* Always acts as a server that can accept connections and send out the stream 
* to remote clients. If in Local mode: run webcam thread. If in Remote mode: 
* act as client to connect to remote SimpleStreamer and receive stream.
* To kill the application, whether in local or remote mode, enter a newline.
*/

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
    private Viewer myViewer = null;
    private LocalViewer localViewer = null;
    public static String img = null; // either WebCam or GetImgThread will write, LocalViewer reads
    private Server server = null;
    private Client client = null; // if remote mode
    private WebCam myWebCam = null; // if local mode

	SimpleStreamer(String[] args){

        this.setSpecifications(args);
        //myViewer = new Viewer();
        //localViewer = new LocalViewer(myViewer);
        
        // example command line argument for remote mode:
        // -sport 6263 -remote localhost -rport 6262 -rate 400
        if (mode == REMOTE_MODE) {
            server = new Server(sport, hostName, rport);
            client = new Client(hostName, rport, sport, sleepTime);
            // img will be set in GetImgThread
        }
        else if (mode == LOCAL_MODE){
            server = new Server(sport);
            myWebCam = new WebCam();
            // sets img from local webcam
        } 
        else
            assert false;
            
	}

	public static void main(String[] args){
        SimpleStreamer app = new SimpleStreamer(args);
        Scanner keyboard = new Scanner(System.in);
        if (keyboard.nextLine().isEmpty())
            app.kill();
        return;
        
	}

    public void kill(){
        if (mode == LOCAL_MODE)
            myWebCam.interrupt();
        if (mode == REMOTE_MODE)
            client.stopClient();
        server.interrupt();
        System.exit(0);
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