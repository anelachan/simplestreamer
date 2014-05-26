package simplestream;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Arrays;

public class OverloadedResponse extends Msg{
	ArrayList<JSONObject> connections = null;
	String connectionList = null;
	JSONObject serverData = null;
	
	OverloadedResponse(ArrayList<JSONObject> c,JSONObject sd){
		connections = c;
		serverData = sd;
		this.setConnectionList();
		try{
			obj.put("response","overloaded");
			obj.put("clients",connectionList);
			obj.put("server",serverData.toString());
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