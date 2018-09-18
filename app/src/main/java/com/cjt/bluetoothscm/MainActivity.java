package com.cjt.bluetoothscm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;

import java.io.IOException;
import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

/*****************
 * 包名：com.cjt.bluetoothscm
 * 类名：MainActivity.java
 * 时间：2018/9/11  23:28
 * 作者：Cao Jiangtao
 * 首页：https://1989jiangtao.github.io/index.html
 ******************/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CONNECT_DEVICE = 0x100;

    TextView mainTitle ;

    // 温度显示仪表盘
    DashboardView tempView ;
    private final static int invs[] = {35, 18, 35};
    private final static int[] colorRes = {R.color.arc1, R.color.arc2, R.color.arc3};

    // 灯组01 ，灯组02 ， 电源开关， 风扇开关
    SwitchIconView lamp01 , lamp02 , powerSw , fanSw ;
    TextView lamp01Name , lamp02Name ,powerName , fanName;

    // 蓝牙通信的地址和两个UUID
    String MAC = "" ;
    UUID serviceUuid , characterUuid ;

    // 经典蓝牙支持
//    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ;
//    BluetoothDevice bluetoothDevice ;
//    BluetoothSocket bluetoothSocket;
//    private static final String CLASSIC_BLUETOOTH_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        // 运行的时候检查是否打开蓝牙，没有打开就开启蓝牙
        if(!MyApp.getBluetoothClient().isBluetoothOpened())
            MyApp.getBluetoothClient().openBluetooth();

    }

    private void initView() {

        mainTitle = findViewById(R.id.main_title);
        tempView = findViewById(R.id.temp_view);
        lamp01 = findViewById(R.id.sw_lamp_01);
        lamp02 = findViewById(R.id.sw_lamp_02);
        powerSw = findViewById(R.id.sw_power);
        fanSw = findViewById(R.id.sw_fan);
        lamp01Name = findViewById(R.id.lamp_01_name);
        lamp02Name = findViewById(R.id.lamp_02_name);
        powerName = findViewById(R.id.power_name);
        fanName = findViewById(R.id.fan_name);

        // 为按钮设置点击事件
        lamp01.setOnClickListener(this);
        lamp02.setOnClickListener(this);
        powerSw.setOnClickListener(this);
        fanSw.setOnClickListener(this);

        // 初始化温度表盘
        String[] str = getResources().getStringArray(R.array.mult_temp_dash);
        tempView.initDash(-20, invs, str, "℃", colorRes);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main , menu); // 加载菜单页面
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_scan){
            Intent intent = new Intent(MainActivity.this , ScanResultActivity.class);
            startActivityForResult(intent , REQUEST_CONNECT_DEVICE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // 关闭蓝牙
        MyApp.getBluetoothClient().closeBluetooth();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        // 当蓝牙有连接，并且MAC地址存在，两个UUID都不为空的情况下，点击按钮才有效
        // 以下只要有一个条件不满足，就不让点击按钮发送数据
        if(!MyApp.getBluetoothClient().isBleSupported()
                || TextUtils.isEmpty(MAC)
                || TextUtils.isEmpty(serviceUuid.toString())
                || TextUtils.isEmpty(characterUuid.toString())){
            Toast.makeText(MainActivity.this , "请先检查蓝牙设备与手机是否连接正常",Toast.LENGTH_SHORT).show();
            return;
        }

        switch (v.getId()){
            case R.id.sw_lamp_01: // 灯组01
                lamp01.switchState();
                lamp01Name.setText(lamp01.isIconEnabled() ? "灯组1开" : "灯组1关");
                writeCmd(MAC , serviceUuid , characterUuid , lamp01.isIconEnabled() ? "L1ON\r\n" :"L1OFF\r\n");
                break;
            case R.id.sw_lamp_02: // 灯组02
                lamp02.switchState();
                lamp02Name.setText(lamp02.isIconEnabled() ? "灯组1开" : "灯组1关");
                writeCmd(MAC , serviceUuid , characterUuid , lamp02.isIconEnabled() ? "L2ON\r\n" :"L2OFF\r\n");
                break;
            case R.id.sw_power: // 电源
                powerSw.switchState();
                powerName.setText(powerSw.isIconEnabled() ? "电源开" : "电源关");
                writeCmd(MAC , serviceUuid , characterUuid , powerSw.isIconEnabled() ? "ON\r\n" :"OFF\r\n");
                break;
            case R.id.sw_fan: // 风扇
                fanSw.switchState();
                fanName.setText(fanSw.isIconEnabled() ? "风扇开" : "风扇关");
                writeCmd(MAC , serviceUuid , characterUuid , fanSw.isIconEnabled() ? "FANON\r\n" :"FANOFF\r\n");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CJT" , "requestCode = "+ requestCode +" ,  resultCode = "+ resultCode + " , data ="+data);
        if(requestCode == REQUEST_CONNECT_DEVICE) {
            // 响应结果
            switch (resultCode) {

                case Activity.RESULT_CANCELED:
                    Toast.makeText(this , "取消了扫描！",Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_OK:
                    // 选择连接的设备
                    final BluetoothDevice device = data.getParcelableExtra(RecycleAdapter.EXTRA_DEVICE);
                    // 得到选择后传过来的MAC地址
                    MAC = device.getAddress();
                    Log.d("CJT" , "address ===================== " +MAC);

                    // 设置BLE设备的连接参数
                    BleConnectOptions options = new BleConnectOptions.Builder()
                            .setConnectRetry(3)   // 连接如果失败重试3次
                            .setConnectTimeout(30000)   // 连接超时30s
                            .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                            .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                            .build();

                    // 开始连接操作
                    MyApp.getBluetoothClient().connect(MAC, options, new BleConnectResponse() {
                        @Override
                        public void onResponse(int code, BleGattProfile data) {
                            Log.d("CJT" , "getBluetoothClient().connect  --- code ----- " + code);

                            // 表示连接成功
                            if(code == REQUEST_SUCCESS){

                                mainTitle.setText("当前连接设备 ："+device.getName());
//                                for(BleGattService sls : data.getServices()){
//                                    Log.d("CJT" , "onActivityResult -------1111111111-------  : "+sls.getUUID());
//                                    for(BleGattCharacter gls : sls.getCharacters()){
//                                        Log.d("CJT" , "onActivityResult *******22222222222*****  : "+gls.getUuid());
//                                    }
//                                }
                                serviceUuid = data.getServices().get(3).getUUID();
                                Log.d("CJT" , "getBluetoothClient().connect  --- serviceUuid  : "+serviceUuid);
                                characterUuid = data.getService(serviceUuid).getCharacters().get(0).getUuid();
                                Log.d("CJT" , "getBluetoothClient().connect  --- characterUuid : "+characterUuid);

                                // 获取温度值
                                getTemperature(MAC , serviceUuid , characterUuid);

                                // 下发数据
                                writeCmd(MAC , serviceUuid , characterUuid , "finish\r\n");

                            }else{
                                mainTitle.setText("当前暂无蓝牙设备连接");
                                Toast.makeText(MainActivity.this , "蓝牙连接不成功！",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    break;
            }

        }
    }

    /***
     * 获取温度值并显示到界面上
     * @param address           设备地址
     * @param serviceUuid       服务UUID
     * @param characterUuid     特征UUID
     */
    private void getTemperature(String address , UUID serviceUuid , UUID characterUuid ){
        MyApp.getBluetoothClient().notify(address, serviceUuid, characterUuid, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                    Log.d("CJT" , "getTemperature -- value2Str -- :" + new String(value));
                    if(new String(value).contains("power_off\r\n")){
                        Toast.makeText(MainActivity.this , "请打开电源开关",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String hexStr = bytesToHexString(value);
                    Log.d("CJT","getTemperature -- hexStr -- : "+hexStr);
                    if(!hexStr.contains("2e")) return ; // 不包含小数点
                    int beginIndex = hexStr.indexOf("2b") + 2; // 加号开始截取，并且跳过加号
                    int endIndex = hexStr.indexOf("2e") + 4 ;  // 小数点开始截取
                    String validTemp = hexStr.substring(beginIndex , endIndex );
                    Log.d("CJT" , "valid temp = "+validTemp+", hex2Str = "+ new String(hexStringToBytes(validTemp)));

                    // 设置温度值
                    tempView.setAngleWithAnim(Double.valueOf(new String(hexStringToBytes(validTemp))));
            }

            @Override
            public void onResponse(int code) {

            }
        });
    }

    /***
     * 向设备下发指令
     * @param address           设备MAC地址
     * @param serviceUuid       服务UUID
     * @param characterUuid     特征UUID
     * @param cmd               待下发的命令
     */
    private void writeCmd(String address , UUID serviceUuid , UUID characterUuid , String cmd){
        MyApp.getBluetoothClient().write(address, serviceUuid, characterUuid, cmd.getBytes(), new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if(code == Constants.REQUEST_SUCCESS){

                }
            }
        });
    }

    /**
     * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
