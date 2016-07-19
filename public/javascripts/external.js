$(function () {
  const external = $('#external');

  function showLinks(content) {
    console.log(content)
    external.html("")
    const ul = $("<ul>Links</ul>")
    for (const e in content)
      ul.append($(`<li style="list-style-image: url('assets/images/${e}_icon.png')"><a href=${content[e]}>${e}</a></li>`))
    external.append(ul)
  }

  External.show = function (song) {
    external.html("Loading external links...");
    $.get("external/" + song.file, l => showLinks(l));
  }
});
External = {};
