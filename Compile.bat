@echo off
javac DatabaseNode.java
javac DatabaseClient.java


if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
) else (
    echo Compilation successful.
)
pause