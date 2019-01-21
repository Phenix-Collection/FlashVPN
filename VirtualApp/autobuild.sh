while [ $# != 0 ]; do
    echo $1
    rm -fr publish/apk/$1 &&  mkdir -p publish/apk/$1
    ./gradlew  :$1:assembleArm32PlayRelease && cp $1/build/outputs/apk/arm32play/release/*.apk publish/apk/$1
    ./gradlew  :$1:assembleArm64PlayRelease && cp $1/build/outputs/apk/arm64play/release/*.apk publish/apk/$1
    shift
done
