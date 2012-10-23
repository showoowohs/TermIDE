/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spartacusrex.spartacuside;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.gesture.GestureOverlayView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spartacusrex.spartacuside.session.TermSession;
import com.spartacusrex.spartacuside.util.TermSettings;

/**
 * A terminal emulator activity.
 */

public class Term extends Activity implements OnGestureListener {
    /**
     * The ViewFlipper which holds the collection of EmulatorView widgets.
     */
    public static TermViewFlipper mViewFlipper;

    /**
     * The name of the ViewFlipper in the resources.
     */
    private static final int VIEW_FLIPPER = R.id.view_flipper;

    private ArrayList<TermSession> mTermSessions;

    private SharedPreferences mPrefs;
    private TermSettings mSettings;

    private final static int SELECT_TEXT_ID = 0;
    private final static int COPY_ALL_ID = 1;
    private final static int PASTE_ID = 2;
    private final static int CLEAR_ALL_ID = 3;

    private boolean mAlreadyStarted = false;

    private Intent TSIntent;

    public static final int REQUEST_CHOOSE_WINDOW = 1;
    public static final String EXTRA_WINDOW_ID = "jackpal.androidterm.window_id";
    private int onResumeSelectWindow = -1;

    //add 1011
    public LinearLayout lay1_X_text;
    public static LinearLayout lay1;
    public static LinearLayout UI_Head;
    public static LinearLayout lay1_X;
    public static LinearLayout lay1_Y;
    
	public LinearLayout lay1_Y_text;
	public LinearLayout lay2;
	public LinearLayout lay2_X_text;

	public LinearLayout lay2_Y_text;

	public LinearLayout lay3_X_text;

	public LinearLayout lay3_Y_text;
    private LinearLayout lay1_X_view, lay1_Y_view;//會被改變
    private TextView[] tv_lay1_X_text = new TextView[30];
    private TextView[] tv_lay1_Y_text = new TextView[23];
    private TextProgressBar[] lay1_X_bar = new TextProgressBar[30];
    private TextProgressBar[] lay1_Y_bar = new TextProgressBar[23];
    private ProgressDialog pdialog;
    private Handler hd_get_text = new Handler();
    private Handler hd_darw = new Handler();
    //add 1022 OnGestureListener Listener
    private LinearLayout ALL_Listener;
    private GestureDetector detector;

    //    private PowerManager.WakeLock mWakeLock;
    //    private WifiManager.WifiLock mWifiLock;

    private TermService mTermService;
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TermDebug.LOG_TAG, "Bound to TermService");
            TermService.TSBinder binder = (TermService.TSBinder) service;
            mTermService = binder.getService();
            populateViewFlipper();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mTermService = null;
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.e(TermDebug.LOG_TAG, "onCreate");
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings = new TermSettings(mPrefs);

        TSIntent = new Intent(this, TermService.class);
        
        //Not Needed..
        //startService(TSIntent);

        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            Log.w(TermDebug.LOG_TAG, "bind to service failed!");
        }
        //add 1011  
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);  
        
        
        setContentView(R.layout.term_activity);
        mViewFlipper = (TermViewFlipper) findViewById(VIEW_FLIPPER);
        registerForContextMenu(mViewFlipper);
        
//        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TermDebug.LOG_TAG);
//        mWakeLock.acquire();

//        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
//        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TermDebug.LOG_TAG);
        
        updatePrefs();
        
        mAlreadyStarted = true;
        

        //add 1011
        init_UI();
        //add 1008
//        Toast_msg("Rooting...");
        pdialog = new ProgressDialog(Term.this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
        Handler handler = new Handler();
        handler.postDelayed(cat_file, 1000);
        //end add
        
    }
    
    
    //add 1011
    //ui
    public void init_UI(){
    	this.lay1_X_text = (LinearLayout) findViewById(R.id.lay1_X_text);
    	this.UI_Head = (LinearLayout) findViewById(R.id.UI_Head);
    	this.lay1 = (LinearLayout) findViewById(R.id.lay1);
    	this.lay1_X = (LinearLayout) findViewById(R.id.lay1_X);
    	this.lay1_Y_text = (LinearLayout) findViewById(R.id.lay1_Y_text);
    	this.lay1_X_view = (LinearLayout) findViewById(R.id.lay1_X_view);
    	this.lay1_Y_view = (LinearLayout) findViewById(R.id.lay1_Y_view);
    	this.lay2 = (LinearLayout) findViewById(R.id.lay2);
    	this.lay1_Y = (LinearLayout) findViewById(R.id.lay1_Y);
    	
    	//1019 add
    	LinearLayout lay3_X = (LinearLayout) findViewById(R.id.lay3_X);
    	LinearLayout lay3_X_text = (LinearLayout) findViewById(R.id.lay3_X_text);
    	LinearLayout lay3_X_view = (LinearLayout) findViewById(R.id.lay3_X_view);
    	LinearLayout lay3_Y_text = (LinearLayout) findViewById(R.id.lay3_Y_text);
    	LinearLayout lay3_Y_view = (LinearLayout) findViewById(R.id.lay3_Y_view);
    	lay3_X.setVisibility(View.GONE);
    	lay3_X_text.setVisibility(View.GONE);
    	lay3_X_view.setVisibility(View.GONE);
    	lay3_Y_text.setVisibility(View.GONE);
    	lay3_Y_view.setVisibility(View.GONE);
    	LinearLayout lay3 = (LinearLayout) findViewById(R.id.lay3);
    	lay3.setVisibility(View.GONE);
    	//1022 add 
    	this.ALL_Listener = (LinearLayout) findViewById(R.id.ALL_Listener);
    	this.detector = new GestureDetector(this);
//    	lay2_text = (LinearLayout) findViewById(R.id.lay2_text);
//    	lay3_text = (LinearLayout) findViewById(R.id.lay3_text);
    	
    	add_TextView();
    	
    	this.ALL_Listener.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
//				Log.i("OnTouchListener", "OnTouchListener");
				return detector.onTouchEvent(event);
			}
		});
    }
    private void add_TextView(){
		//get layout size
				
		for(int i = 0 ; i < tv_lay1_X_text.length ; i++){
			tv_lay1_X_text[i] = new TextView(this);
			lay1_X_bar[i] =  new TextProgressBar(this);
//			this.lay1_X_bar[i].setTextColor(Color.GREEN);
			this.lay1_X_text.addView(Call_TextView(tv_lay1_X_text[i], i, 18, "X"+(i+1)), i);
			this.lay1_X_view.addView(Call_Bar(lay1_X_bar[i], "init..", 20));

			
			if(i < tv_lay1_Y_text.length){
				this.tv_lay1_Y_text[i] = new TextView(this); 
				this.lay1_Y_bar[i] =  new TextProgressBar(this);
//				this.lay1_Y_bar[i].setTextColor(Color.GREEN);
				this.lay1_Y_text.addView(Call_TextView(tv_lay1_Y_text[i], i, 18, "Y"+(i+1)), i);
				this.lay1_Y_view.addView(Call_Bar(lay1_Y_bar[i], "init..", 20));
				
			}
		}
    }
    private TextView Call_TextView(TextView tv, int id,int size, String text){
		tv.setTextSize(size);
		tv.setText(text);
		tv.setGravity(Gravity.CENTER);
		
		return tv; 
	}
	
	private TextProgressBar Call_Bar(TextProgressBar Bar, String str, int paint){
		BeanUtils.setFieldValue(Bar, "mOnlyIndeterminate", new Boolean(false));  
		Bar.setIndeterminate(false);  
		Bar.setProgressDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));  
		Bar.setIndeterminateDrawable(getResources().getDrawable(android.R.drawable.progress_indeterminate_horizontal));  
		Bar.setLayoutParams(new FrameLayout.LayoutParams(100, 24, Gravity.CENTER_HORIZONTAL) );
		Bar.setProgress(paint);
		Bar.setText(str);
		return Bar;
	}
    
    int old = 10;
    public void Toast_msg(String msg){
    	Toast tt = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        tt.show();
    }
    
    
	private Runnable cat_file = new Runnable() {
		public void run() {
			
			getCurrentTermSession().write("su\n");
//	    	getCurrentTermSession().write("cat /dev/input/event1\n");
			getCurrentTermSession().write("cd ../../../\n");
			getCurrentTermSession().write("./data/mtest\n");
//			getCurrentTermSession().write("chmod -R 777 /data/mtest\n");
//			getCurrentTermSession().write("./data/mtest\n");
					
			
			hd_get_text.postDelayed(get_text, 1000);
			
			hd_darw.postDelayed(UI_up, 1000);
//			Thread t = new Thread((new draw()));
//			t.start();
		}
	};
	//add 1020
	private HandlerThread Ht ;
	private Handler boss ;
	public String lay1_text = "";
	public String lay1_v = "" ;
	public String lay2_text = "";
	public String lay2_v = "";
	class draw implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				try {
					Thread.sleep(1);

					if (lay1_text != "" && lay1_v != "") {
						set_lay1_Grid(lay1_text, lay1_v);
						// System.out.println(lay1_text + " x " + lay1_v);
					}
					if (lay2_text != "" && lay2_v != "") {
						set_lay2_Grid(lay2_text, lay2_v);
						// System.out.println(lay1_text + " x " + lay1_v);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	private Runnable UI_up = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(lay1_text != "" &&  lay1_v != ""){
				set_lay1_Grid(lay1_text, lay1_v);
//				System.out.println(lay1_text + " x " + lay1_v);
			}
			if(lay2_text != "" &&  lay2_v != ""){
				set_lay2_Grid(lay2_text, lay2_v);
//				System.out.println(lay1_text + " x " + lay1_v);
			}
//			hd_get_text.removeCallbacks(this);
			
			hd_darw = new Handler();
			hd_darw.postDelayed(this, 1);
		}
		
	};
	
	//add 1009
	String old_str = "";
	int aaa = 0 ;
	
	private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	private Runnable get_text = new Runnable() {
		public void run() {
			if(pdialog != null){
				pdialog.cancel();
				pdialog = null;
			}
//			int length = getCurrentTermSession().getTranscriptText2(old).length();
			String str;
			try {
				
				str = getCurrentTermSession().getTranscriptText2(old);
				
//				old = length;

				//add 1019
				
				if(str.equals("")) {

				} else {
				
						// && str.length() > 25)
//						System.out.println(str);
						if (str.charAt(0) == 'P') {
//							System.out.println(str);
							List<String> list = new ArrayList<String>();
							String[] names = str.split("P");
							for (String name : names) {
								list.add(name);
//								System.out.println(name);
							}
							HashMap<String, Object> lay1_map;
							HashMap<String, Object> lay2_map;
							
							for(int i = 0 ; i < list.size() ; i++){
//								list.get(i);
								lay1_map = new HashMap<String, Object>();
								lay2_map = new HashMap<String, Object>();
								if(list.get(i).length() > 25){
//									System.out.println(list.get(i).toString());
									lay1_map.put("lay1_Grid", list.get(i).substring(0, 5)
											.toString());
									lay1_map.put("lay1_Value", list.get(i).substring(6, 11)
											.toString());
									lay2_map.put("lay2_Grid", list.get(i).substring(12, 17)
											.toString());
									lay2_map.put("lay2_Value", list.get(i).substring(18, 23)
											.toString());
								}
								System.out.println("lay1_Grid " + lay1_map.get("lay1_Grid") + "  lay1_Value "
									+ lay1_map.get("lay1_Value") + " lay2gird  " + lay2_map.get("lay2_Grid") +  " lay2Value " + lay2_map.get("lay2_Value"));
								//畫上lay1
								if((lay1_map.get("lay1_Grid") != null) & (lay1_map.get("lay1_Value") != null)){
//										System.out.println("lay1_Grid " + lay1_map.get("lay1_Grid") + "  lay1_Value "
//												+ lay1_map.get("lay1_Value"));
									
									lay1_text = lay1_map.get("lay1_Grid").toString();
									lay1_v = lay1_map.get("lay1_Value").toString();
//									set_lay1_Grid(lay1_map.get("lay1_Grid").toString(), lay1_map.get("lay1_Value").toString());
								}
//								//畫上lay2
								if((lay2_map.get("lay2_Grid") != null) & (lay2_map.get("lay2_Value") != null)){
//										System.out.println("lay1_Grid " + lay1_map.get("lay1_Grid") + "  lay1_Value "
//												+ lay1_map.get("lay1_Value"));
//									set_lay2_Grid(lay2_map.get("lay2_Grid").toString(), lay2_map.get("lay2_Value").toString());
									lay2_text = lay2_map.get("lay2_Grid").toString();
									lay2_v = lay2_map.get("lay2_Value").toString();
								}
							}
							
							//畫上lay1
//							if((lay1_map.get("lay1_Grid") != null) & (lay1_map.get("lay1_Value") != null)){
////									System.out.println("lay1_Grid " + lay1_map.get("lay1_Grid") + "  lay1_Value "
////											+ lay1_map.get("lay1_Value"));
//								set_lay1_Grid(lay1_map.get("lay1_Grid").toString(), lay1_map.get("lay1_Value").toString());
//							}
//							//畫上lay2
//							if((lay2_map.get("lay2_Grid") != null) & (lay2_map.get("lay2_Value") != null)){
////									System.out.println("lay1_Grid " + lay1_map.get("lay1_Grid") + "  lay1_Value "
////											+ lay1_map.get("lay1_Value"));
//								set_lay2_Grid(lay2_map.get("lay2_Grid").toString(), lay2_map.get("lay2_Value").toString());
//							}							
							
							
							
//							for(int i = 0 ; i < list.size(); i++){
//								System.out.println(str.length());
//							}
							
//							System.out.println(str.length());
//							System.out.println(str);
//							System.out.println(str.substring(1, 6) + " " + str.substring(7, 12) + " " + str.substring(13, 18) + " " + str.substring(19, 24));
							
//							lay2_map = new HashMap<String, Object>();
							
						}
//					if (str.substring(0, 1).equals("0")) {
//						List<String> list = new ArrayList<String>();
//
//						// 將data拆成一筆一筆
//						String[] names = str.split(":");
//						for (String name : names) {
//							String[] names2 = name.split("P[0-9]{3}");
//							for (String name2 : names2) {
////									 System.out.println(name2);
//								list.add(name2);
//								
//							}
//
//						}
////							 System.out.println(list.size());
//						
//						// 拆 lay1, lay2
//						HashMap<String, Object> lay1_map;
//						HashMap<String, Object> lay2_map;
//
//						for (int i = 0; i < list.size(); i++) {
//							// System.out.println(list.get(i));
//							String[] array = list.get(i).split("-");
//							lay1_map = new HashMap<String, Object>();
//							lay2_map = new HashMap<String, Object>();
//							int count = 0 ;
//							for (String name : array) {
//								++count;
////									System.out.println(name + "  " + count);
//								if(name.substring(0, 1).equals("X") && count == 1){
//									lay1_map.put("lay1_Grid", name.substring(4, 9)
//											.toString());
////									 System.out.print(" lay1格 " +
////									 name.substring(4, 9));
//								}else if(name.substring(0, 1).equals("Y") && count == 2){
//									lay1_map.put("lay1_Value", name.substring(4, 9)
//											.toString());
////									 System.out.print(" lay1直  " +
////									 name.substring(4, 9));
//								}else if(name.substring(0, 1).equals("X") && count == 3){
//									lay2_map.put("lay2_Grid", name.substring(4, 9)
//											.toString());
////									 System.out.print(" lay2格 " +
////									 name.substring(4, 9));
//								}else if(name.substring(0, 1).equals("Y") && count == 4){
//									lay2_map.put("lay2_Value", name.substring(4, 9)
//											.toString());
////									 System.out.print(" lay2值" +
////									 name.substring(4, 9));
//								}
//								
//							}
//							System.out.println();
//							//畫上lay1
//							if((lay1_map.get("lay1_Grid") != null) & (lay1_map.get("lay1_Value") != null)){
////									System.out.println("lay1_Grid " + lay1_map.get("lay1_Grid") + "  lay1_Value "
////											+ lay1_map.get("lay1_Value"));
//								set_lay1_Grid(lay1_map.get("lay1_Grid").toString(), lay1_map.get("lay1_Value").toString());
//							}
//							//畫上lay2
//							if((lay2_map.get("lay2_Grid") != null) & (lay2_map.get("lay2_Value") != null)){
////									System.out.println("lay1_Grid " + lay1_map.get("lay1_Grid") + "  lay1_Value "
////											+ lay1_map.get("lay1_Value"));
//								set_lay2_Grid(lay2_map.get("lay2_Grid").toString(), lay2_map.get("lay2_Value").toString());
//							}
//							
//							System.out.println("add " + aaa++);
//							
//						}
//						list = null;
//						lay1_map = null;
//						lay2_map = null;
//						
//					}
				

				}
				str = null;
				//end add 1019
				
				
				/*
				if (str.equals("")) {

				} else {

//					 System.out.println(str);

					if (str.substring(0, 1).equals("P")) {
						List<String> list = new ArrayList<String>();

						// 將data拆成一筆一筆
						String[] names = str.split(":");
						for (String name : names) {
							String[] names2 = name.split("P[0-9]{3}");
							for (String name2 : names2) {
								// System.out.println(name2);
								list.add(name2);
							}

						}

						// 拆-, X, Y
						HashMap<String, Object> map;

						for (int i = 0; i < list.size(); i++) {
							// System.out.println(list.get(i));
							String[] array = list.get(i).split("-");
							map = new HashMap<String, Object>();
							for (String name : array) {
								// System.out.println(name + " " +
								// name.length());
								if (name.substring(0, 1).equals("X")) {
									map.put("Grid", name.substring(4, 9)
											.toString());
									// System.out.println("X" +
									// name.substring(4, 9));
									
								} else if (name.substring(0, 1).equals("Y")) {// Y
									map.put("Value", name.substring(4, 9)
											.toString());
									// System.out.println("Y" +
									// name.substring(4, 9));
									
								}

							}
							listItem.add(map);//no push
//							System.out.println("add " + aaa++);
							if((map.get("Grid") != null) & (map.get("Value") != null)){
//								System.out.println("Grid " + map.get("Grid") + "  Value "
//										+ map.get("Value"));
								setGrid(map.get("Grid").toString(), map.get("Value").toString());
							}
							
							map = null;
						}
						list = null;

						// debug get all array
						// for(int i = 0 ; i < listItem.size() ; i++){
						// HashMap<String, Object> m = listItem.get(i);
						// System.out.println("X = " + m.get("X") + " Y = " +
						// m.get("Y"));
						// }

					}
					str = null;
				}
				*/
				
			} catch (Exception e) {
				Log.e("Error", "Error" + e);
				Toast_msg("程式掛囉!!");
			}
//			Thread t = new Thread((new readTerm()));
//			t.start();
			
			hd_get_text.removeCallbacks(this);
			hd_get_text.postDelayed(this, 1);
			
		}
	};
	
	
	
	//add 1018
	private void set_lay1_Grid(String Grid, String Value){
		int position = Integer.parseInt(Grid.substring(3, 5));
		
		//選格子
		if((position > 0) && position <= 30){
//			System.out.println("position" + position);
//			tv_lay1_X_text[position-1].setText("X"+position);
//			tv_lay1_X_text[position-1].setBackgroundColor(Color.RED);
//			lay1_X_bar[position-1].setText(Value);
//			lay1_X_bar[position-1].setProgress(Integer.parseInt(Value)-10000);
//			lay1_X_bar[position-1].setTextColor(Color.GREEN);
			
			for(int i = 0 ; i < 30 ; i++){
				//條件成立 才畫上去
				if((i + 1) == position){
//					System.out.println("position" + position);
					//畫哪幾格
					
					tv_lay1_X_text[position-1].setText("X"+position);
					tv_lay1_X_text[position-1].setBackgroundColor(Color.RED);
					lay1_X_bar[position-1].setText(Value);
					lay1_X_bar[position-1].setProgress(Integer.parseInt(Value)-10000);
					lay1_X_bar[position-1].setTextColor(Color.GREEN);
					
				}else{
					
//					if(position <= 30){
////						tv_lay1_X_text[position-1].setText(position);
//						tv_lay1_X_text[i].setBackgroundColor(Color.WHITE);
//						lay1_X_bar[position-1].setText("");
//						lay1_X_bar[position-1].setProgress(0);
//						lay1_X_bar[i].setTextColor(Color.WHITE);
//					}else{
////						tv_lay1_Y_text[position-1].setText(position);
//						tv_lay1_Y_text[position-31].setBackgroundColor(Color.WHITE);
//						lay1_Y_bar[position-31].setText("");
//						lay1_Y_bar[position-31].setProgress(0);
//						lay1_Y_bar[position-31].setTextColor(Color.WHITE);
//					}
				}
			}
			
			
		}

		
	}
	
	private void set_lay2_Grid(String Grid, String Value){
		int position = Integer.parseInt(Grid.substring(3, 5));
		
		//選格子
		if((position > 0) && position <= 23){
//			System.out.println("position" + position);
			//add
//			tv_lay1_Y_text[position-1].setText("Y"+(position));
//			tv_lay1_Y_text[position-1].setBackgroundColor(Color.RED);
//			lay1_Y_bar[position-1].setText(Value);
//			lay1_Y_bar[position-1].setProgress(Integer.parseInt(Value)-10000);
//			lay1_Y_bar[position-1].setTextColor(Color.GREEN);
			
			for(int i = 0 ; i < 23 ; i++){
				//條件成立 才畫上去
				if((i + 1) == position){
//					System.out.println("position" + position);
					//畫哪幾格
					tv_lay1_Y_text[position-1].setText("Y"+(position));
					tv_lay1_Y_text[position-1].setBackgroundColor(Color.RED);
					lay1_Y_bar[position-1].setText(Value);
					lay1_Y_bar[position-1].setProgress(Integer.parseInt(Value)-10000);
					lay1_Y_bar[position-1].setTextColor(Color.GREEN);
					
				}else{
					
//					if(position <= 30){
////						tv_lay1_X_text[position-1].setText(position);
//						tv_lay1_X_text[position-1].setBackgroundColor(Color.WHITE);
//						lay1_X_bar[position-1].setText("");
//						lay1_X_bar[position-1].setProgress(0);
//						lay1_X_bar[position-1].setTextColor(Color.WHITE);
//					}else{
////						tv_lay1_Y_text[position-1].setText(position);
//						tv_lay1_Y_text[i].setBackgroundColor(Color.WHITE);
//						lay1_Y_bar[position-31].setText("");
//						lay1_Y_bar[position-31].setProgress(0);
//						lay1_Y_bar[i].setTextColor(Color.WHITE);
//					}
				}
			}
			
			
		}

		
	}
	
	
	private void setGrid(String Grid, String Value){
		int position = Integer.parseInt(Grid.substring(3, 5));
		
		//選格子
		if((position > 0) && position <= 53){
//			System.out.println("position" + position);
			for(int i = 0 ; i < 53 ; i++){
				//條件成立 才畫上去
				if((i + 1) == position){
//					System.out.println("position" + position);
					//畫哪幾格
					if(position <= 30){
						tv_lay1_X_text[position-1].setText("X"+position);
						tv_lay1_X_text[position-1].setBackgroundColor(Color.RED);
						lay1_X_bar[position-1].setText(Value);
						lay1_X_bar[position-1].setProgress(Integer.parseInt(Value)/10);
						lay1_X_bar[position-1].setTextColor(Color.RED);
					}else{
						tv_lay1_Y_text[position-31].setText("Y"+(position-30));
						tv_lay1_Y_text[position-31].setBackgroundColor(Color.RED);
						lay1_Y_bar[position-31].setText(Value);
						lay1_Y_bar[position-31].setProgress(Integer.parseInt(Value)/10);
						lay1_Y_bar[position-31].setTextColor(Color.RED);
					}
				}else{
					
//					if(position <= 30){
////						tv_lay1_X_text[position-1].setText(position);
//						tv_lay1_X_text[position-1].setBackgroundColor(Color.WHITE);
//						lay1_X_bar[position-1].setText("");
//						lay1_X_bar[position-1].setProgress(0);
//						lay1_X_bar[position-1].setTextColor(Color.WHITE);
//					}else{
////						tv_lay1_Y_text[position-1].setText(position);
//						tv_lay1_Y_text[position-31].setBackgroundColor(Color.WHITE);
//						lay1_Y_bar[position-31].setText("");
//						lay1_Y_bar[position-31].setProgress(0);
//						lay1_Y_bar[position-31].setTextColor(Color.WHITE);
//					}
				}
			}
			
			
		}

		
	}
	
	private void onDraw_X(String pos, String str){
		int position = Integer.parseInt(pos);
		if(position <= 30){
			lay1_X_bar[position-1].setText(str);
			lay1_X_bar[position-1].setProgress(Integer.parseInt(str)/10);
			lay1_X_bar[position-1].setTextColor(Color.RED);
			tv_lay1_X_text[position-1].setBackgroundColor(Color.RED);
		}
		
		System.out.println(pos);
		
		
	}
	private void onDraw_Y(String pos, String str){
		int position = Integer.parseInt(pos);
		if(position <= 23){
			lay1_Y_bar[position-1].setText(str);
			lay1_Y_bar[position-1].setProgress(Integer.parseInt(str)/10);
			lay1_Y_bar[position-1].setTextColor(Color.RED);
			tv_lay1_Y_text[position-1].setBackgroundColor(Color.RED);
		}
	}
	
	private void onDraw(){
		
	}
	
    

    //end 1018

    private void populateViewFlipper() {
        if (mTermService != null) {
            mTermSessions = mTermService.getSessions(getFilesDir());

//            if (mTermSessions.size() == 0) {
//                mTermSessions.add(createTermSession());
//            }

            for (TermSession session : mTermSessions) {
                EmulatorView view = createEmulatorView(session);
                mViewFlipper.addView(view);
            }

            updatePrefs();
        }

        //Set back to ESC
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("onDestroy()", "onDestroy()");
        //add 1022 off thread
        hd_darw.removeCallbacks(this.UI_up);
        hd_get_text.removeCallbacks(this.cat_file);
        
        mViewFlipper.removeAllViews();
        unbindService(mTSConnection);
        //stopService(TSIntent);

        mTermService = null;
        mTSConnection = null;

//        if (mWakeLock.isHeld()) {
//            mWakeLock.release();
//        }
//        if (mWifiLock.isHeld()) {
//            mWifiLock.release();
//        }
    }

    private void restart() {
        startActivity(getIntent());
        finish();
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("SpartacusRex GET LOCAL IP : ", ex.toString());
        }
        
        return null;
    }

    /*private TermSession createTermSession() {
        String HOME = getFilesDir().getPath();
        String APK  = getPackageResourcePath();
        String IP   = getLocalIpAddress();
        if(IP == null){
           IP = "127.0.0.1";
        }
        
        String initialCommand = "export HOME="+HOME+";cd $HOME;~/system/init "+HOME+" "+APK+" "+IP;
//        String initialCommand = "export HOME="+HOME+";cd $HOME";


        return new TermSession(this,mSettings, null, initialCommand);
    }*/

    private EmulatorView createEmulatorView(TermSession session) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        EmulatorView emulatorView = new EmulatorView(this, session, mViewFlipper, metrics);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.LEFT
        );
        emulatorView.setLayoutParams(params);

        session.setUpdateCallback(emulatorView.getUpdateCallback());

        return emulatorView;
    }

    private TermSession getCurrentTermSession() {
        return mTermSessions.get(mViewFlipper.getDisplayedChild());
    }

    private EmulatorView getCurrentEmulatorView() {
        return (EmulatorView) mViewFlipper.getCurrentView();
    }

    private void updatePrefs() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        for (View v : mViewFlipper) {
            ((EmulatorView) v).setDensity(metrics);
            ((EmulatorView) v).updatePrefs(mSettings);
        }
        {
            Window win = getWindow();
            WindowManager.LayoutParams params = win.getAttributes();
            final int FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            int desiredFlag = mSettings.showStatusBar() ? 0 : FULLSCREEN;
            if (desiredFlag != (params.flags & FULLSCREEN)) {
                if (mAlreadyStarted) {
                    // Can't switch to/from fullscreen after
                    // starting the activity.
                    restart();
                } else {
                    win.setFlags(desiredFlag, FULLSCREEN);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTermSessions != null && mTermSessions.size() < mViewFlipper.getChildCount()) {
            for (int i = 0; i < mViewFlipper.getChildCount(); ++i) {
                EmulatorView v = (EmulatorView) mViewFlipper.getChildAt(i);
                if (!mTermSessions.contains(v.getTermSession())) {
                    v.onPause();
                    mViewFlipper.removeView(v);
                    --i;
                }
            }
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings.readPrefs(mPrefs);
        updatePrefs();

        if (onResumeSelectWindow >= 0) {
            mViewFlipper.setDisplayedChild(onResumeSelectWindow);
            onResumeSelectWindow = -1;
        } else {
            mViewFlipper.resumeCurrentView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("onPause()", "onPause()");
        //add 1022 off thread
        hd_darw.removeCallbacks(this.UI_up);
        hd_get_text.removeCallbacks(this.cat_file);
        mViewFlipper.pauseCurrentView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        EmulatorView v = (EmulatorView) mViewFlipper.getCurrentView();
        if (v != null) {
            v.updateSize(true);
        }
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public int windows = 0;
    //add 1022
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("Windows id ", "Windows id = "+windows);
        if(id == R.id.Windows1){
//        	Log.d("Windows1", "Windows1");
        	setWindows(1);
        }else if(id == R.id.Windows2){
//        	Log.d("Windows3", "Windows2");
        	setWindows(2);
        }else if(id == R.id.Windows3){
//        	Log.d("Windows3", "Windows3");
        	setWindows(3);
        }else if(id == R.id.Windows4){
        	setContentView(R.layout.test_view);
        }
		return true;
    }
    
    private void setWindows(int i){
    	LinearLayout.LayoutParams FILL = new LinearLayout.LayoutParams(
        	    LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    	LinearLayout.LayoutParams WRAP = new LinearLayout.LayoutParams(
        	    LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
    	switch(i){
	    	case 1:
	    		Log.d("case ", "case = "+1);
	    		if(this.windows == 0){
//	    			params.weight = 0.0f;
					this.mViewFlipper.setLayoutParams(FILL);
	    		}else if(this.windows == 2){
	    			//to r
	    			this.lay1_X.setAnimation(AnimationUtils.loadAnimation(this,
							R.anim.slide_out_left));
	    			
					
					this.UI_Head.setLayoutParams(WRAP);
//					params.weight = 0.0f;
					this.mViewFlipper.setLayoutParams(FILL);
					this.mViewFlipper.setVisibility(View.VISIBLE);
					this.UI_Head.setVisibility(View.GONE);
	    		}else if(this.windows == 3){
//	    			this.UI_Head.setLayoutParams(WRAP);
	    			this.UI_Head.setVisibility(View.GONE);
	    			this.mViewFlipper.setLayoutParams(FILL);
	    			this.mViewFlipper.setVisibility(View.VISIBLE);
	    		}
				
	    		this.windows = 1;
	    		break;
	    	case 2:
	    		Log.d("case ", "case = "+2);
	    		if(this.windows == 1){
	    			this.mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in));   

	    	        this.mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));   
	    			
//	    			params.weight = 0.0f;
	    			this.UI_Head.setLayoutParams(FILL);
	    			this.lay1_X.setLayoutParams(FILL);
	    			this.UI_Head.setVisibility(View.VISIBLE);
	    			
	    			this.lay2.setLayoutParams(WRAP);
	    			
	    		}else if(this.windows == 3){
	    			
	    			this.lay1_Y.setVisibility(View.GONE);
	    			this.lay1_X.setVisibility(View.VISIBLE);
	    			this.lay1_X.setLayoutParams(FILL);
	    		}
	    		this.windows = 2;
	    		break;
	    	case 3:
	    		Log.d("case ", "case = "+3);
	    		if(this.windows == 1){
	    			this.mViewFlipper.setVisibility(View.GONE);
	    			this.mViewFlipper.setLayoutParams(WRAP);
	    			this.UI_Head.setVisibility(View.VISIBLE);
	    			this.UI_Head.setLayoutParams(FILL);
	    			this.lay1_X.setVisibility(View.GONE);
	    			this.lay1_Y.setVisibility(View.VISIBLE);
	    		}else if(this.windows == 2){
	    			this.lay1_X.startAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in));   

	    	        this.lay1_X.startAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));   
	    			
	    			
//	    			p.weight = 0.0f;
	    			this.lay1_X.setLayoutParams(WRAP);
	    			this.lay1_X.setVisibility(View.GONE);
	    			this.lay1_Y.setVisibility(View.VISIBLE);
	    			this.lay1_Y.setLayoutParams(FILL);
	    		}
	    		this.windows = 3;
	    		break;
			default:
				break;
    	}
    }

    private void doCreateNewWindow() {
        if (mTermSessions == null) {
            Log.w(TermDebug.LOG_TAG, "Couldn't create new window because mTermSessions == null");
            return;
        }

//        TermSession session = createTermSession();
//        mTermSessions.add(session);
//        EmulatorView view = createEmulatorView(session);
//        view.updatePrefs(mSettings);
//        mViewFlipper.addView(view);
//        mViewFlipper.setDisplayedChild(mViewFlipper.getChildCount()-1);
    }

    private void doCopyAll() {
        ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(getCurrentTermSession().getTranscriptText().trim());
    }

    private void doPaste() {
        ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(!clip.hasText()){
            Toast tt = Toast.makeText(this, "No text to Paste..", Toast.LENGTH_SHORT);
            tt.show();
            return;
        }

        CharSequence paste = clip.getText();
        byte[] utf8;
        try {
            utf8 = paste.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TermDebug.LOG_TAG, "UTF-8 encoding not found.");
            return;
        }
        
        getCurrentTermSession().write(paste.toString());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Is BACK ESC
//        Log.v("Terminal IDE","TERM : onkeyDown code:"+keyCode+" flags:"+event.getFlags()+" meta:"+event.getMetaState());

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mTermService.isBackESC()){
//                Log.v("SpartacusRex","TERM : ESC sent instead of back.!");
                //Send the ESC sequence..
                int ESC = TermKeyListener.KEYCODE_ESCAPE;
                getCurrentEmulatorView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, ESC));
                getCurrentEmulatorView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,   ESC));
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
}

    //1022 add 手勢
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("onDown", "onDown");
		return false;
	}


	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		Log.i("onFling", "onFling");
		if (arg0.getX() - arg1.getX() > 120) {// 如果是從右向左滑動

			// 註冊flipper的進出效果

			this.ALL_Listener.setAnimation(AnimationUtils.loadAnimation(this,
					R.anim.slide_out_left));
			Log.i("slide_out_left", "slide_out_left");
//			this.ALL_Listener.setOutAnimation(AnimationUtils.loadAnimation(this,
//					R.anim.left_out));
			return true;

		} else if (arg0.getX() - arg1.getX() < -120) {// 如果是從左向右滑動

			this.ALL_Listener.setAnimation(AnimationUtils.loadAnimation(this,
					R.anim.slide_in_right));
			Log.i("slide_in_right", "slide_in_right");
//			this.ALL_Listener.setOutAnimation(AnimationUtils.loadAnimation(this,
//					R.anim.right_out));
			return true;

		}

		return false;
	}


	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("onLongPress", "onLongPress");
	}


	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		Log.i("onScroll", "onScroll");
		return false;
	}


	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("onShowPress", "onShowPress");
	}


	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("onSingleTapUp", "onSingleTapUp");
		return false;
	}


 
    


}
