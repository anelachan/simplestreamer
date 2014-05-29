/* LocalViewer.java
 * Created by kingchan on 22/05/2014.
 * Description: Runs a 320x240 viewer for the local 
 * instance of SimpleStreamer.
 */

package simplestream;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;



import javax.swing.*;
import java.util.concurrent.ArrayBlockingQueue;


public class LocalViewer extends Thread {

    Viewer myViewer = null;

    LocalViewer(Viewer viewer) {
        myViewer = viewer;
        this.start();
    }

    public void run() {
        JFrame frame = null;
        String imgData = null;
        String img = null;

        try {
            frame = new JFrame("Simple Stream Viewer");
            frame.setVisible(true);
            frame.setSize(320, 240);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(myViewer);
        } catch (Exception e) {
            System.out.println("Viewer: " + e.getMessage());
        }

        while(true) {
            if((img = SimpleStreamer.img) != null) {

                try {
                    JSONObject obj = new JSONObject(img);
                    imgData = obj.get("data").toString();
                } catch(JSONException e){
                    e.printStackTrace();
                    System.exit(-1);
                }

                byte[] nobase64_image = Base64.decodeBase64(Base64.decodeBase64(imgData));
                /* Decompress the image */
                byte[] decompressed_image = Compressor.decompress(nobase64_image);
                /* Give the raw image bytes to the viewer. */
                myViewer.ViewerInput(decompressed_image);
                frame.repaint();
            }
            else {
                System.out.println("Waiting for frames...");
            }
        }
    }
}
