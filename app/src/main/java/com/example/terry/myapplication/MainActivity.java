package com.example.terry.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private int index = -1;

    private int step = 0;

    private List<Map<String, String>> contentList;

    TextView txt1;
    TextView txt2;
    TextView txt3;

    Button btn_remember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btn_prev = (Button) findViewById(R.id.btn_prev);
        Button btn_next = (Button) findViewById(R.id.btn_next);
        Button btn_forgot = (Button) findViewById(R.id.btn_forgot);
        btn_remember = (Button) findViewById(R.id.btn_remember);

        Button btn_back10 = (Button) findViewById(R.id.btn_back10);
        Button btn_forward10 = (Button) findViewById(R.id.btn_forward10);

        txt1 =(TextView) findViewById(R.id.txt1);
        txt2 =(TextView) findViewById(R.id.txt2);
        txt3 =(TextView) findViewById(R.id.txt3);

        Map map = loadSetting();

        getIgnoreSentenceList();

        if(map.containsKey("index"))
            index = (Integer)map.get("index");

        if(contentList == null) {
            contentList = getContent();
        }

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                index--;
                showQuiz();
                step = 1;

                saveSetting();

                txt3.setText(String.valueOf(index));
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                index++;
                showQuiz();
                step = 1;

                saveSetting();

                txt3.setText(String.valueOf(index));
            }
        });


        btn_remember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(step == 0 || step == 2)
                {
                    List<Integer> list = getIgnoreSentenceList();

                    while(true) {
                        index++;

                        if(index > contentList.size() - 1) {
                            index = contentList.size() -1;
                            break;
                        }

                        if(list.indexOf(index) >= 0)
                            continue;
                        else
                            break;
                    }
                    showQuiz();
                    step = 1;
                }
                else
                {
                    showAns();
                    step = 2;
                    setSentenceState(index, true);
                }

                saveSetting();

                txt3.setText(String.valueOf(index));

            }
        });

        btn_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAns();
                step = 2;
                setSentenceState(index, false);

            }
        });

        txt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAns();
                btn_remember.setText("Remember");
            }
        });

        btn_back10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                index = index - 10;
                if(index < 0)
                    index = 0;

                showQuiz();
                step = 1;

                saveSetting();

                txt3.setText(String.valueOf(index));
            }
        });

        btn_forward10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                index = index + 10;
                if(index > contentList.size() - 1)
                    index = contentList.size() -1;

                showQuiz();
                step = 1;

                saveSetting();

                txt3.setText(String.valueOf(index));
            }
        });

//       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void showQuiz() {

        if(contentList == null) {
            contentList = getContent();
        }

        if(index > contentList.size() - 1) {
            index = contentList.size() -1;
            return;
        }

        if(index < 0) {
            index = -1;
            return;
        }

        txt1.setText(contentList.get(index).get("key"));
        txt2.setText("");

        btn_remember.setText("Remember");

    }

    private void showAns() {

        if(contentList == null) {
            contentList = getContent();
        }

        if(index > contentList.size() - 1) {
            index = contentList.size() -1;
            return;
        }

        if(index < 0) {
            index = -1;
            return;
        }

        txt2.setText(contentList.get(index).get("value"));

        btn_remember.setText("Go");
    }


    private List<Map<String, String>> getContent()
    {
        List<Map<String, String>> list = new LinkedList<>();

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("content.txt")));

            String line;

            //int i = 0;

            while (true) {

                line = br.readLine();
                if(line == null || line.equals("..."))
                    break;

                Map<String, String> map = new HashMap<>();

                map.put("key", line);

                map.put("value", br.readLine());

                list.add(map);

                line = br.readLine();

                if(line == null || line.equals("..."))
                    break;

            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void saveSetting()
    {
        try {

            //String json = "{\"index\":\"" + String.valueOf(index) + "\"}";

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();

            map.put("index", index);

            map.put("ignoreSentenceList", getIgnoreSentenceList());

            String json = objectMapper.writeValueAsString(map);

            String filename = "mySetting.txt";

            saveTextFile(filename, json);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTextFile(String filename, String content)
    {
        try {

            FileOutputStream outputStream;

            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setSentenceState(int itemIndex, boolean remember)
    {
        List<Integer> list = getIgnoreSentenceList();

        int foundIndex = list.indexOf(itemIndex);

        if( foundIndex >=0 && remember == false)
            list.remove(foundIndex);

        if(remember == true && foundIndex < 0)
        {
            list.add(itemIndex);
        }

    }


    private List<Integer> ignoreSentenceList = null;

    private List<Integer> getIgnoreSentenceList()
    {
        if(ignoreSentenceList == null) {
            ignoreSentenceList = new LinkedList<>();

            Map<String, Object> setting = loadSetting();

            if(setting.containsKey("ignoreSentenceList"))
            {
                ignoreSentenceList = (List<Integer>) setting.get("ignoreSentenceList");
            }
        }

        return ignoreSentenceList;

    }


    private Map<String, Object> loadSetting()
    {

        Map<String, Object> map = new HashMap<>();

        String json = readText("mySetting.txt");

        txt2.setText(json);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>(){});

            return map;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private String readText(String filename)
    {
        //String filename = "mySetting.txt";

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {

            File file = new File(getFilesDir(), filename);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_updateContent) {

            String content = downLoadText("http://7vzrom.com1.z0.glb.clouddn.com/content.txt?" + new Date().getTime());

            if(content != null && !content.trim().equals(""))
            {
                this.contentList = null;
                saveTextFile("content.txt", content);
                getContent();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private String downLoadText(String strurl){                                                             //将要下载的资源的网址作为参数传进来

        StringBuffer sb=new StringBuffer();
        String line=null;
        BufferedReader br=null;
        try {
            URL url=new URL(strurl);                                                                                      //根据参数的网址建立URL对象
            HttpURLConnection urlConn=(HttpURLConnection) url.openConnection();          //获得HttpURLConnection 对象
            //getInputStream得到的是字节流，封装成InputStreamReader，则变成字符流，
            //再封装成BufferedReader，则可以调用其readLine方法，一行一行进行读取
            br=new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while((line=br.readLine())!=null){
                sb.append(line);                                                                                                  //按行读取后，添加到sb里
                sb.append("\n");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{

            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sb.toString();                                                                                            //返回sb，即文本文件的内容
    }
}
