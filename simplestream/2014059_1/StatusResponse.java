/* StatusResponse.java
* Author: Anela Chan
* Date: 15 May 2014
* Description: Sent by server/ MsgPassingThread, 
* includes number of connected clients whether handover is implemented
* (yes), current mode and whether ratelimiting is implemented (yes).
* Inherits from Msg.
*/

package simplestream;
import org.json.JSONObject;
import org.json.JSONException;

public class StatusResponse extends Msg{

	private String mode;
	private int numClients;
	
	StatusResponse(String m,int nc){
		mode = m;
		numClients = nc;
		try{
			obj.put("handover","yes");
			obj.put("ratelimiting","yes");
			obj.put("clients",numClients);
			obj.put("streaming",mode);
			obj.put("response","status");
			
		} catch (JSONException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}