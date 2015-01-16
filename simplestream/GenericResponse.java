/* GenericResponse.java
Author: Anela Chan
Date: 16 May 2014
Description: Generates a JSON object for any simple server response 
with only one field. Inherits from Msg.
*/

package simplestream;

import org.json.JSONException;

public class GenericResponse extends Msg{

	private String response = null;
	
	@SuppressWarnings("unchecked")
	GenericResponse(String r){
		response = r;
		try{
			obj.put("response",response); 
		} catch (JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
}