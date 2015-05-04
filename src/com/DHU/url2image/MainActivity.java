package com.DHU.url2image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private WebView webView;
	private ImageView image;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webView = new WebView(this);
		webView.setVisibility(View.INVISIBLE);
		webView.loadUrl("http://www.baidu.com");
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100) {
					Picture picture = webView.capturePicture();
					Toast.makeText(
							getApplicationContext(),
							String.format("%d %d", picture.getWidth(),
									picture.getHeight()), Toast.LENGTH_LONG)
							.show();
				}
			}
		});

	}

}
