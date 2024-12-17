package com.example.nutrifit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.*;

public class Collabo_Filter {

    // 사용자 데이터 클래스
    static class User {
        double weight;
        double height;
        int age;
        int gender; // 1 for male, 2 for female
        int activity;
        int taste1;
        int taste2;

        public User(double weight, double height, int age, int gender, int activity, int taste1, int taste2) {
            this.weight = weight;
            this.height = height;
            this.age = age;
            this.gender = gender;
            this.activity = activity;
            this.taste1 = taste1;
            this.taste2 = taste2;
        }
    }

    // 음식 데이터 클래스
    static class Food {
        String name;
        String category;
        Map<String, Double> nutrients;
        double recommendationScore;

        public Food(String name, String category, Map<String, Double> nutrients) {
            this.name = name;
            this.category = category;
            this.nutrients = nutrients;
        }

        public void setRecommendationScore(double score) {
            this.recommendationScore = score;
        }
    }

    // 기초대사량 계산
    public static double calculateBMR(User user) {
        if (user.gender == 1) {
            return 10 * user.weight + 6.25 * user.height - 5 * user.age + 5;
        } else {
            return 10 * user.weight + 6.25 * user.height - 5 * user.age - 161;
        }
    }

    // 총 에너지 요구량 계산
    public static double calculateTDEE(double bmr, int activityLevel) {
        double[] activityMultipliers = {1.0, 1.2, 1.4, 1.6, 1.8};
        return bmr * activityMultipliers[activityLevel - 1];
    }

    // 권장 섭취량 계산
    public static Map<String, Double> calculateDailyIntake(User user) {
        double bmr = calculateBMR(user);
        double tdee = calculateTDEE(bmr, user.activity);

        Map<String, Double> dailyIntake = new HashMap<>();
        dailyIntake.put("에너지(kcal)", tdee);
        dailyIntake.put("단백질(g)", 0.8 * user.weight);
        dailyIntake.put("지방(g)", tdee * 0.3 / 9);
        dailyIntake.put("탄수화물(g)", tdee * 0.5 / 4);
        dailyIntake.put("당류(g)", tdee * 0.1 / 4);
        dailyIntake.put("식이섬유(g)", user.gender == 1 ? 38.0 : 25.0);
        dailyIntake.put("칼슘(mg)", 850.0);
        dailyIntake.put("철(mg)", user.gender == 1 ? 10.0 : 18.0);
        dailyIntake.put("나트륨(mg)", 2000.0);
        dailyIntake.put("비타민 A(μg RAE)", user.gender == 1 ? 900.0 : 700.0);
        dailyIntake.put("비타민 C(mg)", 100.0);

        return dailyIntake;
    }

    // 영양소 부족 계산
    @SuppressLint("NewApi")
    public static Map<String, Double> calculateDeficiencies(Map<String, Double> dailyIntake, List<Food> consumedFoods) {
        Map<String, Double> deficiencies = new HashMap<>(dailyIntake);

        for (Food food : consumedFoods) {
            for (Map.Entry<String, Double> nutrient : food.nutrients.entrySet()) {
                deficiencies.put(nutrient.getKey(), deficiencies.getOrDefault(nutrient.getKey(), 0.0) - nutrient.getValue());
            }
        }

        // 결핍 값이 음수가 되지 않도록 보정
        deficiencies.replaceAll((k, v) -> Math.max(0, v));
        return deficiencies;
    }

    // 음식 추천
    @SuppressLint("NewApi")
    public static List<Food> recommendFoods(Map<String, Double> deficiencies, List<Food> foodData,
                                            List<String> categories, int numberOfRecommendations) {
        List<Food> recommendations = new ArrayList<>();

        // 각 카테고리에서 추천된 음식을 하나씩 처리
        for (String category : categories) {
            List<Food> categoryFoods = filterFoodsByCategory(foodData, category);
            Food topRecommendation = getTopRecommendation(categoryFoods, deficiencies);

            if (topRecommendation != null) {
                recommendations.add(topRecommendation);
            }
        }

        return recommendations;
    }

    // 카테고리에 맞는 음식만 필터링하는 함수
    private static List<Food> filterFoodsByCategory(List<Food> foodData, String category) {
        List<Food> filteredFoods = new ArrayList<>();
        for (Food food : foodData) {
            if (food.category.equals(category)) {
                filteredFoods.add(food);
            }
        }
        return filteredFoods;
    }

    // 결핍을 기준으로 추천점수 상위 1개 음식 반환
    private static Food getTopRecommendation(List<Food> foods, Map<String, Double> deficiencies) {
        if (foods.isEmpty()) return null;

        Food bestFood = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Food food : foods) {
            double score = 0.0;
            for (Map.Entry<String, Double> deficiency : deficiencies.entrySet()) {
                score += deficiency.getValue() * food.nutrients.getOrDefault(deficiency.getKey(), 0.0);
            }
            food.setRecommendationScore(score);

            if (score > bestScore) {
                bestScore = score;
                bestFood = food;
            }
        }

        return bestFood;
    }
    // Android에서 호출되는 메서드
    // Android에서 호출되는 메서드
    // Android에서 호출되는 메서드
    public static void runRecommendation(Context context) {
        // 사용자 데이터 로드
        User user = new User(70, 175, 30, 1, 3, 1, 12);

        // 음식 데이터 로드 (샘플 데이터)
        List<Food> foodData = new ArrayList<>();
        foodData.add(new Food("쌀밥", "밥류", Map.of("에너지(kcal)", 200.0, "단백질(g)", 5.0)));
        foodData.add(new Food("김치", "김치류", Map.of("나트륨(mg)", 500.0, "식이섬유(g)", 2.0)));
        foodData.add(new Food("된장찌개", "찌개류", Map.of("에너지(kcal)", 150.0, "단백질(g)", 8.0)));
        foodData.add(new Food("시금치나물", "나물류", Map.of("식이섬유(g)", 3.0, "칼슘(mg)", 50.0)));

        // 소비된 음식 데이터 로드 (샘플 데이터)
        List<Food> consumedFoods = new ArrayList<>();
        consumedFoods.add(new Food("쌀밥", "밥류", Map.of("에너지(kcal)", 200.0, "단백질(g)", 5.0)));

        // 일일 권장량 계산
        Map<String, Double> dailyIntake = calculateDailyIntake(user);

        // 영양소 결핍 계산
        Map<String, Double> deficiencies = calculateDeficiencies(dailyIntake, consumedFoods);

        // 카테고리 리스트 정의
        List<String> categories = Arrays.asList("밥류", "김치류", "찌개류", "나물류");

        // 각 카테고리에서 상위 1개 음식 추천
        List<Food> recommendations = recommendFoods(deficiencies, foodData, categories, 1);

        // 결과 출력
        for (Food food : recommendations) {
            Log.d("추천 음식", "이름: " + food.name + ", 점수: " + food.recommendationScore);
        }
    }
}
