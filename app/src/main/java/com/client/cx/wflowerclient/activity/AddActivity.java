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
import com.client.cx.wflowerclient.bean.Task;
import com.client.cx.wflowerclient.util.Constance;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加任务页面
 */
public class AddActivity extends AppCompatActivity implements View.OnClickListener {
    public static AddActivity instance = null;
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
    //    private int num;//任务序号

    private int type;
    private List<Integer> days;
    private int taskHour;
    private int taskMinute;
    private int yield;
    private int num;//任务编号

    private int dateLength;//日期长度，按周为7，按月为31
    private static final int DATE_LENGTH_WEEK = 7;//按周
    private static final int DATE_LENGTH_MOUTH = 31;//按月

    private MyDateAdapter mAdapter;
    private boolean isFirst = true;

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
                            type = Constance.WEEK_EXE;
                        } else {
                            type = Constance.WEEK_INEXE;
                        }
                        dateLength = DATE_LENGTH_WEEK;
                        days = getDateList();
                        mDateList.setVisibility(View.GONE);
                        break;
                    case R.id.rep_week:
                        //按周
                        type = Constance.WEEK_EXE;
                        dateLength = DATE_LENGTH_WEEK;
                        mDateList.setVisibility(View.VISIBLE);

                        break;
                    case R.id.rep_mouth:
                        //按月
                        type = Constance.MOUTH_EXE;
                        dateLength = DATE_LENGTH_MOUTH;
                        mDateList.setVisibility(View.VISIBLE);
                        break;
                }
                if (!isFirst) {
                    mAdapter.setDateList(getDateList());
                    mAdapter.notifyDataSetChanged();
                }

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
        instance = this;
        Intent intent = getIntent();
        String taskType = intent.getStringExtra("type");
        if ("edit".equals(taskType)) {
            //编辑任务
            Task task = (Task) (intent.getSerializableExtra("task"));

            num = task.getNum();
            mActionBarTitle.setText("编辑任务" + num);
            mTimePicker.setCurrentHour(task.getHour());
            mTimePicker.setCurrentMinute(task.getMinute());
            mYieldSeekBar.setProgress(task.getYield());
            mTimeEt.setText(task.getTime() + "");
            type = task.getType();
            //根据type，设置任务是否启用
            if (type == Constance.MOUTH_EXE || type == Constance.WEEK_EXE) {
                mOcChk.setChecked(true);
                isRun = true;
            } else {
                mOcChk.setChecked(false);
                isRun = false;
            }

            //设置重复日期列表
            if (type == Constance.MOUTH_EXE || type == Constance.MOUTH_INEXE) {
                mRepGroup.check(R.id.rep_mouth);
                dateLength = DATE_LENGTH_MOUTH;
            } else {
                mRepGroup.check(R.id.rep_week);
                dateLength = DATE_LENGTH_WEEK;
            }
            days = task.getDays();
            mAdapter = new MyDateAdapter(this, getDateList(), days);
            mDateList.setVisibility(View.VISIBLE);
        } else if ("add".equals(taskType)) {
            //新增任务
            num = intent.getIntExtra("num", 0);
            mActionBarTitle.setText("新增任务" + num);

            type = Constance.WEEK_EXE;
            //初始化日期列表,默认选中每日
            dateLength = DATE_LENGTH_WEEK;
            days = getDateList();
            mAdapter = new MyDateAdapter(this, days, null);
            mDateList.setVisibility(View.GONE);

        }


        GridLayoutManager manager = new GridLayoutManager(this, 7);
        mDateList.setLayoutManager(manager);

        mDateList.setAdapter(mAdapter);

        mActionBarRightTitle.setText("保存");
        mReturnBtn.setVisibility(View.VISIBLE);
        //设置时间显示为24小时制
        mTimePicker.setIs24HourView(true);
        //默认为按周执行
//        type = WEEK_EXE;
        taskHour = mTimePicker.getCurrentHour();
        taskMinute = mTimePicker.getCurrentMinute();

        isFirst = false;

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
        if (mRepGroup.getCheckedRadioButtonId() != R.id.everday) {
            days = mAdapter.getSelectList();
        }
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
        Task task = new Task();
        task.setNum(num);
        task.setType(type);
        task.setDays(days);
        task.setYield(yield);
        task.setHour(taskHour);
        task.setMinute(taskMinute);
        task.setTime(Integer.parseInt(time));
//        MainActivity.mCommandUtil.sendSetTask(num, type, days, yield, taskHour, taskMinute, Integer.parseInt(time));
        MainActivity.mCommandUtil.sendSetTask(task);
    }

    /**
     * 退出该页面
     */
    private void exit() {
        this.finish();
    }


}
