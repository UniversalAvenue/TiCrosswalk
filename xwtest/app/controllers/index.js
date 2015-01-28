var test = require('com.universalavenue.ticrosswalk');
Ti.API.info("module is => " + test);
Ti.API.info("module example() method returns => " + test.example());
Ti.API.info("module exampleProp is => " + test.exampleProp);
test.exampleProp = "This is a test value";

var webView = test.createWebView({ html: 'http://universalavenue.com' });
$.index.add(webView);

webView.addEventListener('load', function (event) {
  console.log('load!!', event);
});

var move = Ti.UI.createButton({
  left: 10, right: 10, bottom: 10, height: 44, title: 'move!'
});
move.addEventListener('click', function () {
  webView.setUrl('http://google.se');
});
$.index.add(move);

var evaljs = Ti.UI.createButton({
  left: 10, right: 10, bottom: 54, height: 44, title: 'evals!'
});
evaljs.addEventListener('click', function () {
  console.log('I can haz title:', webView.evalJS('document.title'));
});
$.index.add(evaljs);


$.index.open();
