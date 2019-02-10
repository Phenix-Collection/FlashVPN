echo MineSweeper
rm -fr publish/apk/MineSweeper &&  mkdir -p publish/apk/MineSweeper
./gradlew --offline :MineSweeper:assemblePlayRelease && cp MineSweeper/build/outputs/apk/play/release/*.apk publish/apk/MineSweeper
shift
