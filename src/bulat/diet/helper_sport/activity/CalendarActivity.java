package bulat.diet.helper_sport.activity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import bulat.diet.helper_sport.R;
import bulat.diet.helper_sport.adapter.DaysAdapter;
import bulat.diet.helper_sport.db.TodayDishHelper;
import bulat.diet.helper_sport.item.Day;
import bulat.diet.helper_sport.item.DishType;
import bulat.diet.helper_sport.item.TodayDish;
import bulat.diet.helper_sport.utils.SaveUtils;
import bulat.diet.helper_sport.utils.ServerUpdater;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class CalendarActivity extends BaseActivity {
	protected static final int DIALOG_CHART = 0;
	protected static final int DIALOG_WEIGHT = 1;
	TextView header;
	ListView daysList;
	Cursor c;
	int sum;
	private TextView avgFatView;
	private TextView avgProteinView;
	private TextView avgCarbonView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//TodayDish td = new TodayDish("", "", 12, "", 0, 0, "",0, 0, "");
		//td.setServerId(11);
		//TodayDishHelper.addNewDish(td,this);
		TodayDishHelper.removeSinchDish(this);
		//new ServerUpdater(CalendarActivity.this).execute(); 	
		View viewToLoad = LayoutInflater.from(this.getParent()).inflate(
				R.layout.days_list, null);
		header = (TextView) viewToLoad.findViewById(R.id.textViewTitle);

		Button templateTab = (Button) viewToLoad.findViewById(R.id.templateTab);
		templateTab.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					Intent intent = new Intent();
					intent.setClass(getParent(), TemplateActivity.class);
					CalendarActivityGroup activityStack = (CalendarActivityGroup) getParent();
					activityStack.push("TemplateActivity", intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Button exitButton = (Button) viewToLoad.findViewById(R.id.buttonExit);
		exitButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				System.exit(0);
			}
		});
		Button resetButton = (Button) viewToLoad.findViewById(R.id.buttonReset);
		resetButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = null;

				builder = new AlertDialog.Builder(CalendarActivity.this
						.getParent());
				builder.setMessage(R.string.reset_dialog)
						.setPositiveButton(getString(R.string.yes),
								dialogClickListener)
						.setNegativeButton(getString(R.string.no),
								dialogClickListener).show();
			}
		});
		Button chartButton = (Button) viewToLoad.findViewById(R.id.buttonChart);
		chartButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showDialog(DIALOG_CHART);
				Intent intent = new Intent();
				intent.setClass(getParent(), WeightChartActivity.class);
				CalendarActivityGroup activityStack = (CalendarActivityGroup) getParent();
				activityStack.push("WeightChartActivity", intent);
			}
		});
		setContentView(viewToLoad);
		LinearLayout graphHeader = (LinearLayout) findViewById(R.id.linearLayoutChart);
		graphHeader.setOnClickListener(showChartListener);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (c != null)
			c.close();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		Button buttonOk;
		Button nobutton;
		switch (id) {

		case DIALOG_CHART:
			GraphView graphView;
			// graph with custom labels and drawBackground
			graphView = new LineGraphView(this, "");
			((LineGraphView) graphView).setDrawBackground(false);

			// custom static labels
			GraphViewData[] data = new GraphViewData[] {};
			List<Day> days = TodayDishHelper
					.getDaysStat(getApplicationContext());
			Collections.reverse(days);
			try {

				data = new GraphViewData[days.size()];
				int i = 0;
				for (Day day : days) {
					data[i] = new GraphViewData(i, day.getBodyWeight());
					i++;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			GraphViewSeries exampleSeries = new GraphViewSeries(data);
			LinearLayout layout = (LinearLayout) dialog
					.findViewById(R.id.linearLayoutChart);
			layout.removeAllViews();
			if (days.size() > 1) {
				String[] hor = new String[days.size()];
				int j = 0;
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM",
						new Locale( SaveUtils.getLang(this)));
				TreeSet<Float> weights = new TreeSet<Float>();
				for (Day day : days) {
					weights.add(day.getBodyWeight());
					if (days.size() > 8 && days.size() < 16) {
						if ((j % 2) == 0) {
							hor[j] = String.valueOf(sdf.format(new Date(day
									.getDateInt())));
						} else {
							hor[j] = "";
						}
					} else if (days.size() >= 16 && days.size() < 24) {
						if ((j % 3) == 0) {
							hor[j] = String.valueOf(sdf.format(new Date(day
									.getDateInt())));
						} else {
							hor[j] = "";
						}
					} else if (days.size() >= 24) {
						if ((j % 4) == 0) {
							hor[j] = String.valueOf(sdf.format(new Date(day
									.getDateInt())));
						} else {
							hor[j] = "";
						}
					} else {
						hor[j] = String.valueOf(sdf.format(new Date(day
								.getDateInt())));
					}
					j++;
				}
				/*
				 * String[] vertical = new String[data.length]; for (int i = 0;
				 * i < data.length; i++) {
				 * vertical[i]=String.valueOf(data[i].valueY); }
				 */
				if (weights.size() > 1) {
					graphView.setHorizontalLabels(hor);
					graphView.setScalable(false);
					graphView.setVerticalLabels(null);
					graphView.addSeries(exampleSeries); // data

					TextView empty = (TextView) dialog
							.findViewById(R.id.textViewEmpty);
					empty.setVisibility(View.GONE);

					layout.addView(graphView);
				} else {
					TextView empty = (TextView) dialog
							.findViewById(R.id.textViewEmpty);
					empty.setVisibility(View.VISIBLE);
				}
			} else {
				TextView empty = (TextView) dialog
						.findViewById(R.id.textViewEmpty);
				empty.setVisibility(View.VISIBLE);
			}

			break;

		default:
			dialog = null;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		Button buttonOk;
		Button nobutton;
		switch (id) {
		case DIALOG_CHART:
			dialog = new Dialog(this.getParent());
			dialog.setContentView(R.layout.user_chart_dialog);
			dialog.setTitle(R.string.user_chart_dialog);
			final Spinner chartTypeSpiner = (Spinner) dialog
					.findViewById(R.id.SpinnerChartType);
			// chartTypeSpiner
			ArrayList<DishType> types = getChartTypes(CalendarActivity.this);
					

			ArrayAdapter<DishType> adapter = new ArrayAdapter<DishType>(this,
					android.R.layout.simple_spinner_item, types);
			final List<Day> days = TodayDishHelper
					.getDaysStat(getApplicationContext());
			final GraphViewData[] data = new GraphViewData[days.size()];
			Collections.reverse(days);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			chartTypeSpiner.setAdapter(adapter);
			chartTypeSpiner.setSelection(0);
			chartTypeSpiner
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							int dtype = ((DishType) chartTypeSpiner
									.getSelectedItem()).getTypeKey();
							// custom static labels
							LinearLayout layout = (LinearLayout) dialog
									.findViewById(R.id.linearLayoutChart);
							layout.removeAllViews();
							
							try {

								int i = 0;
								for (Day day : days) {
									switch (dtype) {
									case 1:
										data[i] = new GraphViewData(i, day
												.getBodyWeight());
										break;
									case 2:
										data[i] = new GraphViewData(i, day
												.getChest());
										break;
									case 3:
										data[i] = new GraphViewData(i, day
												.getBiceps());
										break;
									case 4:
										data[i] = new GraphViewData(i, day
												.getForearm());
										break;
									case 5:
										data[i] = new GraphViewData(i, day
												.getHip());
										break;
									case 6:
										data[i] = new GraphViewData(i, day
												.getNeck());
										break;
									case 7:
										data[i] = new GraphViewData(i, day
												.getShin());
										break;
									case 8:
										data[i] = new GraphViewData(i, day
												.getPelvis());
										break;
									case 9:
										data[i] = new GraphViewData(i, day
												.getWaist());
										break;
									default:
										break;
									}

									i++;
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
							
							GraphView graphView = new LineGraphView(
									CalendarActivity.this, "");
							((LineGraphView) graphView)
									.setDrawBackground(false);

							GraphViewSeries exampleSeries = new GraphViewSeries(
									data);
							if (days.size() > 1) {
								String[] hor = new String[days.size()];
								int j = 0;
								SimpleDateFormat sdf = new SimpleDateFormat(
										"dd/MM", new Locale(
												 SaveUtils.getLang(CalendarActivity.this)));
								TreeSet<Float> weights = new TreeSet<Float>();
								for (Day day : days) {
									weights.add(day.getBodyWeight());
									if (days.size() > 8 && days.size() < 16) {
										if ((j % 2) == 0) {
											hor[j] = String.valueOf(sdf
													.format(new Date(day
															.getDateInt())));
										} else {
											hor[j] = "";
										}
									} else if (days.size() >= 16
											&& days.size() < 24) {
										if ((j % 3) == 0) {
											hor[j] = String.valueOf(sdf
													.format(new Date(day
															.getDateInt())));
										} else {
											hor[j] = "";
										}
									} else if (days.size() >= 24) {
										if ((j % 4) == 0) {
											hor[j] = String.valueOf(sdf
													.format(new Date(day
															.getDateInt())));
										} else {
											hor[j] = "";
										}
									} else {
										hor[j] = String.valueOf(sdf
												.format(new Date(day
														.getDateInt())));
									}
									j++;
								}
								/*
								 * String[] vertical = new String[data.length];
								 * for (int i = 0; i < data.length; i++) {
								 * vertical[i]=String.valueOf(data[i].valueY); }
								 */
								if (weights.size() > 1) {
									graphView.setHorizontalLabels(hor);
									graphView.setScalable(false);
									graphView.setVerticalLabels(null);
									graphView.addSeries(exampleSeries); // data

								    layout = (LinearLayout) dialog
											.findViewById(R.id.linearLayoutChart);
									layout.addView(graphView);
								} else {
									TextView empty = (TextView) dialog
											.findViewById(R.id.textViewEmpty);
									empty.setVisibility(View.VISIBLE);
								}
							} else {
								TextView empty = (TextView) dialog
										.findViewById(R.id.textViewEmpty);
								empty.setVisibility(View.VISIBLE);
							}

						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub

						}
					});

			buttonOk = (Button) dialog.findViewById(R.id.buttonYes);
			buttonOk.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			break;

		default:
			dialog = null;
		}

		return dialog;
	}

	private ArrayList<DishType> getChartTypes(CalendarActivity calendarActivity) {
		ArrayList<DishType> types = new ArrayList<DishType>();
		types.add(new DishType(1,
				getString(R.string.change_weight_dialog_title)));
		if (SaveUtils.getChestEnbl(CalendarActivity.this))
			types.add(new DishType(2, getString(R.string.volume_chest)));
		if (SaveUtils.getBicepsEnbl(CalendarActivity.this))
			types.add(new DishType(3, getString(R.string.volume_biceps)));
		if (SaveUtils.getForearmEnbl(CalendarActivity.this))
			types.add(new DishType(4, getString(R.string.volume_forearm)));
		if (SaveUtils.getHipEnbl(CalendarActivity.this))
			types.add(new DishType(5, getString(R.string.volume_hip)));
		if (SaveUtils.getNeckEnbl(CalendarActivity.this))
			types.add(new DishType(6, getString(R.string.volume_neck)));
		if (SaveUtils.getShinEnbl(CalendarActivity.this))
			types.add(new DishType(7, getString(R.string.volume_shin)));
		if (SaveUtils.getPelvisEnbl(CalendarActivity.this))
			types.add(new DishType(8, getString(R.string.volume_pelvis)));
		if (SaveUtils.getWaistEnbl(CalendarActivity.this))
			types.add(new DishType(9, getString(R.string.volume_waist)));
		return types;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (c != null)
			c.close();
	}

	DaysAdapter da;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// TODO Auto-generated method stub
		avgCalorisityView = (TextView) findViewById(R.id.textViewAVGValue);
		avgFatView = (TextView) findViewById(R.id.textViewFat);
		avgProteinView = (TextView) findViewById(R.id.textViewProtein);
		avgCarbonView = (TextView) findViewById(R.id.textViewCarbon);
		String[] avgVals = TodayDishHelper.getAvgDishCalorisity(this);
		TextView tvFp = (TextView) findViewById(R.id.textViewFatPercent);
		TextView tvCp = (TextView) findViewById(R.id.textViewCarbonPercent);
		TextView tvPp = (TextView) findViewById(R.id.textViewProteinPercent);

		avgCalorisityView.setText(avgVals[0] + " " + getString(R.string.kcal));

		avgFatView.setText(avgVals[1]);
		avgCarbonView.setText(avgVals[2]);
		avgProteinView.setText(avgVals[3]);

		DecimalFormat df = new DecimalFormat("###.#");

		float sum = Float.valueOf(avgVals[1].replace(",", ".")) + Float.valueOf(avgVals[2].replace(",", "."))
				+ Float.valueOf(avgVals[3].replace(",", "."));
		tvFp.setText("(" + df.format(Float.valueOf(avgVals[1].replace(",", ".")) * 100 / sum)
				+ "%)");
		tvCp.setText("(" + df.format(Float.valueOf(avgVals[2].replace(",", ".")) * 100 / sum)
				+ "%)");
		tvPp.setText("(" + df.format(Float.valueOf(avgVals[3].replace(",", ".")) * 100 / sum)
				+ "%)");

		checkLimit(Integer.valueOf(avgVals[0]));
		header.setText(getString(R.string.tab_calendar_header));
		c = TodayDishHelper.getDaysNew(getApplicationContext());
		if (c != null) {
			try {
				int customLimit = SaveUtils.getCustomLimit(this);
				if (customLimit > 0) {
					SaveUtils.saveBMR(String.valueOf(customLimit), this);
					SaveUtils.saveMETA(String.valueOf(customLimit), this);
				}
				da = new DaysAdapter(this, getApplicationContext(), c,
						(CalendarActivityGroup) getParent());
				// TodayDishAdapter da = new
				// TodayDishAdapter(getApplicationContext(),
				// c,(DishActivityGroup) getParent());
				daysList = (ListView) findViewById(R.id.listViewDays);
				daysList.setAdapter(da);
				daysList.setItemsCanFocus(true);

				daysList.setOnItemClickListener(daysListOnItemClickListener);

				/*
				 * if (c.getCount() > 0){ c.moveToFirst(); sum = sum +
				 * Integer.parseInt
				 * (c.getString(c.getColumnIndex(DishProvider.TODAY_DISH_CALORICITY
				 * ))); while(c.moveToNext()){
				 * 
				 * sum = sum +
				 * Integer.parseInt(c.getString(c.getColumnIndex(DishProvider
				 * .TODAY_DISH_CALORICITY))); } TextView tv = (TextView)
				 * findViewById(R.id.textViewTotalValue);
				 * tv.setText(String.valueOf(sum)); }else{ TextView tv =
				 * (TextView) findViewById(R.id.textViewTotalValue);
				 * tv.setText(String.valueOf(0)); }
				 */
			} catch (Exception e) {
				if (c != null)
					c.close();
			} finally {
				// c.close();
			}
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (c != null)
			c.close();
	}

	private OnItemClickListener daysListOnItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
			// switchTabInActivity(1)
			Intent intent = new Intent();
			TextView day = (TextView) v.findViewById(R.id.textViewDay);

			intent.putExtra(AddTodayDishActivity.TITLE,
					getString(R.string.edit_today_dish));
			intent.putExtra(DishActivity.DATE, day.getText());
			intent.putExtra(DishActivity.BACKBTN, true);
			intent.putExtra(DishActivity.PARENT_NAME,
					CalendarActivityGroup.class.toString());
			intent.setClass(getParent(), DishActivity.class);
			CalendarActivityGroup activityStack = (CalendarActivityGroup) getParent();
			activityStack.push("DishDayActivity", intent);
		}
	};
	private TextView avgCalorisityView;

	public void switchTabInActivity(int indexTabToSwitchTo) {
		DietHelperActivity parentActivity;
		parentActivity = (DietHelperActivity) this.getParent();
		parentActivity.switchTab(indexTabToSwitchTo);
	}

	public void checkLimit(int sum) {
		int mode = SaveUtils.getMode(this);
		int customLimit = SaveUtils.getCustomLimit(this);
		if (customLimit > 0) {
			SaveUtils.saveBMR(String.valueOf(customLimit), this);
			SaveUtils.saveMETA(String.valueOf(customLimit), this);
		}
		try {
			switch (mode) {
			case 0:
				if (sum > Integer.parseInt(SaveUtils.getBMR(this))) {
					LinearLayout totalLayout = (LinearLayout) findViewById(R.id.linearLayoutAVG);
					totalLayout.setBackgroundResource(R.color.light_red);
				} else {
					LinearLayout totalLayout = (LinearLayout) findViewById(R.id.linearLayoutAVG);
					totalLayout.setBackgroundResource(R.color.light_green);
				}

				break;
			case 1:
				if (sum > Integer.parseInt(SaveUtils.getMETA(this))) {
					LinearLayout totalLayout = (LinearLayout) findViewById(R.id.linearLayoutAVG);
					totalLayout.setBackgroundResource(R.color.light_red);
				} else {
					LinearLayout totalLayout = (LinearLayout) findViewById(R.id.linearLayoutAVG);
					totalLayout.setBackgroundResource(R.color.light_green);
				}
				break;
			case 2:
				if (sum < Integer.parseInt(SaveUtils.getMETA(this))) {
					LinearLayout totalLayout = (LinearLayout) findViewById(R.id.linearLayoutAVG);
					totalLayout.setBackgroundResource(R.color.light_red);

				} else {
					LinearLayout totalLayout = (LinearLayout) findViewById(R.id.linearLayoutAVG);
					totalLayout.setBackgroundResource(R.color.light_green);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private OnClickListener showChartListener = new OnClickListener() {

		public void onClick(View v) {
			showDialog(DIALOG_CHART);
		}
	};
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				try {
					TodayDishHelper.clearAll(CalendarActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	public void resume() {
		c.requery();
		da.notifyDataSetChanged();
	}
}
