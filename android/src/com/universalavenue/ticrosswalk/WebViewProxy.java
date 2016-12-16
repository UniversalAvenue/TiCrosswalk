/**
  * Appcelerator Titanium Mobile
  * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
  * Licensed under the terms of the Apache Public License
  * Please see the LICENSE included with this distribution for details.
  *
  */

package com.universalavenue.ticrosswalk;

import java.lang.Object;
import java.lang.Runnable;
import java.util.LinkedList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
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
	private static final String LCAT = "TiCrosswalk";

	private static final int MSG_FIRST_ID = TiViewProxy.MSG_LAST_ID + 1;

	private static final int MSG_GO_BACK = MSG_FIRST_ID + 101;
	private static final int MSG_GO_FORWARD = MSG_FIRST_ID + 102;
	private static final int MSG_RELOAD = MSG_FIRST_ID + 103;
	private static final int MSG_STOP_LOADING = MSG_FIRST_ID + 104;
	private static final int MSG_SET_HTML = MSG_FIRST_ID + 105;
	private static final int MSG_SET_USER_AGENT = MSG_FIRST_ID + 106;
	private static final int MSG_GET_USER_AGENT = MSG_FIRST_ID + 107;
	private static final int MSG_CAN_GO_BACK = MSG_FIRST_ID + 108;
	private static final int MSG_CAN_GO_FORWARD = MSG_FIRST_ID + 109;
	private static final int MSG_RELEASE = MSG_FIRST_ID + 110;
	private static final int MSG_PAUSE = MSG_FIRST_ID + 111;
	private static final int MSG_RESUME = MSG_FIRST_ID + 112;

	protected static final int MSG_LAST_ID = MSG_FIRST_ID + 999;

	private LinkedList<Object[]> evalAsyncCallQueue;

	public WebViewProxy() {
		super();
		evalAsyncCallQueue = new LinkedList<Object[]>();
	}

	@Override
	public TiUIView createView(Activity activity)
	{
		WebView view = new WebView(this);
		view.getLayoutParams().autoFillsHeight = true;
		view.getLayoutParams().autoFillsWidth = true;

		while (!evalAsyncCallQueue.isEmpty()) {
			Object[] call = evalAsyncCallQueue.removeFirst();
			this.evalAsync(view, (String) call[0], (KrollFunction) call[1]);
		}

		return view;
	}

	public WebView getWebView()
	{
		return (WebView) peekView();
	}


	@Override
	public boolean handleMessage(Message msg)
	{
		if (peekView() != null) {
			switch (msg.what) {
				case MSG_GO_BACK:
					goBack();
					return true;
				case MSG_GO_FORWARD:
					goForward();
					return true;
				// case MSG_RELOAD:
				// 	getWebView().reload();
				// 	return true;
				// case MSG_STOP_LOADING:
				// 	getWebView().stopLoading();
				// 	return true;
				// case MSG_SET_USER_AGENT:
				// 	getWebView().setUserAgentString(msg.obj.toString());
				// 	return true;
				// case MSG_GET_USER_AGENT: {
				// 	AsyncResult result = (AsyncResult) msg.obj;
				// 	result.setResult(getWebView().getUserAgentString());
				// 	return true;
				// }
				case MSG_CAN_GO_BACK: {
					AsyncResult result = (AsyncResult) msg.obj;
					result.setResult(canGoBack());
					return true;
				}
				case MSG_CAN_GO_FORWARD: {
					AsyncResult result = (AsyncResult) msg.obj;
					result.setResult(canGoForward());
					return true;
				}
				// case MSG_RELEASE:
				// 	TiUIWebView webView = (TiUIWebView) peekView();
				// 	if (webView != null) {
				// 		webView.destroyWebViewBinding();
				// 	}
				// 	super.releaseViews();
				// 	return true;
				// case MSG_PAUSE:
				// 	getWebView().pauseWebView();
				// 	return true;
				// case MSG_RESUME:
				// 	getWebView().resumeWebView();
				// 	return true;
				// case MSG_SET_HTML:
				// 	String html = TiConvert.toString(getProperty(TiC.PROPERTY_HTML));
				// 	HashMap<String, Object> d = (HashMap<String, Object>) getProperty(OPTIONS_IN_SETHTML);
				// 	getWebView().setHtml(html, d);
				// 	return true;
			}
		}
		return super.handleMessage(msg);
	}

	@Kroll.method
	public boolean canGoBack()
	{
		WebView view = getWebView();
		if (view != null) {
			if (TiApplication.isUIThread()) {
				return view.canGoBack();
			} else {
				return (Boolean) TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_CAN_GO_BACK));
			}
		}
		return false;
	}

	@Kroll.method
	public boolean canGoForward()
	{
		WebView view = getWebView();
		if (view != null) {
			if (TiApplication.isUIThread()) {
				return view.canGoForward();
			} else {
				return (Boolean) TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_CAN_GO_FORWARD));
			}
		}
		return false;
	}

	@Kroll.method
	public void goBack()
	{
		if (!TiApplication.isUIThread()) {
			getMainHandler().sendEmptyMessage(MSG_GO_BACK);
		} else if (canGoBack()) {
			getWebView().goBack();
		}
	}

	@Kroll.method
	public void goForward()
	{
		if (!TiApplication.isUIThread()) {
			getMainHandler().sendEmptyMessage(MSG_GO_FORWARD);
		} else if (canGoForward()) {
			getWebView().goForward();
		}
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
		WebView view = getWebView();

		if (view == null) {
			Log.e(LCAT, "evalJS failed, view not available");
			return null;
		}

		Log.d(LCAT, "Evaluating '" + code + "' in WebView");
		return view.getJSValue(code);
	}

	@Kroll.method
	public void evalAsync(final String code, @Kroll.argument(optional=true) KrollFunction callback) {
		WebView view = getWebView();

		if (view == null) {
			Log.w(LCAT, "evalAsync failed, view not available. Will try again later.");
			Object[] call = {code, callback};
			evalAsyncCallQueue.add(call);
			return;
		}

		evalAsync(view, code, callback);
	}

	private void evalAsync(final WebView view, final String code, KrollFunction callback) {
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

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
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
