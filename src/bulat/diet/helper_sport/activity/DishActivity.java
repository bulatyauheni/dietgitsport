package bulat.diet.helper_sport.activity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import bulat.diet.helper_sport.R;
import bulat.diet.helper_sport.adapter.TodayDishAdapter;
import bulat.diet.helper_sport.db.DishProvider;
import bulat.diet.helper_sport.db.TemplateDishHelper;
import bulat.diet.helper_sport.db.TodayDishHelper;
import bulat.diet.helper_sport.item.DishType;
import bulat.diet.helper_sport.item.TodayDish;
import bulat.diet.helper_sport.utils.IabHelper;
import bulat.diet.helper_sport.utils.IabResult;
import bulat.diet.helper_sport.item.Inventory;
import bulat.diet.helper_sport.item.Purchase;
import bulat.diet.helper_sport.utils.SaveUtils;
import bulat.diet.helper_sport.utils.SocialUpdater;

public class DishActivity extends BaseActivity {
	public static final String DATE = "date";
	public static final String PARENT_NAME = "parentname";
	public static final String BACKBTN = "backbtn";
	private static final int DIALOG_TEMPLATE_NAME = 0;
	// SKU for our subscription ()

	private Spinner templateSpinner;
	String curentDateandTime;
	ListView dishesList;
	Cursor c;
	Intent copyIntent = new Intent();
	TextView header;
	String parentName = null;
	int sum;
	int sumLoose;
	float sumF;
	float sumC;
	float sumP;
	private DishActivityGroup parentDishActivity;
	private CalendarActivityGroup parentCalendarActivity;
	private boolean flagLoad = false;
	TodayDishAdapter da;

	@Override
	protected void onResume() {
		// colors
		// main FFF0E5
		// header FF9730
		// title FFF6EF
		super.onResume();
		// Listener that's called when we finish querying the items and
		// subscriptions we own
		templateSpinner = (Spinner) findViewById(R.id.template_spinner);
		loadTemplates();

		Bundle extras = getIntent().getExtras();
		String date = null;
		parentName = DishActivityGroup.class.toString();
		if (extras != null) {
			date = extras.getString(DATE);
			parentName = extras.getString(PARENT_NAME);
		}

		Locale locale = new Locale(SaveUtils.getLang(this));
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		SimpleDateFormat title = new SimpleDateFormat("dd MMMM", new Locale(
				SaveUtils.getLang(this)));
		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMMM", new Locale(
				SaveUtils.getLang(this)));
		if (date == null) {

			curentDateandTime = sdf.format(new Date());
		} else {
			curentDateandTime = date;
		}

		try {
			header.setText(date == null ? title.format(new Date()) : title
					.format(sdf.parse(date)));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		c = TodayDishHelper.getDishesByDate(getApplicationContext(),
				curentDateandTime);
		if (c != null) {
			try {
				if (CalendarActivityGroup.class.toString().equals(parentName)) {
					da = new TodayDishAdapter(getApplicationContext(), c,
							(CalendarActivityGroup) getParent());
				} else {
					da = new TodayDishAdapter(getApplicationContext(), c,
							(DishActivityGroup) getParent());
				}

				dishesList = (ListView) findViewById(R.id.listViewTodayDishes);
				dishesList.setAdapter(da);
				dishesList.setItemsCanFocus(true);

				dishesList
						.setOnItemLongClickListener(new OnItemLongClickListener() {

							public boolean onItemLongClick(AdapterView<?> arg0,
									View arg1, int pos, long id2) {
								try {
									copyIntent = new Intent();
									TextView id = (TextView) arg1
											.findViewById(R.id.textViewId);
									TodayDish dishForCopy = TodayDishHelper
											.getDishById(id.getText()
													.toString(),
													DishActivity.this);
									TextView name = (TextView) arg1
											.findViewById(R.id.textViewDishName);
									TextView time = (TextView) arg1
											.findViewById(R.id.textViewTime);
									TextView timeHH = (TextView) arg1
											.findViewById(R.id.textViewTimeHH);
									TextView timeMM = (TextView) arg1
											.findViewById(R.id.textViewTimeMM);
									TextView fat = (TextView) arg1
											.findViewById(R.id.textViewFatTrue);
									TextView carbon = (TextView) arg1
											.findViewById(R.id.textViewCarbonTrue);
									TextView protein = (TextView) arg1
											.findViewById(R.id.textViewProteinTrue);
									TextView calorisity = (TextView) arg1
											.findViewById(R.id.textViewCaloricity);
									TextView absCalorisity = (TextView) arg1
											.findViewById(R.id.textViewAbsCaloricity);
									TextView fatAbsView = (TextView) arg1
											.findViewById(R.id.textViewFat);
									TextView weight = (TextView) arg1
											.findViewById(R.id.textViewWeight);

									TextView carbonAbsView = (TextView) arg1
											.findViewById(R.id.textViewCarbon);
									copyIntent.putExtra(
											AddTodayDishActivity.TITLE,
											getString(R.string.add_today_dish));
									copyIntent.putExtra(
											AddTodayFitnesActivity.ADD, 1);
									copyIntent.putExtra(DishActivity.DATE,
											curentDateandTime);
									copyIntent.putExtra(
											AddTodayDishActivity.DISH_NAME,
											name.getText().toString());
									copyIntent.putExtra(
											AddTodayDishActivity.DISH_CATEGORY,
											Integer.parseInt(dishForCopy
													.getCategory()));
									copyIntent
											.putExtra(
													AddTodayDishActivity.DISH_STAT_TYPE,
													dishForCopy.getType());
									copyIntent.putExtra(
											AddTodayDishActivity.DISH_NAME,
											name.getText().toString());
									copyIntent
											.putExtra(
													AddTodayDishActivity.DISH_CALORISITY,
													Integer.valueOf(calorisity
															.getText()
															.toString()));
									copyIntent.putExtra(
											AddTodayDishActivity.DISH_FAT, fat
													.getText().toString());
									copyIntent.putExtra(
											AddTodayDishActivity.DISH_CARBON,
											carbon.getText().toString());
									copyIntent.putExtra(
											AddTodayDishActivity.DISH_PROTEIN,
											protein.getText().toString());
									copyIntent
											.putExtra(
													AddTodayDishActivity.DISH_ABSOLUTE_CALORISITY,
													Integer.valueOf(absCalorisity
															.getText()
															.toString()));
									copyIntent.putExtra(
											AddTodayDishActivity.ID,
											id.getText());
									if (time.getText() != null) {
										copyIntent.putExtra(
												AddTodayDishActivity.DISH_TIME,
												time.getText());
									}
									if (timeHH.getText() != null) {
										copyIntent
												.putExtra(
														AddTodayDishActivity.DISH_TIME_HH,
														timeHH.getText());
									}
									if (timeMM.getText() != null) {
										copyIntent
												.putExtra(
														AddTodayDishActivity.DISH_TIME_MM,
														timeMM.getText());
									}
									if (Integer.valueOf(absCalorisity.getText()
											.toString()) >= 0) {
										copyIntent.setClass(DishActivity.this,
												AddTodayDishActivity.class);
										copyIntent
												.putExtra(
														AddTodayDishActivity.DISH_WEIGHT,
														Integer.valueOf(weight
																.getText()
																.toString()));
									} else {
										copyIntent.putExtra(
												AddFitnesActivity.FITNES_WEY,
												carbon.getText().toString());
										copyIntent
												.putExtra(
														AddFitnesActivity.FITNES_WEIGHT,
														fat.getText()
																.toString());
										copyIntent
												.putExtra(
														AddFitnesActivity.FITNES_COUNT_SELECTION,
														Integer.valueOf(fatAbsView
																.getText()
																.toString()));
										copyIntent
												.putExtra(
														AddFitnesActivity.FITNES_WEIGHT_SELECTION,
														Integer.valueOf(carbonAbsView
																.getText()
																.toString()));
										copyIntent
												.putExtra(
														AddFitnesActivity.FITNES_WEIGHTDEC_SELECTION,
														Integer.valueOf(protein
																.getText()
																.toString()));

										copyIntent
												.putExtra(
														AddTodayFitnesActivity.CALORYBURN,
														"0");
										copyIntent.setClass(DishActivity.this,
												AddTodayFitnesActivity.class);
									}

									copyIntent
											.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

								} catch (Exception e) {
									e.printStackTrace();
								}

								// TODO Auto-generated method stub
								// ((TextView)arg1.findViewById(R.id.textViewDishName)).getText()

								AlertDialog.Builder builder = null;

								builder = new AlertDialog.Builder(
										DishActivity.this.getParent());

								builder.setMessage(R.string.copy_dialog)
										.setPositiveButton(
												DishActivity.this
														.getString(R.string.yes),
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														if (CalendarActivityGroup.class
																.toString()
																.equals(parentName)) {
															CalendarActivityGroup activityStack = (CalendarActivityGroup) DishActivity.this
																	.getParent();
															activityStack
																	.push("AddTodayDishActivityCalendar",
																			copyIntent);
														} else {
															DishActivityGroup activityStack = (DishActivityGroup) DishActivity.this
																	.getParent();
															activityStack
																	.push("AddTodayDishActivity",
																			copyIntent);
														}
													}
												})
										.setNegativeButton(
												DishActivity.this
														.getString(R.string.no),
												null).show();
								return true;

							}
						});
				da.registerDataSetObserver(new DataSetObserver() {
					@Override
					public void onChanged() {
						sum = 0;
						sumLoose = 0;
						sumF = 0;
						sumC = 0;
						sumP = 0;
						initDishTable();
					}

				});
				dishesList.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> arg0, View v,
							int arg2, long arg3) {
					}
				});
				initDishTable();

			} catch (Exception e) {
				e.printStackTrace();
				if (c != null)
					c.close();
			} finally {
				// c.close();
			}
		}
	}

	private void loadTemplates() {
		ArrayList<DishType> types = new ArrayList<DishType>();
		types.add(new DishType(0, getString(R.string.choosetemplate)));
		types.addAll(TemplateDishHelper.getDaysArray(DishActivity.this));

		ArrayAdapter<DishType> adapter2 = new ArrayAdapter<DishType>(
				DishActivity.this, android.R.layout.simple_dropdown_item_1line,
				types);

		((ArrayAdapter<DishType>) adapter2)
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		templateSpinner.setAdapter(adapter2);
		templateSpinner.setOnItemSelectedListener(spinnerListener);

	}

	IabHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Locale locale = new Locale(SaveUtils.getLang(this));
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
		Bundle extras = getIntent().getExtras();
		View viewToLoad = LayoutInflater.from(this.getParent()).inflate(
				R.layout.today_list, null);
		header = (TextView) viewToLoad.findViewById(R.id.textViewTitle);
		Button exitButton = (Button) viewToLoad.findViewById(R.id.buttonExit);
		exitButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent();
				i.setAction(BaseActivity.CUSTOM_INTENT);
				DishActivity.this.sendBroadcast(i);
				finish();
				finish();
				System.exit(0);
			}
		});
		if (extras != null) {
			Boolean backb = extras.getBoolean(BACKBTN);
			if (backb) {
				Button backButton = (Button) viewToLoad
						.findViewById(R.id.buttonBack);
				backButton.setVisibility(View.VISIBLE);
				exitButton.setVisibility(View.GONE);
				backButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						onBackPressed();
					}
				});
			}
		}

		Button addButton = (Button) viewToLoad.findViewById(R.id.buttonAdd);
		addButton.setOnClickListener(addTodayDishListener);
		Button waterButton = (Button) viewToLoad.findViewById(R.id.buttonWater);
		waterButton.setOnClickListener(waterTodayListener);
		Button fitesButton = (Button) viewToLoad
				.findViewById(R.id.buttonFitnes);
		fitesButton.setOnClickListener(addTodayFitnesListener);
		Button saveButton = (Button) viewToLoad.findViewById(R.id.savetemplate);
		saveButton.setOnClickListener(saveListener);
		Button loadButton = (Button) viewToLoad.findViewById(R.id.loadtemplate);
		loadButton.setOnClickListener(loadListener);

		setContentView(viewToLoad);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (c != null)
			c.close();
		Intent i = new Intent();
		i.setAction(BaseActivity.CUSTOM_INTENT);
		DishActivity.this.sendBroadcast(i);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (c != null)
			c.close();

	}

	private OnClickListener addTodayDishListener = new OnClickListener() {

		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra(DishActivity.DATE, curentDateandTime);
			intent.putExtra(PARENT_NAME, parentName);
			intent.setClass(getParent(), DishListActivity.class);
			if (CalendarActivityGroup.class.toString().equals(parentName)) {
				CalendarActivityGroup activityStack = (CalendarActivityGroup) getParent();
				activityStack.push("DishListActivityCalendar", intent);
			} else {
				DishActivityGroup activityStack = (DishActivityGroup) getParent();
				activityStack.push("DishListActivity", intent);
			}
		}
	};
	private OnClickListener waterTodayListener = new OnClickListener() {

		public void onClick(View v) {
			Intent intent = new Intent();

			try {

				intent.setClass(getParent(), AddTodayDishActivity.class);
				intent.putExtra(AddTodayDishActivity.TITLE,
						getString(R.string.add_today_dish));
				intent.putExtra(AddTodayDishActivity.ADD, 1);
				intent.putExtra(DishActivity.DATE, curentDateandTime);
				intent.putExtra(AddTodayDishActivity.DISH_NAME,
						getString(R.string.water_name));
				intent.putExtra(AddTodayDishActivity.DISH_CALORISITY, 0);
				intent.putExtra(AddTodayDishActivity.DISH_FAT, "0");
				intent.putExtra(AddTodayDishActivity.DISH_CARBON, "0");
				intent.putExtra(AddTodayDishActivity.DISH_PROTEIN, "0");
				intent.putExtra(AddTodayDishActivity.DISH_ABSOLUTE_CALORISITY,
						0);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// TODO Auto-generated method stub
			// ((TextView)arg1.findViewById(R.id.textViewDishName)).getText()

			if (CalendarActivityGroup.class.toString().equals(parentName)) {
				CalendarActivityGroup activityStack = (CalendarActivityGroup) DishActivity.this
						.getParent();
				activityStack.push("AddTodayDishActivityCalendar", intent);
			} else {
				DishActivityGroup activityStack = (DishActivityGroup) DishActivity.this
						.getParent();
				activityStack.push("AddTodayDishActivity", intent);
			}

		}
	};

	private OnClickListener addTodayFitnesListener = new OnClickListener() {

		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra(DishActivity.DATE, curentDateandTime);
			intent.putExtra(PARENT_NAME, parentName);
			intent.putExtra(AddTodayFitnesActivity.ADD, 1);
			intent.setClass(getParent(), SportListActivity.class);
			if (CalendarActivityGroup.class.toString().equals(parentName)) {
				CalendarActivityGroup activityStack = (CalendarActivityGroup) getParent();
				activityStack.push("FitnesActivityCalendar", intent);
			} else {
				DishActivityGroup activityStack = (DishActivityGroup) getParent();
				activityStack.push("FitnesActivity", intent);
			}
		}
	};

	private OnClickListener saveListener = new OnClickListener() {

		public void onClick(View v) {
			showDialog(DIALOG_TEMPLATE_NAME);
		}
	};

	private OnClickListener loadListener = new OnClickListener() {

		public void onClick(View v) {
			templateSpinner.setSelection(0);
			flagLoad = true;
			if (TemplateDishHelper.getDaysArray(DishActivity.this).size() == 0) {
				Toast.makeText(DishActivity.this,
						getString(R.string.templatesempty), Toast.LENGTH_LONG)
						.show();
			} else {
				templateSpinner.performClick();
			}

		}
	};

	private void initDishTable() {
		if (c.getCount() > 0) {
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			    // do what you need with the cursor here
				Log.e("Cursor",c.getString(c
						.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)) );
				if (c.getString(c
						.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)) != null) {
					
					int val = Integer.parseInt(c.getString(c
							.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)));
					
					if (val > 0) {
						sum = sum + val;
					} else {
						sumLoose = sumLoose + val;
					}
					
					Log.e("Cursor category",c.getString(c.getColumnIndex(DishProvider.TODAY_CATEGORY)) );
					if (!c.getString(c.getColumnIndex(DishProvider.TODAY_CATEGORY))
							.equals("0")) {
						sumF = sumF
								+ c.getFloat(c
										.getColumnIndex(DishProvider.TODAY_DISH_FAT));
						sumC = sumC
								+ c.getFloat(c
										.getColumnIndex(DishProvider.TODAY_DISH_CARBON));
						sumP = sumP
								+ c.getFloat(c
										.getColumnIndex(DishProvider.TODAY_DISH_PROTEIN));
					}
				}
			}
		
		/*if (c.getCount() > 0) {
			c.moveToFirst();
			if (c.getString(c
					.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)) != null) {
				int val = Integer.parseInt(c.getString(c
						.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)));
				if (val > 0) {
					sum = sum + val;
				} else {
					sumLoose = sumLoose + val;
				}
				if (!c.getString(c.getColumnIndex(DishProvider.TODAY_CATEGORY))
						.equals("0")) {
					sumF = sumF
							+ c.getFloat(c
									.getColumnIndex(DishProvider.TODAY_DISH_FAT));
					sumC = sumC
							+ c.getFloat(c
									.getColumnIndex(DishProvider.TODAY_DISH_CARBON));
					sumP = sumP
							+ c.getFloat(c
									.getColumnIndex(DishProvider.TODAY_DISH_PROTEIN));
				}
			}
			while (c.moveToNext()) {
				int val = Integer.parseInt(c.getString(c
						.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)));
				if (val > 0) {
					sum = sum + val;
				} else {
					sumLoose = sumLoose + val;
				}
				if (!c.getString(c.getColumnIndex(DishProvider.TODAY_CATEGORY))
						.equals("0")) {
					sumF = sumF
							+ c.getFloat(c
									.getColumnIndex(DishProvider.TODAY_DISH_FAT));
					sumC = sumC
							+ c.getFloat(c
									.getColumnIndex(DishProvider.TODAY_DISH_CARBON));
					sumP = sumP
							+ c.getFloat(c
									.getColumnIndex(DishProvider.TODAY_DISH_PROTEIN));
				}
			}*/
			TextView tv = (TextView) findViewById(R.id.textViewTotalValue);
			TextView tvLoose = (TextView) findViewById(R.id.textViewTotalLooseValue);
			TextView tvF = (TextView) findViewById(R.id.textViewFatTotal);
			TextView tvC = (TextView) findViewById(R.id.textViewCarbonTotal);
			TextView tvP = (TextView) findViewById(R.id.textViewProteinTotal);
			TextView tvFp = (TextView) findViewById(R.id.textViewFatPercent);
			TextView tvCp = (TextView) findViewById(R.id.textViewCarbonPercent);
			TextView tvPp = (TextView) findViewById(R.id.textViewProteinPercent);
			tv.setText(String.valueOf(sum));
			tvLoose.setText(String.valueOf(sumLoose));
			DecimalFormat df = new DecimalFormat("###.#");
			tvF.setText(df.format(Float.valueOf(sumF)));
			tvC.setText(df.format(Float.valueOf(sumC)));
			tvP.setText(df.format(Float.valueOf(sumP)));
			float sum = Float.valueOf(sumF) + Float.valueOf(sumC)
					+ Float.valueOf(sumP);
			tvFp.setText("(" + df.format(Float.valueOf(sumF) * 100 / sum)
					+ "%)");
			tvCp.setText("(" + df.format(Float.valueOf(sumC) * 100 / sum)
					+ "%)");
			tvPp.setText("(" + df.format(Float.valueOf(sumP) * 100 / sum)
					+ "%)");
		} else {
			LinearLayout emptyHeader = (LinearLayout) findViewById(R.id.linearLayoutEmptyListHeader);
			emptyHeader.setVisibility(View.VISIBLE);
			emptyHeader.setOnClickListener(addTodayDishListener);
			TextView tv = (TextView) findViewById(R.id.textViewTotalValue);
			TextView tvLoose = (TextView) findViewById(R.id.textViewTotalLooseValue);
			TextView tvF = (TextView) findViewById(R.id.textViewFatTotal);
			TextView tvC = (TextView) findViewById(R.id.textViewCarbonTotal);
			TextView tvP = (TextView) findViewById(R.id.textViewProteinTotal);
			TextView tvFp = (TextView) findViewById(R.id.textViewFatPercent);
			TextView tvCp = (TextView) findViewById(R.id.textViewCarbonPercent);
			TextView tvPp = (TextView) findViewById(R.id.textViewProteinPercent);
			tv.setText(String.valueOf(0));
			tvLoose.setText(String.valueOf(0));
			DecimalFormat df = new DecimalFormat("###.#");
			tvF.setText(df.format(Float.valueOf(sumF)));
			tvC.setText(df.format(Float.valueOf(sumC)));
			tvP.setText(df.format(Float.valueOf(sumP)));
			float sum = Float.valueOf(sumF) + Float.valueOf(sumC)
					+ Float.valueOf(sumP);
			tvFp.setText("(" + df.format(Float.valueOf(sumF) * 100 / sum)
					+ "%)");
			tvCp.setText("(" + df.format(Float.valueOf(sumC) * 100 / sum)
					+ "%)");
			tvPp.setText("(" + df.format(Float.valueOf(sumP) * 100 / sum)
					+ "%)");
		}

		checkLimit(sum + sumLoose);
	}

	private OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (flagLoad) {
				flagLoad = false;
				AlertDialog.Builder builder = null;

				builder = new AlertDialog.Builder(DishActivity.this.getParent());

				builder.setMessage(R.string.remove_dialog)
						.setPositiveButton(
								DishActivity.this.getString(R.string.yes),
								dialogClickListener)
						.setNegativeButton(
								DishActivity.this.getString(R.string.no),
								dialogClickListener).show();

			}

		}

		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				try {
					LinearLayout emptyHeader = (LinearLayout) findViewById(R.id.linearLayoutEmptyListHeader);
					emptyHeader.setVisibility(View.GONE);
					Cursor cTemp = null;
					SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMMM",
							new Locale(SaveUtils.getLang(DishActivity.this)));
					Date curentDateandTimeLong = null;
					try {
						curentDateandTimeLong = sdf.parse(curentDateandTime);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Date nowDate = new Date();
					try {
						curentDateandTimeLong.setYear(nowDate.getYear());
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						cTemp = TemplateDishHelper.getDishesByDate(
								DishActivity.this, ((DishType) templateSpinner
										.getSelectedItem()).getDescription());
						if (cTemp.getCount() > 0) {
							if (0 != SaveUtils.getUserUnicId(DishActivity.this)) {
								// removeDaySocial(curentDateandTime);
							}
							// TodayDishHelper.removeDishesByDay(curentDateandTime,
							// DishActivity.this);
							da.newCursor = true;

						} else {
							Toast.makeText(DishActivity.this,
									getString(R.string.loadtemplateempty),
									Toast.LENGTH_LONG).show();
						}
						while (cTemp.moveToNext()) {
							try {
								TodayDish td = new TodayDish(
										cTemp.getFloat(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_ID)) > 0 ? cTemp
												.getFloat(cTemp
														.getColumnIndex(DishProvider.TODAY_DISH_ID))
												: SaveUtils
														.getRealWeight(DishActivity.this),
										cTemp.getString(cTemp
												.getColumnIndex(DishProvider.TODAY_NAME)),
										cTemp.getString(cTemp
												.getColumnIndex(DishProvider.TODAY_DESCRIPTION)),
										cTemp.getInt(cTemp
												.getColumnIndex(DishProvider.TODAY_CALORICITY)),
										cTemp.getString(cTemp
												.getColumnIndex(DishProvider.TODAY_CATEGORY)),
										cTemp.getInt(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_WEIGHT)),
										cTemp.getInt(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)),
										curentDateandTime,
										curentDateandTimeLong.getTime(),
										cTemp.getInt(cTemp
												.getColumnIndex(DishProvider.TODAY_IS_DAY)),
										cTemp.getString(cTemp
												.getColumnIndex(DishProvider.TODAY_TYPE)),
										cTemp.getFloat(cTemp
												.getColumnIndex(DishProvider.TODAY_FAT)),
										cTemp.getFloat(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_FAT)),
										cTemp.getFloat(cTemp
												.getColumnIndex(DishProvider.TODAY_CARBON)),
										cTemp.getFloat(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_CARBON)),
										cTemp.getFloat(cTemp
												.getColumnIndex(DishProvider.TODAY_PROTEIN)),
										cTemp.getFloat(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_PROTEIN)),
										cTemp.getInt(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_TIME_HH)),
										cTemp.getInt(cTemp
												.getColumnIndex(DishProvider.TODAY_DISH_TIME_MM)));
								td.setId(TodayDishHelper.addNewDish(td,
										DishActivity.this));
								if (0 != SaveUtils
										.getUserUnicId(DishActivity.this)) {
									new SocialUpdater(DishActivity.this, td,
											false).execute();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (cTemp != null) {
							cTemp.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				templateSpinner.setSelection(0);
				Toast.makeText(DishActivity.this,
						getString(R.string.loadtemplate), Toast.LENGTH_LONG)
						.show();
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				templateSpinner.setSelection(0);
				break;
			}
		}
	};
	private EditText templateName;

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		Button buttonOk;
		Button nobutton;
		switch (id) {

		case DIALOG_TEMPLATE_NAME:
			dialog = new Dialog(this.getParent());
			dialog.setContentView(R.layout.user_name_dialog);
			dialog.setTitle(R.string.templates);

			templateName = (EditText) dialog
					.findViewById(R.id.editTextUserName);
			TextView maintext = (TextView) dialog
					.findViewById(R.id.textViewSerchActivityLevelLabel);
			maintext.setText(getText(R.string.template_save));
			TextView nametext = (TextView) dialog
					.findViewById(R.id.textViewNameLabel);
			nametext.setText(getText(R.string.template_name));
			buttonOk = (Button) dialog.findViewById(R.id.buttonYes);
			buttonOk.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					if (templateName.getText().length() < 1) {
						templateName.setBackgroundColor(Color.RED);
					} else {
						Cursor cTemp = null;
						try {
							cTemp = TodayDishHelper.getDishesByDate(
									DishActivity.this, curentDateandTime);
							while (cTemp.moveToNext()) {
								try {
									TemplateDishHelper.addNewDish(
											new TodayDish(
													cTemp.getFloat(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_ID)),
													cTemp.getString(cTemp
															.getColumnIndex(DishProvider.TODAY_NAME)),
													cTemp.getString(cTemp
															.getColumnIndex(DishProvider.TODAY_DESCRIPTION)),
													cTemp.getInt(cTemp
															.getColumnIndex(DishProvider.TODAY_CALORICITY)),
													cTemp.getString(cTemp
															.getColumnIndex(DishProvider.TODAY_CATEGORY)),
													cTemp.getInt(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_WEIGHT)),
													cTemp.getInt(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY)),
													templateName.getText()
															.toString(),
													cTemp.getLong(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_DATE_LONG)),
													cTemp.getInt(cTemp
															.getColumnIndex(DishProvider.TODAY_IS_DAY)),
													cTemp.getString(cTemp
															.getColumnIndex(DishProvider.TODAY_TYPE)),
													cTemp.getFloat(cTemp
															.getColumnIndex(DishProvider.TODAY_FAT)),
													cTemp.getFloat(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_FAT)),
													cTemp.getFloat(cTemp
															.getColumnIndex(DishProvider.TODAY_CARBON)),
													cTemp.getFloat(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_CARBON)),
													cTemp.getFloat(cTemp
															.getColumnIndex(DishProvider.TODAY_PROTEIN)),
													cTemp.getFloat(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_PROTEIN)),
													cTemp.getInt(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_TIME_HH)),
													cTemp.getInt(cTemp
															.getColumnIndex(DishProvider.TODAY_DISH_TIME_MM))),
											DishActivity.this);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (cTemp != null) {
								cTemp.close();
							}
						}
						Toast.makeText(DishActivity.this,
								getString(R.string.savetemplate),
								Toast.LENGTH_LONG).show();
						loadTemplates();
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

	protected void removeDaySocial(String date) {
		// TODO Auto-generated method stub
		Cursor c = TodayDishHelper.getDishesByDate(DishActivity.this, date);
		if (c != null) {
			try {
				while (c.moveToNext()) {
					new SocialUpdater(DishActivity.this, c.getString(c
							.getColumnIndex("_id"))).execute();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				c.close();
			}
		}

	}

		/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		if (p != null) {
			return String.valueOf(SaveUtils.getUserAdvId(this)).equals(
					p.getDeveloperPayload());
		} else {
			return false;
		}

	}

	void complain(String message) {

		// alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(this.getParent()
				.getParent());
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);

		bld.create().show();
	}

}
