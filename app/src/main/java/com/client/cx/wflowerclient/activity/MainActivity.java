package com.client.cx.wflowerclient.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.client.cx.wflowerclient.R;
import com.client.cx.wflowerclient.adapter.MyTaskAdapter;
import com.client.cx.wflowerclient.bean.Task;
import com.client.cx.wflowerclient.customerView.Divider;
import com.client.cx.wflowerclient.util.CommandUtil;
import com.warkiz.widget.IndicatorSeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号(蓝牙串口服务)


    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean bRun = true;
    boolean bThread = false;
    boolean isLinked = false;//蓝牙连接是否成功

    private InputStream is;    //输入流，用来接收蓝牙数据
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
    private String address; //连接地址
    private long millisTime;//后台计时

    private ImageButton mReturnIB;
    private TextView mLinkTv, mTimeTv;
    private RelativeLayout mLayoutNotLinked;
    private Button mReviseBtn, mStartBtn, mStopBtn;
    private IndicatorSeekBar mSeekBar;
    private ImageView mAddIv;
    private RecyclerView mTaskRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private MyTaskAdapter mAdapter;

    public static CommandUtil mCommandUtil;
    private static final String TIME_REVISE = "TIME";//时间设定
    private static final String ADD_TASK = "SET";//添加任务
    private static final String OPEN = "OPEN";//手动开启
    private static final String CLOSE = "CLOSE";//手动关闭
    private static final String DEL_TASK = "DEL";//删除任务
    private static final String QUERY_TASK = "INQTASK";//查询任务
    private static final String QUERY_TIME = "INQTIME";//查询时间
    private static final String INVALID = "INVALID";//无效指令
    Timer mTimer1 = null;//定时器
    TimerTask mTask1 = null;
    List<Task> tasks = new ArrayList<>();//任务列表


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //权限检测
//        checkPermission();
        //打卡本地蓝牙并设置可搜索
        if (checkBlueTooth()) {
            onConnect();
        }

        initData();

    }

    private void initData() {

//        final List<Task> tasks = new ArrayList<>();
        mAdapter = new MyTaskAdapter(this, tasks);
        mAdapter.setOnremoveListnner(new MyTaskAdapter.OnremoveListnner() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void ondelect(final int i) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("确定删除任务" + tasks.get(i).getNum() + "吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                tasks.remove(i);
                                doDelTask(tasks.get(i).getNum());
                                mAdapter.notifyDataSetChanged();
//                                Toast.makeText(MainActivity.this, "" + i, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();


            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        mTaskRecyclerView.addItemDecoration(Divider.builder().
                color(getResources().getColor(R.color.bg_shadow))
                .height(1)
                .build());
        mTaskRecyclerView.setLayoutManager(manager);
        mTaskRecyclerView.setAdapter(mAdapter);

    }

    /**
     * 初始化view
     */
    private void initView() {
        mReturnIB = (ImageButton) this.findViewById(R.id.return_back);
        mLinkTv = (TextView) this.findViewById(R.id.right_title);
        mTimeTv = (TextView) this.findViewById(R.id.time_data);
        mLayoutNotLinked = (RelativeLayout) this.findViewById(R.id.not_linked);
        mReviseBtn = (Button) this.findViewById(R.id.revise_btn);
        mStartBtn = (Button) this.findViewById(R.id.start_btn);
        mStopBtn = (Button) this.findViewById(R.id.stop_btn);
        mSeekBar = (IndicatorSeekBar) this.findViewById(R.id.percent_indicator);
        mAddIv = (ImageView) this.findViewById(R.id.add_iv);
        mTaskRecyclerView = (RecyclerView) this.findViewById(R.id.task_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipeRefreshLayout);

        mReturnIB.setImageResource(R.drawable.refresh);
        mReturnIB.setOnClickListener(this);
        mLinkTv.setOnClickListener(this);
        mReviseBtn.setOnClickListener(this);
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mAddIv.setOnClickListener(this);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.my_blue));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTasks();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        refreshView();
        if (TextUtils.isEmpty(address)) {
            return;
        }
//        linkBluetooth(address);
//        //查询设备时间
//        getDeviceTime();
    }

    //关闭程序调用处理部分
    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (_socket != null)  //关闭连接socket
//        {
//            try {
//                _socket.close();
//            } catch (IOException e) {
//            }
//        }
        onQuitButtonClicked();
        //	_bluetooth.disable();  //关闭蓝牙服务
    }

    // 监视键盘的返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (_socket != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("确定退出吗？")
                        .setMessage("退出后将自动断开蓝牙连接")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onQuitButtonClicked();
                            }
                        })
                        .setNegativeButton("取消", null);
                builder.show();
                return false;
            } else {
                return super.onKeyDown(keyCode, event);
            }

        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     *  退出按键响应函数
     */

    public void onQuitButtonClicked() {

        //---安全关闭蓝牙连接再退出，避免报异常----//
        if (_socket != null) {
            //关闭连接socket
            try {
                bRun = false;
//                Thread.sleep(1000);

                is.close();
                _socket.close();
                _socket = null;
            } catch (IOException e) {
            }
        }

        finish();
    }

    /**
     * 打卡本地蓝牙并设置可搜索
     */
    private Boolean checkBlueTooth() {
        final boolean[] result = {false};
        //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null) {
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        // 设置设备可以被搜索
        new Thread() {
            @Override
            public void run() {
                if (_bluetooth.isEnabled() == false) {
                    _bluetooth.enable();
                }
            }
        }.start();
        return true;
    }

    //连接蓝牙
    public void onConnect() {

        if (_bluetooth.isEnabled() == false) {  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            return;
        }

        //如未连接设备则打开DeviceListActivity进行设备搜索
        if (_socket == null) {
            Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
        } else {
            //关闭连接socket
            try {
                bRun = false;
                Thread.sleep(2000);

                is.close();
                _socket.close();
                _socket = null;
                Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义

            } catch (IOException e) {
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    //接收活动结果，响应startActivityForResult()
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    linkBluetooth(address);
                    //查询设备时间
//                    getDeviceTime();
                    //获取任务列表
//                    getTasks();
//                    doDelTask(2);

                }
                break;
            default:
                break;
        }
    }

    private void linkBluetooth(String address) {
        // 得到蓝牙设备句柄
        _device = _bluetooth.getRemoteDevice(address);

        // 用服务号得到socket
        try {
            _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));

        } catch (IOException e) {
            isLinked = false;
            Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
        }
        //连接socket
//                    Button btn = (Button) findViewById(R.id.BtnConnect);
        try {
            isLinked = true;
            _socket.connect();
            Toast.makeText(this, "连接" + _device.getName() + "成功！", Toast.LENGTH_SHORT).show();
//                        btn.setText("断开");
        } catch (IOException e) {
            try {
                isLinked = false;
                Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                _socket.close();
                _socket = null;
            } catch (IOException ee) {
                isLinked = false;
                Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
            }

            return;
        }


        //打开接收线程
        try {
            is = _socket.getInputStream();   //得到蓝牙数据输入流
        } catch (IOException e) {
            Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bThread == false) {
            readThread.start();
            bThread = true;
        } else {
            bRun = true;
        }
        mCommandUtil = new CommandUtil(this, _socket);
        //查询设备时间
        getDeviceTime();
        //刷新界面
        refreshView();
    }

    /**
     * 根据蓝牙是否连接成功，刷新界面
     */
    private void refreshView() {
        if (isLinked) {
            mLayoutNotLinked.setVisibility(View.GONE);
        } else {
            mLayoutNotLinked.setVisibility(View.VISIBLE);
        }
    }

    //接收数据线程
    Thread readThread = new Thread() {

        @Override
        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            //接收线程
            while (true) {
                try {
                    while (is.available() == 0) {
                        while (bRun == false) {
                        }
                    }
                    while (true) {
                        if (!bThread)//跳出循环
                        {
                            return;
                        }

                        num = is.read(buffer);         //读入数据
                        n = 0;

                        String s0 = new String(buffer, 0, num);
                        fmsg += s0;    //保存收到数据
                        for (i = 0; i < num; i++) {
                            if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
                                buffer_new[n] = 0x0a;
                                i++;
                            } else {
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new, 0, n);
                        smsg += s;   //写入接收缓存
                        if (is.available() == 0) {
                            break;  //短时间没有数据才跳出进行显示
                        }
                    }
                    //发送显示消息，进行显示刷新
//                    Message message = Message.obtain();
//                    message.obj = smsg;
//                    handler.sendMessage(message);
                    handler.sendMessage(Message.obtain());

                    //此处处理板卡返回的信息
                } catch (IOException e) {
                }
            }
        }
    };

    //消息处理队列
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            System.out.println("1111111111111111111msg = " + smsg);
//            String receiveMsg = msg.obj.toString();
            String receiveMsg = smsg;
            smsg = "";
            //将收到的指令分成三段
            String[] msgArr = receiveMsg.split(":");
            //获取指令标志，并根据标志匹配指令
            if (msgArr.length < 1) {
                return;
            }
            String data = "";
            if (msgArr.length >= 3) {
                data = msgArr[2].replaceAll("\n", "");
            }
            switch (msgArr[0]) {
                case TIME_REVISE:
                    //时间设定
                    Toast.makeText(MainActivity.this, "时间已校正" + data, Toast.LENGTH_SHORT).show();
                    startTimeTask(data);
                    break;
                case ADD_TASK:
                    Toast.makeText(MainActivity.this, "任务设置成功", Toast.LENGTH_SHORT).show();
                    //添加任务
                    if (AddActivity.instance == null) {
                        return;
                    }

                    AddActivity.instance.finish();
                    getTasks();

                    break;
                case OPEN:
                    //手动开启
                    Toast.makeText(MainActivity.this, "开启成功", Toast.LENGTH_SHORT).show();
                    mSeekBar.setProgress(Float.parseFloat(data));
                    break;
                case CLOSE:
                    //手动关闭
                    Toast.makeText(MainActivity.this, "关闭成功", Toast.LENGTH_SHORT).show();
                    break;
                case DEL_TASK:
                    //删除任务
                    Toast.makeText(MainActivity.this, "任务已删除", Toast.LENGTH_SHORT).show();
                    getTasks();
                    break;
                case QUERY_TASK:
                    //查询任务
                    //关闭下拉刷新
                    mSwipeRefreshLayout.setRefreshing(false);
                    String[] taskArr = data.split("\\&");
                    if (taskArr == null || taskArr.length < 1) {
                        return;
                    }
                    tasks.clear();
                    //根据返回的指令，获取task信息
                    for (int i = 1; i < taskArr.length; i++) {
                        String taskDetailStr = taskArr[i];
                        String[] taskDetailArr = taskDetailStr.split(",");
                        if (taskDetailArr == null || taskDetailArr.length < 1) {
                            return;
                        }

                        Task task = new Task();
                        try {
                            task.setNum(Integer.parseInt(taskDetailArr[0]));
                            task.setType(Integer.parseInt(taskDetailArr[1]));
                            //获取重复日期总天数
                            int dayNum = Integer.parseInt(taskDetailArr[2]);
                            List<Integer> days = new ArrayList<>();
                            //取出重复日期列表
                            for (int d = 3; d < 3 + dayNum; d++) {
                                days.add(Integer.parseInt(taskDetailArr[d]));
                            }
                            task.setDays(days);
                            task.setHour(Integer.parseInt(taskDetailArr[3 + dayNum]));
                            task.setMinute(Integer.parseInt(taskDetailArr[3 + dayNum + 1]));
                            task.setSec(Integer.parseInt(taskDetailArr[3 + dayNum + 2]));
                            task.setYield(Integer.parseInt(taskDetailArr[3 + dayNum + 3]));
                            task.setTime(Integer.parseInt(taskDetailArr[3 + dayNum + 4]));
                        } catch (Exception e) {

                        }

                        tasks.add(task);
                        updateTaskList(tasks);
                        mSwipeRefreshLayout.setRefreshing(false);

                    }

                    break;
                case QUERY_TIME:
                    //查询时间
                    Toast.makeText(MainActivity.this, "设备时间已获取", Toast.LENGTH_SHORT).show();
                    startTimeTask(data);
                    //开启查询任务指令
                    getTasks();
                    break;
                case INVALID:
                    //无效指令
                    Toast.makeText(MainActivity.this, "无效指令：" + data, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    //刷新设备列表
    private void updateTaskList(List<Task> tasks) {
        mAdapter.updatedatas(tasks);
        mAdapter.notifyDataSetChanged();
    }


    private void startTimeTask(String data) {
        Calendar c = Calendar.getInstance();
        try {
            //将字符串数据转化为毫秒数
            c.setTime(new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss").parse(data));
//                        System.out.println("时间转化后的毫秒数为：" + c.getTimeInMillis());
            millisTime = c.getTimeInMillis();

            // 将毫秒数转化为时间
            mTimeTv.setText(updateDeviceTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //开启计时器

        if (mTimer1 == null && mTask1 == null) {
            mTimer1 = new Timer();
            mTask1 = new TimerTask() {
                @Override
                public void run() {
                    Message message = timeHandler.obtainMessage(1);
                    timeHandler.sendMessage(message);
                }
            };
            mTimer1.schedule(mTask1, 0, 1000);
        }
    }

    /**
     * 更新设备时间
     *
     * @return
     */
    private String updateDeviceTime() {
        String time;
        Date date = new Date(millisTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        System.out.println("毫秒数转化后的时间为：" + sdf.format(date));
        time = sdf.format(date);
        return time;
    }

    //设备时间显示
    Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            millisTime += 1000;
            mTimeTv.setText(updateDeviceTime());

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_back:
                getDeviceTime();
                mSwipeRefreshLayout.setRefreshing(true);
                break;
            case R.id.right_title:
                onConnect();
                break;
            case R.id.revise_btn:
                doRevise();
                break;
            case R.id.start_btn:
                doStart();
                break;
            case R.id.stop_btn:
                doStop();
                break;
            case R.id.add_iv:
                goAddActivity();
                break;
            default:
                break;

        }
    }

    //查询设备时间
    private void getDeviceTime() {
        new CommandUtil(this, _socket).sendGetDeviceTime();
    }

    //获取任务列表
    private void getTasks() {
        new CommandUtil(this, _socket).sendGetTasks();
    }

    /**
     * 跳转到添加任务页面
     */
    private void goAddActivity() {
        mCommandUtil = new CommandUtil(this, _socket);
        //新增任务时，判断新增的任务编号，若编号队列中有空缺，则为空缺，若无空缺，则为末尾编号加1
        int addNum = 0;
        if (tasks == null || tasks.size() < 1) {
            //当无任务列表时，新增任务编号为1
            addNum = 1;
        } else if (tasks.size() > 20) {
            Toast.makeText(MainActivity.this, "当前已存在20条任务，不能再新增，请删除部分任务再试吧", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (tasks.get(0).getNum() != 1) {
                //编号1空缺，则新增编号为编号1
                addNum = 1;
            } else {
                for (int i = 1; i < tasks.size(); i++) {
                    //中途空缺，这新增编号为空缺编号
                    if (tasks.get(i).getNum() - tasks.get(i - 1).getNum() > 1) {
                        addNum = tasks.get(i - 1).getNum() + 1;
                        break;
                    }
                    //编号无空缺，则新增编号为末尾编号加1
                    addNum = tasks.get(tasks.size() - 1).getNum() + 1;
                }
            }
        }

        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        intent.putExtra("num", addNum);
        intent.putExtra("type", "add");
        this.startActivity(intent);
    }

    /**
     * 时间校正
     */
    private void doRevise() {
        //获取系统时间
        Date date = new Date(System.currentTimeMillis());
        new CommandUtil(this, _socket).sendTimeRevise(date);
    }

    /**
     * 手动开启
     */
    private void doStart() {
        //获取用户设置进度
        int yield = mSeekBar.getProgress();
        new CommandUtil(this, _socket).sendStart(yield);
    }

    /**
     * 手动关闭
     */
    private void doStop() {
        new CommandUtil(this, _socket).sendStop();
    }

    /**
     * 删除任务
     */
    private void doDelTask(int num) {
        new CommandUtil(this, _socket).sendDelTask(num);
    }


}
