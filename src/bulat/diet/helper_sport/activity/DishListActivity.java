package bulat.diet.helper_sport.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dm.zbar.android.scanner.ZBarScannerActivity;





import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import bulat.diet.helper_sport.R;
import bulat.diet.helper_sport.adapter.DishAdapter;
import bulat.diet.helper_sport.adapter.DishArrayAdapter;
import bulat.diet.helper_sport.db.DishListHelper;
import bulat.diet.helper_sport.item.Dish;
import bulat.diet.helper_sport.item.DishType;
import bulat.diet.helper_sport.utils.Constants;
import bulat.diet.helper_sport.utils.NetworkState;
import bulat.diet.helper_sport.utils.SaveUtils;

public class DishListActivity extends BaseActivity {

	private static final String URL = Constants.URL_DISHBASE;
	private static final String URL_BARCOD = Constants.URL_BARCOD;
	protected static final int DIALOG_TYPE_NAME = 0;
	protected String selectedInemId;
	ListView dishesList;
	Spinner typeSpinner;
	String currDate;
	String id = null;
	TextView header;
	Cursor c;
	protected boolean templateFlag = false;
	DishListActivityGroup parent = null;
	private String parentName;
	private EditText searchEditText;
	private String searchString;
	private Button clearButton;
	private LinearLayout spinerLayot;
	private InputMethodManager imm;
	ArrayList<Dish> list = new ArrayList<Dish>();
	View viewToLoad;
	private RelativeLayout serchLayout;
	private TextView loadingView;
	private TextView badSearchView;
	private LinearLayout emptyLayout;
	private LinearLayout tabLayout;
	private boolean localSerch = false;
	private String recepyId = null;
	private Button imageVoice;
	private Button imageBar;
	public static final int ZBAR_SCANNER_REQUEST = 2;
	public static final int ZBAR_QR_SCANNER_REQUEST = 3;
	
	static {
	    System.loadLibrary("iconv");
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		viewToLoad = LayoutInflater.from(this.getParent()).inflate(
				R.layout.dish_list, null);
		header = (TextView) viewToLoad.findViewById(R.id.textViewTitle);
		spinerLayot = (LinearLayout) viewToLoad.findViewById(R.id.spinerLayout);
		serchLayout = (RelativeLayout) viewToLoad
				.findViewById(R.id.searchLayout);
		tabLayout = (LinearLayout) viewToLoad.findViewById(R.id.tabLayout);
		emptyLayout = (LinearLayout) viewToLoad.findViewById(R.id.emptyLayout);
		loadingView = (TextView) viewToLoad.findViewById(R.id.textViewLoading);
		badSearchView = (TextView) viewToLoad
				.findViewById(R.id.textViewBadSearch);
		searchEditText = (EditText) viewToLoad
				.findViewById(R.id.editTextSearch);
		searchEditText.setOnEditorActionListener(searchEditorListener);
		searchEditText.addTextChangedListener(searchEditTextWatcher);
		imageVoice = (Button) viewToLoad.findViewById(R.id.imageVoice);
		imageVoice.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				searchEditText.setText("");
				
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				 
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				
				String lang = SaveUtils.getLang(DishListActivity.this);
				if(lang.length()<1){
					lang = "ru-RU";
				}
		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, lang);
		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang); 
		        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, lang);
		        try {
		            getParent().startActivityForResult(intent, 1);                
		        } catch (ActivityNotFoundException a) {
		            Toast t = Toast.makeText(getApplicationContext(),
		            		getParent().getString(R.string.voice_error),
		                        Toast.LENGTH_SHORT);
		            t.show();

		        }
				
			}
		});
		imageBar = (Button) viewToLoad.findViewById(R.id.imageBarcode);
		imageBar.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (isCameraAvailable()) {
		            Intent intent = new Intent(DishListActivity.this, ZBarScannerActivity.class);
		            getParent().startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
		        } else {
		            Toast.makeText(DishListActivity.this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
		        }
			}
		});
		
		clearButton = (Button) viewToLoad.findViewById(R.id.buttonClear);
		clearButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				searchEditText.setText("");
			}
		});
		Bundle extras = getIntent().getExtras();
		parentName = null;
		if (extras != null) {
			currDate = extras.getString(DishActivity.DATE);
			id = extras.getString(AddTodayDishActivity.ID);
			
			recepyId  = extras.getString(RecepyActivity.ID);
			
			templateFlag = extras.getBoolean(NewTemplateActivity.TEMPLATE);
			parentName = extras.getString(DishActivity.PARENT_NAME);
		}
		if (parentName == null) {
			if (DishListActivityGroup.class.getName().equals(
					getParent().getClass().getName())) {
				try {
					parent = (DishListActivityGroup) getParent();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
		Button exitButton = (Button) viewToLoad.findViewById(R.id.buttonExit);
		exitButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				System.exit(0);
			}
		});
		Button sportTab = (Button) viewToLoad.findViewById(R.id.sportTab);
		sportTab.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					Intent intent = new Intent();
					intent.setClass(getParent(), SportListActivity.class);
					DishListActivityGroup activityStack = (DishListActivityGroup) getParent();
					activityStack.push("SportListActivity", intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Button recepyTab= (Button) viewToLoad.findViewById(R.id.myDishTab);
		recepyTab.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					Intent intent = new Intent();					
					intent.setClass(getParent(), RecepyListActivity.class);					
					DishListActivityGroup activityStack = (DishListActivityGroup) getParent();
					activityStack.push("recepyListActivity", intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Button backButton = (Button) viewToLoad.findViewById(R.id.buttonBack);
		Button addButton = (Button) viewToLoad.findViewById(R.id.buttonAdd);
		Button addTypeButton = (Button) viewToLoad
				.findViewById(R.id.buttonAddType);
		RelativeLayout addTypeButtonLayout = (RelativeLayout) viewToLoad
				.findViewById(R.id.addTypeLayout);
		if (parent != null) {
			addButton.setVisibility(View.VISIBLE);
			imageBar.setVisibility(View.GONE);
			tabLayout.setVisibility(View.VISIBLE);
			addTypeButtonLayout.setVisibility(View.VISIBLE);
			localSerch  = true;
		} else {
			imageBar.setVisibility(View.VISIBLE);
			backButton.setVisibility(View.VISIBLE);
			exitButton.setVisibility(View.GONE);
			serchLayout.setVisibility(View.VISIBLE);
		}
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});

		addButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent();
				try {
					intent.putExtra(AddTodayDishActivity.ADD, 1);
					intent.setClass(getParent(), AddDishActivity.class);
					intent.putExtra(AddTodayDishActivity.TITLE,
							getString(R.string.add_dish));
					intent.putExtra(AddDishActivity.DISH_TYPE,
							(int)typeSpinner.getSelectedItemId());
					DishListActivityGroup activityStack = (DishListActivityGroup) getParent();
					activityStack.push("AddDishActivity", intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		addTypeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showDialog(DIALOG_TYPE_NAME);
			}
		});
		setContentView(viewToLoad);
	}
	public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

	public void showingDialogCancel(String id) {
		selectedInemId = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
		builder.setMessage(R.string.remove_dialog)
				.setPositiveButton(getString(R.string.yes), dialogClickListener)
				.setNegativeButton(getString(R.string.no), dialogClickListener)
				.show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (c != null)
			c.close();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (c != null)
			c.close();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 
	    Locale locale = new Locale(SaveUtils.getLang(this));
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				      getBaseContext().getResources().getDisplayMetrics());

		imm = (InputMethodManager) DishListActivity.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		String header_text = getString(R.string.dish_list);
		setSpiner();
		header.setText(header_text);
		if (typeSpinner.getSelectedItem() != null) {
			c = DishListHelper.getDishesByCategory(getApplicationContext(),
					((DishType) typeSpinner.getSelectedItem()).getTypeKey());
			if (c != null) {
				try {
					DishAdapter da = new DishAdapter(this,
							getApplicationContext(), c, parent);
					dishesList = (ListView) findViewById(R.id.listViewDishes);
					dishesList.setAdapter(da);
				} catch (Exception e) {
					e.printStackTrace();

				} finally {
					c.close();
				}
			}
			dishesList.setOnItemClickListener(dishesListOnItemClickListener);
		}
		if(!"".equals(Constants.ST)){
			searchEditText.setText(Constants.ST);
			Constants.ST = "";
			runFoundTask();
		}
	}
	void runFoundTask(){
		Long bar=(long) 0;
		try{
			bar = Long.decode(searchEditText.getText().toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		if(bar > 1000 ){
			new SetFoundTask(true).execute();
		}else{
			new SetFoundTask(false).execute();
		}
	}
	
	private void setSpiner() {
		// TODO Auto-generated method stub
		typeSpinner = (Spinner) findViewById(R.id.dihs_type_spinner);
		ArrayList<DishType> types = new ArrayList<DishType>();
		if ( DishListHelper.getPopularDishesCount(this)>5){
			types.add(new DishType(1000, getString(R.string.popular)));
		}
		if (parentName == null) {
			types.addAll(DishListHelper.getAllDishCategories(this));
		} else {
			types.addAll(DishListHelper.getUsingDishCategories(this));
		}

		ArrayAdapter<DishType> adapter = new ArrayAdapter<DishType>(this,
				android.R.layout.simple_spinner_item, types);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(adapter);
		typeSpinner.setSelection(0);
		typeSpinner.setOnItemSelectedListener(spinnerListener);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			c.close();
			c = DishListHelper.getDishesByCategory(getApplicationContext(),
					((DishType) typeSpinner.getSelectedItem()).getTypeKey());
			if (c != null) {
				try {
					DishAdapter da = new DishAdapter(DishListActivity.this,
							getApplicationContext(), c, parent);
					// dishesList = (ListView)
					// viewToLoad.findViewById(R.id.listViewDishes);
					dishesList.setAdapter(da);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	};

	private OnItemClickListener dishesListOnItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
			Dish temp = null;
			
			Intent intent = new Intent();
			TextView name = (TextView) v.findViewById(R.id.textViewDishName);
			TextView idView = (TextView) v.findViewById(R.id.textViewId);
			TextView typeView = (TextView) v.findViewById(R.id.textViewDishType);
			
			try {
				DishListHelper
						.incDishPopularity(idView.getText().toString(), DishListActivity.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			TextView calorisity = (TextView) v
					.findViewById(R.id.textViewCaloricity);
			TextView fat = (TextView) v.findViewById(R.id.textViewFat);
			TextView carbon = (TextView) v.findViewById(R.id.textViewCarbon);
			TextView protein = (TextView) v.findViewById(R.id.textViewProtein);
			TextView balancetypeView = (TextView) v
					.findViewById(R.id.textViewDishType);
			intent.putExtra(AddTodayDishActivity.DISH_NAME, name.getText());
			// adding in local db
			if ("0".equals(idView.getText())) {
				temp = (Dish) dishesList.getAdapter().getItem(arg2);
				temp.setId(String.valueOf(DishListHelper.addNewDish(temp,
						DishListActivity.this)));
			}
			if (id != null) {
				intent.putExtra(AddTodayDishActivity.TITLE,
						getString(R.string.edit_today_dish));
				intent.putExtra(AddTodayDishActivity.ID, id);
			} else {
				intent.putExtra(AddTodayDishActivity.TITLE,
						getString(R.string.add_today_dish));
				intent.putExtra(AddTodayDishActivity.ADD, 1);
			}
			intent.putExtra(AddTodayDishActivity.DISH_CALORISITY,
					Integer.valueOf(calorisity.getText().toString()));
			intent.putExtra(AddTodayDishActivity.DISH_FAT, fat.getText()
					.toString());
			intent.putExtra(AddTodayDishActivity.DISH_CARBON, carbon.getText()
					.toString());
			intent.putExtra(AddTodayDishActivity.DISH_PROTEIN, protein
					.getText().toString());
			intent.putExtra(AddTodayDishActivity.DISH_CATEGORY,
					((DishType) typeSpinner.getSelectedItem()).getTypeKey());
			intent.putExtra(AddTodayDishActivity.DISH_STAT_TYPE, typeView
					.getText().toString());
			intent.putExtra(DishActivity.DATE, currDate);
			intent.putExtra(DishActivity.PARENT_NAME, parentName);

			if (CalendarActivityGroup.class.getName().equals(
					getParent().getClass().getName())) {
				intent.putExtra(NewTemplateActivity.TEMPLATE, templateFlag);
				intent.setClass(getParent(), AddTodayDishActivity.class);
				CalendarActivityGroup activityStack = (CalendarActivityGroup) getParent();
				activityStack.push("AddTodayDishActivityCalendar", intent);
			} else if (DishActivityGroup.class.getName().equals(
					getParent().getClass().getName())) {
				try {
					/*
					 * if (!"0".equals(idView.getText().toString())) {
					 * DishListHelper.incDishPopularity(idView.getText()
					 * .toString(), DishListActivity.this); } else { if (temp !=
					 * null) { DishListHelper.incDishPopularity(temp.getId(),
					 * DishListActivity.this); } }
					 */
					intent.setClass(getParent(), AddTodayDishActivity.class);
					DishActivityGroup activityStack = (DishActivityGroup) getParent();
					activityStack.push("AddTodayDishActivity", intent);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (DishListActivityGroup.class.getName().equals(
					getParent().getClass().getName()) && !"Recepy".equals(parentName)) {
				try {
					intent.putExtra(AddTodayDishActivity.ID, idView.getText()
							.toString());
					intent.putExtra(AddTodayDishActivity.ADD, 0);
					intent.putExtra(AddTodayDishActivity.TITLE,
							getString(R.string.edit_dish));
					intent.putExtra(AddTodayDishActivity.DISH_FAT, fat
							.getText().toString());
					intent.putExtra(AddTodayDishActivity.DISH_CARBON, carbon
							.getText().toString());
					intent.putExtra(AddTodayDishActivity.DISH_PROTEIN, protein
							.getText().toString());
					intent.putExtra(AddDishActivity.DISH_TYPE,
							((int)typeSpinner.getSelectedItemId())
							);
					intent.putExtra(AddDishActivity.DISH_BALANCETYPE,
							(balancetypeView.getText().toString())
							);
					intent.setClass(getParent(), AddDishActivity.class);
					DishListActivityGroup activityStack = (DishListActivityGroup) getParent();
					activityStack.push("AddDishActivity", intent);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}else if (DishListActivityGroup.class.getName().equals(
					getParent().getClass().getName()) && "Recepy".equals(parentName)) {
				try {
					intent.putExtra(NewTemplateActivity.TEMPLATE, templateFlag);
					intent.putExtra(RecepyActivity.ID, recepyId);
					intent.setClass(getParent(), AddTodayDishActivity.class);					
					DishListActivityGroup activityStack = (DishListActivityGroup) getParent();
					activityStack.push("AddDishActivity", intent);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
	};

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				try {
					DishListHelper.removeDish(selectedInemId,
							DishListActivity.this);
					selectedInemId = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				selectedInemId = null;
				break;
			}
		}
	};

	private OnEditorActionListener searchEditorListener = new OnEditorActionListener() {

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				searchString = searchEditText.getText().toString().trim();
				if(localSerch){
					c.close();
					c = DishListHelper.searchInAll(getApplicationContext(),searchString.toLowerCase());
					if (c != null) {
						try {
							DishAdapter da = new DishAdapter(DishListActivity.this,
									getApplicationContext(), c, parent);
							// dishesList = (ListView)
							// viewToLoad.findViewById(R.id.listViewDishes);
							dishesList.setAdapter(da);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}else{
					runFoundTask();
				}
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return true;
			}
			return false;
		}

	};
	protected int couter_2;
	protected Object lastSearchString;
	private TextWatcher searchEditTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
			searchString = searchEditText.getText().toString().trim();
			if (!searchString.equals(lastSearchString)) {
				lastSearchString = searchString;
				if (searchString.length() == 0) {
					emptyLayout.setVisibility(View.GONE);
					badSearchView.setVisibility(View.GONE);
					loadingView.setVisibility(View.GONE);
					spinerLayot.setVisibility(View.VISIBLE);
					if (typeSpinner != null) {
						typeSpinner.getOnItemSelectedListener().onItemSelected(
								null, null, 0, 0);
					}
					clearButton.setVisibility(View.INVISIBLE);
					clear();
				} else {
					spinerLayot.setVisibility(View.GONE);
					clearButton.setVisibility(View.VISIBLE);
				}
				if (searchString.length() >= 3
						&& searchString.length() % 2 == 1) {
					if(localSerch){
						c.close();
						c = DishListHelper.searchInAll(getApplicationContext(),searchString.toLowerCase());
						if (c != null) {
							try {
								DishAdapter da = new DishAdapter(DishListActivity.this,
										getApplicationContext(), c, parent);
								// dishesList = (ListView)
								// viewToLoad.findViewById(R.id.listViewDishes);
								dishesList.setAdapter(da);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}else{
						runFoundTask();
					}					
					couter_2++;
				}
			}
		}
	};
	public int count;
	private EditText typeName;

	private void clear() {
		searchString = "";
	}

	private class SetFoundTask extends AsyncTask<Void, Void, Void> {
		boolean barmod = false;
		public SetFoundTask (boolean mode ){
			super();
			barmod = mode;
			
		}
		@Override
		protected void onPreExecute() {
			emptyLayout.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			boolean remoteSearchFlag = true;
			String temp = searchString;			
			if(barmod){
				list = DishListHelper.searchInAllBar(searchString.toLowerCase(), getParent());
			}else{
				list = DishListHelper.searchInAll(searchString.toLowerCase(), getParent());
			}
			try {
				searchString = URLEncoder.encode(searchString, "UTF-8");

			Set<Dish> unicDish = new TreeSet<Dish>();			
			unicDish.addAll(list);
			/*if(!unicDish.isEmpty()){
				for (Dish dish : unicDish) {
					if(dish.getName().toLowerCase().equals(temp.toLowerCase())){
						unicDish.clear();
						unicDish.add(dish);
						remoteSearchFlag = false;
						list = new ArrayList<Dish>(unicDish);
						break;
					}
					
				}
			}*/
			if (NetworkState.isOnline(getApplicationContext())) {
				count++;

				StringBuilder builder = new StringBuilder();
				HttpClient client = new DefaultHttpClient();
				searchString = searchString.replaceAll(" ", "%20");
				
				HttpGet httpGet = null;
				if(barmod){
					httpGet = new HttpGet(URL_BARCOD + "?name=" + searchString);
				}else{
					httpGet = new HttpGet(URL + "?name=" + searchString+ "&full=1"+ "&lang=" + SaveUtils.getLang(DishListActivity.this));
				}
				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established.
				// The default value is zero, that means the timeout is not used. 
				int timeoutConnection = 3000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT) 
				// in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 5000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
				try {
					HttpResponse response = client.execute(httpGet);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
					} else {

					}
					String resultString = builder.toString();
					try {
						JSONObject jsonRoot = new JSONObject(resultString);
						JSONArray jsonArray = new JSONArray(
								jsonRoot.getString("dishes"));
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							Dish d = new Dish(jsonObject.getString("name"),
									jsonObject.getString("name"),
									(int)Float.parseFloat(jsonObject
											.getString("caloric")),
									Integer.parseInt(jsonObject
											.getString("category_id")), 0, 0,
									jsonObject.getString("type"),
									jsonObject.getString("fat").replace(',',
											'.'), jsonObject
											.getString("carbon").replace(',',
													'.'), jsonObject.getString(
											"protein").replace(',', '.'));
							unicDish.add(d);
						}
						list = new ArrayList<Dish>(unicDish);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// return builder.toString();
			}
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setAdapter();
		}

	}

	private void setAdapter() {
		if (!NetworkState.isOnline(getApplicationContext())) {
			ImageView conection = (ImageView) viewToLoad.findViewById(R.id.imageConection);
			conection.setVisibility(View.VISIBLE);
		}else{
			ImageView conection = (ImageView) viewToLoad.findViewById(R.id.imageConection);
			conection.setVisibility(View.GONE);
		}
		emptyLayout.setVisibility(View.GONE);
		loadingView.setVisibility(View.GONE);
		markUnvalidDishes(list);
		if (list.size() > 0) {
			badSearchView.setVisibility(View.GONE);
			try {
				DishArrayAdapter da = new DishArrayAdapter(
						getApplicationContext(), R.layout.dish_list_row, list);
				dishesList.setAdapter(da);
				da.notifyDataSetChanged();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LinearLayout emptyHeader = (LinearLayout)findViewById(R.id.linearLayoutEmptyListHeader);
			emptyHeader.setVisibility(View.VISIBLE);
			emptyHeader.setOnClickListener(addDishListener);
			emptyLayout.setVisibility(View.VISIBLE);
			badSearchView.setVisibility(View.VISIBLE);
		}

	}

	public void markUnvalidDishes(ArrayList<Dish> list2) {
		/*for (Iterator<Dish> iterator = list2.iterator(); iterator.hasNext(); ) {
			Dish dish = iterator.next();
			Float delta = Float.valueOf(dish.getCarbonStr())*4 + Float.valueOf(dish.getFatStr())*9 + Float.valueOf(dish.getProteinStr())*4 - dish.getCaloricity();
			if (delta > 5 || delta < -5  ) {
				iterator.remove();
			}
		}	 */ 
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		Button buttonOk;
		Button nobutton;
		switch (id) {

		case DIALOG_TYPE_NAME:
			dialog = new Dialog(this.getParent());
			dialog.setContentView(R.layout.user_name_dialog);
			dialog.setTitle(R.string.types);

			typeName = (EditText) dialog.findViewById(R.id.editTextUserName);
			TextView maintext = (TextView) dialog
					.findViewById(R.id.textViewSerchActivityLevelLabel);
			maintext.setText(getText(R.string.type_save));
			TextView nametext = (TextView) dialog
					.findViewById(R.id.textViewNameLabel);
			nametext.setText(getText(R.string.type_name));
			buttonOk = (Button) dialog.findViewById(R.id.buttonYes);
			buttonOk.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					if (typeName.getText().toString().trim().length() < 1) {
						typeName.setBackgroundColor(Color.RED);
					} else {
						DishListHelper.addType(typeName.getText().toString()
								.trim(), DishListActivity.this);
						setSpiner();
						dialog.cancel();
					}

				}
			});
			nobutton = (Button) dialog.findViewById(R.id.buttonNo);
			nobutton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					dialog.cancel();
				}
			});

			break;
		default:
			dialog = null;
		}

		return dialog;
	}
	private OnClickListener addDishListener = new OnClickListener() {
		
		public void onClick(View v) {
			Intent intent = new Intent();
			try {
				intent.putExtra(AddTodayDishActivity.ADD, 1);
				intent.setClass(getParent(), AddDishActivity.class);
				intent.putExtra(AddTodayDishActivity.TITLE,
						getString(R.string.add_dish));
				Long bar=(long) 0;
				try{
					bar = Long.decode(searchEditText.getText().toString());
				}catch(Exception e){
					e.printStackTrace();
				}
				if(bar > 1000 ){
					intent.putExtra(AddTodayDishActivity.BARCODE, searchEditText.getText().toString());
				}
				intent.putExtra(AddDishActivity.DISH_TYPE,
						(int)typeSpinner.getSelectedItemId());
				DishActivityGroup activityStack = (DishActivityGroup) getParent();
				activityStack.push("AddDishActivity", intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
