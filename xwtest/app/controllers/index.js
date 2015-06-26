/*** Alloy Example ***/
var webView = $.webView;
webView.setUrl('http://universalavenue.com');

/*** Classic Example ***
var crossWalk = require('com.universalavenue.ticrosswalk'),
    webView;
Ti.API.info("module is => " + test);

webView = crossWalk.createWebView({ html: 'http://universalavenue.com' });
$.index.add(webView);
/*** End Classic Example ***/

webView.addEventListener('load', function (event) {
    console.log('load!!', event);
});

/*** Controls from external buttons ***/
var pageChange,
    pageNext,
    pagePrev,
    evalJS;

pagePrev = Ti.UI.createButton({
    left: 10,
    bottom: 10,
    height: 44,
    width: '24%',
    title: 'Prev'
});
pagePrev.addEventListener('click', function () {
    if (webView.canGoBack()) {
        webView.goBack();
    }
});
$.index.add(pagePrev);
pageChange = Ti.UI.createButton({
    left: '25%',
    right: 10,
    bottom: 10,
    height: 44,
    width: '50%',
    title: 'Go to Google.se!'
});
pageChange.addEventListener('click', function () {
    webView.setUrl('http://google.se');
});
$.index.add(pageChange);
pageNext = Ti.UI.createButton({
    right: 10,
    bottom: 10,
    height: 44,
    width: '24%',
    title: 'Next'
});
pageNext.addEventListener('click', function () {
    if (webView.canGoForward()) {
        webView.goForward();
    }
});
$.index.add(pageNext);

evalJS = Ti.UI.createButton({
    left: 10,
    right: 10,
    bottom: 54,
    height: 44,
    title: 'Send JS to webView!'
});
evalJS.addEventListener('click', function () {
    console.log('I can haz title: ', webView.evalJS('document.title'));
});
$.index.add(evalJS);

$.index.open();
