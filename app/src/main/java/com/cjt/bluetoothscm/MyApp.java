package com.cjt.bluetoothscm;

import android.app.Application;

import com.inuker.bluetooth.library.BluetoothClient;

/*****************
 * 包名：com.cjt.bluetoothscm
 * 类名：MyApp.java
 * 时间：2018/9/17  23:49
 * 作者：Cao Jiangtao
 * 首页：https://1989jiangtao.github.io/index.html
 ******************/

public class MyApp extends Application {

    private static MyApp instance ;

    private static BluetoothClient bluetoothClient ;

    public static MyApp getInstance() {
        return instance;
    }

    public static BluetoothClient getBluetoothClient() {
        return bluetoothClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this ;

        // 新建全局的蓝牙客户端实例
        bluetoothClient = new BluetoothClient(this);

    }

}
