package com.DHU.url2image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CaptureActivity extends Activity {

	private WebView webView;
	private String url;
	private TextView textViewTitle;
	private ClipboardManager cm;
	private static final String TAG = "CAPTURE";

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		webView.loadUrl(url);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.capture);
		cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		webView = (WebView) findViewById(R.id.webView);
		textViewTitle = (TextView) findViewById(R.id.title);
		cm.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener() {
			@Override
			public void onPrimaryClipChanged() {
				ClipData data = cm.getPrimaryClip();
				Item item = data.getItemAt(0);
				url = item.getText().toString();
				webView.loadUrl(url);
			}
		});
		final Button captureBtn = (Button) findViewById(R.id.captureBtn);
		WebChromeClient wcc = new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				textViewTitle.setText(title);
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (newProgress == 100) {
					captureBtn.setClickable(true);
				} else {
					captureBtn.setClickable(false);
				}
			}
		};
		webView.setWebChromeClient(wcc);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setTextZoom(50);
		webView.setWebViewClient(new WebViewClient() {
			// 当点击链接时,希望覆盖而不是打开新窗口
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url); // 加载新的url
				return true; // 返回true,代表事件已处理,事件流到此终止
			}

		});
		webView.getSettings().setBlockNetworkImage(false);
		captureBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				captureBtn.setClickable(false);
				Picture picture = webView.capturePicture();
				int width = picture.getWidth();
				int height = picture.getHeight();
				if (width > 0 && height > 0) {
					// 创建指定高宽的Bitmap对象
					Bitmap bitmap = Bitmap.createBitmap(width, height,
							Bitmap.Config.ARGB_8888);
					// 创建Canvas,并以bitmap为绘制目标
					Canvas canvas = new Canvas(bitmap);
					// 将WebView影像绘制在Canvas上
					picture.draw(canvas);
					saveImageToGallery(getApplicationContext(), bitmap);
				}
				captureBtn.setClickable(true);
				Toast.makeText(getApplicationContext(), R.string.success,
						Toast.LENGTH_SHORT).show();
			}
		});
		// webView.setVisibility(View.INVISIBLE);
	}

	public static String getOneHtml(final String htmlurl) {
		URL url;
		String temp;
		final StringBuffer sb = new StringBuffer();
		try {
			url = new URL(htmlurl);
			final BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream(), "utf-8"));// 读取网页全部内容
			while ((temp = in.readLine()) != null) {
				sb.append(temp);
			}
			in.close();
		} catch (final MalformedURLException me) {
			System.out.println("你输入的URL格式有问题！请仔细输入");
			me.getMessage();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param s
	 * @return 获得网页标题
	 */
	public static String getTitle(final String s) {
		String regex;
		regex = "<title>(.*?)</title>";
		final Pattern pa = Pattern.compile(regex);
		final Matcher ma = pa.matcher(s);
		if (ma.find() && ma.groupCount() >= 1) {
			return ma.group(1);
		} else
			return "";
	}

	public static void saveImageToGallery(Context context, Bitmap bmp) {
		// 首先保存图片
		File appDir = new File(Environment.getExternalStorageDirectory(),
				"urlCapture");
		if (!appDir.exists()) {
			appDir.mkdir();
		}
		String fileName = System.currentTimeMillis() + ".jpg";
		File file = new File(appDir, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} finally {

			if (!bmp.isRecycled()) {
				bmp.recycle();
				System.gc();
			}
		}

		// 其次把文件插入到系统图库

		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), fileName, null);

		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		// 最后通知图库更新
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.parse("file://" + file.getAbsolutePath())));
	}
}
