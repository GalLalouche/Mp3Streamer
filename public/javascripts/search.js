$(function() {
  $("#searchbox").bind('input change keyup', function(e) {
    if (e.keyCode != 13) // enter
      return
    text = $(this).val()
    $.get("music/search/" + text, function(e) { console.log(e) }) 
  });
});
