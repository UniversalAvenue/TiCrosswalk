# Titanium Crosswalk Module

## Description

A cross platform web view extension/replacement for Titanium Mobile that uses the fantastic 
[Crosswalk WebView](https://crosswalk-project.org/) on Android and extends the standard 
Titanium iOS WebView API to match.

## Usage

##### Using the module in a Alloy-view

```xml
<WebView id="webView" platform="ios" />
<WebView id="webView" platform="android" module="com.universalavenue.ticrosswalk" />
```

##### Creating a webView directly

```js
function createWebView (props) {
  if (OS_ANDROID) {
    var xwalk = require('com.universalavenue.ticrosswalk');
    return xwalk.createWebView(props);
  }
  return Ti.UI.createWebView(props);
}

var win = Ti.UI.createWindow(),
    webView = createWebView({ url: 'https://universalavenue.com' });
win.add(webView);
win.open();
```

##### Evaluating JS

Use the `evalAsync(code, [callback])` method to evaluate JS inside the WebView without blocking the main thread. This is a major speed boost, especially on Android.

```js
var code = '(function(arg){ return { some: "object", arg: arg }; })(["<your args>"])';
webView.evalAsync(code, function (res) {
  var obj = res && JSON.parse(res);
});
```

The iOS version of this module simply extends the existing Titanium WebView, meaning you can use the same APIs as on Android.

**Note:** The behavior of evalAsync differ a bit between platforms. In order for things to work cross platform you need to wrap your call in an IIFE (like above) and make sure to skip the final semicolon! See issue #6 for details.


## Contributors

**Jonatan Lundin**  
Twitter: @mr_lundis  

## License

The MIT License (MIT)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
