require(["dojo/_base/declare",
         "dojo/_base/lang",
         "dojo/aspect",
         "ecm/widget/dialog/AddContentItemDialog",
         "sampleDojo/ICCReconciliationReportDialog"], 
function(declare,lang,aspect,AddContentItemDialog,ICCReconciliationReportDialog) {		
	/**
	 * Use this function to add any global JavaScript methods your plug-in requires.
	 */
lang.setObject("action", function(repository, items, callback, teamspace, resultSet, parameterMap) {
 /*
  * Add custom code for your action here. For example, your action might launch a dialog or call a plug-in service.
  */
	
	
	var dlg = new ICCReconciliationReportDialog();
	dlg.show(repository,callback,dlg);
	    
	    
});
lang.setObject("nj", function(repository, items, callback, teamspace, resultSet, parameterMap) {
 /*
  * Add custom code for your action here. For example, your action might launch a dialog or call a plug-in service.
  */
});
});
