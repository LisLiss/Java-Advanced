package ru.ifmo.rain.Nikolaeva.i18n;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

class Statistic {
    int count = 0;
    int uniqueCount = 0;
    String minValue = null;
    String maxValue = null;
    int minLength = 0;
    int maxLength = 0;
    String minLengthString = null;
    String maxLengthString = null;
    double average = 0;
    Set<String> uniqueSet = new HashSet<>();
    Comparator cmp;
}