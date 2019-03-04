echo Nova
rm -fr publish/apk/Nova &&  mkdir -p publish/apk/Nova
./gradlew --offline :NovaVPN:assemblePlayRelease && cp NovaVPN/build/outputs/apk/play/release/*.apk publish/apk/Nova
shift
