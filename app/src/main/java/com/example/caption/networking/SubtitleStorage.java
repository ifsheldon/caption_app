package com.example.caption.networking;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;
import java.util.*;

class SubtitleStorage
{

    int maxLines = 10;
    int currentSentence = 0;
    List<List<Pair<Integer, Integer>>> wordNumbersOnScreen = new ArrayList<>(maxLines);
    List<List<String>> linesOnScreen = new ArrayList<>(maxLines);
    boolean lastSentenceFinished = false;
    String lastSentence = "";
    String line1 = "";
    String line2 = "";
    List<Double> pastSpeed = new ArrayList<>(maxLines);
    long currentBegin = 0L;
    double currentSpeed = 0.0;
    long startTime = 0L;
    int totalWordCount = 0;

    Queue<String> allFinishedSentences = new LinkedList<>();

    public Queue<String> getAllFinishedSentences()
    {
        return allFinishedSentences;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public double getMovingAvgSpeed() {
        return pastSpeed.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public String getStat(){
        int timePassed = (int)((System.currentTimeMillis() - startTime) / 1000L);
        GregorianCalendar cal = new GregorianCalendar(0,0,0,0,0,timePassed);
        Date dNow = cal.getTime();
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");
        return String.format("Caption - Time: %s, Words: %d, Sentences: %d, WPM: %.1f, AVG: %.1f", ft.format(dNow), totalWordCount, currentSentence, currentSpeed, getMovingAvgSpeed());
    }

    public SubtitleStorage() {
        // 简单初始化，避免后面的复杂判断
        linesOnScreen.add(new ArrayList<>());
        linesOnScreen.add(new ArrayList<>());
        wordNumbersOnScreen.add(new ArrayList<>());
        wordNumbersOnScreen.add(new ArrayList<>());

    }

    public static boolean isLineLegal(List<String> line) {
        // 两个每行的限制
        int maxWordPerLine = 100;
        int maxLineLength = 50;

        if(line.size()>maxWordPerLine) return false;
        int totalLength = line.stream().mapToInt(String::length).sum() + line.size()-1 ;
        if(totalLength>maxLineLength) return false;

        return true;
    }

    public void add(String tr, boolean sentenceFinished){
        System.out.println("tr="+tr+" finish"+sentenceFinished);

        if(sentenceFinished){
            allFinishedSentences.offer(tr);
        }

        if(currentBegin==0){
            currentBegin = System.currentTimeMillis();
        }
        if(startTime==0){
            startTime = System.currentTimeMillis();
        }

        boolean isNewSentence = (lastSentenceFinished);
        lastSentenceFinished = sentenceFinished;

        System.out.println("isNew="+isNewSentence);

        if(isNewSentence){
            lastSentence = "";
            currentSentence++;
            currentBegin = System.currentTimeMillis();
            if(pastSpeed.size()>maxLines) pastSpeed.remove(0);
        }

        // 移除标点符号（可选开放？）
        // 阿里云 API 的限制
        tr = tr.replaceAll("[,.!?\\\\-]", " ");

        String[] currentSentenceWords = tr.split("\\s+");
        String[] lastSentenceWords = lastSentence.split("\\s+");

        totalWordCount = totalWordCount - lastSentenceWords.length + currentSentenceWords.length;

        if(!isNewSentence && !pastSpeed.isEmpty()) pastSpeed.remove(pastSpeed.size()-1);
        currentSpeed = (currentSentenceWords.length) * 1.0 / ((System.currentTimeMillis() - currentBegin) *1.0 / 60000 + 0.001);
        pastSpeed.add(currentSpeed);

        boolean redrawRequired = false;

        // 找到第一处差异
        int firstDifferenceIndex = 0;
        for (; firstDifferenceIndex < currentSentenceWords.length; firstDifferenceIndex++) {
            if(firstDifferenceIndex >= lastSentenceWords.length){
                redrawRequired = false;
                break;
            }
            if(!currentSentenceWords[firstDifferenceIndex].equals(lastSentenceWords[firstDifferenceIndex])){
                redrawRequired = true;
                break;
            }
        }

        if(isNewSentence) redrawRequired = false;


        int redrawFromLineNumber = Math.max(wordNumbersOnScreen.size() - 1, 0);
        int redrawFromWordNumber = Math.max(wordNumbersOnScreen.get(redrawFromLineNumber).size(), 0);
        if(redrawRequired) {
            // TODO: 处理首行
            // 找到对应的屏幕上行位置
            for (int j = wordNumbersOnScreen.size() - 1; j >= 0; j--) {
                List<Pair<Integer, Integer>> currentLineWordNumbers = wordNumbersOnScreen.get(j);
                for (int k = 0; k < currentLineWordNumbers.size(); k++) {
                    if (currentLineWordNumbers.get(k).equals(new MutablePair<Integer, Integer>(currentSentence, firstDifferenceIndex))) {
                        redrawFromLineNumber = j;
                        redrawFromWordNumber = k;
                    }
                }
            }
        }

        // 清除之前的排版数据
        // 需要倒过来删除
        if(redrawRequired) {
            for (int i = linesOnScreen.get(redrawFromLineNumber).size() - 1; i >= redrawFromWordNumber; i--) {
                linesOnScreen.get(redrawFromLineNumber).remove(i);
                wordNumbersOnScreen.get(redrawFromLineNumber).remove(i);
            }
            for (int i = linesOnScreen.size() - 1; i >= redrawFromLineNumber + 1; i--) {
                System.out.println("triggered!, newLine="+isNewSentence+" redraw="+redrawRequired);
                List<String> removed1 = linesOnScreen.remove(i);
                removed1.clear();
                List<Pair<Integer, Integer>> removed2 = wordNumbersOnScreen.remove(i);
                removed2.clear();
            }
        }

        if(linesOnScreen.size()==0){
            linesOnScreen.add(new ArrayList<>());
            wordNumbersOnScreen.add(new ArrayList<>());
        }

        // 从对应位置开始重排
        // 需要同时更新两个结构的数据
        int writingLine = redrawFromLineNumber;
        for (int i = firstDifferenceIndex; i < currentSentenceWords.length; i++) {

            String currentWord = currentSentenceWords[i];

            // TODO: 重构写入过程
            linesOnScreen.get(writingLine).add(currentWord);
            wordNumbersOnScreen.get(writingLine).add(new MutablePair<>(currentSentence, i));
            if(!isLineLegal(linesOnScreen.get(writingLine))){
                linesOnScreen.get(writingLine).remove(linesOnScreen.get(writingLine).size()-1);
                wordNumbersOnScreen.get(writingLine).remove(wordNumbersOnScreen.get(writingLine).size()-1);

                writingLine++;
                linesOnScreen.add(new ArrayList<>());
                wordNumbersOnScreen.add(new ArrayList<>());

                linesOnScreen.get(writingLine).add(currentWord);
                wordNumbersOnScreen.get(writingLine).add(new MutablePair<>(currentSentence, i));
            }

        }

        // 字幕展示
        // 只显示最后两行

        if(linesOnScreen.size() >= 2) {
            line1 = String.join(" ", linesOnScreen.get(linesOnScreen.size() - 2));
        }
        if(linesOnScreen.size() >= 1){
            line2 = String.join(" ", linesOnScreen.get(linesOnScreen.size() - 1));
        }

        System.out.println(redrawFromLineNumber + " " + redrawFromWordNumber);
        System.out.println(linesOnScreen);
        System.out.println(wordNumbersOnScreen);
        System.out.println(line1);
        System.out.println(line2);
        System.out.println("----");
//        in.nextInt();


        // 删除顶行
        if(linesOnScreen.size() > maxLines){
            List<String> remove1 = linesOnScreen.remove(0);
            remove1.clear();
            List<Pair<Integer, Integer>> remove2 = wordNumbersOnScreen.remove(0);
            remove2.clear();
        }

        // TODO: 这里可以优化，直接存数组
        lastSentence = tr;
    }

    public String get(){
        return line1 +"\n" + line2;
    }
}
