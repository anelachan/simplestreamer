/* StopRequest.java
* Author: Anela Chan
* Date: 15 May 2014
* Description: Sent by client to request to stop stream,
* generated if user hits newline. Inherits from Msg.
*/


package simplestream;
import org.json.JSONObject;
import org.json.JSONException;

public class StopRequest extends Msg{

	StopRequest(){
		try{
			obj.put("request","stopstream");
		} catch(JSONException e) {
			e.printStackTrace();
			System.exit(-1);
		}	
	}
	
}