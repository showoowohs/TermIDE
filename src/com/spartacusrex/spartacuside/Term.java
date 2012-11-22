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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import sun.security.util.Cache;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spartacusrex.spartacuside.session.TermSession;
import com.spartacusrex.spartacuside.util.TermSettings;
import com.sun.source.tree.NewArrayTree;

/**
 * A terminal emulator activity.
 */

public class Term extends Activity {
	/**
	 * The ViewFlipper which holds the collection of EmulatorView widgets.
	 */
	private TermViewFlipper mViewFlipper;

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

	// add 1011
	private LinearLayout lay1_X_text, lay1_Y_text, lay2_X_text, lay2_Y_text,
			lay3_X_text, lay3_Y_text;
	private LinearLayout lay1_X_view, lay1_Y_view;// 會被改變
	private TextView[] tv_lay1_X_text = new TextView[30];
	private TextView[] tv_lay1_Y_text = new TextView[23];
	private TextProgressBar[] lay1_X_bar = new TextProgressBar[30];
	private TextProgressBar[] lay1_Y_bar = new TextProgressBar[23];
	ProgressDialog pdialog;

	// add 1119
	private LinearLayout all;
	private GestureDetector gDetector;

	// add 1120
	public static boolean uuup = true;
	public static boolean dddown = true;

	// private PowerManager.WakeLock mWakeLock;
	// private WifiManager.WifiLock mWifiLock;

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

		// Not Needed..
		// startService(TSIntent);

		if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
			Log.w(TermDebug.LOG_TAG, "bind to service failed!");
		}
		// add 1011
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.term_activity);
		mViewFlipper = (TermViewFlipper) findViewById(VIEW_FLIPPER);
		// registerForContextMenu(mViewFlipper);

		// PowerManager pm =
		// (PowerManager)getSystemService(Context.POWER_SERVICE);
		// mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		// TermDebug.LOG_TAG);
		// mWakeLock.acquire();

		// WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		// mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL,
		// TermDebug.LOG_TAG);

		updatePrefs();

		mAlreadyStarted = true;
		gDetector = new GestureDetector(this, new LearnGestureListener());

		// add 1011
		init_UI();
		// add 1008
		// Toast_msg("Rooting...");
		pdialog = new ProgressDialog(Term.this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();
		Handler handler = new Handler();
		handler.postDelayed(cat_file, 1000);
		// end add

	}

	// add 1011
	// ui
	public void init_UI() {
		lay1_X_text = (LinearLayout) findViewById(R.id.lay1_X_text);
		lay1_Y_text = (LinearLayout) findViewById(R.id.lay1_Y_text);
		lay1_X_view = (LinearLayout) findViewById(R.id.lay1_X_view);
		lay1_Y_view = (LinearLayout) findViewById(R.id.lay1_Y_view);

		// 1019 add
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

		// lay2_text = (LinearLayout) findViewById(R.id.lay2_text);
		// lay3_text = (LinearLayout) findViewById(R.id.lay3_text);

		// add 1119
		all = (LinearLayout) findViewById(R.id.all);
		all.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				return gDetector.onTouchEvent(event);
			}
		});

		add_TextView();
		init_clear();
	}

	private void init_clear() {
		for (int i = 0; i < clear1.length; i++) {
			clear1[i] = 0;
			clear2[i] = 0;
			Log.i("clear1", "clear1" + i);
		}

	}

	private void add_TextView() {
		// get layout size

		for (int i = 0; i < tv_lay1_X_text.length; i++) {
			tv_lay1_X_text[i] = new TextView(this);
			lay1_X_bar[i] = new TextProgressBar(this);
			// this.lay1_X_bar[i].setTextColor(Color.GREEN);
			this.lay1_X_text.addView(
					Call_TextView(tv_lay1_X_text[i], i, 18, "X" + (i + 1)), i);
			this.lay1_X_view.addView(Call_Bar(lay1_X_bar[i], "0", 100));

			if (i < tv_lay1_Y_text.length) {
				this.tv_lay1_Y_text[i] = new TextView(this);
				this.lay1_Y_bar[i] = new TextProgressBar(this);
				// this.lay1_Y_bar[i].setTextColor(Color.GREEN);
				this.lay1_Y_text.addView(
						Call_TextView(tv_lay1_Y_text[i], i, 18, "Y" + (i + 1)),
						i);
				this.lay1_Y_view.addView(Call_Bar(lay1_Y_bar[i], "0", 100));

			}
		}
	}

	private TextView Call_TextView(TextView tv, int id, int size, String text) {
		tv.setTextSize(size);
		tv.setText(text);
		tv.setGravity(Gravity.CENTER);

		return tv;
	}

	private TextProgressBar Call_Bar(TextProgressBar Bar, String str, int paint) {
		BeanUtils.setFieldValue(Bar, "mOnlyIndeterminate", new Boolean(false));
		Bar.setIndeterminate(false);
		Bar.setProgressDrawable(getResources().getDrawable(
				android.R.drawable.progress_horizontal));
		Bar.setIndeterminateDrawable(getResources().getDrawable(
				android.R.drawable.progress_indeterminate_horizontal));
		Bar.setLayoutParams(new FrameLayout.LayoutParams(100, 24,
				Gravity.CENTER_HORIZONTAL));
		Bar.setProgress(paint);
		Bar.setText(str);
		return Bar;
	}

	int old = 10;

	public void Toast_msg(String msg) {
		Toast tt = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		tt.show();
	}

	private Runnable cat_file = new Runnable() {
		public void run() {

			getCurrentTermSession().write("su\n");
			// getCurrentTermSession().write("cat /dev/input/event1\n");
			getCurrentTermSession().write("cd ../../../\n");
			getCurrentTermSession().write("./data/mtest\n");
			// getCurrentTermSession().write("chmod -R 777 /data/mtest\n");
			// getCurrentTermSession().write("./data/mtest\n");

			hd_get_text.postDelayed(get_text, 1000);
		}
	};

	//add 1122
	private void clear_data(){
		for(int i = 0; i < save_list.size() ;i++){
			String tmp = save_list.get(i).substring(0, 5);
			Log.i("clear", tmp + "   " + save_list.get(i).substring(5, 10));
			set_lay1_Grid(tmp, "0");
		}
	}
	
	
	// add 1121
	int[] clear1 = new int[53];
	int[] clear2 = new int[53];
	private boolean save = true;
	private List<String> save_list = null;
//	List<Integer> list_saveing = new ArrayList<Integer>();
	
	static boolean down_tmp = true;
	// add 1009
	String old_str = "";
	int aaa = 0;
	Handler hd_get_text = new Handler();
	private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	private Runnable get_text = new Runnable() {
		public void run() {
			if (pdialog != null) {
				pdialog.cancel();
				pdialog = null;
			}
			// int length =
			// getCurrentTermSession().getTranscriptText2(old).length();
			String str;
			// Log.i("clear_data", "clear_data = "+clear_data);
			if (clear_data == true) {
				// set_lay1_Grid("0000"+1,"0");
				// set_lay1_Grid("0000"+2,"0");
				// set_lay1_Grid("0000"+3,"0");
				// set_lay1_Grid("0000"+4,"0");
				// set_lay1_Grid("0000"+5,"0");
				// set_lay1_Grid("0000"+6,"0");
				// set_lay1_Grid("0000"+7,"0");
				// set_lay1_Grid("0000"+8,"0");
				// set_lay1_Grid("0000"+9,"0");
				// set_lay1_Grid("000"+10,"0");
				// set_lay1_Grid("000"+11,"0");
				for (int i = 1; i <= 53; i++) {
					//
					if (i < 10) {
						set_lay1_Grid("0000" + i, "0000");
					} else {
						// Log.i("i = ", "i = "+ i);
						set_lay1_Grid("000" + i, "0000");
					}
					//
				}
				// Log.i("clear_data", "clear_data = "+clear_data);
				clear_data = false;
			} else {
				try {

					str = getCurrentTermSession().getTranscriptText2(old);
					// Log.v("str ", ""+str);

					// old = length;

					// add 1019

					if (str.equals("")) {
						
					} else {
						
						// && str.length() > 25)
						// System.out.println(str);
						if (str.charAt(0) == 'P') {
							// System.out.println(str);
							
							//add 1121
							if( down_tmp == true ){//str.length() == 3 ||
								save = false;
								Log.v("save", "save exit" );
								if(save_list != null){
									Log.i("save_list", "save_list size" + save_list.size() );
//									clear_data();
									//add 1122 clear all
									for (int i = 1; i <= 53; i++) {
										//
										if (i < 10) {
											set_lay1_Grid("0000" + i, "0");
										} else {
											// Log.i("i = ", "i = "+ i);
											set_lay1_Grid("000" + i, "0");
										}
										//
									}
									down_tmp = false;
									save_list = null;
								}
								Log.v("save", "save" );
								save = true;
								save_list = new ArrayList<String>();

							}
							
							List<String> list = new ArrayList<String>();
							String[] names = str.split("P");
							for (String name : names) {
								list.add(name);
								// System.out.println(name);
							}
							HashMap<String, Object> lay1_map;
							HashMap<String, Object> lay2_map;

							int tmp = 0;
							for (int i = 0; i < list.size(); i++) {
								// list.get(i);
								lay1_map = new HashMap<String, Object>();
								lay2_map = new HashMap<String, Object>();
								if (list.get(i).length() > 25) {
									// System.out.println(list.get(i).toString());
									lay1_map.put("lay1_Grid", list.get(i)
											.substring(0, 5).toString());
									lay1_map.put("lay1_Value", list.get(i)
											.substring(6, 11).toString());
									lay2_map.put("lay2_Grid", list.get(i)
											.substring(12, 17).toString());
									lay2_map.put("lay2_Value", list.get(i)
											.substring(18, 23).toString());
								}
								System.out.println("lay1_Grid "
										+ lay1_map.get("lay1_Grid")
										+ "  lay1_Value "
										+ lay1_map.get("lay1_Value")
										+ " lay2gird  "
										+ lay2_map.get("lay2_Grid")
										+ " lay2Value "
										+ lay2_map.get("lay2_Value"));

								// 畫上lay1
								if ((lay1_map.get("lay1_Grid") != null)
										& (lay1_map.get("lay1_Value") != null)) {
									// System.out.println("lay1_Grid " +
									// lay1_map.get("lay1_Grid") +
									// "  lay1_Value "
									// + lay1_map.get("lay1_Value"));
									set_lay1_Grid(lay1_map.get("lay1_Grid")
											.toString(),
											lay1_map.get("lay1_Value")
													.toString());
									// 紀錄前一筆 add 1120
									if(save == true && save_list != null){
//										Log.i("log", lay1_map.get("lay1_Grid")
//											.toString()+ lay1_map.get("lay1_Value")
//													.toString());
//										save_list.add(lay1_map.get("lay1_Grid")
//											.toString()+ lay1_map.get("lay1_Value")
//													.toString());
										
									}
									// System.out.println("dddown   "+ dddown
									// +"    uuup   " +uuup );
//									if ((dddown == true) && (uuup == true)) {
//										int temp = Integer.parseInt(""
//												+ lay1_map.get("lay1_Grid")) - 1;
//										int temp_Value = Integer.parseInt(""
//												+ lay1_map.get("lay1_Value"));
//										System.out.println("cl [" + temp
//												+ "]    " + temp_Value);
//										clear1[temp] = temp_Value;
//										// save_del_list.add(lay1_map.get("lay1_Grid")
//										// + " o "+ lay1_map.get("lay1_Value"));
//
//										// System.out.println(lay1_map.get("lay1_Grid")
//										// + " o "+ lay1_map.get("lay1_Value"));
//									}
								}
								// 畫上lay2
								if ((lay2_map.get("lay2_Grid") != null)
										& (lay2_map.get("lay2_Value") != null)) {
									// System.out.println("lay1_Grid " +
									// lay1_map.get("lay1_Grid") +
									// "  lay1_Value "
									// + lay1_map.get("lay1_Value"));
									set_lay1_Grid(lay2_map.get("lay2_Grid")
											.toString(),
											lay2_map.get("lay2_Value")
													.toString());
									if(save == true && save_list != null){
//										Log.i("log2", lay2_map.get("lay2_Grid")
//											.toString()+
//											lay2_map.get("lay2_Value")
//													.toString());
//										save_list.add(lay2_map.get("lay2_Grid")
//											.toString()+
//											lay2_map.get("lay2_Value")
//													.toString());
									}
									// set_lay2_Grid(lay2_map.get("lay2_Grid").toString(),
									// lay2_map.get("lay2_Value").toString());
									// if((dddown == true) && (uuup == true)){
									// clear2[Integer.parseInt(""+lay2_map.get("lay2_Grid"))-1]
									// =
									// Integer.parseInt(""+lay2_map.get("lay2_Value"));
									// //
									// save_del_list.add(lay2_map.get("lay2_Grid")
									// + " o "+ lay2_map.get("lay2_Value"));
									// // System.out.println("ssssssss");
									// //
									// System.out.println(lay1_map.get("lay1_Grid")
									// + " o "+ lay1_map.get("lay1_Value"));
									// }
								}

								// add 1120
								// if(tmp++ < 0){
								// for(int index = 0 ; index <
								// save_del_list.size(); index++){
								//
								// set_lay1_Grid(save_del_list.get(index).substring(0,
								// 4), "1");
								// }
								// }
							}
							// add 1121 clear
							// if(save_del_list != null){
							// for(int index = 0 ; index < save_del_list.size();
							// index++){
							// Log.i("clear",
							// "clear "+save_del_list.get(index).substring(0,
							// 5));
							// set_lay1_Grid(save_del_list.get(index).substring(0,
							// 5), "1");
							// }
							// }

							// 畫上lay1
							// if((lay1_map.get("lay1_Grid") != null) &
							// (lay1_map.get("lay1_Value") != null)){
							// // System.out.println("lay1_Grid " +
							// lay1_map.get("lay1_Grid") + "  lay1_Value "
							// // + lay1_map.get("lay1_Value"));
							// set_lay1_Grid(lay1_map.get("lay1_Grid").toString(),
							// lay1_map.get("lay1_Value").toString());
							// }
							// //畫上lay2
							// if((lay2_map.get("lay2_Grid") != null) &
							// (lay2_map.get("lay2_Value") != null)){
							// // System.out.println("lay1_Grid " +
							// lay1_map.get("lay1_Grid") + "  lay1_Value "
							// // + lay1_map.get("lay1_Value"));
							// set_lay2_Grid(lay2_map.get("lay2_Grid").toString(),
							// lay2_map.get("lay2_Value").toString());
							// }

							// for(int i = 0 ; i < list.size(); i++){
							// System.out.println(str.length());
							// }

							// System.out.println(str.length());
							// System.out.println(str);
							// System.out.println(str.substring(1, 6) + " " +
							// str.substring(7, 12) + " " + str.substring(13,
							// 18) + " " + str.substring(19, 24));

							// lay2_map = new HashMap<String, Object>();

						}
						
						// if (str.substring(0, 1).equals("0")) {
						// List<String> list = new ArrayList<String>();
						//
						// // 將data拆成一筆一筆
						// String[] names = str.split(":");
						// for (String name : names) {
						// String[] names2 = name.split("P[0-9]{3}");
						// for (String name2 : names2) {
						// // System.out.println(name2);
						// list.add(name2);
						//
						// }
						//
						// }
						// // System.out.println(list.size());
						//
						// // 拆 lay1, lay2
						// HashMap<String, Object> lay1_map;
						// HashMap<String, Object> lay2_map;
						//
						// for (int i = 0; i < list.size(); i++) {
						// // System.out.println(list.get(i));
						// String[] array = list.get(i).split("-");
						// lay1_map = new HashMap<String, Object>();
						// lay2_map = new HashMap<String, Object>();
						// int count = 0 ;
						// for (String name : array) {
						// ++count;
						// // System.out.println(name + "  " + count);
						// if(name.substring(0, 1).equals("X") && count == 1){
						// lay1_map.put("lay1_Grid", name.substring(4, 9)
						// .toString());
						// // System.out.print(" lay1格 " +
						// // name.substring(4, 9));
						// }else if(name.substring(0, 1).equals("Y") && count ==
						// 2){
						// lay1_map.put("lay1_Value", name.substring(4, 9)
						// .toString());
						// // System.out.print(" lay1直  " +
						// // name.substring(4, 9));
						// }else if(name.substring(0, 1).equals("X") && count ==
						// 3){
						// lay2_map.put("lay2_Grid", name.substring(4, 9)
						// .toString());
						// // System.out.print(" lay2格 " +
						// // name.substring(4, 9));
						// }else if(name.substring(0, 1).equals("Y") && count ==
						// 4){
						// lay2_map.put("lay2_Value", name.substring(4, 9)
						// .toString());
						// // System.out.print(" lay2值" +
						// // name.substring(4, 9));
						// }
						//
						// }
						// System.out.println();
						// //畫上lay1
						// if((lay1_map.get("lay1_Grid") != null) &
						// (lay1_map.get("lay1_Value") != null)){
						// // System.out.println("lay1_Grid " +
						// lay1_map.get("lay1_Grid") + "  lay1_Value "
						// // + lay1_map.get("lay1_Value"));
						// set_lay1_Grid(lay1_map.get("lay1_Grid").toString(),
						// lay1_map.get("lay1_Value").toString());
						// }
						// //畫上lay2
						// if((lay2_map.get("lay2_Grid") != null) &
						// (lay2_map.get("lay2_Value") != null)){
						// // System.out.println("lay1_Grid " +
						// lay1_map.get("lay1_Grid") + "  lay1_Value "
						// // + lay1_map.get("lay1_Value"));
						// set_lay2_Grid(lay2_map.get("lay2_Grid").toString(),
						// lay2_map.get("lay2_Value").toString());
						// }
						//
						// System.out.println("add " + aaa++);
						//
						// }
						// list = null;
						// lay1_map = null;
						// lay2_map = null;
						//
						// }

					}
					
					str = null;
					// end add 1019

					/*
					 * if (str.equals("")) {
					 * 
					 * } else {
					 * 
					 * // System.out.println(str);
					 * 
					 * if (str.substring(0, 1).equals("P")) { List<String> list
					 * = new ArrayList<String>();
					 * 
					 * // 將data拆成一筆一筆 String[] names = str.split(":"); for
					 * (String name : names) { String[] names2 =
					 * name.split("P[0-9]{3}"); for (String name2 : names2) { //
					 * System.out.println(name2); list.add(name2); }
					 * 
					 * }
					 * 
					 * // 拆-, X, Y HashMap<String, Object> map;
					 * 
					 * for (int i = 0; i < list.size(); i++) { //
					 * System.out.println(list.get(i)); String[] array =
					 * list.get(i).split("-"); map = new HashMap<String,
					 * Object>(); for (String name : array) { //
					 * System.out.println(name + " " + // name.length()); if
					 * (name.substring(0, 1).equals("X")) { map.put("Grid",
					 * name.substring(4, 9) .toString()); //
					 * System.out.println("X" + // name.substring(4, 9));
					 * 
					 * } else if (name.substring(0, 1).equals("Y")) {// Y
					 * map.put("Value", name.substring(4, 9) .toString()); //
					 * System.out.println("Y" + // name.substring(4, 9));
					 * 
					 * }
					 * 
					 * } listItem.add(map);//no push //
					 * System.out.println("add " + aaa++); if((map.get("Grid")
					 * != null) & (map.get("Value") != null)){ //
					 * System.out.println("Grid " + map.get("Grid") + "  Value "
					 * // + map.get("Value"));
					 * setGrid(map.get("Grid").toString(),
					 * map.get("Value").toString()); }
					 * 
					 * map = null; } list = null;
					 * 
					 * // debug get all array // for(int i = 0 ; i <
					 * listItem.size() ; i++){ // HashMap<String, Object> m =
					 * listItem.get(i); // System.out.println("X = " +
					 * m.get("X") + " Y = " + // m.get("Y")); // }
					 * 
					 * } str = null; }
					 */

				} catch (Exception e) {
					Log.e("Error", "Error" + e);
					Toast_msg("程式掛囉!!");
				}
			}

			// Thread t = new Thread((new readTerm()));
			// t.start();

			hd_get_text.removeCallbacks(this);
			hd_get_text.postDelayed(this, 1);

		}
	};

	// add 1018
	private void set_lay1_Grid_2(String Grid, String Value) {
		int position = Integer.parseInt(Grid.substring(3, 5));
		// Log.i("position", "position = "+position);
		// 選格子
		if ((position > 0) && position <= 30) {
			// System.out.println("position" + position);
			// tv_lay1_X_text[position-1].setText("X"+position);
			// tv_lay1_X_text[position-1].setBackgroundColor(Color.RED);
			// lay1_X_bar[position-1].setText(Value);
			// lay1_X_bar[position-1].setProgress(Integer.parseInt(Value)-10000);
			// lay1_X_bar[position-1].setTextColor(Color.GREEN);

			for (int i = 0; i <= 30; i++) {
				// 條件成立 才畫上去
				if ((i + 1) == position) {
					// System.out.println("position" + position);
					// 畫哪幾格

					tv_lay1_X_text[position - 1].setText("X" + position);
					// tv_lay1_X_text[position-1].setBackgroundColor(Color.RED);
					lay1_X_bar[position - 1].setText(Value);
					lay1_X_bar[position - 1].setProgress(Integer
							.parseInt(Value));
					lay1_X_bar[position - 1].setTextColor(Color.RED);

				} else {

					// if(position <= 30){
					// // tv_lay1_X_text[position-1].setText(position);
					// tv_lay1_X_text[i].setBackgroundColor(Color.WHITE);
					// lay1_X_bar[position-1].setText("");
					// lay1_X_bar[position-1].setProgress(0);
					lay1_X_bar[position - 1].setTextColor(Color.GREEN);
					// }else{
					// // tv_lay1_Y_text[position-1].setText(position);
					// tv_lay1_Y_text[position-31].setBackgroundColor(Color.WHITE);
					// lay1_Y_bar[position-31].setText("");
					// lay1_Y_bar[position-31].setProgress(0);
					// lay1_Y_bar[position-31].setTextColor(Color.WHITE);
					// }
				}
			}

		} else if ((position > 30) && (position <= 53)) {
			// Log.i("position = ", position+"");
			for (int i = 31; i <= 53; i++) {
				if (position == i) {

					lay1_Y_bar[position - 31].setText(Value);
					lay1_Y_bar[position - 31].setProgress(Integer
							.parseInt(Value));
					lay1_Y_bar[position - 31].setTextColor(Color.RED);
				} else {
					lay1_Y_bar[position - 31].setTextColor(Color.GREEN);
				}

			}
		}

	}

	// add 1116
	private void set_lay1_Grid(String Grid, String Value) {
		int position = Integer.parseInt(Grid.substring(3, 5));

		// 選格子
		// for(int i = 0 ; i < 53 ; i++){
		// if(clear2 != null && i >30){
		// lay1_Y_bar[i].setText(clear2[i]+"");
		// }
		// }

		if (((position > 0) && position <= 53)) {
			// Log.i("position", "position = "+position);
			for (int i = 1; i <= 53; i++) {
				if (position <= 30) {
					if ((i) == position) {
						// System.out.println("position" + position);
						// 畫哪幾格

						tv_lay1_X_text[position - 1].setText("X" + position);
						// tv_lay1_X_text[position-1].setBackgroundColor(Color.RED);
						lay1_X_bar[position - 1].setText(Value);
						lay1_X_bar[position - 1].setProgress(Integer
								.parseInt(Value));
						lay1_X_bar[position - 1].setTextColor(Color.RED);
						// Log.i("i", "ok = "+position);
					} else {

						if ((i - 1) < 30) {
							// Log.i("i", "i = "+i);
							lay1_X_bar[i - 1].setTextColor(Color.GREEN);
							// if(clear1 != null){
							// lay1_X_bar[i-1].setText(clear1[i]+"");
							// }
						}

					}
				} else if ((position > 30)) {
					if (position == i) {

						lay1_Y_bar[position - 31].setText(Value);
						lay1_Y_bar[position - 31].setProgress(Integer
								.parseInt(Value));
						lay1_Y_bar[position - 31].setTextColor(Color.RED);
						// Log.i("i", "ok = "+position);
					} else {
						if (((i - 1) >= 30) && (i - 1) < 53) {
							// Log.i("i", "i = "+i);
							lay1_Y_bar[i - 31].setTextColor(Color.GREEN);

						}

						// lay1_Y_bar[position-31].setTextColor(Color.GREEN);
					}
				}
			}
		}

	}

	private void set_lay2_Grid(String Grid, String Value) {
		int position = Integer.parseInt(Grid.substring(3, 5));

		// 選格子
		if ((position > 0) && position <= 23) {
			// System.out.println("position" + position);
			// add
			// tv_lay1_Y_text[position-1].setText("Y"+(position));
			// tv_lay1_Y_text[position-1].setBackgroundColor(Color.RED);
			// lay1_Y_bar[position-1].setText(Value);
			// lay1_Y_bar[position-1].setProgress(Integer.parseInt(Value)-10000);
			// lay1_Y_bar[position-1].setTextColor(Color.GREEN);

			for (int i = 0; i < 23; i++) {
				// 條件成立 才畫上去
				if ((i + 1) == position) {
					// System.out.println("position" + position);
					// 畫哪幾格
					tv_lay1_Y_text[position - 1].setText("Y" + (position));
					// tv_lay1_Y_text[position-1].setBackgroundColor(Color.RED);
					lay1_Y_bar[position - 1].setText(Value);
					lay1_Y_bar[position - 1].setProgress(Integer
							.parseInt(Value) - 10000);
					lay1_Y_bar[position - 1].setTextColor(Color.RED);

				} else {

					// if(position <= 30){
					// // tv_lay1_X_text[position-1].setText(position);
					// tv_lay1_X_text[position-1].setBackgroundColor(Color.WHITE);
					// lay1_X_bar[position-1].setText("");
					// lay1_X_bar[position-1].setProgress(0);
					// lay1_X_bar[position-1].setTextColor(Color.WHITE);
					// }else{
					// // tv_lay1_Y_text[position-1].setText(position);
					// tv_lay1_Y_text[i].setBackgroundColor(Color.WHITE);
					// lay1_Y_bar[position-31].setText("");
					// lay1_Y_bar[position-31].setProgress(0);
					lay1_Y_bar[i].setTextColor(Color.GREEN);
					// }
				}
			}

		}

	}

	private void setGrid(String Grid, String Value) {
		int position = Integer.parseInt(Grid.substring(3, 5));

		// 選格子
		if ((position > 0) && position <= 53) {
			// System.out.println("position" + position);
			for (int i = 0; i < 53; i++) {
				// 條件成立 才畫上去
				if ((i + 1) == position) {
					// System.out.println("position" + position);
					// 畫哪幾格
					if (position <= 30) {
						tv_lay1_X_text[position - 1].setText("X" + position);
						tv_lay1_X_text[position - 1]
								.setBackgroundColor(Color.RED);
						lay1_X_bar[position - 1].setText(Value);
						lay1_X_bar[position - 1].setProgress(Integer
								.parseInt(Value) / 10);
						lay1_X_bar[position - 1].setTextColor(Color.RED);
					} else {
						tv_lay1_Y_text[position - 31].setText("Y"
								+ (position - 30));
						tv_lay1_Y_text[position - 31]
								.setBackgroundColor(Color.RED);
						lay1_Y_bar[position - 31].setText(Value);
						lay1_Y_bar[position - 31].setProgress(Integer
								.parseInt(Value) / 10);
						lay1_Y_bar[position - 31].setTextColor(Color.RED);
					}
				} else {

					// if(position <= 30){
					// // tv_lay1_X_text[position-1].setText(position);
					// tv_lay1_X_text[position-1].setBackgroundColor(Color.WHITE);
					// lay1_X_bar[position-1].setText("");
					// lay1_X_bar[position-1].setProgress(0);
					// lay1_X_bar[position-1].setTextColor(Color.WHITE);
					// }else{
					// // tv_lay1_Y_text[position-1].setText(position);
					// tv_lay1_Y_text[position-31].setBackgroundColor(Color.WHITE);
					// lay1_Y_bar[position-31].setText("");
					// lay1_Y_bar[position-31].setProgress(0);
					// lay1_Y_bar[position-31].setTextColor(Color.WHITE);
					// }
				}
			}

		}

	}

	private void onDraw_X(String pos, String str) {
		int position = Integer.parseInt(pos);
		if (position <= 30) {
			lay1_X_bar[position - 1].setText(str);
			lay1_X_bar[position - 1].setProgress(Integer.parseInt(str) / 10);
			lay1_X_bar[position - 1].setTextColor(Color.RED);
			tv_lay1_X_text[position - 1].setBackgroundColor(Color.RED);
		}

		System.out.println(pos);

	}

	private void onDraw_Y(String pos, String str) {
		int position = Integer.parseInt(pos);
		if (position <= 23) {
			lay1_Y_bar[position - 1].setText(str);
			lay1_Y_bar[position - 1].setProgress(Integer.parseInt(str) / 10);
			lay1_Y_bar[position - 1].setTextColor(Color.RED);
			tv_lay1_Y_text[position - 1].setBackgroundColor(Color.RED);
		}
	}

	private void onDraw() {

	}

	// end 1018

	private void populateViewFlipper() {
		if (mTermService != null) {
			mTermSessions = mTermService.getSessions(getFilesDir());

			// if (mTermSessions.size() == 0) {
			// mTermSessions.add(createTermSession());
			// }

			for (TermSession session : mTermSessions) {
				EmulatorView view = createEmulatorView(session);
				mViewFlipper.addView(view);
			}

			updatePrefs();
		}

		// Set back to ESC

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mViewFlipper.removeAllViews();
		unbindService(mTSConnection);

		// stopService(TSIntent);

		mTermService = null;
		mTSConnection = null;

		// if (mWakeLock.isHeld()) {
		// mWakeLock.release();
		// }
		// if (mWifiLock.isHeld()) {
		// mWifiLock.release();
		// }
		System.exit(0);
	}

	private void restart() {
		startActivity(getIntent());
		finish();
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
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

	/*
	 * private TermSession createTermSession() { String HOME =
	 * getFilesDir().getPath(); String APK = getPackageResourcePath(); String IP
	 * = getLocalIpAddress(); if(IP == null){ IP = "127.0.0.1"; }
	 * 
	 * String initialCommand =
	 * "export HOME="+HOME+";cd $HOME;~/system/init "+HOME+" "+APK+" "+IP; //
	 * String initialCommand = "export HOME="+HOME+";cd $HOME";
	 * 
	 * 
	 * return new TermSession(this,mSettings, null, initialCommand); }
	 */

	private EmulatorView createEmulatorView(TermSession session) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		EmulatorView emulatorView = new EmulatorView(this, session,
				mViewFlipper, metrics);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
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

		if (mTermSessions != null
				&& mTermSessions.size() < mViewFlipper.getChildCount()) {
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
		// getMenuInflater().inflate(R.menu.main, menu);
		Log.i("mmmmmmmmmmmmm", "mmmmmmmmmm");
		return true;
	}

	private void doCreateNewWindow() {
		if (mTermSessions == null) {
			Log.w(TermDebug.LOG_TAG,
					"Couldn't create new window because mTermSessions == null");
			return;
		}

		// TermSession session = createTermSession();
		// mTermSessions.add(session);
		// EmulatorView view = createEmulatorView(session);
		// view.updatePrefs(mSettings);
		// mViewFlipper.addView(view);
		// mViewFlipper.setDisplayedChild(mViewFlipper.getChildCount()-1);
	}

	private void doCopyAll() {
		ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clip.setText(getCurrentTermSession().getTranscriptText().trim());
	}

	private void doPaste() {
		ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if (!clip.hasText()) {
			Toast tt = Toast.makeText(this, "No text to Paste..",
					Toast.LENGTH_SHORT);
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
		// Is BACK ESC
		// Log.v("Terminal IDE","TERM : onkeyDown code:"+keyCode+" flags:"+event.getFlags()+" meta:"+event.getMetaState());

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mTermService.isBackESC()) {
				// Log.v("SpartacusRex","TERM : ESC sent instead of back.!");
				// Send the ESC sequence..
				int ESC = TermKeyListener.KEYCODE_ESCAPE;
				getCurrentEmulatorView().dispatchKeyEvent(
						new KeyEvent(KeyEvent.ACTION_DOWN, ESC));
				getCurrentEmulatorView().dispatchKeyEvent(
						new KeyEvent(KeyEvent.ACTION_UP, ESC));
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			Log.i("clear", "clear");
			// Handler hd = new Handler();
			// hd.postDelayed(clearn, 1);
			this.clear_data = true;
			// Thread mThread = new Thread(clearn);
			// mThread.start();

		}

		return super.onKeyDown(keyCode, event);
	}

	final int check_clearn = 1;
	public boolean clear_data = false;
	private Runnable clearn = new Runnable() {
		public void run() {
			mHandler.obtainMessage(check_clearn).sendToTarget();
		}
	};
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case check_clearn:

				// for(int i = 0 ; i < tv_lay1_X_text.length ; i++){
				// // Log.i("thread ", "for" + i);
				// lay1_X_bar[i].setText("");
				//
				//
				// lay1_X_bar[i].setTextColor(Color.WHITE);
				// if(i < tv_lay1_Y_text.length){
				//
				// lay1_Y_bar[i].setText("");
				// lay1_Y_bar[i].setTextColor(Color.WHITE);
				// }
				// }
				break;
			default:

			}
		}
	};

}
