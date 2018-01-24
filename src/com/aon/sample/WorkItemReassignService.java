package com.aon.sample;

/**
 * 
 */

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.filenet.api.collection.UserSet;
import com.filenet.api.constants.PrincipalSearchAttribute;
import com.filenet.api.constants.PrincipalSearchSortType;
import com.filenet.api.constants.PrincipalSearchType;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.security.Realm;
import com.filenet.api.security.User;
import com.ibm.ecm.extension.PluginService;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.json.java.JSONObject;

import filenet.vw.api.VWFetchType;
import filenet.vw.api.VWQueue;
import filenet.vw.api.VWQueueQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWStepElement;


public class WorkItemReassignService extends PluginService {

	@Override
	public void execute(PluginServiceCallbacks callbacks, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		JSONObject responseJSON = new JSONObject();
		
		StringBuilder buffer = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        buffer.append(line);
	    }
	    String data = buffer.toString();
	    
	    JSONObject jsonobj = JSONObject.parse(data);
	    
	    String fromUserId= (String)jsonobj.get("fromUid");
	    String toUserId= (String)jsonobj.get("toUid");
	    String server = (String)jsonobj.get("server");
	    
	    String requestType = (String)jsonobj.get("requestType");
	    
	    if(requestType.equals("getWorkflowDetails"))
	    {
	    	
	    	Domain domain = callbacks.getP8Domain(server,null);

		    VWSession vwSession = new VWSession();
		    
			vwSession.setBootstrapCEURI("http://bngwidap105.aonnet.aon.net:9080/wsi/FNCEWS40MTOM/");
			
			vwSession.logonByDomain("P8V52_DEVLS", "A0693019", "", "POCCP");
			
			System.out.println("vwsession aa "+vwSession.getConnectionPointName());
			
		       
		    String fromUidExists = checkUserExistsOrNot(domain, fromUserId);
		    String toUidExists = checkUserExistsOrNot(domain, toUserId);
		    
		    String message="";
		    String status="";
		    int count=0;
		    
		    if(fromUidExists.equals("")){
		    	message= message +"From Userid :  "+fromUserId +"is not exists. ";
		    	status="invalidid";
		    }
		    	
		    if(toUidExists.equals("")){
		    	message= message +"To Userid : "+ toUserId+" is not exists. ";
		    	status="invalidid";
		    }
		    
		    if(!fromUidExists.equals("") && !toUidExists.equals("")){
		    	
		    	String workflowName= (String)jsonobj.get("workflowName");
		    	
		        count=getWorkflowCount(vwSession,fromUserId,workflowName);
		    	
		        if(count==0)
		        {
		        	message = "No work items found for the user :  "+fromUserId;
		        }
		        else
		        {
		        	message = "Total "+count+" Work items found for the user : " +fromUserId;
		        }
		    	status="validid";
		    }
		    
			
			
			
			responseJSON.put("message1",message);
			responseJSON.put("message2",status);
			responseJSON.put("message3",fromUidExists);
			responseJSON.put("message4",toUidExists);
			responseJSON.put("message5",count);
	    }
	    if(requestType.equals("reassignWorkFlow"))
	    {
	    	
	    	Domain domain = callbacks.getP8Domain(server,null);

		    VWSession vwSession = new VWSession();
		    
			vwSession.setBootstrapCEURI("http://bngwidap105.aonnet.aon.net:9080/wsi/FNCEWS40MTOM/");
			
			vwSession.logonByDomain("P8V52_DEVLS", "A0693019", "", "POCCP");
			
			System.out.println("vwsession aa "+vwSession.getConnectionPointName());
			
		       
		    /*String fromUidExists = checkUserExistsOrNot(domain, fromUserId);
		    String toUidExists = checkUserExistsOrNot(domain, toUserId);*/
		    
		    String message="";
		    String status="";
		    
		    
		    VWQueue queue = vwSession.getQueue("Inbox(0)");
	        System.out.println("Total Items in Inbox: "+queue.fetchCount());
	                
	        int queryFlag = VWQueue.QUERY_READ_LOCKED;
	        int queryType = VWFetchType.FETCH_TYPE_QUEUE_ELEMENT;
	        
	        queryType = VWFetchType.FETCH_TYPE_STEP_ELEMENT;
	        
	        int fromNameId = vwSession.convertUserNameToId(fromUserId);
	        
	        /*String eventLogQueryFilter = "(F_BoundUser= :1)";
	        
	        Object substitutionVars[] = new Object[1];

	        substitutionVars[0] = fromNameId;*/
	        
	      
	        String eventLogQueryFilter = null;
	        Object substitutionVars[] =null;
	        
	        String workflowName= (String)jsonobj.get("workflowName");
	        Long workflowReassignCount= (Long)jsonobj.get("reassignCount");
	        int workflowReassignCounts = workflowReassignCount.intValue();
	        
	        
	        if(workflowName.equals("All"))
	        {
	        	eventLogQueryFilter= "(F_BoundUser= :1)";
	        	substitutionVars = new Object[1];
	            substitutionVars[0] = fromNameId;
	        }
	        else
	        {
	        	eventLogQueryFilter= "(F_BoundUser= :1) and (F_Subject=:2)";
	            substitutionVars= new Object[2];
	            substitutionVars[0] = fromNameId;
	            substitutionVars[1] = workflowName;
	        }
	      
	        VWQueueQuery query = queue.createQuery(null, null, null, queryFlag, eventLogQueryFilter, substitutionVars, queryType);
	        int counter = 0;
	        int sucessCount=0;
	        
	        while (query.hasNext()) {

	            VWStepElement stepElement = (VWStepElement) query.next();
	            System.out.println("Step Name: "+stepElement.getStepName());
	            
	            if(workflowReassignCounts==counter)
	            	break;
	            
	            stepElement.doLock(true);
	            
	            try
	            {
	            	stepElement.doReassign(toUserId,false,null);
	            	sucessCount++;
	            	System.out.println("workflow reassigned sucessfully");
	            }
	            catch(Exception e)
	            {
	            	stepElement.doSave(true);
	            	e.printStackTrace();
	            }
	            
	            counter++;

	            
	        }
			
			message="Reassigned total "+sucessCount+" workflows to user "+toUserId;
			
			responseJSON.put("message1",message);
			
	    }
	    
	    
		
		PrintWriter responseWriter = response.getWriter();	
		if(responseJSON!=null)
		{
			responseWriter.print(responseJSON);
		}
		else
		{
			responseWriter.print(new JSONObject());
		}
		responseWriter.close();
	}
	
	public String checkUserExistsOrNot(Domain domain, String userid)
	{
		Realm realm = Factory.Realm.fetchCurrent(domain.getConnection(), null);	   
	    
		
	    UserSet users = realm.findUsers(userid, PrincipalSearchType.EXACT,
	    		PrincipalSearchAttribute.SHORT_NAME, PrincipalSearchSortType.ASCENDING, 0, null);	  
	    
	    Iterator<?> userIterator = users.iterator();
		while (userIterator.hasNext())
		{
			User thisUser = (User)userIterator.next();
			
			String username = thisUser.get_ShortName();
			
			String userDisplayName=thisUser.get_DisplayName();
			
			if(username.equalsIgnoreCase(userid))
			{
				return userDisplayName;
			}
		}
		return "";
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "WorkItemReassignService";
	}
	
	public int getWorkflowCount(VWSession vwSession, String fromUserId, String workflowName)
	{
		int count=0;
		
		VWQueue queue = vwSession.getQueue("Inbox(0)");
        System.out.println("Total Items in Inbox: "+queue.fetchCount());
                
        int queryFlag = VWQueue.QUERY_READ_LOCKED;
        int queryType = VWFetchType.FETCH_TYPE_QUEUE_ELEMENT;
        
        queryType = VWFetchType.FETCH_TYPE_STEP_ELEMENT;
        
        int fromNameId = vwSession.convertUserNameToId(fromUserId);
        
        String eventLogQueryFilter = null;
        Object substitutionVars[] =null;
        
        if(workflowName.equals("All"))
        {
        	eventLogQueryFilter= "(F_BoundUser= :1)";
        	substitutionVars = new Object[1];
            substitutionVars[0] = fromNameId;
        }
        else
        {
        	eventLogQueryFilter= "(F_BoundUser= :1) and (F_Subject=:2)";
            substitutionVars= new Object[2];
            substitutionVars[0] = fromNameId;
            substitutionVars[1] = workflowName;
        }
         
      
        VWQueueQuery query = queue.createQuery(null, null, null, queryFlag, eventLogQueryFilter, substitutionVars, queryType);
        
        count = query.fetchCount();
        System.out.println("count is "+count);
		
		return count;
	}
	
	
}
