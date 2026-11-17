@echo off
cd /d "%~dp0"
java -cp "out;lib\sqlite-jdbc.jar" com.uniaccounts.ShowAccounts
