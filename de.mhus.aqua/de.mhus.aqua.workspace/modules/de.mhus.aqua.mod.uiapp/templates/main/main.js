
function showEditor(cmd) {
	var url = "${baseurl}/res/_ajax_0.0/get?";
	aquaExecute(url+"nid="+aquaConfig["path"]+"&bid=editor&format=json&action=" + cmd);
}	

function aquaExecute(cmd) {
	var url = cmd;
	var myCallback;
	yconsole.log("send req: " + cmd);
	YUI().use("console", "dump", "datasource-io", "datasource-jsonschema", "node", function(Y) {
		yconsole.log(url);
	    var myDataSource = new Y.DataSource.IO({
	        source:url}),
	        myCallback = {
	            success: function(e){
	            	var rc = e.response.meta['rc'];
	            	yconsole.log("success req: " + rc);
	            	if (rc == "ok") {
	            		for(var i=0,len=e.response.results.length; value=e.response.results[i], i<len; i++) {
		            		var a = value['a'];
		            		var n = value['n'];
		            		yconsole.log(a + " " + n);
		            		if ( a == "content") {
		            			Y.one(n).setContent(value['v']);
		            		} else
		            		if ( a == "first") {
			            		Y.one(n).insert(value['v'],0);
			            		if (aquaEditComponent && aquaEditComponent.update) {
			            			yconsole.log("found a editor");
			            			aquaEditComponent.update();
			            		} else {
			            			yconsole.log("not found a editor");			            			
			            		}
		            		} else
		            		if ( a == "remove") {
		            			Y.one(n).remove();
		            		}
	            		}
	            	} else
	            	if (rc == "error") {
	            		yconsole.log("error from ajax " + url);
	            	}
	            },
	            failure: function(e){
	                // alert("Could not retrieve data: " + e.error.message);
	            	yconsole.log("Could not retrieve data: " + e.error.message);
	            }
	        };
	 	 
	    myDataSource.plug(Y.Plugin.DataSourceJSONSchema, {
	    	schema: {
	    		metaFields: {rc:"rc"},
	    	    resultListLocator: "results",
	    	    resultFields: [{key:"a"},{key:"n"}, {key:"v"}]
	        }
	    });

	    myDataSource.sendRequest({
	        request:"",
	        callback:myCallback
	    });
	});
}	