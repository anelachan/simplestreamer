/* Msg.java
* Author: Anela Chan
* Date: 15 May 2014
* Description: Models any message passed in SimpleStreamer 
* protocol, contains a JSONObject with the ability to convert
* to string for easy writing on socket.
*/

package simplestream;
import org.json.JSONObject;

public class Msg{
	JSONObject obj = null;

	Msg(){
		obj = new JSONObject();
	}

	public String toJSONString(){
		return obj.toString();
	}
}