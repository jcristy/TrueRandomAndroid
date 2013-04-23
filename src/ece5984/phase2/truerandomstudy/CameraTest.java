package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraTest implements Test {

	Context context;
	final ArrayList<ArrayList<DataPair>> random_data = new ArrayList<ArrayList<DataPair>>();
	Timer t;
	final Camera cam = getCameraInstance();
	
	@Override
	public int initialize(Context context, ByteArrayOutputStream raw) {
		this.context = context;
		random_data.add(new ArrayList<DataPair>());
		
		Looper.prepare();
		SurfaceView dummy=new SurfaceView(context);
        try {
			cam.setPreviewDisplay(dummy.getHolder());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Camera.Parameters parameters = cam.getParameters();
        parameters.setPreviewFormat(ImageFormat.RGB_565);
        cam.setParameters(parameters);
        cam.setPreviewCallback(new preview());
        cam.startPreview();
//		t = new Timer();
//		t.schedule(new TimerTask(){
//
//			@Override
//			public void run() {
//				//getImageFromCamera(null);
//			}
//			
//		}, 3000);
		return 0;
	}

	@Override
	public ArrayList<DataPair> getData() {
		// TODO Auto-generated method stub
		return random_data.get(0);
	}

	@Override
	public void finish() {
		cam.release();
	}

	@Override
	public String[] describeTests() {
		return new String[]{"LSBs"};
	}

	@Override
	public boolean timeMatters() {
		return true;
	}

	@Override
	public void clear() {
		for (ArrayList<DataPair> dataset : random_data)
		{
			dataset.clear();
		}
	}
	
	public void getImageFromCamera(DataOutputStream dos) {
		if (!context.getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			
			return;
		}
		Camera.Parameters parameters = cam.getParameters();
		parameters.setJpegQuality(100);
		parameters.setAntibanding(Parameters.ANTIBANDING_OFF);
		parameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
		parameters.setPreviewSize(100,100);
		cam.setParameters(parameters);
		Log.d("Camera", "Taking picture NOW");
		JpegCallback jpeg = new JpegCallback(dos);
		cam.takePicture(null, null, jpeg);

	}
	public class preview implements Camera.PreviewCallback
	{
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) 
		{
			Log.d("Camera","data[0]: "+data[0]);
			return;
			/*
				{
					int pixel = bmp.getPixel(i*(bmp.getWidth()/squares), j*(bmp.getHeight()/squares));
					int red   = Color.red(pixel);
					int blue  = Color.blue(pixel);
					int green = Color.green(pixel);
					
					
					x[0] = x[0] << 1;
					int thisbit = ((red&0x1)^(blue&0x1)^(green&0x1));
					x[0] = x[0] | thisbit;
					Log.d("Camera",""+red+" "+green+" "+blue+" "+thisbit);
					bits++;
					if (bits==32)
					{
						random_data.get(0).add(new DataPair(32,x[0]));
						bits = 1;
					}
					
				}
				*/
		}
	}
	public class OurAutoFocusCallback implements AutoFocusCallback {
		DataOutputStream dos;

		public OurAutoFocusCallback(DataOutputStream dos) {
			super();
			this.dos = dos;
		}

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			JpegCallback jpeg = new JpegCallback(dos);
			camera.takePicture(null, null, jpeg);
		}
	}

	public class JpegCallback implements PictureCallback {
		public DataOutputStream dos;

		// public DataOutputStream dos2;
		public JpegCallback(DataOutputStream writeToThis) {
			super();
			dos = writeToThis;
		}

		public void onPictureTaken(byte[] data, Camera camera) 
		{	
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			Log.d("Camera",bmp.getWidth()+" "+bmp.getHeight());
			int squares = 100;
			int bits = 0;
			int[] x = {0};
			for (int i=0; i<squares;i++)
				for (int j=0; j<squares;j++)
				{
					int pixel = bmp.getPixel(i*(bmp.getWidth()/squares), j*(bmp.getHeight()/squares));
					int red   = Color.red(pixel);
					int blue  = Color.blue(pixel);
					int green = Color.green(pixel);
					
					
					x[0] = x[0] << 1;
					int thisbit = ((red&0x1)^(blue&0x1)^(green&0x1));
					x[0] = x[0] | thisbit;
					Log.d("Camera",""+red+" "+green+" "+blue+" "+thisbit);
					bits++;
					if (bits==32)
					{
						random_data.get(0).add(new DataPair(32,x[0]));
						bits = 1;
					}
					
				}
			cam.startPreview();
		}
	};

	public Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
			Log.d("Camera", "We opened the camera");
		} catch (Exception e) {
			
		}
		if (c == null)
			Log.d("Camera", "camera is null still");
		return c;
	}
}
