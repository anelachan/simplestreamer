/* Msg.java
* Author: Anela Chan
* Date: 15 May 2014
* Description: Overloaded response contains "response":"overloaded", 
* a JSONArray converted to string representing each client and its 
* sport, and optionally a server field representing the source of 
* the stream if in remote mode. Inherits from Msg.
*/

package simplestream;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Arrays;

public class OverloadedResponse extends Msg{
	ArrayList<JSONObject> connections = null;
	String connectionList = null;
	JSONObject remoteServerData = null; 
	// remoteServerData may remain null, only has value if in remote mode
	
	OverloadedResponse(ArrayList<JSONObject> c,JSONObject rsd){ 
		connections = c;
		remoteServerData = rsd;
		this.setConnectionList();
		try{
			obj.put("response","overloaded");
			obj.put("clients",connectionList);
			if (remoteServerData != null)
				obj.put("server",remoteServerData.toString());
		} catch (JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void setConnectionList(){
		ArrayList<String> jsonStrings = new ArrayList<String>(3);
		for (JSONObject el: connections)
			jsonStrings.add(el.toString());
		connectionList = Arrays.toString(jsonStrings.toArray());
	}

}