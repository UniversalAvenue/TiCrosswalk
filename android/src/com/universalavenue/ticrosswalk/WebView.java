/**
	* Appcelerator Titanium Mobile
	* Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
	* Licensed under the terms of the Apache Public License
	* Please see the LICENSE included with this distribution for details.
	*
	*/

package com.universalavenue.ticrosswalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.view.TiBackgroundDrawable;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiMimeTypeHelper;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.TextureView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

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
		view.setFocusableInTouchMode(false);

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

		// If TiUIView's processProperties ended up making a TiBackgroundDrawable
		// for the background, we must set the WebView background color to transparent
		// in order to see any of it.
		if (nativeView != null && nativeView.getBackground() instanceof TiBackgroundDrawable) {
			nativeView.setBackgroundColor(Color.TRANSPARENT);
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
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}

		// If TiUIView's propertyChanged ended up making a TiBackgroundDrawable
		// for the background, we must set the WebView background color to transparent
		// in order to see any of it.
		boolean isBgRelated = (key.startsWith(TiC.PROPERTY_BACKGROUND_PREFIX) || key.startsWith(TiC.PROPERTY_BORDER_PREFIX));
		if (isBgRelated && nativeView != null && nativeView.getBackground() instanceof TiBackgroundDrawable) {
			nativeView.setBackgroundColor(Color.TRANSPARENT);
		}
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


	private boolean mightBeHtml(String url)
	{
		String mime = TiMimeTypeHelper.getMimeType(url);
		if (mime.equals("text/html")) {
			return true;
		} else if (mime.equals("application/xhtml+xml")) {
			return true;
		} else {
			return false;
		}
	}

	public void setUrl(String url)
	{
		String finalUrl = url;
		Uri uri = Uri.parse(finalUrl);
		boolean originalUrlHasScheme = (uri.getScheme() != null);

		if (!originalUrlHasScheme) {
			finalUrl = getProxy().resolveUrl(null, finalUrl);
		}

		if (TiFileFactory.isLocalScheme(finalUrl) && mightBeHtml(finalUrl)) {
			TiBaseFile tiFile = TiFileFactory.createTitaniumFile(finalUrl, false);
			if (tiFile != null) {
				StringBuilder out = new StringBuilder();
				InputStream fis = null;
				boolean bindingCodeInjected = false;

				try {
					fis = tiFile.getInputStream();
					InputStreamReader reader = new InputStreamReader(fis, "utf-8");
					BufferedReader breader = new BufferedReader(reader);
					String line = breader.readLine();
					while (line != null) {
						if (!bindingCodeInjected) {
							int pos = line.indexOf("<html");
							if (pos >= 0) {
								int posEnd = line.indexOf(">", pos);
								if (posEnd > pos) {
									out.append(line.substring(pos, posEnd + 1));
									out.append(binding.SCRIPT_TAG_INJECTION_CODE);
									if ((posEnd + 1) < line.length()) {
										out.append(line.substring(posEnd + 1));
									}
									out.append("\n");
									bindingCodeInjected = true;
									line = breader.readLine();
									continue;
								}
							}
						}
						out.append(line);
						out.append("\n");
						line = breader.readLine();
					}
					// keep app:// etc. intact in case html in file contains links to JS that use app:// etc.
					getXWalkView().load((originalUrlHasScheme ? url : finalUrl), out.toString());
					return;

				} catch (IOException ioe) {
					Log.e(LCAT, "Problem reading from " + url + ": " + ioe.getMessage()
							+ ". Will let WebView try loading it directly.", ioe);
				} finally {
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {
							Log.w(LCAT, "Problem closing stream: " + e.getMessage(), e);
						}
					}
				}
			}
		}

		Log.d(LCAT, "WebView will load " + url + " directly.", Log.DEBUG_MODE);
		getXWalkView().load(url, null);
	}

	public void setHtml(String html)
	{
		Log.d(LCAT, "Loading html string");
		XWalkView view = getXWalkView();
		view.load("file://", html);
	}

	public String getJSValue(String expression)
	{
		return binding.getJSValue(expression);
	}

	public void onCreate(Activity activity, Bundle savedInstanceState) {
		Log.i(LCAT, "onCreate");
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
