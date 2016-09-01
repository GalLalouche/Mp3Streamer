const elem = (name, content) => $(`<${name}>${content || ""}</${name}>`)
String.prototype.format = String.prototype.f = function () {
  var s = this, i = arguments.length;

  while (i--)
    s = s.replace(new RegExp('\\{' + i + '\\}', 'gm'), arguments[i]);
  return s;
};
Number.prototype.timeFormat = function () {
  var hours = Math.floor(this / 3600);
  var minutes = Math.floor((this - (hours * 3600)) / 60);
  var seconds = this - (hours * 3600) - (minutes * 60);

  if (hours < 10) hours = "0" + hours;
  if (minutes < 10) minutes = "0" + minutes;
  if (seconds < 10) seconds = "0" + seconds;

  const hourPrefix = hours === "00" ? "" : hours + ":";
  return hourPrefix + minutes + ':' + seconds;
};
