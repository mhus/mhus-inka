
YUI({filter:"raw"}).use("datatable-scroll", function (Y) {
var sampleData = [	{ ANSI: "00000", STATE: "UNITED STATES", TOTAL_POP: 307006550, LAND_AREA: 3537438.44, POP_PER_SQ_MILE: 79.6 },
					{ ANSI: "01000", STATE: "ALABAMA", TOTAL_POP: 4708708, LAND_AREA: 50744, POP_PER_SQ_MILE: 87.6 },
					{ ANSI: "02000", STATE: "ALASKA", TOTAL_POP: 698473, LAND_AREA: 571951.26, POP_PER_SQ_MILE: 1.1 },
					{ ANSI: "04000", STATE: "ARIZONA", TOTAL_POP: 6595778, LAND_AREA: 113634.57, POP_PER_SQ_MILE: 45.2 }];
					
  var currentSelection = null;
  
  var dtScrollingY = new Y.DataTable.Base({
        columnset: [
            {key:"STATE", label:"State"},
            {key:"TOTAL_POP", label:"Total Population"}
        ],
        recordset: sampleData,
        summary: "Y axis scrolling table"
    });
    
    var selOn = function(e) {
    	// window.alert("aaa");
    	// this.addClass('yui-pass');
    	e.currentTarget.all("div.yui3-datatable-liner").addClass('yui-pass');
    	// e.target.addClass('yui-pass');
    };
    
    var selOff = function(e) {
    	e.currentTarget.all("div.yui3-datatable-liner").removeClass('yui-pass');    	
    };

	var tclick = function(e) {
		if (currentSelection != null) currentSelection.all("div.yui3-datatable-liner").removeClass('yui-selected'); 
		currentSelection = e.currentTarget;
		e.currentTarget.all("div.yui3-datatable-liner").addClass('yui-selected');
	}
	  
	
    dtScrollingY.plug(Y.Plugin.DataTableScroll, {
        height:"200px"
    });
    dtScrollingY.render("#scrolling-y");
    
	Y.all("tbody.yui3-datatable-data tr").on("mouseover", selOn); 
	Y.all("tbody.yui3-datatable-data tr").on("mouseout", selOff); 
	Y.all("tbody.yui3-datatable-data tr").on("click", tclick); 

});
