package com.cjt.bluetoothscm;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/*****************
 * 包名：com.cjt.bluetoothscm
 * 类名：ScanResultActivity.java
 * 时间：2018/9/18  0:27
 * 作者：Cao Jiangtao
 * 首页：https://1989jiangtao.github.io/index.html
 ******************/
public class ScanResultActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView ; // 列表展示扫描的结果

    ProgressBar progressBar ; // 页面上的进度条

    Button exitBtn ; // 退出按钮

    RecycleAdapter adapter ; // 列表适配器

    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        initView(); // 界面初始化

        startScan(); // 开启扫描

    }

    @Override
    protected void onDestroy() {
        // 停止扫描
        MyApp.getBluetoothClient().stopSearch();
        super.onDestroy();
    }

    /**开启蓝牙扫描**/
    private void startScan() {
        // 新建一个扫描结果集
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        // 开始扫描
        MyApp.getBluetoothClient().search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                progressBar.setVisibility(View.VISIBLE);
                Log.d("CJT" , "onSearchStarted ***** ");
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Log.d("CJT" , "onDeviceFounded ==   name :"+ device.device.getName()+", address : " + device.device.getAddress());
                if(!TextUtils.isEmpty(device.device.getName())){
                    bluetoothDeviceList.add(device.device);
                }
            }

            @Override
            public void onSearchStopped() {
                progressBar.setVisibility(View.GONE);
                // 实时更新列表适配器
                adapter.notifyData(removeDuplicate(bluetoothDeviceList));
                Log.d("CJT" , "onSearchStopped ######  设备数量 ："+removeDuplicate(bluetoothDeviceList).size());
            }

            @Override
            public void onSearchCanceled() {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    /***
     * 列表去重复
     * @param list 待去除重复数据的列表
     * @return      返回去重后的列表
     */
    public static List removeDuplicate(List list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
        return list;
    }

    /**初始化页面和控件**/
    private void initView(){
        // 初始化页面控件
        recyclerView = findViewById(R.id.result_recycle_list);
        progressBar = findViewById(R.id.progressBar);
        exitBtn = findViewById(R.id.btn_exit);

        exitBtn.setOnClickListener(this);

        // 设置recycleView显示方式
        recyclerView.setLayoutManager(new LinearLayoutManager(this , LinearLayoutManager.VERTICAL , false));

        adapter = new RecycleAdapter();
        // 设置适配器
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
