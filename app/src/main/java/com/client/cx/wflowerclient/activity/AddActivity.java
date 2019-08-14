package com.client.cx.wflowerclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.client.cx.wflowerclient.R;
import com.client.cx.wflowerclient.adapter.MyDateAdapter;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加任务页面
 */
public class AddActivity extends AppCompatActivity implements View.OnClickListener {
    private TimePicker mTimePicker;
    private TextView mActionBarTitle, mActionBarRightTitle;
    private ImageButton mReturnBtn;
    private RadioGroup mRepGroup;
    private IndicatorSeekBar mYieldSeekBar;
    private CheckBox mOcChk;
    private EditText mTimeEt;
    private RecyclerView mDateList;
    private NestedScrollView mScrollView;

    private boolean isRun = true;//是否执行任务
    private int num;//任务序号
    private static final int MOUTH_EXE = 1;//按月执行
    private static final int MOUTH_INEXE = 3;//按月执行,但任务挂起
    private static final int WEEK_EXE = 2;//按周执行
    private static final int WEEK_INEXE = 4;//按周执行，但任务挂起
    private int type;
    private List<Integer> days;
    private int taskHour;
    private int taskMinute;
    private int yield;

    private int dateLength;//日期长度，按周为7，按月为31
    private static final int DATE_LENGTH_NONE = 0;//按周
    private static final int DATE_LENGTH_WEEK = 7;//按周
    private static final int DATE_LENGTH_MOUTH = 31;//按月

    private MyDateAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        initView();
        initData();
    }

    private void initView() {
        mTimePicker = (TimePicker) this.findViewById(R.id.timepicker);
        mActionBarTitle = (TextView) this.findViewById(R.id.title);
        mActionBarRightTitle = (TextView) this.findViewById(R.id.right_title);
        mReturnBtn = (ImageButton) this.findViewById(R.id.return_back);
        mRepGroup = (RadioGroup) this.findViewById(R.id.rep_group);
        mYieldSeekBar = (IndicatorSeekBar) this.findViewById(R.id.yield_seekbar);
        mOcChk = (CheckBox) this.findViewById(R.id.oc_chk);
        mTimeEt = (EditText) this.findViewById(R.id.time_content);
        mDateList = (RecyclerView) this.findViewById(R.id.date_list);
        mScrollView = (NestedScrollView) this.findViewById(R.id.scrollview);

        mActionBarRightTitle.setOnClickListener(this);
        mReturnBtn.setOnClickListener(this);
        mOcChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRun = isChecked;
            }
        });
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                taskHour = hourOfDay;
                taskMinute = minute;
            }
        });
        mRepGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (mRepGroup.getCheckedRadioButtonId()) {
                    case R.id.everday:
                        //每天
                        if (isRun) {
                            type = WEEK_EXE;
                        } else {
                            type = WEEK_INEXE;
                        }
                        dateLength = DATE_LENGTH_NONE;
                        days = getDateList();
                        break;
                    case R.id.rep_week:
                        type = WEEK_EXE;
                        dateLength = DATE_LENGTH_WEEK;
                        //按周
                        break;
                    case R.id.rep_mouth:
                        //按月
                        type = MOUTH_EXE;
                        dateLength = DATE_LENGTH_MOUTH;
                        break;
                }
                mAdapter.setDateList(getDateList());
                mAdapter.notifyDataSetChanged();

                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });

    }

    private void initData() {
        Intent intent = getIntent();
        num = intent.getIntExtra("num", 1);
        mActionBarTitle.setText("设置任务");
        mActionBarRightTitle.setText("保存");
        mReturnBtn.setVisibility(View.VISIBLE);
        //设置时间显示为24小时制
        mTimePicker.setIs24HourView(true);
        //默认为按周执行
        type = WEEK_EXE;
        days = getDateList();
        taskHour = mTimePicker.getCurrentHour();
        taskMinute = mTimePicker.getCurrentMinute();

        //初始化日期列表,默认选中每日
        dateLength = DATE_LENGTH_NONE;
        GridLayoutManager manager = new GridLayoutManager(this, 7);
        mDateList.setLayoutManager(manager);
        mAdapter = new MyDateAdapter(this, getDateList());
        mDateList.setAdapter(mAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_title:
//                doSave();
                sendTask();
                break;
            case R.id.return_back:
                exit();
                break;
        }
    }

    private List<Integer> getDateList() {
        List<Integer> dates = new ArrayList<>();
        for (int i = 0; i < dateLength; i++) {
            dates.add(i + 1);
        }
        return dates;

    }

    private void sendTask() {
        days = mAdapter.getSelectList();
        yield = mYieldSeekBar.getProgress();
        String time = mTimeEt.getText().toString();
        if (TextUtils.isEmpty(time)) {
            Toast.makeText(this, "请输入喷洒时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Integer.parseInt(time) < 1 || Integer.parseInt(time) > 200) {
            Toast.makeText(this, "请确保喷洒时间为1~200之间的整数", Toast.LENGTH_SHORT).show();
            return;
        }
        MainActivity.mCommandUtil.sendSetTask(num, type, days, yield, taskHour, taskMinute, time);
    }

    /**
     * 退出该页面
     */
    private void exit() {
        this.finish();
    }
}
