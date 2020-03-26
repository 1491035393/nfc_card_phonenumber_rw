package com.xinhaosi.nfcdemo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class NfcTestActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button2;
    private Button button3;
    /**
     * 密码
     */
    private EditText password_text;
    /**
     * 读取的扇区
     */
    private EditText block_text;
    /**
     * 读取的块
     */
    private EditText piece_text;
    /**
     * 写的扇区
     */
    private EditText write_block_text;
    /**
     * 写的块
     */
    private EditText write_piece_text;
    /**
     * 写的内容
     */
    private EditText write_text;
    /**
     * 改密码的扇区
     */
    private EditText change_block_text;
    /**
     * 新密码
     */
    private EditText new_password_text;
    /**
     * 重复新密码
     */
    /*一些变量 2020.3.24*/
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    private IntentFilter[] mFilters;

    private EditText repeat_new_password_text;

    private TextView ReadData;
    private LinearLayout read_data;
    private LinearLayout write;
    private LinearLayout changePassword;
    private int flag = 1;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                toToast(msg.getData().getString("data"));
            } else if (msg.what == 2) {
                ReadData.setText(msg.getData().getString("data"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_test);
        init();
        initData();
        //调用这个函数以获得一个nfcadapter对象，用于和nfc系统模块交互，2020.3.24
         mAdapter = NfcAdapter.getDefaultAdapter(this);
        //构造一个pendingintent供nfc系统模块派发, 2020.3.24
         mPendingIntent = PendingIntent.getActivity(this,0,new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
        //监听ACTION_NDEF_DISCOVERED通知，并设置MIME类型为“*/*”，对任何MIME类型的NDEF消息都感兴趣,2020.3.24
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        //ndef.addDataType("*/*");

        //同时监听ACTION_TECH_DISCOVERED和ACTION_TAG_DISCOVERED通知 2020.3.24
        mFilters = new IntentFilter[]{
                ndef,
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
        };

        //对于ACTION_TECH_DISCOVERED通知来说，还需要注册对那些Tag Technology感兴趣，2020.3.24
        mTechLists = new String[][]{
                new String[]{NfcA.class.getName()},
                new String[]{MifareClassic.class.getName()}
        };
    }
    /*
    * 将oncreate中设置的配置信息传递给NFC系统模块。2020.3.24
    */
    public void onResume(){
        super.onResume();
        //调用NfcAdapter的enableForegroundDispath函数启动前台分发系统。同时需要将分发条件传递给NFC系统模块
        mAdapter.enableForegroundDispatch(this,mPendingIntent,mFilters,mTechLists);
    }

    /**
     * 初始化控件
     */
    private void init() {
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        write_text = (EditText) findViewById(R.id.write_text);
        read_data = (LinearLayout) findViewById(R.id.read_data);
        write = (LinearLayout) findViewById(R.id.write);
        ReadData = (TextView) findViewById(R.id.data);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (null == adapter) {
            Toast.makeText(this, "不支持NFC功能", Toast.LENGTH_SHORT).show();
        } else if (!adapter.isEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            // 根据包名打开对应的设置界面
            startActivity(intent);
        }
    }

    /**
     * 每次刷NFC都会进这个方法
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ReadData.setText("");
        switch (flag) {
            //读取特定数据
            case 1:
                readData(intent);
                break;
            //写入数据
            case 2:
                writeData(intent);
                break;
            default:
        }
    }

    /**
     * 写入数据
     */
    private void writeData(Intent intent) {
        String password = "";
        //String password = password_text.getText().toString().trim();2020.3.25
        String data = write_text.getText().toString().trim();
        /*
        2020.3.25
        if (write_block_text.getText().toString().trim().equals("")) {
            return;
        }
        if (write_piece_text.getText().toString().trim().equals("")) {
            return;
        }
        int block = Integer.parseInt(write_block_text.getText().toString().trim());
        int piece = Integer.parseInt(write_piece_text.getText().toString().trim());
        */
        int block = 2;
        int piece = 0;
        NFCWriteHelper.getInstence(intent)
                .setReadPassword(password)
                .writeData(data, block, piece, new NFCWriteHelper.NFCCallback() {
                    @Override
                    public void isSusses(boolean flag) {
                        Bundle bundle = new Bundle();
                        if (flag) {
                            bundle.putString("data", "写入成功");
                        } else {
                            bundle.putString("data", "写入失败");
                        }
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                });
    }

    /**
     * 吐司
     *
     * @param str
     */
    private void toToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    /**
     * 读取指定数据
     */
    private void readData(Intent intent) {
        /*
        2020.3.25
        String password = password_text.getText().toString().trim();
        if (block_text.getText().toString().trim().equals("")) {
            return;
        }
        if (piece_text.getText().toString().trim().equals("")) {
            return;
        }
        int block = Integer.parseInt(block_text.getText().toString().trim());
        int piece = Integer.parseInt(piece_text.getText().toString().trim());
        */
        String password = "";
        int block = 2;
        int piece = 0;
        NfcReadHelper.getInstence(intent)
                .setPassword(password)
                .getData(block, piece, new NfcReadHelper.NFCCallback() {
                    @Override
                    public void callBack(Map<String, List<String>> data) {
                    }

                    @Override
                    public void callBack(String data) {
                        //转换显示格式,2020.3.25
                        String final_data = "此卡电话号码是：";
                        boolean data_flag = false;
                        for(int i=0;i<(data.length()-4);i++){
                            if(data.charAt(i) != '0'){
                                final_data += data.charAt(i);
                            }
                            else{
                                if(data.charAt(i) == '0'&data.charAt(i+1)=='0'&data.charAt(i+2)=='0'&data.charAt(i+3)=='0'){
                                    //连续四个是零，则表示到了数据尾部，不再取了
                                    if(final_data.length()==18){
                                        //防止电话号码尾部是零
                                        final_data += '0';
                                    }
                                    break;
                                }
                                else if (data.charAt(i) == '0'&data.charAt(i+1)=='0'&data.charAt(i+2)=='0'){
                                    //连续三个是零，加一个flag
                                    data_flag = true;
                                }
                                else if(data_flag){
                                    final_data += data.charAt(i);
                                    data_flag = false;
                                }
                            }
                        }
                        Bundle bundle = new Bundle();
                        //原代码，2020.3.26，将data改final_data
                       // bundle.putString("data", data);
                        bundle.putString("data",final_data);
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 2;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void error() {
                        Bundle bundle = new Bundle();
                        bundle.putString("data", "读取失败");
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                });
    }

    /**
     * 点击监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*
            2020.3.25
            case R.id.button1:
                flag = 0;
                changeVisiable(1);
                break;
                */
            case R.id.button2:
                changeVisiable(2);
                flag = 1;
                break;
            case R.id.button3:
                changeVisiable(3);
                flag = 2;
                break;
            /*
            2020.3.25
            case R.id.button4:
                changeVisiable(4);
                flag = 3;
                break;
                */
            default:
        }
    }

    /**
     * 改变界面状态
     *
     * @param i
     */
    private void changeVisiable(int i) {
        switch (i) {
            /*
            2020.3.25
            case 1:
                button1.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                button2.setTextColor(getResources().getColor(R.color.shadow, null));
                button3.setTextColor(getResources().getColor(R.color.shadow, null));
                button4.setTextColor(getResources().getColor(R.color.shadow, null));
                read_data.setVisibility(View.GONE);
                write.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                break;
                */
            case 2:
                //button1.setTextColor(getResources().getColor(R.color.shadow, null));
                button2.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                button3.setTextColor(getResources().getColor(R.color.shadow, null));
                //button4.setTextColor(getResources().getColor(R.color.shadow, null));
                read_data.setVisibility(View.VISIBLE);
                write.setVisibility(View.GONE);
                //changePassword.setVisibility(View.GONE);
                break;
            case 3:
                //button1.setTextColor(getResources().getColor(R.color.shadow, null));
                button2.setTextColor(getResources().getColor(R.color.shadow, null));
                button3.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                //button4.setTextColor(getResources().getColor(R.color.shadow, null));
                read_data.setVisibility(View.GONE);
                write.setVisibility(View.VISIBLE);
                //changePassword.setVisibility(View.GONE);
                break;
            /*
            2020.3.25
            case 4:
                button1.setTextColor(getResources().getColor(R.color.shadow, null));
                button2.setTextColor(getResources().getColor(R.color.shadow, null));
                button3.setTextColor(getResources().getColor(R.color.shadow, null));
                button4.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                read_data.setVisibility(View.GONE);
                write.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                break;
                */
            default:
        }
    }
}
