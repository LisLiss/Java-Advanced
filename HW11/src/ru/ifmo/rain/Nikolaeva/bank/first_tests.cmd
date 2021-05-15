@echo off
SET now=%cd%
cd ../../../../..
javac -cp .;../../java-advanced-2020/lib/hamcrest-core-1.3.jar;../../java-advanced-2020/lib/junit-4.11.jar ru/ifmo/rain/Nikolaeva/bank/*.java
java -cp .;../../java-advanced-2020/lib/hamcrest-core-1.3.jar;../../java-advanced-2020/lib/junit-4.11.jar org.junit.runner.JUnitCore ru.ifmo.rain.Nikolaeva.bank.BankTests
cd %now%
del /f *.class
sleep 300