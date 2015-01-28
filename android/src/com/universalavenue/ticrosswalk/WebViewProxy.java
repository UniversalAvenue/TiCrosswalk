/**
  * Appcelerator Titanium Mobile
  * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
  * Licensed under the terms of the Apache Public License
  * Please see the LICENSE included with this distribution for details.
  *
  */

package com.universalavenue.ticrosswalk;

import java.lang.Runnable;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.proxy.TiViewProxy;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import org.xwalk.core.XWalkView;
import android.webkit.ValueCallback;

// The proxy is declared with the @Kroll.proxy annotation

@Kroll.proxy(creatableInModule = TiCrosswalkModule.class)
public class WebViewProxy extends TiViewProxy
{
	// Standard Debugging variables
	private static final String LCAT = "TiCrosswalk";

	public WebViewProxy() {
		super();
	}

	@Override
	public TiUIView createView(Activity activity)
	{
		WebView view = new WebView(this);
		view.getLayoutParams().autoFillsHeight = true;
		view.getLayoutParams().autoFillsWidth = true;
		return view;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options)
	{
		// This method is called from handleCreationArgs if there is at least
		// argument specified for the proxy creation call and the first argument
		// is a KrollDict object.
		// Calling the superclass method ensures that the properties specified
		// in the dictionary are properly set on the proxy object.
		super.handleCreationDict(options);
	}

	public void handleCreationArgs(KrollModule createdInModule, Object[] args)
	{
		// This method is one of the initializers for the proxy class. The arguments
		// for the create call are passed as an array of objects. If your proxy
		// simply needs to handle a single KrollDict argument, use handleCreationDict.
		// The superclass method calls the handleCreationDict if the first argument
		// to the create method is a dictionary object.

		super.handleCreationArgs(createdInModule, args);
	}

	@Kroll.setProperty @Kroll.method
	public void setUrl(String url) {
		setPropertyAndFire("url", url);
	}

	@Kroll.setProperty @Kroll.method
	public void setHtml(String html) {
		setPropertyAndFire("html", html);
	}

	@Kroll.method
	public Object evalJS(String code) {
		WebView view = (WebView) peekView();

		if (view == null) {
			Log.e(LCAT, "evalJS failed, view not available");
			return null;
		}

		Log.d(LCAT, "Evaluating '" + code + "' in WebView");
		return view.getJSValue(code);
	}

	@Kroll.method
	public void evalAsync(final String code, @Kroll.argument(optional=true) KrollFunction callback) {
		WebView view = (WebView) peekView();

		if (view == null) {
			Log.e(LCAT, "evalAsync failed, view not available");
			return;
		}

		final KrollFunction _cb = callback;
		final ValueCallback valueCallback = new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String s) {
				if (_cb != null) {
					Log.d(LCAT, "Received: '" + s + "', dispatching callback");
					Object[] args = new Object[1];
					args[0] = s;
					_cb.callAsync(getKrollObject(), args);
				} else {
					Log.d(LCAT, "Received: '" + s + "'");
				}
			}
		};

		getActivity().runOnUiThread(new Runnable () {
			@Override
			public void run() {
				WebView view = (WebView) peekView();
				if (view != null) {
					XWalkView webView = (XWalkView) view.getNativeView();
					if (webView != null) {
						Log.d(LCAT, "Evaluating '" + code + "' async in WebView");
						webView.evaluateJavascript(code, valueCallback);
					} else {
						Log.e(LCAT, "evalAsync failed, webView not available");
					}
				} else {
					Log.e(LCAT, "evalAsync failed, view not available");
				}
			}
		});
	}
}
