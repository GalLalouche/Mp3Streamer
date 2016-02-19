$(function() {
	var TREE_RES = "music/tree";
	var TREE_ID = "#tree"; 
	function getTree() {
		$.ajax({
			url: TREE_RES,
			success:
			function(data) {
				$(TREE_ID).jstree({
					// the 'plugins' array allows you to configure the active plugins on this instance
					"plugins" : [ "themes", "json_data", "ui" ],
					// it makes sense to configure a plugin only if overriding the defaults
					json_data:{
						data: data
					},
					themes: {
						theme: 'classic',
						dots: false
					}
				}).bind("select_node.jstree", function (e, data) {
					var path = $(data.rslt.obj[0].children[1]).attr('path');
					if (path) {
						$.get("/music/albums/" + path, function(data) {
							playlist.add(data, false);
						});
					}		
				});
				
			},
			error: function(data) {
				if (data.status != 304)
					alert("error retrieving tree!");
			},
			ifModified: true,
		})
	};
	openConnection("tree", function(msg) {if (msg.data == "tree") getTree()}).onmessage({data: "tree"})
	
});
	
	