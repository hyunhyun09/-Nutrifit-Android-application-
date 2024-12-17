package com.example.nutrifit;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

public class CameraResultActivity extends AppCompatActivity {

    private ArrayList<HashMap<String, String>> foodResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_result);

        foodResult = new ArrayList<>();

        // 반환된 JSON 데이터를 받고 String으로 변환
        String resultJson = getIntent().getStringExtra("result_json");

        // 반환된 JSON 데이터 처리
        if (resultJson != null) {
            processData(resultJson);
            saveToJSONFile();
        } else {
            Log.e("ERROR", "NO return data from API.");
        }
    }

    private void processData(String resultJson) {
        try {
            JSONObject jsonObject = new JSONObject(resultJson);
            JSONArray foodPositionList = jsonObject.getJSONArray("foodPositionList");

            for (int i = 0; i < foodPositionList.length(); i++) {
                JSONObject foodItem = foodPositionList.getJSONObject(i);

                // eatAmount를 float로 변환
                float eatAmount = (float) foodItem.getDouble("eatAmount");

                // foodCandidates 배열 가져오기
                JSONArray foodCandidates = foodItem.getJSONArray("foodCandidates");

                for (int j = 0; j < foodCandidates.length(); j++) {
                    JSONObject candidate = foodCandidates.getJSONObject(j);

                    int foodId = candidate.getInt("foodId");
                    String foodName = candidate.getString("foodName");

                    // nutrition 키가 있는지 확인
                    if (candidate.has("nutrition") && !candidate.isNull("nutrition")) {
                        JSONObject nutrition = candidate.getJSONObject("nutrition");

                        JSONObject formatFloat = new JSONObject();
                        Iterator<String> keys = nutrition.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            try {
                                Object value = nutrition.get(key);
                                if (value instanceof Number) { // Nutrition 값이 숫자인 경우 float로 변환
                                    formatFloat.put(key, (float) nutrition.getDouble(key));
                                } else { // 숫자가 아니면 그대로 저장
                                    formatFloat.put(key, value);
                                }
                            } catch (Exception e) {
                                Log.w("Nutrition Parsing", "Key: " + key + " 처리 중 오류 발생", e);
                            }
                        }

                        // HashMap에 data 저장
                        HashMap<String, String> foodMap = new HashMap<>();
                        foodMap.put("foodID", String.valueOf(foodId));
                        foodMap.put("foodName", foodName);
                        foodMap.put("nutrition", formatFloat.toString());
                        foodMap.put("eatAmount", String.valueOf(eatAmount));

                        foodResult.add(foodMap);

                        Log.i("Data", "Food ID: " + foodId + ", Food Name: " + foodName +
                                ", Nutrition: " + formatFloat + ", Eat Amount: " + eatAmount);
                    } else {
                        Log.w("JSON Parsing", "Nutrition empty " + foodName);
                    }
                }
            }

        } catch (Exception e) {
            Log.e("Data Processing Error", "JSON Processing Error", e);
        }
    }

    // JSON 파일로 저장
    private void saveToJSONFile() {
        try {
            JSONArray jsonArray = new JSONArray();

            for (HashMap<String, String> foodMap : foodResult) {
                JSONObject foodObject = new JSONObject();
                foodObject.put("foodID", Integer.parseInt(foodMap.get("foodID"))); // int
                foodObject.put("foodName", foodMap.get("foodName")); // String
                foodObject.put("nutrition", new JSONObject(foodMap.get("nutrition"))); // JSONObject
                foodObject.put("eatAmount", Float.parseFloat(foodMap.get("eatAmount"))); // float
                jsonArray.put(foodObject);
            }

            // JSON 파일로 저장
            String fileName = "food_results.json";
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(jsonArray.toString(4));
            writer.close();
            fos.close();

            Log.i("File save", "JSON data saved to: " + getFilesDir() + "/" + fileName);

        } catch (Exception e) {
            Log.e("File Save Error", "Error saving JSON data to file", e);
        }
    }
}