package com.cjt.bluetoothscm;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*****************
 * 包名：com.cjt.bluetoothscm
 * 类名：RecycleAdapter.java
 * 时间：2018/9/14  10:14
 * 作者：Cao Jiangtao
 * 首页：https://1989jiangtao.github.io/index.html
 ******************/
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyViewHolder> {

    public static final String EXTRA_DEVICE = "extra_device";

    List<BluetoothDevice> deviceList = new ArrayList<>();

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("CJT"," RecycleAdapter --- onCreateViewHolder --  ");
        return new RecycleAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.deviceName.setText(getItem(position).getName());
        holder.deviceAddress.setText(getItem(position).getAddress());
        // 为列表添加点击事件，传入的参数是当前项的蓝牙设备BluetoothDevice
        holder.deviceItem.setOnClickListener(new itemClick(getItem(position)));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    /***
     * 获取指定位置的元素
     * @param position
     * @return
     */
    private BluetoothDevice getItem(int position){
        return deviceList.get(position);
    }

    /***
     * 数据集合改变的方法
     * @param deviceList
     */
    public void notifyData(List<BluetoothDevice> deviceList){
        Log.d("CJT" , "notifyData == %%%%%%%%%%%%%% ");
        this.deviceList = deviceList ;
        this.notifyDataSetChanged();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout deviceItem ; // item的单个布局
        TextView deviceName ; // 设备名称
        TextView deviceAddress; // 设备地址

        public MyViewHolder(View itemView) {
            super(itemView);
            deviceItem = itemView.findViewById(R.id.item_layout);
            deviceName = itemView.findViewById(R.id.item_device_name);
            deviceAddress = itemView.findViewById(R.id.item_device_address);
        }
    }

    /**点击事件内部类**/
    private class itemClick implements View.OnClickListener {

        private BluetoothDevice device ;

        public itemClick(BluetoothDevice item) {
            this.device = item ;
        }

        @Override
        public void onClick(View v) {
            // 设置返回数据
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE, device);
            Log.d("CJT" , "device.getAddress() == "+device.getAddress());
            // 设置返回值并结束程序
            ((Activity)v.getContext()).setResult(Activity.RESULT_OK, intent);
            ((Activity)v.getContext()).finish();
        }
    }
}
