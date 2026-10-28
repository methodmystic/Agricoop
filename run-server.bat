@echo off
echo ========================================================
echo   Starting AgriCoop Java JDBC + MySQL Server
echo ========================================================

REM Step 1: Check if compiled classes exist
if not exist "server-java\target\classes" (
    echo Compiling Java project with Maven...
    mvn -f server-java/pom.xml compile
)

REM Step 2: Launch Java HTTP Server with JDBC and MySQL
echo Launching AgriCoopServer...
java -cp "server-java/target/classes;server-java/lib/*" com.agricoop.AgriCoopServer
pause
