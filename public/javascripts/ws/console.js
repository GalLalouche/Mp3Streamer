$(function() {
	openConnection("console", function(msg) {
		console.log("(Server)" + msg.data)
	});
});
