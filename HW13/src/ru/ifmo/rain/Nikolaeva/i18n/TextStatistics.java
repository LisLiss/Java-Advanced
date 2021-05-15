package ru.ifmo.rain.Nikolaeva.i18n;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class TextStatistics {

    private static Locale getIOLocale(String ioLocaleString) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getISO3Language().equals(ioLocaleString)) {
                return locale;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void minMaxStat(Statistic statistic, String word, String word2) {
        if (statistic.minLengthString == null || statistic.minLength > word.length()) {
            statistic.minLength = word.length();
            statistic.minLengthString = word;
        }
        if (statistic.maxLengthString == null || statistic.maxLength < word.length()) {
            statistic.maxLength = word.length();
            statistic.maxLengthString = word;
        }
        if (statistic.minValue == null) {
            statistic.minValue = word2;
        } else {
            if (statistic.cmp.compare(statistic.minValue, word2) > 0) {
                statistic.minValue = word2;
            }
        }
        if (statistic.maxValue == null) {
            statistic.maxValue = word2;
        } else {
            if (statistic.cmp.compare(word2, statistic.maxValue) > 0) {
                statistic.maxValue = word2;
            }
        }
    }

    private static void makeLineStatistic(Statistic lineStatistic, String word) {
        if (word.isEmpty()) return;
        lineStatistic.count++;
        lineStatistic.average += word.length();
        lineStatistic.uniqueSet.add(word);
        lineStatistic.uniqueCount = lineStatistic.uniqueSet.size();
        minMaxStat(lineStatistic, word, word);
    }

    static Statistic makeWordStatistic(Locale locale, String text, BreakIterator breakIterator) {
        Statistic statistic = new Statistic();
        statistic.cmp = Collator.getInstance(locale);
        breakIterator.setText(text);
        int beginInd = breakIterator.first();
        int endInd = breakIterator.next();
        while (endInd != BreakIterator.DONE) {
            String word = text.substring(beginInd, endInd);
            beginInd = endInd;
            endInd = breakIterator.next();
            if (word.isEmpty()) continue;
            if (word.length() == 1 && ((int) word.charAt(0)) < 65) {
                continue;
            }
            if (Character.isWhitespace(word.charAt(word.length() - 1))) {
                word = word.substring(0, word.length() - 1);
            }
            try {
                NumberFormat.getNumberInstance(locale).parse(word);
                continue;
            } catch (ParseException ignored) {

            }
            try {
                NumberFormat.getCurrencyInstance(locale).parse(word);
                continue;
            } catch (ParseException ignored) {

            }
            statistic.count++;
            statistic.average += word.length();
            statistic.uniqueSet.add(word);
            statistic.uniqueCount = statistic.uniqueSet.size();
            minMaxStat(statistic, word, word);
        }
        if (statistic.count != 0) statistic.average /= statistic.count;
        return statistic;
    }

    @SuppressWarnings("unchecked")
    static Statistic makeNumStatistic(Locale locale, String text, BreakIterator breakIterator, NumberFormat numberFormat, boolean isMoney) {
        Statistic statistic = new Statistic();
        statistic.cmp = (Comparator<String>) (a, b) -> {
            try {
                return Double.compare(numberFormat.parse(a).doubleValue(), numberFormat.parse(b).doubleValue());
            } catch (ParseException e) {
                return 0;
            }
        };
        breakIterator.setText(text);
        int beginInd = breakIterator.first();
        int endInd = breakIterator.next();
        while (endInd != BreakIterator.DONE) {
            String word = text.substring(beginInd, endInd);
            beginInd = endInd;
            endInd = breakIterator.next();
            if (word.isEmpty()) continue;
            if (Character.isWhitespace(word.charAt(word.length() - 1))) {
                word = word.substring(0, word.length() - 1);
            }
            Number numWord;
            try {
                numWord = numberFormat.parse(word);
            } catch (ParseException ignored) {
                continue;
            }
            try {
                numWord = NumberFormat.getCurrencyInstance(locale).parse(word);
                if (!isMoney) {
                    continue;
                }
            } catch (ParseException ignored) {

            }
            statistic.count++;
            statistic.average += numWord.doubleValue();
            statistic.uniqueSet.add(numWord.toString());
            statistic.uniqueCount = statistic.uniqueSet.size();
            minMaxStat(statistic, word, word);
        }
        if (statistic.count != 0) statistic.average /= statistic.count;
        return statistic;
    }

    static Statistic makeDateStatistic(Locale locale, String text, BreakIterator breakIterator, DateFormat dateFormat) {
        Statistic statistic = new Statistic();
        statistic.cmp = (Comparator<String>) (a, b) -> {
            try {
                return dateFormat.parse(a).compareTo(dateFormat.parse(b));
            } catch (ParseException e) {
                return 0;
            }
        };
        breakIterator.setText(text);
        int beginInd = breakIterator.first();
        int endInd = breakIterator.next();
        while (endInd != BreakIterator.DONE) {
            String word = text.substring(beginInd, endInd);
            beginInd = endInd;
            endInd = breakIterator.next();
            if (word.isEmpty()) continue;
            if (Character.isWhitespace(word.charAt(word.length() - 1))) {
                word = word.substring(0, word.length() - 1);
            }
            Date date;
            try {
                date = dateFormat.parse(word);
            } catch (ParseException ignored) {
                continue;
            }
            statistic.count++;
            statistic.average += word.length();
            statistic.uniqueSet.add(String.valueOf(date));
            statistic.uniqueCount = statistic.uniqueSet.size();
            minMaxStat(statistic, word, word);
        }
        if (statistic.count != 0) statistic.average /= statistic.count;
        return statistic;
    }

    private static void writeStat(StringBuilder answer, ResourceBundle outputBundle, Statistic statistic, String[] keys) {
        MessageFormat numSmth = new MessageFormat(outputBundle.getString("Number") + " {0}: {1} ");
        answer.append("<p><b>").append(outputBundle.getString(keys[0])).append(":</b><br>\n");
        Object[] argNumSmth = {outputBundle.getString(keys[1]), statistic.count};
        answer.append(numSmth.format(argNumSmth));

        MessageFormat uniqueMF = new MessageFormat("({0})<br>\n");
        double[] numUnique = {0, 1, 2};
        String[] wordUnique = {"{0} " + outputBundle.getString("uniques"), "{0}  " + outputBundle.getString(keys[2]), "{0}  " + outputBundle.getString("uniques")};
        ChoiceFormat formUnique = new ChoiceFormat(numUnique, wordUnique);
        uniqueMF.setFormatByArgumentIndex(0, formUnique);
        Object[] argUnique = {statistic.uniqueCount};
        answer.append(uniqueMF.format(argUnique));

        MessageFormat minMaxSmth = new MessageFormat("{0} {1}: {2}<br>\n");
        Object[] argMinMaxSmth = {outputBundle.getString(keys[3]), outputBundle.getString(keys[4]), statistic.minValue};
        answer.append(minMaxSmth.format(argMinMaxSmth));
        argMinMaxSmth = new Object[]{outputBundle.getString(keys[5]), outputBundle.getString(keys[6]), statistic.maxValue};
        answer.append(minMaxSmth.format(argMinMaxSmth));

        MessageFormat minMaxLengthSmth = new MessageFormat("{0} {1} {2}: {3} ({4})<br>\n");
        Object[] argMinMaxLengthSmth = {outputBundle.getString(keys[7]), outputBundle.getString("length"), outputBundle.getString(keys[8]), statistic.minLength, statistic.minLengthString};
        answer.append(minMaxLengthSmth.format(argMinMaxLengthSmth));
        argMinMaxLengthSmth = new Object[]{outputBundle.getString(keys[9]), outputBundle.getString("length"), outputBundle.getString(keys[10]), statistic.maxLength, statistic.maxLengthString};
        answer.append(minMaxLengthSmth.format(argMinMaxLengthSmth));

        answer.append(keys[11]);
        answer.append(": ").append(statistic.average).append("<br>\n</p>");

    }

    private static String writeHTML(ResourceBundle outputBundle, Statistic lineStatistic, Statistic numStatistic, Statistic moneyStatistic, Statistic wordStatistic, Statistic sentenceStatistic, Statistic dateStatistic) {
        StringBuilder answer = new StringBuilder("<html>\n<body>\n");
        answer.append("<p><b>").append(outputBundle.getString("allStat")).append("</b><br>\n");
        MessageFormat numSmth = new MessageFormat(outputBundle.getString("Number") + " {0}: {1, number}<br>\n");
        Object[] argNumSmth = {outputBundle.getString("numbersParent"), numStatistic.count};
        answer.append(numSmth.format(argNumSmth));
        argNumSmth = new Object[]{outputBundle.getString("moneyParent"), moneyStatistic.count};
        answer.append(numSmth.format(argNumSmth));
        argNumSmth = new Object[]{outputBundle.getString("datesParent"), dateStatistic.count};
        answer.append(numSmth.format(argNumSmth));
        argNumSmth = new Object[]{outputBundle.getString("sentencesParent"), sentenceStatistic.count};
        answer.append(numSmth.format(argNumSmth));
        argNumSmth = new Object[]{outputBundle.getString("wordsParent"), wordStatistic.count};
        answer.append(numSmth.format(argNumSmth));
        argNumSmth = new Object[]{outputBundle.getString("linesParent"), lineStatistic.count};
        answer.append(numSmth.format(argNumSmth));

        String[] keysNumber = {"numberStatistic", "numbersParent", "uniqueNoun", "minNoun", "number", "maxNoun", "number", "minFemale", "numberParent", "maxFemale", "numberParent", outputBundle.getString("averageNoun") + " " + outputBundle.getString("number")};
        writeStat(answer, outputBundle, numStatistic, keysNumber);
        String[] keysMoney = {"moneyStatistic", "moneyParent", "uniques", "minMany", "moneyMany", "maxMany", "moneyMany", "minFemale", "moneyParent", "maxFemale", "moneyParent", outputBundle.getString("averageNoun") + " " + outputBundle.getString("number") + " " + outputBundle.getString("money")};
        writeStat(answer, outputBundle, moneyStatistic, keysMoney);
        String[] keysDates = {"dateStatistic", "datesParent", "uniqueFemale", "minFemale", "date", "maxFemale", "date", "minFemale", "dateParent", "maxFemale", "dateParent", outputBundle.getString("averageFemale") + " " + outputBundle.getString("length") + " " + outputBundle.getString("dateParent")};
        writeStat(answer, outputBundle, dateStatistic, keysDates);
        String[] keysSentence = {"sentenceStatistic", "sentencesParent", "uniqueNoun", "minNoun", "sentence", "maxNoun", "sentence", "minFemale", "sentenceParent", "maxFemale", "sentenceParent", outputBundle.getString("averageFemale") + " " + outputBundle.getString("length") + " " + outputBundle.getString("sentenceParent")};
        writeStat(answer, outputBundle, sentenceStatistic, keysSentence);
        String[] keysWords = {"wordStatistic", "wordsParent", "uniqueNoun", "minNoun", "word", "maxNoun", "word", "minFemale", "wordParent", "maxFemale", "wordParent", outputBundle.getString("averageFemale") + " " + outputBundle.getString("length") + " " + outputBundle.getString("wordParent")};
        writeStat(answer, outputBundle, wordStatistic, keysWords);
        String[] keysLines = {"lineStatistic", "linesParent", "uniqueFemale", "minFemale", "line", "maxFemale", "line", "minFemale", "lineParent", "maxFemale", "lineParent", outputBundle.getString("averageFemale") + " " + outputBundle.getString("length") + " " + outputBundle.getString("lineParent")};
        writeStat(answer, outputBundle, lineStatistic, keysLines);
        answer.append("</body>\n</html>\n");
        return answer.toString();
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Args is null");
            return;
        }
        if (args.length != 4) {
            System.err.println("Wrong number of args");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Args is null");
                return;
            }
        }
        Locale inputLocale = getIOLocale(args[0].toLowerCase());
        Locale outputLocale = getIOLocale(args[1].toLowerCase());
        if (inputLocale == null || outputLocale == null) {
            System.err.println("Don't have this locale");
            return;
        }
        if (!outputLocale.getLanguage().equals("en") && !outputLocale.getLanguage().equals("ru")) {
            System.err.println("Output locale isn't ru or en");
            return;
        }
        ResourceBundle outputBundle;
        if (outputLocale.getLanguage().equals("ru")) {
            outputBundle = ResourceBundle.getBundle("ru.ifmo.rain.Nikolaeva.i18n.BundleRU");
        } else {
            outputBundle = ResourceBundle.getBundle("ru.ifmo.rain.Nikolaeva.i18n.BundleEN");
        }

        Statistic lineStatistic = new Statistic();
        Statistic numStatistic, dateStatistic, moneyStatistic, wordStatistic, sentenceStatistic;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[2]), StandardCharsets.UTF_8))) {
            String word;
            StringBuilder wordBuilder = new StringBuilder();
            lineStatistic.cmp = Collator.getInstance(inputLocale);
            while ((word = bufferedReader.readLine()) != null) {
                makeLineStatistic(lineStatistic, word);
                wordBuilder.append(word).append('\n');
            }
            lineStatistic.average /= lineStatistic.count;
            word = wordBuilder.toString();
            numStatistic = makeNumStatistic(inputLocale, word, BreakIterator.getWordInstance(inputLocale), NumberFormat.getNumberInstance(inputLocale), false);
            moneyStatistic = makeNumStatistic(inputLocale, word, BreakIterator.getWordInstance(inputLocale), NumberFormat.getCurrencyInstance(inputLocale), true);
            wordStatistic = makeWordStatistic(inputLocale, word, BreakIterator.getWordInstance(inputLocale));
            sentenceStatistic = makeWordStatistic(inputLocale, word, BreakIterator.getSentenceInstance(inputLocale));
            dateStatistic = makeDateStatistic(inputLocale, word, BreakIterator.getLineInstance(inputLocale), DateFormat.getDateInstance(DateFormat.SHORT, inputLocale));
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(args[3], StandardCharsets.UTF_8))) {
                String answer = writeHTML(outputBundle, lineStatistic, numStatistic, moneyStatistic, wordStatistic, sentenceStatistic, dateStatistic);
                bufferedWriter.write(answer);
            } catch (IOException e) {
                System.err.println("Can't open output file");
            }
        } catch (IOException e) {
            System.err.println("Can't open input file");
            System.err.println(args[2]);
        }
    }
}
