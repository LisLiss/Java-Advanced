@echo off
SET NOW=%cd%
cd ../../../../..
javac -encoding UTF8 -cp .;../../java-advanced-2020/lib/hamcrest-core-1.3.jar;../../java-advanced-2020/lib/junit-4.11.jar ru/ifmo/rain/Nikolaeva/i18n/*.java
java -cp .;../../java-advanced-2020/lib/hamcrest-core-1.3.jar;../../java-advanced-2020/lib/junit-4.11.jar ru.ifmo.rain.Nikolaeva.i18n.TextStatistics ENG RUS %NOW%\input.txt %NOW%\output.html
cd %NOW%
del /f *.class
sleep 100