package com.client.cx.wflowerclient.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.client.cx.wflowerclient.R;
import com.client.cx.wflowerclient.activity.AddActivity;
import com.client.cx.wflowerclient.activity.MainActivity;
import com.client.cx.wflowerclient.bean.Task;
import com.client.cx.wflowerclient.util.Constance;

import java.util.List;

/**
 * @author cx
 * @class describe 日期列表adapter
 * @time 2019/8/14 9:57
 */
public class MyTaskAdapter extends RecyclerView.Adapter<MyTaskAdapter.MyViewHolder> {
    private Context mContext;
    private List<Task> datas;


    public MyTaskAdapter(Context context, List<Task> datas) {
        this.mContext = context;
        this.datas = datas;
    }

    public void updatedatas(List<Task> datas) {
        this.datas = datas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_task_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (datas == null || datas.size() <1) {
            return;
        }
        holder.mTime.setText(datas.get(position).getHour() + ":" + datas.get(position).getMinute());
        holder.mTaskNum.setText(datas.get(position).getNum() + "");
        holder.mRepMsg.setText(datas.get(position).getRepMsg());
        holder.mYield.setText(datas.get(position).getYield() + "%");
        holder.mYieldTime.setText(datas.get(position).getTime() + "s");
        int type = datas.get(position).getType();
        if (type == Constance.MOUTH_EXE || type == Constance.WEEK_EXE) {
            holder.mOcChk.setChecked(true);
        } else {
            holder.mOcChk.setChecked(false);
        }

        holder.mOcChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task task = datas.get(position);
                int type = datas.get(position).getType();
                if (type == Constance.MOUTH_EXE || type == Constance.MOUTH_INEXE) {
                    if (((CheckBox)v).isChecked()) {
                        task.setType(Constance.MOUTH_EXE);
                    } else {
                        task.setType(Constance.MOUTH_INEXE);
                    }
                } else {
                    if (((CheckBox)v).isChecked()) {
                        task.setType(Constance.WEEK_EXE);
                    } else {
                        task.setType(Constance.WEEK_INEXE);
                    }
                }
                MainActivity.mCommandUtil.sendSetTask(task);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onremoveListnner != null) {
                    onremoveListnner.ondelect(position);
                }

                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddActivity.class);
                intent.putExtra("type", "edit");
                intent.putExtra("task",  datas.get(position));
                mContext.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTaskNum, mRepMsg, mTime, mYield, mYieldTime;
        private CheckBox mOcChk;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTaskNum = itemView.findViewById(R.id.task_num);
            mRepMsg = itemView.findViewById(R.id.rep_msg);
            mTime = itemView.findViewById(R.id.tvTime);
            mYield = itemView.findViewById(R.id.tv_yield);
            mYieldTime = itemView.findViewById(R.id.tv_yield_time);
            mOcChk = itemView.findViewById(R.id.ocChk);
        }
    }

    public interface OnremoveListnner {
        void ondelect(int i);
    }

    private OnremoveListnner onremoveListnner;

    public void setOnremoveListnner(OnremoveListnner onremoveListnner) {
        this.onremoveListnner = onremoveListnner;
    }
}

