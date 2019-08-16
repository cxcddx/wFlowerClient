package com.client.cx.wflowerclient.util;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.client.cx.wflowerclient.bean.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

/**
 * @author cx
 * @class describe
 * @time 2019/8/9 8:48
 */
public class CommandUtil {
    private static final String TIME_REVISE = "TIME";//时间设定
    private static final String ADD_TASK = "SET";//添加任务
    private static final String OPEN = "OPEN";//手动开启
    private static final String CLOSE = "CLOSE";//手动关闭
    private static final String DEL_TASK = "DEL";//删除任务
    private static final String QUERY_TASK = "INQTASK";//查询任务
    private static final String QUERY_TIME = "INQTIME";//查询时间

    private BluetoothSocket _socket;
    private Context mContext;

    public CommandUtil(Context context, BluetoothSocket _socket) {
        this._socket = _socket;
        this.mContext = context;
    }


    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMsg(String msg) {
        int i = 0;
        int n = 0;
        if (_socket == null) {
            Toast.makeText(mContext, "请先连接HC模块", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(mContext, "请先输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        try {

            OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
            byte[] bos = msg.getBytes();
            for (i = 0; i < bos.length; i++) {
                if (bos[i] == 0x0a) {
                    n++;
                }
            }
            byte[] bos_new = new byte[bos.length + n];
            n = 0;
            for (i = 0; i < bos.length; i++) { //手机中换行为0a,将其改为0d 0a后再发送
                if (bos[i] == 0x0a) {
                    bos_new[n] = 0x0d;
                    n++;
                    bos_new[n] = 0x0a;
                } else {
                    bos_new[n] = bos[i];
                }
                n++;
            }

            os.write(bos_new);
        } catch (IOException e) {
        }
    }

    /**
     * 时间校正
     */
    public void sendTimeRevise(Date date) {
        Calendar calendar = Calendar.getInstance();
        //获取系统的日期
        //年
        int year = calendar.get(Calendar.YEAR);
        //月
        int month = calendar.get(Calendar.MONTH) + 1;
        //日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //获取系统时间
        //小时
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //分钟
        int minute = calendar.get(Calendar.MINUTE);
        //秒
        int second = calendar.get(Calendar.SECOND);
        StringBuilder str = new StringBuilder();
        str.append(TIME_REVISE).append(":").append(year).append(",").append(month).append(",")
                .append(day).append(",").append(hour).append(",").append(minute).append(",")
                .append(second).append("\n");
        sendMsg(str.toString());
    }

    /**
     * 手动开启
     * @param yield 喷水量
     */
    public void sendStart(int yield) {
        StringBuilder str = new StringBuilder();
        str.append(OPEN).append(":").append(yield).append("\n");
        sendMsg(str.toString());
    }

    /**
     * 手动关闭
     */
    public void sendStop() {
        StringBuilder str = new StringBuilder();
        str.append(CLOSE).append("\n");
        sendMsg(str.toString());
    }

    /**
     * 设置任务
     */
    public void sendSetTask(Task task) {
        StringBuilder dayStr = new StringBuilder();
        //默认为0秒
        int second = 0;
        for (int i = 0; i < task.getDays().size(); i++) {
            dayStr.append(task.getDays().get(i));
            if (i < task.getDays().size()-1) {
                dayStr.append(",");
            }
        }
//        dayStr.append(days.get(days.size() -1));
        StringBuilder str = new StringBuilder();
        str.append(ADD_TASK).append(":").append(task.getNum()).append(",").append(task.getType()).append(",").append(task.getDays().size()).append(",")
                .append(dayStr).append(",").append(task.getHour()).append(",").append(task.getMinute()).append(",").append(second)
                .append(",").append(task.getYield()).append(",").append(task.getTime()).append("\n");
        sendMsg(str.toString());

    }

    /**
     * 查询设备时间
     */
    public void sendGetDeviceTime() {
        StringBuilder str = new StringBuilder();
        str.append(QUERY_TIME).append("\n");
        sendMsg(str.toString());
    }
    /**
     * 获取任务列表
     */
    public void sendGetTasks() {
        StringBuilder str = new StringBuilder();
        str.append(QUERY_TASK).append("\n");
        sendMsg(str.toString());
    }
    /**
     * 删除任务
     */
    public void sendDelTask(int num) {
        StringBuilder str = new StringBuilder();
        str.append(DEL_TASK).append(":").append(num).append("\n");
        sendMsg(str.toString());
    }


}
