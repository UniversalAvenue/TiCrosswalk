/**
	* Appcelerator Titanium Mobile
	* Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
	* Licensed under the terms of the Apache Public License
	* Please see the LICENSE included with this distribution for details.
	*
	*/

package com.universalavenue.ticrosswalk;

import java.util.ArrayList;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.view.TiBackgroundDrawable;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.TiC;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.TextureView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkNavigationHistory.Direction;

public class WebView extends TiUIView implements OnLifecycleEvent
{
	// Standard Debugging variables
	private static final String LCAT = "TiCrosswalk";

	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_HTML = "html";

	private WebViewResourceClient resourceClient;
	private WebViewBinding binding;

	public WebView(WebViewProxy proxy)
	{
		super(proxy);

		Log.d(LCAT, "[VIEW LIFECYCLE EVENT] view, on ui thread: " + TiApplication.isUIThread());

		TiApplication appContext = TiApplication.getInstance();
		Activity activity = proxy.getActivity();
		((TiBaseActivity) activity).addOnLifecycleEventListener(this);

		Log.d(LCAT, "Activity: " + activity + ", context:" + appContext);

		XWalkView view = new XWalkView(appContext, activity);

		binding = new WebViewBinding(view);
		resourceClient = new WebViewResourceClient(view, proxy, binding);

		view.setResourceClient(resourceClient);
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		view.setBackgroundColor(Color.TRANSPARENT);

		// Set the view as the native view. You must set the native view
		// for your view to be rendered correctly.
		setNativeView(view);
	}


	// The view is automatically registered as a model listener when the view
	// is realized by the view proxy. That means that the processProperties
	// method will be called during creation and that propertiesChanged and
	// propertyChanged will be called when properties are changed on the proxy.

	@Override
	public void processProperties(KrollDict props)
	{
		super.processProperties(props);

		Log.d(LCAT,"WebView created with props: " + props);

		if (props.containsKey(PROPERTY_HTML)) {
			setHtml(props.getString(PROPERTY_HTML));
		} else if (props.containsKey(PROPERTY_URL)) {
			setUrl(props.getString(PROPERTY_URL));
		}
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy)
	{
		// This method is called whenever a proxy property value is updated. Note that this
		// method is only called if the new value is different than the current value.

		if (key.equals(PROPERTY_HTML)) {
			setHtml((String) newValue);
		}
		if (key.equals(PROPERTY_URL)) {
			setUrl((String) newValue);
		}

		super.propertyChanged(key, oldValue, newValue, proxy);
	}

	public XWalkView getXWalkView()
	{
		return (XWalkView) getNativeView();
	}

	public boolean canGoBack()
	{
		return getXWalkView().getNavigationHistory().canGoBack();
	}

	public boolean canGoForward()
	{
		return getXWalkView().getNavigationHistory().canGoForward();
	}

	public void goBack()
	{
		getXWalkView().getNavigationHistory().navigate(Direction.BACKWARD, 1);
	}

	public void goForward()
	{
		getXWalkView().getNavigationHistory().navigate(Direction.FORWARD, 1);
	}


	public void setUrl(String url)
	{
		Log.d(LCAT, "Loading new url: " + url);
		XWalkView view = getXWalkView();
		view.load(url, null);
	}

	public void setHtml(String html)
	{
		Log.d(LCAT, "Loading html string");
		XWalkView view = getXWalkView();
		view.load(null, html);
	}

	public String getJSValue(String expression)
	{
		return binding.getJSValue(expression);
	}

	@Override
	public void onStart(Activity activity) {
		Log.i(LCAT, "onStart");
	}

	@Override
	public void onPause(Activity activity) {
		Log.i(LCAT, "onPause");
		XWalkView view = getXWalkView();
		if (view != null) {
			Log.i(LCAT, "onPause view found");
			view.pauseTimers();
			view.onHide();
		}
	}

	@Override
	public void onResume(Activity activity) {
		Log.i(LCAT, "onResume");
		XWalkView view = getXWalkView();
		if (view != null) {
			Log.i(LCAT, "onResume view found");
			view.resumeTimers();
			view.onShow();
		}
	}

	@Override
	public void onDestroy(Activity activity) {
		Log.i(LCAT, "onDestroy");
		XWalkView view = getXWalkView();
		if (view != null) {
			Log.i(LCAT, "onDestroy view found");
			view.onDestroy();
		}
	}

	@Override
	public void onStop(Activity activity) {
		Log.i(LCAT, "onStop");
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		XWalkView view = getXWalkView();
		Log.i(LCAT, "onActivityResult");
		if (view != null) {
			Log.i(LCAT, "onActivityResult view found");
			view.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onNewIntent(Intent intent) {
		Log.i(LCAT, "onNewIntent");
		XWalkView view = getXWalkView();
		if (view != null) {
			Log.i(LCAT, "onNewIntent view found");
			view.onNewIntent(intent);
		}
	}
}
