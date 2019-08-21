package com.client.cx.wflowerclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.client.cx.wflowerclient.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cx
 * @class describe 日期列表adapter
 * @time 2019/8/14 9:57
 */
public class MyDateAdapter extends RecyclerView.Adapter<MyDateAdapter.MyViewHolder> {
    private List<Integer> dateList = new ArrayList<>();
    private List<Integer> initCheckedList = new ArrayList<>();
    private Context mContext;
    SparseBooleanArray mSelectedPositions = new SparseBooleanArray();


    public MyDateAdapter(Context context, List<Integer> dateList, List<Integer> checkedList) {
        this.mContext = context;
        this.dateList = dateList;
        this.initCheckedList = checkedList;
        if (!(initCheckedList != null && initCheckedList.size() > 0)) {
            for (int i = 0; i < dateList.size(); i++) {
                setItemChecked(i, false);
            }
        } else {
            for (int i = 0; i < dateList.size(); i++) {
                if (checkedList.contains(dateList.get(i))) {
                    setItemChecked(i, true);
                } else {
                    setItemChecked(i, false);
                }
            }
        }
    }

    public void setDateList(List<Integer> dates) {
        for (int i = 0; i < dateList.size(); i++) {
            setItemChecked(i, false);
        }
        this.dateList = dates;
    }

    public List<Integer> getSelectList() {
        List<Integer> selectDates = new ArrayList<>();
        for (int i = 0; i < dateList.size(); i++) {
            if (isItemChecked(i)) {
                selectDates.add(i + 1);
            }
        }
        return selectDates;
    }

    //设置选中状态
    private void setItemChecked(int position, boolean isChecked) {
        mSelectedPositions.put(position, isChecked);
    }

    //根据位置判断条目是否选中
    private boolean isItemChecked(int position) {
        return mSelectedPositions.get(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_datelist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.mDateChk.setText(dateList.get(position) + "");
        holder.mDateChk.setChecked(isItemChecked(position));
        holder.mDateChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isItemChecked(position)) {
                    setItemChecked(position, false);
                } else {
                    setItemChecked(position, true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mDateChk;

        public MyViewHolder(View itemView) {
            super(itemView);
            mDateChk = itemView.findViewById(R.id.chk_date);
        }
    }
}
