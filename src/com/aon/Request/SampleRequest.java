package com.aon.Request;


import javax.servlet.http.HttpServletRequest;

import com.ibm.ecm.extension.PluginRequestFilter;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONArtifact;
import com.ibm.json.java.JSONObject;

/**
 * Provides an abstract class that is extended to create a filter for requests to a particular service. The filter is provided with the 
 * request parameters before being examined by the service. The filter can change the parameters or reject the request.
 */
public  class SampleRequest extends PluginRequestFilter {

	/**
	 * Returns the names of the services that are extended by this filter.
	 * 
	 * @return A <code>String</code> array that contains the names of the services.
	 */
	public String[] getFilteredServices() {
		return new String[] { "/p8/search" };
	}

	/**
	 * Filters a request that is submitted to a service.
	 * 
	 * @param callbacks
	 *            An instance of <code>PluginServiceCallbacks</code> that contains several functions that can be used by the
	 *            service. These functions provide access to plug-in configuration and content server APIs.
	 * @param request
	 *            The <code>HttpServletRequest</code> object that provides the request. The service can access the invocation parameters from the
	 *            request. <strong>Note:</strong> The request object can be passed to a response filter to allow a plug-in to communicate 
	 *            information between a request and response filter.
	 * @param jsonRequest
	 *            A <code>JSONArtifact</code> that provides the request in JSON format. If the request does not include a <code>JSON Artifact</code>  
	 *            object, this parameter returns <code>null</code>.
	 * @return A <code>JSONObject</code> object. If this object is not <code>null</code>, the service is skipped and the
	 *            JSON object is used as the response.
	 */
	public  JSONObject filter(PluginServiceCallbacks callbacks, HttpServletRequest request, JSONArtifact jsonRequest) throws Exception {
		
		//Getting Request Object
		JSONObject jsonObj = (JSONObject) jsonRequest;
		
		System.out.println("RequestObjectBefore"+jsonObj.toString());
		
		//searchCriteria JSON array contians all the search criteria which user selects in UI
		JSONArray criteria = (JSONArray) jsonObj.get("searchCriteria");
		
		//Below is the JSON Object for Language.
		/*{
			"defaultOperator": "IN",
			"minValue": null,
			"format": null,
			"itemId": "",
			"id": "Language",
			"valueRequired": false,
			"dataType": "xs:string",
			"readOnly": false,
			"selectedOperator": "IN",
			"values": ["en", "de"],
			"availableOperators": ["IN", "INANY", "NOTIN", "NULL", "NOTNULL"],
			"maxLength": 2,
			"hidden": false,
			"name": "Language",
			"allowedValues": ["de", "en"],
			"defaultValue": ["en", "de"],
			"cardinality": "LIST",
			"maxValue": null
		}*/
		
		
		JSONObject language=null;
		boolean isLanguageAvailable= false;
		
		//Chekcing whether Language is available in user search criteria.
		//If it not available then create a new Language with values DE and EN. Else main  
		for(int i=0;i<criteria.size();i++)
		{
			JSONObject values = (JSONObject) criteria.get(i);
			
			//If the searchCriteria contains Language ,Don't create a new object.
			if(values.get("id").equals("Language"))
			{
				language= (JSONObject) criteria.get(i);
				isLanguageAvailable=true;
				break;
			}
		}
		
		if(!isLanguageAvailable)
			language= new JSONObject();

		language.put("defaultOperator", "IN");
		language.put("minValue", null);
		language.put("format", null);
		language.put("itemId", "");
		language.put("id", "Language");
		language.put("valueRequired", false);
		language.put("dataType", "xs:string");
		language.put("readOnly", false);
		language.put("selectedOperator", "IN");
		
		JSONArray values = new JSONArray();
		values.add("en");
		values.add("de");
		language.put("values", values);
		
		JSONArray availableOperations = new JSONArray();
		availableOperations.add("IN");
		availableOperations.add("INANY");
		availableOperations.add("NOTIN");
		availableOperations.add("NULL");
		availableOperations.add("NOTNULL");
		language.put("availableOperators", availableOperations);
		
		language.put("maxLength", 2);
		language.put("hidden", false);
		language.put("name", "Language");
		
		JSONArray allowedValues= new JSONArray();
		allowedValues.add("de");
		allowedValues.add("en");
		language.put("allowedValues", allowedValues);
		
		JSONArray defaultValue = new JSONArray();
		defaultValue.add("en");
		defaultValue.add("de");
		language.put("defaultValue", defaultValue);
		
		language.put("cardinality", "LIST");
		language.put("maxValue", null);
		
		//Adding Language Property and it's values EN and DE for the search criteria.
		if(!isLanguageAvailable)
			criteria.add(language);
		
		JSONObject jsonObj1 = (JSONObject) jsonRequest;
		System.out.println("RequestObjectAfter"+jsonObj1.toString());
		
		return null;

	}
	
}
