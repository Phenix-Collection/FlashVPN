echo Flash
rm -fr publish/apk/Flash &&  mkdir -p publish/apk/Flash
./gradlew --offline :FlashVPN:assemblePlayRelease && cp FlashVPN/build/outputs/apk/play/release/*.apk publish/apk/Flash
shift
