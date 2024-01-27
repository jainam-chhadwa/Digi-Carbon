package com.vatsal.android.digitaldetox.fragments;

import static org.antlr.v4.runtime.misc.MurmurHash.finish;

import android.app.ActionBar;
import android.app.Activity;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.GenericItemAdapter;
import com.turingtechnologies.materialscrollbar.DateAndTimeIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;
import com.vatsal.android.digitaldetox.R;
import com.vatsal.android.digitaldetox.activities.AppUsageStatisticsActivity;
import com.vatsal.android.digitaldetox.adapters.ScrollAdapter;
import com.vatsal.android.digitaldetox.models.AppFilteredEvents;
import com.vatsal.android.digitaldetox.models.DisplayEventEntity;
import com.vatsal.android.digitaldetox.recycler.TotalItem;
import com.vatsal.android.digitaldetox.utils.FormatEventsViewModel;
import com.vatsal.android.digitaldetox.utils.Tools;
import com.vatsal.android.digitaldetox.utils.AppList;
import android.util.Log;
import android.widget.Toast;
import android.widget.Toolbar;
//import com.vatsal.android.digitaldetox.activities.CarbonCalculation;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class AppDetailFragment extends Fragment {

    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_DATE_OFFSET = "dateOffset";
    int count = 0;
    int wicount=0;
    int other_wicount=0;
    int other_count=0;
    String name=null;

//    UsageStatsManager usageStatsManager = (UsageStatsManager) getContext().getSystemService(Context.USAGE_STATS_SERVICE);

    @BindView(R.id.recyclerview_app_detail)
    RecyclerView mRecyclerView;
    @BindView(R.id.detail_chart)
    PieChart mChart;
    @BindView(R.id.detail_no_usage)
    TextView noUsageTV;
    @BindView(R.id.detail_chart_no_usage)
    TextView noUsageChartTV;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.textView4)
    TextView carbon_fprint;
    @BindView(R.id.textView5)
    TextView app_count;
//    @BindView(R.id.predict)
//    Button predict;
    @BindView(R.id.result)
    TextView result;

    Button backArrow;
    Button predict;
    private Unbinder mUnbinder;
    private FormatEventsViewModel formatCustomUsageEvents;
    private String mAppName;
    private int mDateOffset;
    public String carbon_footprint;
    long time_sec;
    String url = "https://sahilhate01.pythonanywhere.com/predict";

    public static AppDetailFragment newInstance(String appName, int dateOffset) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_APP_NAME, appName);
        bundle.putInt(KEY_DATE_OFFSET, dateOffset);
        AppDetailFragment fragment = new AppDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAppName = bundle.getString(KEY_APP_NAME);
            mDateOffset = bundle.getInt(KEY_DATE_OFFSET);
        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_detail, container, false);
        mUnbinder = ButterKnife.bind(this, v);

        backArrow=v.findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
//                inflater.inflate(R.layout.activity_app_usage_statistics, container, false);
            }
        });



        predict = v.findViewById(R.id.predict);
        result = v.findViewById(R.id.result);
        Log.d("d","d=" + app_count.getText().toString());
        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hit the API -> Volley
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String data = jsonObject.getString("result");
                                    if(!data.equals("Please choose another app")){
                                        Log.d("long", " data=" +data);
                                        data = data.substring(0, data.indexOf('.'));

                                        long time = Long.parseLong(data);
                                        time=time/60;
                                        Log.d("long", "appname="+ mAppName + " min=" +time);
                                        String carbon = CarbonCalculation(mAppName, time);
                                        carbon = carbon + "gm";
                                        Log.d("carbon", "predicted carbonfootprint="+ carbon);

                                        result.setText(carbon);
                                    }else{
                                        result.setText(data);
                                    }

                                    Log.d("resultss", "resultss="+ data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }){

                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String,String>();
                        String s1=Integer.toString(count);
                        String s2=Integer.toString(wicount);
                        params.put("appname", mAppName);
                        if(mAppName.equals("com.whatsapp") || mAppName.equals("com.instagram.android") ){
                            params.put("appcount",s2);
                        }else{
                            params.put("appcount",s1);
                        }


                        return params;
                    }

                };
                RequestQueue queue = Volley.newRequestQueue(getContext());
                queue.add(stringRequest);
            }
        });

        return v;
    }

    private void moveToNewActivity () {

        Intent i = new Intent(getActivity(), AppUsageStatisticsActivity.class);
        startActivity(i);
        ((Activity) getActivity()).overridePendingTransition(0, 0);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("","back="+ getActivity() );
//        Log.d("","back=" + getActivity().onBackPressed());

        Button btnBack = view.findViewById(R.id.back_arrow);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the parent activity
                moveToNewActivity();
            }

        });
        mRecyclerView.scrollToPosition(0);

        FastAdapter<TotalItem> mFastAdapter = new FastAdapter<>();
        GenericItemAdapter<DisplayEventEntity, TotalItem> mTotalAdapter =
                new GenericItemAdapter<>(TotalItem.class, DisplayEventEntity.class);
        ScrollAdapter scrollAdapter = new ScrollAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext()));
        mRecyclerView.setItemAnimator(null);

        mRecyclerView.setAdapter(scrollAdapter.wrap(mTotalAdapter.wrap(mFastAdapter)));

        TouchScrollBar materialScrollBar = new TouchScrollBar(getActivity().getApplicationContext(),
                mRecyclerView, true);
        materialScrollBar.setHandleColourRes(R.color.colorAccent);
        materialScrollBar.setBarColourRes(R.color.scrollbarBgGray);
        materialScrollBar.addIndicator(new DateAndTimeIndicator(getActivity().
                getApplicationContext(), false, false, false, true), true);

        UsageStatsManager usageStatsManager = (UsageStatsManager) getContext().getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);

        long startTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        long endTime = System.currentTimeMillis();
        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        Log.d("cnt", "appname= " + mAppName);

        if(!mAppName.contains(".")){
            AppList applist = new AppList();
            name = applist.getPackageName(mAppName);
        }
        Log.d("cnt", "appname= " + name);
        while (usageEvents.hasNextEvent()) {

            Event event = new Event();
            usageEvents.getNextEvent(event);
            if(mAppName.equals("com.whatsapp") || mAppName.equals("com.instagram.android") || mAppName.equals("WhatsApp") || mAppName.equals("Instagram")  ){
                if(mAppName.contains(".")){
                    if (event.getEventType() == Event.ACTIVITY_STOPPED
                            && event.getPackageName().equals(mAppName)) {
                        wicount++;
                    }
                }else{
                    if (event.getEventType() == Event.ACTIVITY_STOPPED
                            && event.getPackageName().equals(name)) {
                        other_wicount++;
                    }

                }

//                count=count/2;
            }
            else{
                if(mAppName.contains(".")){
                    if (event.getEventType() == Event.ACTIVITY_RESUMED
                            && event.getPackageName().equals(mAppName)) {
//                Log.d("name", "name=" +  event.getPackageName());
                        count++;
                    }
                }else{
                    if (event.getEventType() == Event.ACTIVITY_RESUMED
                            && event.getPackageName().equals(name)) {
                        other_count++;
                    }
                }

            }

        }

        wicount=wicount/14;
        count=count/7;
        Log.d("other", "other= " +mAppName +" ="+ other_wicount);
        Log.d("other", "other= " +mAppName +" ="+ other_count);
        other_wicount=other_wicount/14;
        other_count=other_count/7;
        Log.d("cmt", "Number of times the app was launched: " +mAppName +"="+ wicount);
        Log.d("cmt", "Number of times the app was launched: " +mAppName +"="+ count);

        TextView myTextView = app_count.findViewById(R.id.textView5);

        if(mAppName.equals("com.whatsapp") || mAppName.equals("com.instagram.android") || mAppName.equals("WhatsApp") || mAppName.equals("Instagram") ){
            if(mAppName.contains(".")){
                myTextView.setText("Average App Count= " + wicount);
            }else{
                myTextView.setText("Average App Count= " + other_wicount);
            }

        }
        else{
            if(mAppName.contains(".")){
                myTextView.setText("Average App Count= " + count);
            }else{
                myTextView.setText("Average App Count= " + other_count);
            }
        }

        count=0;


        formatCustomUsageEvents = ViewModelProviders
                .of(this)
                .get(FormatEventsViewModel.class);

        formatCustomUsageEvents.getAppDetailEventsList().observe(this, allEvents -> {
                    assert allEvents != null;
                    AppFilteredEvents appFilteredEvents = Tools.getSpecificAppEvents(allEvents, mAppName);
                    Log.d("allevents", "allevents= "+ allEvents.get(0).date);
                    Log.d("allevents", "allevents= "+ allEvents.get(allEvents.size()-1).date);
//                    Log.d("detevents","detailedevents="+formatCustomUsageEvents.getAppDetailEventsList().toString());
                    if (appFilteredEvents.appEvents == null || appFilteredEvents.appEvents.size() == 0) {
                        mTotalAdapter.clear();
                        noUsageTV.setVisibility(View.VISIBLE);
                        noUsageChartTV.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                        return;
                    } else {
                        Log.d("app", "filtered1="+ appFilteredEvents.appEvents.size());
                        Log.d("app", "listname"+ appFilteredEvents.appEvents.getClass().getSimpleName());


                        noUsageTV.setVisibility(View.GONE);
                        noUsageChartTV.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }

                    setPie(appFilteredEvents);

                    if (mTotalAdapter.getItem(0) != null) {
                        int index = findItemInList(appFilteredEvents.appEvents, mTotalAdapter.getItem(1).getModel());
                        if (index > -1) {
                            mTotalAdapter.removeModel(0);
                        }
                        for (int i = index - 1; i >= 0; i--) {
                            mTotalAdapter.addModel(0, appFilteredEvents.appEvents.get(i));
                        }
                    } else {
                        mTotalAdapter.clear();
                        mTotalAdapter.addModel(appFilteredEvents.appEvents);
                    }

                });

        triggerEvents();
    }

//    public static long getAppLaunchCountLegacy(Context context, String packageName, long startTime, long endTime) {
//        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//        long launchCount = 0;
//        for (UsageStats usageStats : usageStatsList) {
//            if (usageStats.getPackageName().equals(packageName)) {
//                try {
//                    Method method = usageStats.getClass().getMethod("getLaunchCount");
//                    int count = (int) method.invoke(usageStats);
//                    Log.d("cnt1", "cnt1" + count);
//                    launchCount += count;
//                } catch (Exception e) {
//                    Log.d("err", "err" + e);
//                    e.printStackTrace();
//                }
//            }
//        }
//        return launchCount;
//    }

//    public int getAppLaunchCount(String packageName) {
//        UsageEvents usageEvents = usageStatsManager.queryEvents(
//                System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
//                System.currentTimeMillis());
//        UsageEvents.Event event = new UsageEvents.Event();
//        int count = 0;
//        while (usageEvents.hasNextEvent()) {
//            usageEvents.getNextEvent(event);
//            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND
//                    && event.getPackageName().equals(packageName)) {
//                count++;
//            }
//        }
//        return count;
//    }



    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    private int findItemInList(List<DisplayEventEntity> list, DisplayEventEntity event) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).appName.equals(event.appName) && list.get(i).startTime == event.startTime)
                Log.i("name", "xyz=" + i);
                return i;
        }
        return -1;
    }


    private String CarbonCalculation(String Name, long time) {
        double  carbon=0;

        if(Name.equalsIgnoreCase("com.google.android.youtube") || Name.equalsIgnoreCase("YouTube") )
            carbon=time*4.6;
        else if(Name.equalsIgnoreCase("com.android.chrome") || Name.equalsIgnoreCase("Chrome"))
            carbon=time*1.76;
        else if(Name.equalsIgnoreCase("com.whatsapp") || Name.equalsIgnoreCase("WhatsApp"))
            carbon=time*0.14;

        else if(Name.equalsIgnoreCase("com.facebook.katana") || Name.equalsIgnoreCase("Facebook"))
            carbon=time*0.79;

        else if(Name.equalsIgnoreCase("com.reddit.frontpage") || Name.equalsIgnoreCase("Reddit"))
            carbon=time*2.48;

        else if(Name.equalsIgnoreCase("Pinterest"))
            carbon=time*1.3;
        else if(Name.equalsIgnoreCase("com.netflix.mediaclient") || Name.equalsIgnoreCase("Netflix"))
            carbon=time*6.7;
        else if(Name.equalsIgnoreCase("Zoom"))
            carbon=time*1.13;
        else if(Name.equalsIgnoreCase("com.microsoft.teams") || Name.equalsIgnoreCase("Microsoft Teams"))
            carbon=time*1.2;
        else if(Name.equalsIgnoreCase("com.instagram.android") || Name.equalsIgnoreCase("Instagram"))
            carbon=time*1.05;
        else if(Name.equalsIgnoreCase("com.google.android.gm") || Name.equalsIgnoreCase("Gmail"))
            carbon=time*0.3;
        else if(Name.equalsIgnoreCase("org.telegram.messenger") || Name.equalsIgnoreCase("Telegram"))
            carbon=time*0.14;
        else if(Name.equalsIgnoreCase("com.google.android.apps.youtube.music") || Name.equalsIgnoreCase("YouTube Music"))
            carbon=time*0.83;

        return String.valueOf(String.format("%.2f",carbon));

    }


    private void triggerEvents() {
        Calendar startCalender = Calendar.getInstance();
        if (mDateOffset < 0)
            startCalender.add(Calendar.DATE, mDateOffset);
        startCalender.set(Calendar.HOUR_OF_DAY, 0);
        startCalender.set(Calendar.MINUTE, 0);
        startCalender.set(Calendar.SECOND, 0);
        startCalender.set(Calendar.MILLISECOND, 0);


        Calendar endCalendar = Calendar.getInstance();
        if (mDateOffset < 0)
            endCalendar.add(Calendar.DATE, mDateOffset);
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);

        formatCustomUsageEvents
                .setCachedEventsList(startCalender.getTimeInMillis(),
                        endCalendar.getTimeInMillis());
    }


    private void setPie(AppFilteredEvents appFilteredEvents) {

        Calendar elapsedTodayCalendar = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        elapsedTodayCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        endTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        elapsedTodayCalendar.setTimeInMillis(elapsedTodayCalendar.getTimeInMillis()
                + TimeZone.getDefault().getOffset(elapsedTodayCalendar.getTimeInMillis()));
        endTime.setTimeInMillis(appFilteredEvents.endTime
                + TimeZone.getDefault().getOffset(endTime.getTimeInMillis()));
        long adjustedPeriodEndTime;

        if (endTime.get(Calendar.YEAR) < elapsedTodayCalendar.get(Calendar.YEAR)
                || endTime.get(Calendar.DAY_OF_YEAR) < elapsedTodayCalendar.get(Calendar.DAY_OF_YEAR)) {

            Calendar startTime = Calendar.getInstance();
            startTime.setTimeZone(TimeZone.getTimeZone("UTC"));
            startTime.setTimeInMillis(appFilteredEvents.startTime
                    + TimeZone.getDefault().getOffset(startTime.getTimeInMillis()));

            int offset = endTime.get(Calendar.DAY_OF_YEAR) - startTime.get(Calendar.DAY_OF_YEAR);

            endTime.set(Calendar.YEAR, 1970);
            endTime.set(Calendar.DAY_OF_YEAR, offset + 1);
            endTime.set(Calendar.HOUR_OF_DAY, 23);
            endTime.set(Calendar.MINUTE, 59);
            endTime.set(Calendar.SECOND, 59);
            endTime.set(Calendar.MILLISECOND, 999);
            adjustedPeriodEndTime = endTime.getTimeInMillis();
        } else {
            elapsedTodayCalendar.set(Calendar.YEAR, 1970);
            elapsedTodayCalendar.set(Calendar.DAY_OF_YEAR, 1);
            adjustedPeriodEndTime = elapsedTodayCalendar.getTimeInMillis();
        }

        final double TIME_DAY = adjustedPeriodEndTime / 1000;
        final double TIME_USED_OTHERS = Tools.findTotalUsage(appFilteredEvents.otherEvents) / 1000;
        final double TIME_USED_THIS = Tools.findTotalUsage(appFilteredEvents.appEvents) / 1000;

        float otherPercent = (float) (TIME_USED_OTHERS / TIME_DAY * 100);
        float dayRemainingPercent = (float) ((TIME_DAY - TIME_USED_OTHERS - TIME_USED_THIS) / TIME_DAY * 100);
        float thisPercent = (float) (TIME_USED_THIS / TIME_DAY * 100);

        // Anything less than 1 tends to be invisible on the chart
        if (otherPercent < 1)
            otherPercent = 1;
        if (thisPercent < 1)
            thisPercent = 1;


        long totalUsage = Tools.findTotalUsage(appFilteredEvents.appEvents);
        String formattedTime = Tools.formatTotalTime(0, totalUsage, true);
        HashMap<String, String> map = AppList.adddata();
        String app = AppList.getAppName(map,mAppName);

        time_sec = totalUsage/60000;
        carbon_footprint = CarbonCalculation(mAppName,time_sec);
        Log.d("tag", "Carbon footprint for= " + app + " is= " + carbon_footprint);
        if(carbon_footprint == "0"){

        }
        else {
            TextView myTextView = carbon_fprint.findViewById(R.id.textView4);
            myTextView.setText("Today's Carbon Footprint: " + carbon_footprint + " gm");

            ArrayList<PieEntry> entries = new ArrayList<>();

            entries.add(new PieEntry(otherPercent, getResources().getString(R.string.detail_other_apps)));
            entries.add(new PieEntry(dayRemainingPercent, getResources().getString(R.string.detail_unused)));
            entries.add(new PieEntry(thisPercent, getResources().getString(R.string.detail_this_app)));

            PieDataSet dataSet = new PieDataSet(entries, "App usage");
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(6f);
            dataSet.setColors(Tools.getColours(entries.size()));
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setDrawValues(false);

            mChart.setUsePercentValues(true);
            mChart.getDescription().setEnabled(false);
            mChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);
            mChart.setDragDecelerationFrictionCoef(0.95f);
            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()).getApplicationContext(), R.color.textWhite));
            mChart.setCenterText(generateCenterSpannableText(mAppName, formattedTime));
            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            mChart.setHoleRadius(58f);
            mChart.setTransparentCircleRadius(64f);
            mChart.setRotationAngle(0);
            mChart.setRotationEnabled(true);
            mChart.setHighlightPerTapEnabled(true);
            mChart.setEntryLabelColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.textBlack));
            float finalOtherPercent = otherPercent;
            float finalThisPercent = thisPercent;
            mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry entry, Highlight highlight) {
                    if (entry == null)
                        return;

                    float label = entry.getY();
                    if (label == dayRemainingPercent) {
                        String formattedTime = Tools.formatTotalTime(0, (long) ((TIME_DAY - TIME_USED_OTHERS - TIME_USED_THIS) * 1000), true);
                        mChart.setCenterText(generateCenterSpannableText(
                                getString(R.string.detail_unused), formattedTime));
                    } else if (label == finalOtherPercent) {
                        String formattedTime = Tools.formatTotalTime(0, (long) (TIME_USED_OTHERS * 1000), true);
                        mChart.setCenterText(generateCenterSpannableText(
                                getString(R.string.detail_other_apps), formattedTime));
                    } else if (label == finalThisPercent) {
                        String formattedTime = Tools.formatTotalTime(0, (long) (TIME_USED_THIS * 1000), true);
                        mChart.setCenterText(generateCenterSpannableText(mAppName, formattedTime));
                    }
                }

                @Override
                public void onNothingSelected() {
                    String formattedTime = Tools.formatTotalTime(0, (long) (TIME_USED_THIS * 1000), true);
                    mChart.setCenterText(generateCenterSpannableText(mAppName, formattedTime));
                }
            });

            PieData data = new PieData(dataSet);

            Legend l = mChart.getLegend();
            l.setEnabled(false);

            mChart.setData(data);
            mChart.setVisibility(View.VISIBLE);
            mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        }
    }

    private SpannableString generateCenterSpannableText(String name, String formattedTime) {

        SpannableString s = new SpannableString(name + "\n\n" + ((formattedTime == null) ?
                getResources().getString(R.string.no_usage) : formattedTime));

        s.setSpan(new RelativeSizeSpan(0.9f), 0, name.length(), 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), name.length(), s.length(), 0);
        s.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(Objects.requireNonNull(getActivity()).getApplicationContext(), R.color.textBlack)),
                0, name.length(), 0);
        s.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.textDarkGray)),
                name.length() + 1, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(0.8f), name.length() + 1, s.length(), 0);
        return s;
    }





}
