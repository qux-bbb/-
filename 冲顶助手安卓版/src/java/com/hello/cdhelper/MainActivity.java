package com.hello.cdhelper;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    TextView dataArea;
    ScrollView scrollView;

    String pattern  = "\\((.*)\\)";
    Pattern r = Pattern.compile(pattern);

    List<String> result_had = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataArea = findViewById(R.id.dataArea);
        scrollView = findViewById(R.id.scrollView);

        new updateTask().execute();
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        // 设置header
        connection.setRequestProperty("Host", "wdpush.sogoucdn.com");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_5 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13G36 Sogousearch/Ios/5.9.7");
        connection.setRequestProperty("Accept-Language", "zh-cn");
        connection.setRequestProperty("Referer", "https://assistant.sogoucdn.com/v5/cheat-sheet?channel=hj&icon=http%3A%2F%2Fapp.sastatic.sogoucdn.com%2Fdati%2Fhj.png&name=%E7%99%BE%E4%B8%87%E8%B5%A2%E5%AE%B6&appName=%E8%8A%B1%E6%A4%92%E7%9B%B4%E6%92%AD%2F%E5%BF%AB%E8%A7%86%E9%A2%91");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Cookie", "APP-SGS-ID=e6ccf289bbd571f66fc55009dd13711c4e02924f5ddc");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0 , bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }


    // 线程读数据，更新界面
    private class updateTask extends AsyncTask<String, String, String> {

        private InputStream mmInStream = null;

        @Override
        protected String doInBackground(String... params) {
            try{
                String result = getUrlString("https://wdpush.sogoucdn.com/api/anspush?key=cddh&wdcallback=jQuery200012885665125213563_1518087319437&_=1518087319438");
                Matcher m = r.matcher(result);
                if(m.find()){
                    result =  m.group(1);
                }else{
                    Log.e(TAG, "doInBackground: No matching data");
                }
                Log.i(TAG, "doInBackground: Success to getURLString: " + result);
                publishProgress(result);
            } catch (IOException ioe){
                Log.e(TAG, "doInBackground: Failed to getURLString: ", ioe);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d(TAG, values[0]);

            try {
                JSONObject jsonBody = new JSONObject(values[0]);
                JSONArray results = new JSONArray(new String(Base64.decode(jsonBody.getString("result"), Base64.DEFAULT)));
                Log.i(TAG, "onProgressUpdate: json_result: " + results);

                for(int i = 0; i < results.length(); i++){
                    // 判定第i个result是否在里面，用java有点费劲
                    boolean flag = false;
                    for(int j = 0; j < result_had.size(); j++){
                        if(results.getString(i).equals(result_had.get(j))){
                            flag = true;
                            break;
                        }
                    }
                    if(flag == false){
                        result_had.add(results.getString(i));

                        String problem = "";
                        try {
                            JSONObject json_obj = new JSONObject(results.getString(i));
                            problem += json_obj.getString("title") + "\n";
                            JSONArray answers = (JSONArray)json_obj.get("answers");
                            problem += "A." + answers.getString(0) + " B." + answers.getString(1) + " C." + answers.getString(2) + "\n";
                            problem += "===================\n";
                            problem += "分析： " + (new JSONObject(((JSONArray)json_obj.get("search_infos")).getString(0))).getString("summary") + "\n";
                            problem += "===================\n";
                            problem += "推荐答案：  " + json_obj.getString("recommend") + "\n\n";
                        }catch (Exception e){
                            Log.e(TAG, "onProgressUpdate: " + "题目解析出错");
                            problem = "本题出错！";
                        }

                        // 追加数据
                        dataArea.append(problem);
                        // 自动滚动到底部
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }

                }

            } catch (JSONException je) {
                Log.e(TAG, "onProgressUpdate: Failed to parse JSON", je);
            }

        }
    }
}
