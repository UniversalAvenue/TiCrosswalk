/**
	* Appcelerator Titanium Mobile
	* Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
	* Licensed under the terms of the Apache Public License
	* Please see the LICENSE included with this distribution for details.
	*
	*/

package com.universalavenue.ticrosswalk;

import java.util.HashMap;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.TiC;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.app.Activity;

import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkResourceClient;

public class WebViewResourceClient extends XWalkResourceClient
{
	private WebViewProxy proxy;
	private WebViewBinding binding;

	private static final String LCAT = "WebViewResourceClient";

	WebViewResourceClient(XWalkView view, WebViewProxy webViewProxy, WebViewBinding webViewBinding) {
		super(view);

		proxy = webViewProxy;
		binding = webViewBinding;
	}

	@Override
	public void onLoadFinished (XWalkView view, String url) {
		super.onLoadFinished(view, url);

		view.evaluateJavascript(binding.INJECTION_CODE, null);
		view.evaluateJavascript(binding.POLLING_CODE, null);

		Log.d(LCAT, "WebView finished loading url: " + url);

		KrollDict data = new KrollDict();
		data.put("url", url);
		proxy.fireEvent(TiC.EVENT_LOAD, data);
	}


}
