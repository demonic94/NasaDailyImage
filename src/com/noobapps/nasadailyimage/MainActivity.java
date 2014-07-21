package com.noobapps.nasadailyimage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private IotdHandler iotdHandler;
	protected Bitmap image;
	protected String title;
	protected Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
         
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                              activeNetwork.isConnectedOrConnecting();
        if(isConnected == true){
	        handler = new Handler();
	        loadFeed();
        }
        else{
        	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        	alertDialog.setTitle("No Internet Connection");
        	alertDialog.setMessage("no internet connection\napp will now exit");
        	alertDialog.setNeutralButton("exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	android.os.Process.killProcess(android.os.Process.myPid());
                	System.exit(1);
                } });
            alertDialog.show();
        	android.os.Process.killProcess(android.os.Process.myPid());
        	System.exit(1);
        }
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    
    	switch(item.getItemId()){
    	
	    	case R.id.refresh : 
	    		loadFeed();
	    		return true;
	    	
	    	case R.id.setwall :
			try {
				setWallpaper(image);
			} 
			catch (IOException e) {
				Toast.makeText(MainActivity.this, "@string/Tfail", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
	    		return true;
	    	case R.id.store :
	    		storeSD();
	    		
	    	default :
	    		return super.onOptionsItemSelected(item);
    	}
    }
    
	private void resetDisplay(String title, String date,Bitmap image, StringBuffer description){
    	TextView titleview = (TextView)findViewById(R.id.title);
    	titleview.setText(title);
    	TextView dateview = (TextView)findViewById(R.id.date);
    	dateview.setText(date);
    	ImageView imageview = (ImageView)findViewById(R.id.image);
    	imageview.setImageBitmap(image);
    	TextView desview = (TextView)findViewById(R.id.description);
    	desview.setText(description);
    }
	
	protected void loadFeed(){
		
		final ProgressDialog dialog = ProgressDialog.show(this,"Loading","Loading The Image");
		Thread th = new Thread(){
			public void run(){
				if(iotdHandler == null)
					iotdHandler = new IotdHandler();
		        iotdHandler.processFeed();
		        image = iotdHandler.getImage();
		        title = iotdHandler.getTitle();
		        handler.post(new Runnable(){
		        	public void run(){
		        		resetDisplay(title, iotdHandler.getDate(), image, iotdHandler.getDescription());
				        dialog.dismiss();
			        } 	
		        });
			}
		};
		th.start();
	}
	
	protected void setWallpaer(){
		
		Thread th1 = new Thread(){
			
			public void run(){
				
				WallpaperManager wm = WallpaperManager.getInstance(MainActivity.this);
				try{
					wm.setBitmap(image);
					handler.post(new Runnable(){
			        	public void run(){
			        		Toast.makeText(MainActivity.this, "@string/Tsuccess", Toast.LENGTH_LONG).show();
				        } 	
			        });
				}
				catch(Exception e){
					e.printStackTrace();
					handler.post(new Runnable(){
			        	public void run(){
			        		Toast.makeText(MainActivity.this, "@string/Tfail", Toast.LENGTH_LONG).show();
			        	}
					});
				}
			}
		};
		th1.start();
	}
	
	void storeSD(){
		
		String sdcard = null ;
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	    if(isSDPresent)
	    {
	    	sdcard = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NASA Daily Image";
	    }
	    else
	    {
	    	Toast.makeText(MainActivity.this, "@string/errorSD", Toast.LENGTH_LONG).show();
	    	return;
	    }
		
        File dir = new File(sdcard);

        if (!dir.exists())
            dir.mkdirs();
        File file = new File(sdcard, title);
        try {
            FileOutputStream out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            
        }
	}
    
}
