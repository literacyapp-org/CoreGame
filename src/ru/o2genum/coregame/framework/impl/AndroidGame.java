package ru.o2genum.coregame.framework.impl;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import ru.o2genum.coregame.R;
import ru.o2genum.coregame.framework.*;

public abstract class AndroidGame extends Activity implements Game {
	AndroidFastRenderView renderView;
	Graphics graphics;
	Audio audio;
	Input input;
	FileIO fileIO;
	Vibration vibration;
	Screen screen;
	List<Bitmap> bitmaps;
	Bitmap selectBackgroud;
	Bitmap wayPoint;
	SurfaceView surfaceView;
	Button btn_pause;
	Button btn_music;
	Button btn_refresh;
	Button btn_tip;
	ProgressBar progress_game;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		int width = surfaceView.getWidth();
		int height = surfaceView.getHeight();
		Bitmap frameBuffer = Bitmap.createBitmap(getWindowManager()
				.getDefaultDisplay().getWidth(), getWindowManager()
				.getDefaultDisplay().getHeight(), Config.RGB_565);

		InitBitmap();
		renderView = new AndroidFastRenderView(surfaceView, this, frameBuffer);
		graphics = new AndroidGraphics(frameBuffer);
		fileIO = new AndroidFileIO(getAssets());
		audio = new AndroidAudio(this);
		input = new AndroidInput(this, surfaceView);
		vibration = new AndroidVibration(this);
		screen = getStartScreen();
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	}

	private void InitControls() {
		btn_music = (Button) findViewById(R.id.btn_music);
		btn_pause = (Button) findViewById(R.id.btn_pause);
		btn_refresh = (Button) findViewById(R.id.btn_refresh);
		btn_tip = (Button) findViewById(R.id.btn_tip);
		progress_game = (ProgressBar) findViewById(R.id.progress);

		btn_music.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				test_getQuota();
			}
		});
	}

	private void test_getQuota() {
	}

	private void InitBitmap() {
		bitmaps = new ArrayList<Bitmap>();
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1001));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1002));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1003));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1004));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1005));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1006));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1007));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1008));

		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1009));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1010));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png1011));
		bitmaps.add(BitmapFactory.decodeResource(this.getResources(),
				R.drawable.png0016));

		selectBackgroud = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.selectbackground);
		wayPoint = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.point);
	}

	@Override
	public void onResume() {
		super.onResume();
		screen.resume();
		renderView.resume();
	}

	@Override
	public void onPause() {
		super.onPause();
		renderView.pause();
		screen.pause();

		if (isFinishing())
			screen.dispose();
	}

	@Override
	public Input getInput() {
		return input;
	}

	@Override
	public FileIO getFileIO() {
		return fileIO;
	}

	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	@Override
	public Audio getAudio() {
		return audio;
	}

	@Override
	public void setScreen(Screen screen) {
		if (screen == null)
			throw new IllegalArgumentException("Screen must not be null");

		this.screen.pause();
		this.screen.dispose();
		screen.resume();
		screen.update(0);
		this.screen = screen;
	}

	public Screen getCurrentScreen() {
		return screen;
	}

	public Vibration getVibration() {
		return vibration;
	}

	public List<Bitmap> getBitmap() {
		return bitmaps;
	}

	public int getBackGroud() {
		return this.getResources().getColor(R.color.backcolor);
		// return Resources.getSystem().getColor(R.color.backcolor);
	}

	public Bitmap getSelectBackGroud() {
		return selectBackgroud;
	}

	public Bitmap getWayPoint() {
		return wayPoint;
	}

	private void btn_refresh_OnClick() {
		progress_game.setMax(100);
		progress_game.setProgress(10);
	}
}
