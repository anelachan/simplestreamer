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