define([
	"dojo/_base/declare",
	"dojo/dom-class",
	"dojo/dom-geometry",
	"dojo/_base/lang",
	"dojo/data/ItemFileWriteStore", 
	"dojox/grid/DataGrid", 
	"dojox/grid/_CheckBoxSelector",
	"dijit/form/DateTextBox",
	"ecm/widget/dialog/BaseDialog",
	"dojo/store/Memory", 
	"dijit/form/FilteringSelect", 
	"dojo/dom-style",
	"dojo/dom",
	"dojo/dom-attr",
	"ecm/model/Repository",
	"ecm/Messages",
	"ecm/model/Message",
	"ecm/model/Request",
	"ecm/Logger",
	"ecm/widget/dialog/BatchStatusDialog",
	"dojo/parser",
	"dojo/text!sampleDojo/templates/ICCReconciliationReportDialogContent.html"
],

function(declare, 
domClass, 
domGeom, 
lang, 
ItemFileWriteStore,
DataGrid,
_CheckBoxSelector,
DateTextBox,
BaseDialog, 
Memory,
FilteringSelect,
domStyle,
dom,
domAttr,
Repository,
Messages, 
Message,
Request,
Logger,
BatchStatusDialog,
parser,
template) {

	/**
	 * @name softdeletePluginDojo.SoftDeleteDocumentChooserDialog
	 * @class Provides a dialog box that is used choose a document to be Undeleted.
	 * @augments ecm.widget.dialog.BaseDialog
	 */
	return declare("sampleDojo.ICCReconciliationReportDialog", [
		BaseDialog
	], {
		_messages: ecm.messages,
		contentString: template,
		widgetsInTemplate: true,
		_callback: null,
		_repository: null,
		messagesresources:null,
		resizeFlag:false,
	
		
	postCreate: function() {
		
		this.inherited(arguments);

		//Setting BaseDialog Properties
		this.setSize(750,500);
//		this.setMaximized(true);
		this.set("title", "Work Item Reassignment" );
		this.setIntroText("Work Item Reassignment Dialog");
		
		//Setting Label values
		this.fromUIDLabel.set('value'," From User ID ");
		this.toUIDLabel.set('value'," To user ID ");
		this.workflowTypeLabel.set('value'," Workflow Name ");
		this.messageLabel.set('value'," Message :  ");
		this.ReassignCountLabel.set('value',"Reassigning Count : ");

		//Setting response message to empty
		domAttr.set("responsemessage", "innerHTML", "");
		
		//Disabling Buttons
		this.getDetailsButton.set('disabled',true);
		this.reassignButton.set('disabled',true);
		
		//Hiding the custom message and reassign
		this._hideDom("wfMessageDiv");
		this._hideDom("wfReassignDiv");
		
		this._customReset;	
		
	},
	show: function(repository,callback,myDialog) {
		
		this.inherited(arguments)
		
		this._callback = callback;
		this._repository = repository;
		
		//Constructing Work flow Names Select Options
		var mydata = [];
		mydata.push({ name: "All",
			id: "All"
		});
		
		mydata.push({ name: "ReassignWorkflow",
			id: "ReassignWorkflow"
		});
		
		mydata.push({ name: "TestReassign",
			id: "TestReassign"
		});
		
		var osStore = new Memory({
	        data: mydata
	    });

	    this.workflowTypeSelect = new FilteringSelect({
	        id: this.id+"_workflowTypeLabelSelect",
	        name: "WorkFlowType",
	        value: "All",
	        store: osStore,
	        searchAttr: "name"
	        	
	    }, this.id+"_workflowTypeLabelSelect");
	    
	    this.workflowTypeSelect.startup();
	    
	    myDialog.connect(myDialog, "hide", function(e){
//	        dijit.byId("user_submit").destroy(); 
	    	
	    	dojo.destroy("wfMessageDiv");
			dojo.destroy("wfReassignDiv");
			dojo.destroy("fromUserName");
			dojo.destroy("toUserName");
			  
	    });
		
	},
	
	//Calls when user clicks on Reset Button
	_customReset : function()
	{
		this.fromUIDtb.set('value',"");
		this.toUIDtb.set('value',"");
		this.workflowTypeSelect.set('value',"All");
		this.getDetailsButton.set('disabled',true);
		
		domAttr.set("fromUserName", "innerHTML", "");
		domAttr.set("toUserName", "innerHTML", "");
		
		this._hideDom("wfMessageDiv");
		this._hideDom("wfReassignDiv");
	},
	
	//Calls when user enters from user id or to user id
	_onValueEnteredTB : function()
	{
		this._hideDom("wfMessageDiv");
		this._hideDom("wfReassignDiv");
		
		var fromUid = this.fromUIDtb.get('value');
		var toUid = this.toUIDtb.get('value');
		
		if(fromUid.length > 1 && toUid.length >1)
			this.getDetailsButton.set('disabled',false);
		else
			this.getDetailsButton.set('disabled',true);
			
	},
	
	//Calls when user clicks on Get details button in UI.
	_getWorkflowDetails : function()
	{
		
		this._showDom("wfMessageDiv");
		
		var fromUid = this.fromUIDtb.get('value');
		var toUid = this.toUIDtb.get('value');
		
		if(fromUid==toUid)
		{
			domAttr.set("responsemessage", "innerHTML","From and To User id should be different " );
			domAttr.set("responsemessage", "style", {color:"red"});
			
			this._hideDom("wfReassignDiv");
		}
		else
		{
			var workflowName=this.workflowTypeSelect.get('value');
			
			
			var serviceParams = new Object();
			serviceParams.server = this._repository.id;
			serviceParams.serverType = this._repository.type;
			serviceParams.desktopId = ecm.model.desktop.id;
			serviceParams.userId = this._repository.userId;
			serviceParams.osName = this._repository.objectStoreName;
			serviceParams.fromUid= fromUid;
			serviceParams.toUid= toUid;
			serviceParams.requestType= "getWorkflowDetails";
			serviceParams.workflowName=workflowName;
			
			Request.postPluginService("Sample", "WorkItemReassignService","application/json ; charset=UTF-8",
				{
					requestBody: JSON.stringify(serviceParams),
					requestCompleteCallback: function(response) {	
						if(response){
							
							var res=response.message1;
							
							domAttr.set("responsemessage", "innerHTML",res );
							domAttr.set("responsemessage", "style", {color:"green"});
							
							domAttr.set("fromUserName", "innerHTML", response.message3);
							domAttr.set("toUserName", "innerHTML", response.message4);
							
							domAttr.set("fromUserName", "style", {color:"green"});
							domAttr.set("toUserName", "style", {color:"green"});
							
							//If from user id/ to user id is wrong. show the message in red color and also hide the reassign task.  
							if(response.message2=='invalidid')
							{
								var res=response.message1;
								
								domAttr.set("responsemessage", "innerHTML",res );
								domAttr.set("responsemessage", "style", {color:"red"});
								
//								this._hideDom("wfReassignDiv");
								
								var myNode = dom.byId("wfReassignDiv");
								domStyle.set(myNode, "display", "none");
								
							}
							//If everything right. show the message in green color and also show the reassign task.
							if(response.message2=='validid')
							{
								var res=response.message1;
								
								domAttr.set("responsemessage", "innerHTML",res );
								domAttr.set("responsemessage", "style", {color:"green"});
								
								if(response.message5==0)
								{
									var myNode = dom.byId("wfReassignDiv");
									domStyle.set(myNode, "display", "none");
								}
								else
								{
									var myNode = dom.byId("wfReassignDiv");
									domStyle.set(myNode, "display", "");
								}
							}
								
						}
					}
				});
		}
			
		
	},
	
	_reassignWorkFlow: function()
	{

		this._showDom("wfMessageDiv");
		
		var fromUid = this.fromUIDtb.get('value');
		var toUid = this.toUIDtb.get('value');
		var workflowName=this.workflowTypeSelect.get('value');
		var reassignCount = this.reassignCounttb.get('value');
		
		//shows processing Dilaog
		/*if (this._batchStatusDialog) {
			this._batchStatusDialog.destroy();
		}
		
		this._batchStatusDialog = new BatchStatusDialog({
			title:"Processing",
			cancellable: false,
			modeless:false
		});
		
		
		
		this._batchStatusDialog.updateStatusMessage("Loading....");
		
		this._batchStatusDialog.show();*/
		
		
		var serviceParams = new Object();
		serviceParams.server = this._repository.id;
		serviceParams.serverType = this._repository.type;
		serviceParams.desktopId = ecm.model.desktop.id;
		serviceParams.userId = this._repository.userId;
		serviceParams.osName = this._repository.objectStoreName;
		serviceParams.fromUid= fromUid;
		serviceParams.toUid= toUid;
		serviceParams.requestType= "reassignWorkFlow";
		serviceParams.workflowName=workflowName;
		serviceParams.reassignCount= reassignCount;
		
		Request.postPluginService("Sample", "WorkItemReassignService","application/json ; charset=UTF-8",
			{
				requestBody: JSON.stringify(serviceParams),
				requestCompleteCallback: function(response) {	
					if(response){
						
						if(response.message1)
						{
							var res=response.message1;
							
							domAttr.set("responsemessage", "innerHTML",res );
							domAttr.set("responsemessage", "style", {color:"green"});
							
							var myNode = dom.byId("wfReassignDiv");
							domStyle.set(myNode, "display", "none");
						}
						
						/*
						setTimeout(lang.hitch(this, function() {
							
							this._batchStatusDialog.hide();
							
							}), 300);*/
						//shows processing Dilaog
						/*if (this._batchStatusDialog) {
							this._batchStatusDialog.destroy();
						}*/
						
						/*domAttr.set("fromUserName", "innerHTML", response.message3);
						domAttr.set("toUserName", "innerHTML", response.message4);
						
						domAttr.set("fromUserName", "style", {color:"green"});
						domAttr.set("toUserName", "style", {color:"green"});
						
						//If from user id/ to user id is wrong. show the message in red color and also hide the reassign task.  
						if(response.message2=='invalidid')
						{
							var res=response.message1;
							
							domAttr.set("responsemessage", "innerHTML",res );
							domAttr.set("responsemessage", "style", {color:"red"});
							
							this._hideDom("wfReassignDiv");
						}
						//If everything right. show the message in green color and also show the reassign task.
						if(response.message2=='validid')
						{
							var res=response.message1;
							
							domAttr.set("responsemessage", "innerHTML",res );
							domAttr.set("responsemessage", "style", {color:"green"});
							
							var myNode = dom.byId("wfReassignDiv");
							domStyle.set(myNode, "display", "");
						}*/
							
					}
				}
			});
	},
	
	_onValueEnteredreassignTb: function()
	{
		var reassign = this.reassignCounttb.get('value');
		
		if(reassign >0 && reassign <=500){
			this.reassignButton.set('disabled',false);
		}
		else{
			this.reassignButton.set('disabled',true);
		}
		
	},
	
	_hideDom: function(element)
	{
		var myNode = dom.byId(element);
		domStyle.set(myNode, "display", "none");
	},
	_showDom: function(element)
	{
		var myNode = dom.byId(element);
		domStyle.set(myNode, "display", "");
	},
	
	resize: function() {
		this.inherited(arguments);
	},
	
	destroy: function() {
		
		this.inherited(arguments);
	}		
	});
});
