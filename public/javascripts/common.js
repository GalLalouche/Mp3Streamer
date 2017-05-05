const elem = (name, content) => $(`<${name}>${content || ""}</${name}>`)
String.prototype.format = String.prototype.f = function() {
  let s = this, i = arguments.length;

  while (i--)
    s = s.replace(new RegExp('\\{' + i + '\\}', 'gm'), arguments[i]);
  return s;
};
Number.prototype.timeFormat = function() {
  let hours = Math.floor(this / 3600);
  let minutes = Math.floor((this - (hours * 3600)) / 60);
  let seconds = this - (hours * 3600) - (minutes * 60);

  if (hours < 10) hours = "0" + hours;
  if (minutes < 10) minutes = "0" + minutes;
  if (seconds < 10) seconds = "0" + seconds;

  const hourPrefix = hours === "00" ? "" : hours + ":";
  return hourPrefix + minutes + ':' + seconds;
};
// Copied from http://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript#answer-30810322.
// Comments removed for brevity.
function copyTextToClipboard(text) {
  const textArea = document.createElement("textarea");
  textArea.style.position = 'fixed';
  textArea.style.top = 0;
  textArea.style.left = 0;
  textArea.style.width = '2em';
  textArea.style.height = '2em';
  textArea.style.padding = 0;
  textArea.style.border = 'none';
  textArea.style.outline = 'none';
  textArea.style.boxShadow = 'none';
  textArea.style.background = 'transparent';
  textArea.value = text;

  document.body.appendChild(textArea);

  textArea.select();

  try {
    document.execCommand('copy');
  } catch (err) {
    console.log('Oops, unable to copy');
  }

  document.body.removeChild(textArea);
}

function isEmptyObject(obj) {
  for (const prop in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, prop)) {
      return false;
    }
  }
  return true;
}

jQuery.each(["put", "delete"], function(i, method) {
  jQuery[method] = function(url, data, callback, type) {
    if (jQuery.isFunction(data)) {
      type = type || callback;
      callback = data;
      data = undefined;
    }

    return jQuery.ajax({
      url: url,
      type: method,
      dataType: type,
      data: data,
      success: callback
    });
  };
});

function _ajaxJson(method, url, data, success) {
  data = typeof data === 'string' ? data : JSON.stringify(data)
  $.ajax({
    url: url,
    data: data,
    type: method,
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: success})
}
function postJson(url, data, success) {
  _ajaxJson("POST", url, data, success)
}

function putJson(url, data, success) {
  _ajaxJson("PUT", url, data, success)
}
function isValidUrl(urlString) {
  const pattern = new RegExp('^(https?:\\/\\/)?'+ // protocol
      '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|'+ // domain name
      '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
      '(\\:\\d+)?(\\/[-a-z\\d%_.~+\\:]*)*'+ // port and path
      '(\\?[\\;&a-z\\d%_.~+=-]*)?'+ // query string
      '(\\#[-a-z\\d_]*)?$','i'); // fragment locator
  return !pattern.test(urlString)
}
