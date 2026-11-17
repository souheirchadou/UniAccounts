@echo off
cd /d "%~dp0"
if not exist lib mkdir lib
if not exist lib\sqlite-jdbc.jar (
  curl -L -o lib\sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar
)
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac -d out -cp lib\sqlite-jdbc.jar @sources.txt
if errorlevel 1 exit /b 1
java -cp "out;lib\sqlite-jdbc.jar" com.uniaccounts.Main