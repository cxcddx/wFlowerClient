package com.client.cx.wflowerclient.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.client.cx.wflowerclient.R;
import com.client.cx.wflowerclient.util.CommandUtil;
import com.warkiz.widget.IndicatorSeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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

    private ImageButton mReturnIB;
    private TextView mLinkTv;
    private RelativeLayout mLayoutNotLinked;
    private Button mReviseBtn, mStartBtn, mStopBtn;
    private IndicatorSeekBar mSeekBar;
    private ImageView mAddIv;

    public static CommandUtil mCommandUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //权限检测
        checkPermission();
        //打卡本地蓝牙并设置可搜索
        if (checkBlueTooth()) {
            onConnect();
            mCommandUtil = new CommandUtil(this, _socket);

            //查询设备时间
            getDeviceTime();
        }

    }

    /**
     * 初始化view
     */
    private void initView() {
        mReturnIB = (ImageButton) this.findViewById(R.id.return_back);
        mLinkTv = (TextView) this.findViewById(R.id.right_title);
        mLayoutNotLinked = (RelativeLayout) this.findViewById(R.id.not_linked);
        mReviseBtn = (Button) this.findViewById(R.id.revise_btn);
        mStartBtn = (Button) this.findViewById(R.id.start_btn);
        mStopBtn = (Button) this.findViewById(R.id.stop_btn);
        mSeekBar = (IndicatorSeekBar) this.findViewById(R.id.percent_indicator);
        mAddIv = (ImageView) this.findViewById(R.id.add_iv);

        mReturnIB.setOnClickListener(this);
        mLinkTv.setOnClickListener(this);
        mReviseBtn.setOnClickListener(this);
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mAddIv.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
//        refreshView();
        if (TextUtils.isEmpty(address)) {
            return;
        }
        linkBluetooth(address);
    }

    //关闭程序调用处理部分
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_socket != null)  //关闭连接socket
        {
            try {
                _socket.close();
            } catch (IOException e) {
            }
        }
        //	_bluetooth.disable();  //关闭蓝牙服务
    }

    /**
     * 权限检测
     */
    private void checkPermission() {
    /* 解决兼容性问题，6.0以上使用新的API*/
        final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
        final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
                Log.e("11111", "ACCESS_COARSE_LOCATION");
            }
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
                Log.e("11111", "ACCESS_FINE_LOCATION");
            }
        }
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
        //刷新界面
        refreshView();
    }

    /**
     * 根据蓝牙是否连接成功，刷新界面
     */
    private void refreshView() {
//        if(isLinked) {
//            mLayoutNotLinked.setVisibility(View.GONE);
//        } else {
//            mLayoutNotLinked.setVisibility(View.VISIBLE);
//        }
    }

    //接收数据线程
    Thread readThread = new Thread() {

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
//                    handler.sendMessage(handler.obtainMessage());
//                    String b = smsg;
                    receiveMsg(smsg);
                    //此处处理板卡返回的信息
                } catch (IOException e) {
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_back:
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

    /**
     * 跳转到添加任务页面
     */
    private void goAddActivity() {
        mCommandUtil = new CommandUtil(this, _socket);
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
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
     * 处理接受到的消息
     * @param smsg
     */
    private void receiveMsg(String smsg) {
    }


}
