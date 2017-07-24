set jdkpath=C:\Program Files\Java\jdk1.8.0_102\bin\java.exe
set zipalign=D:\Android\sdk\build-tools\25.0.2\zipalign.exe
"%jdkpath%" -jar AndResGuard-cli-1.2.3.jar input.apk -config config.xml -out outapk -zipalign "%zipalign%" -7zip D:\7za\7za.exe
pause
