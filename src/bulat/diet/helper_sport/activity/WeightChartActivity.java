package bulat.diet.helper_sport.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import bulat.diet.helper_sport.R;
import bulat.diet.helper_sport.db.TodayDishHelper;
import bulat.diet.helper_sport.item.Day;
import bulat.diet.helper_sport.item.DishType;
import bulat.diet.helper_sport.utils.SaveUtils;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class WeightChartActivity extends Activity {
	Spinner chartTypeSpiner;
	GraphView graphView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View viewToLoad = LayoutInflater.from(this.getParent().getParent()).inflate(
				R.layout.statistics_weight, null);
		setContentView(viewToLoad);	
		
		///
		Button backButton = (Button) viewToLoad.findViewById(R.id.buttonBack);				
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		
		chartTypeSpiner = (Spinner) viewToLoad
				.findViewById(R.id.SpinnerChartType);
		// chartTypeSpiner
					ArrayList<DishType> types = new ArrayList<DishType>();
					types.add(new DishType(1,
							getString(R.string.change_weight_dialog_title)));
					if (SaveUtils.getChestEnbl(WeightChartActivity.this))
						types.add(new DishType(2, getString(R.string.volume_chest)));
					if (SaveUtils.getBicepsEnbl(WeightChartActivity.this))
						types.add(new DishType(3, getString(R.string.volume_biceps)));
					if (SaveUtils.getForearmEnbl(WeightChartActivity.this))
						types.add(new DishType(4, getString(R.string.volume_forearm)));
					if (SaveUtils.getHipEnbl(WeightChartActivity.this))
						types.add(new DishType(5, getString(R.string.volume_hip)));
					if (SaveUtils.getNeckEnbl(WeightChartActivity.this))
						types.add(new DishType(6, getString(R.string.volume_neck)));
					if (SaveUtils.getShinEnbl(WeightChartActivity.this))
						types.add(new DishType(7, getString(R.string.volume_shin)));
					if (SaveUtils.getPelvisEnbl(WeightChartActivity.this))
						types.add(new DishType(8, getString(R.string.volume_pelvis)));
					if (SaveUtils.getWaistEnbl(WeightChartActivity.this))
						types.add(new DishType(9, getString(R.string.volume_waist)));

					ArrayAdapter<DishType> adapter = new ArrayAdapter<DishType>(this,
							android.R.layout.simple_spinner_item, types);
		// init example series data  
		// graph with dynamically genereated horizontal and vertical labels
		
		// graph with custom labels and drawBackground
		
		graphView = new LineGraphView(
		this
		, ""
		);
		((LineGraphView) graphView).setDrawBackground(false);
		
		// custom static labels
		
		final List<Day> days  = TodayDishHelper.getDaysStat(getApplicationContext());		
		Collections.reverse(days);
		final GraphViewData[] data = new GraphViewData[days.size()];
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		chartTypeSpiner.setAdapter(adapter);
		chartTypeSpiner.setSelection(0);
		chartTypeSpiner	.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						try {
							
							int i = 0;
							int dtype = ((DishType) chartTypeSpiner
									.getSelectedItem()).getTypeKey();
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
					
						}catch (Exception e) {
							e.printStackTrace();
						}
						GraphViewSeries exampleSeries = new GraphViewSeries(data);
						if(days.size() > 1){
							TextView tv = (TextView) findViewById(R.id.textViewEmpty);  
							tv.setVisibility(View.GONE);
							String[] hor = new String[days.size()];
							int j=0;
							SimpleDateFormat sdf = new SimpleDateFormat("dd/MM",new Locale( SaveUtils.getLang(WeightChartActivity.this)));
							for (Day day : days) {	
								if(days.size()>8 && days.size()<16){
									if((j%2)==0){				
										hor[j]=String.valueOf(sdf.format(new Date(day.getDateInt())));
									}else{
										hor[j]="";
									}
								}else if(days.size()>=16 && days.size()<24){
									if((j%3)==0){				
										hor[j]=String.valueOf(sdf.format(new Date(day.getDateInt())));
									}else{
										hor[j]="";
									}
								}else if(days.size()>=24){
									if((j%4)==0){				
										hor[j]=String.valueOf(sdf.format(new Date(day.getDateInt())));
									}else{
										hor[j]="";
									}
								}else{
									hor[j]=String.valueOf(sdf.format(new Date(day.getDateInt())));
								}
								j++;
							}
							/*String[] vertical = new String[data.length];
							for (int i = 0; i < data.length; i++) {
								vertical[i]=String.valueOf(data[i].valueY);
							}*/
							graphView.setHorizontalLabels(hor);
							graphView.setScalable(false);
							graphView.setVerticalLabels(null);
							graphView.addSeries(exampleSeries); // data
						      
						    LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutChart);  
						    layout.removeAllViews();
						    layout.addView(graphView); 
						}else{
							TextView tv = (TextView) findViewById(R.id.textViewEmpty);  
							tv.setVisibility(View.VISIBLE);
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
						
					}
				});
		
		

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
/*		LinearLayout linear=(LinearLayout) findViewById(R.id.linearLayoutChart);
        values=calculateData(values);
        linear.addView(new MyGraphview(this,values));
        
        LinearLayout linear2=(LinearLayout) findViewById(R.id.linearLayoutChart2);
        values2 = getValues(TodayDishHelper.getStatistic(this));
        values2=calculateData(values2);
        linear2.removeAllViews();
        linear2.addView(new MyGraphview(this,values2));*/
	}

}
