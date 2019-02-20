//
// Created by wei on 16-12-4.
//

#define   LOG_TAG    "code"
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#ifndef AESJNIENCRYPT_SIGNACTURECHECK_H
#define AESJNIENCRYPT_SIGNACTURECHECK_H

/**
 * whatsclone -1048786450
 * domultiple.jks -1208429200
 * polestar.jks 1642031816
 * polestar-team.jks 1404035346
 * domultipe-new.jks 128670161
 * nova-vpn.jks -2145233489
 */
//合法的APP包名
//static const char *app_packageName = "com.androidyuan.aesjniencrypt";
//合法的hashcode -625644214:这个值是我生成的这个可以store文件的hash值
/*static const int app_signature_hash_code = -1085342784;

static const int signature_hash_codes[] = {-1273784504, -1048786450, -1208429200,
                                           1642031816, 1404035346, 128670161, -2145233489};
*/

static void filleSignatures();

static int appendjava(int n, char in[]) {
    in[n++] = 'j';
    in[n++] = 'a';
    in[n++] = 'v';
    in[n++] = 'a';
    return n;
}

static int appendsecurity(int n, char in[]) {
    in[n++] = 's';
    in[n++] = 'e';
    in[n++] = 'c';
    in[n++] = 'u';
    in[n++] = 'r';
    in[n++] = 'i';
    in[n++] = 't';
    in[n++] = 'y';
    return n;
}

static int appendcert(int n, char in[]) {
    in[n++] = 'c';
    in[n++] = 'e';
    in[n++] = 'r';
    in[n++] = 't';
    return n;
}

static int appendMessageDigest(int n, char in[]) {
    in[n++] = 'M';
    in[n++] = 'e';
    in[n++] = 's';
    in[n++] = 's';
    in[n++] = 'a';
    in[n++] = 'g';
    in[n++] = 'e';
    in[n++] = 'D';
    in[n++] = 'i';
    in[n++] = 'g';
    in[n++] = 'e';
    in[n++] = 's';
    in[n++] = 't';
    return n;
}

static int appendjavasm(int n, char in[]) {
    //java/security/MessageDigest
    n = appendjava(n, in);
    in[n++] = '/';
    n = appendsecurity(n, in);
    in[n++] = '/';
    n = appendMessageDigest(n, in);
    return n;
}

static int appendjavascPrefix(int n, char in[]) {
    //java/security/cert/
    n = appendjava(n, in);
    in[n++] = '/';
    n = appendsecurity(n, in);
    in[n++] = '/';
    n = appendcert(n, in);
    in[n++] = '/';
    return n;
}

static int appendget(int n, char in[]) {
    in[n++] = 'g';
    in[n++] = 'e';
    in[n++] = 't';
    return n;
}

static int appendInfo(int n, char in[]) {
    in[n++] = 'I';
    in[n++] = 'n';
    in[n++] = 'f';
    in[n++] = 'o';
    return n;
}

static int appendPackage(int n, char in[]) {
    in[n++] = 'P';
    in[n++] = 'a';
    in[n++] = 'c';
    in[n++] = 'k';
    in[n++] = 'a';
    in[n++] = 'g';
    in[n++] = 'e';
    return n;
}

static int appendManager(int n, char in[]) {
    in[n++] = 'M';
    in[n++] = 'a';
    in[n++] = 'n';
    in[n++] = 'a';
    in[n++] = 'g';
    in[n++] = 'e';
    in[n++] = 'r';
    return n;
}

static int appendName(int n, char in[]) {
    in[n++] = 'N';
    in[n++] = 'a';
    in[n++] = 'm';
    in[n++] = 'e';
    return n;
}

static int appendandroid(int n, char in[]) {
    in[n++] = 'a';
    in[n++] = 'n';
    in[n++] = 'd';
    in[n++] = 'r';
    in[n++] = 'o';
    in[n++] = 'i';
    in[n++] = 'd';
    return n;
}

static int appendcontent(int n, char in[]) {
    in[n++] = 'c';
    in[n++] = 'o';
    in[n++] = 'n';
    in[n++] = 't';
    in[n++] = 'e';
    in[n++] = 'n';
    in[n++] = 't';
    return n;
}

static int appendpmPrefix(int n, char in[]) {
    //Landroid/content/pm/
    in[n++] = 'L';
    n = appendandroid(n, in);
    in[n++] = '/';
    n = appendcontent(n, in);
    in[n++] = '/';
    in[n++] = 'p';
    in[n++] = 'm';
    in[n++] = '/';
    return n;
}

static int appendLString(int n, char in[]) {
    //Ljava/lang/String;
    in[n++] = 'L';
    in[n++] = 'j';
    in[n++] = 'a';
    in[n++] = 'v';
    in[n++] = 'a';
    in[n++] = '/';
    in[n++] = 'l';
    in[n++] = 'a';
    in[n++] = 'n';
    in[n++] = 'g';
    in[n++] = '/';
    in[n++] = 'S';
    in[n++] = 't';
    in[n++] = 'r';
    in[n++] = 'i';
    in[n++] = 'n';
    in[n++] = 'g';
    in[n++] = ';';
    return n;
}

static int appendCertificate(int n, char in[]) {
    in[n++] = 'C';
    in[n++] = 'e';
    in[n++] = 'r';
    in[n++] = 't';
    in[n++] = 'i';
    in[n++] = 'f';
    in[n++] = 'i';
    in[n++] = 'c';
    in[n++] = 'a';
    in[n++] = 't';
    in[n++] = 'e';
    return n;
}

static int appendFactory(int n, char in[]) {
    in[n++] = 'F';
    in[n++] = 'a';
    in[n++] = 'c';
    in[n++] = 't';
    in[n++] = 'o';
    in[n++] = 'r';
    in[n++] = 'y';
    return n;
}

static char getPackageName[20];
static char* getPackageNameFunc() {
    int n = 0;
    n = appendget(n, getPackageName);
    n = appendPackage(n, getPackageName);
    n = appendName(n, getPackageName);
    getPackageName[n++] = 0;
    return getPackageName;
}

static char getPackageNameSig[100];
static char* getPackageNameSigFunc() {
    //()Ljava/lang/String;
    int n = 0;
    getPackageNameSig[n++] = '(';
    getPackageNameSig[n++] = ')';
    n = appendLString(n, getPackageNameSig);
    getPackageNameSig[n++] = 0;
    return getPackageNameSig;
}

static char getPackageManagerSig[100];
static char* getPackageManagerSigFunc() {
    //()Landroid/content/pm/PackageManager;
    int n = 0;
    getPackageManagerSig[n++] = '(';
    getPackageManagerSig[n++] = ')';
    n = appendpmPrefix(n, getPackageManagerSig);
    n = appendPackage(n, getPackageManagerSig);
    n = appendManager(n, getPackageManagerSig);
    getPackageManagerSig[n++] = ';';
    getPackageManagerSig[n++] = 0;
    return getPackageManagerSig;
}

static char getPackageManager[50];
static char* getPackageManagerFunc() {
    int n = 0;
    n = appendget(n, getPackageManager);
    n = appendPackage(n, getPackageManager);
    n = appendManager(n, getPackageManager);
    getPackageManager[n++] = 0;
    return getPackageManager;
};


static char getPackageInfo[20];
static char* getPackageInfoFunc() {
    int n = 0;
    n = appendget(n, getPackageInfo);
    n = appendPackage(n, getPackageInfo);
    n = appendInfo(n, getPackageInfo);
    getPackageInfo[n++] = 0;
    return getPackageInfo;
}

static char getPackageInfoSig[100];
static char* getPackageInfoSigFunc() {
    //(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;
    int n = 0;
    getPackageInfoSig[n++] = '(';
    n = appendLString(n, getPackageInfoSig);
    getPackageInfoSig[n++] = 'I';
    getPackageInfoSig[n++] = ')';
    n = appendpmPrefix(n, getPackageInfoSig);
    n = appendPackage(n, getPackageInfoSig);
    n = appendInfo(n, getPackageInfoSig);
    getPackageInfoSig[n++] = ';';
    getPackageInfoSig[n++] = 0;
    return getPackageInfoSig;
}

static char signatures[20];
static char* signaturesFunc() {
    int n = 0;
    signatures[n++] = 's';
    signatures[n++] = 'i';
    signatures[n++] = 'g';
    signatures[n++] = 'n';
    signatures[n++] = 'a';
    signatures[n++] = 't';
    signatures[n++] = 'u';
    signatures[n++] = 'r';
    signatures[n++] = 'e';
    signatures[n++] = 's';
    signatures[n++] = 0;
    return signatures;
}

static char signaturesSig[100];
static char* signaturesSigFunc() {
    //[Landroid/content/pm/Signature;
    int n = 0;
    signaturesSig[n++] = '[';
    n = appendpmPrefix(n, signaturesSig);
    signaturesSig[n++] = 'S';
    signaturesSig[n++] = 'i';
    signaturesSig[n++] = 'g';
    signaturesSig[n++] = 'n';
    signaturesSig[n++] = 'a';
    signaturesSig[n++] = 't';
    signaturesSig[n++] = 'u';
    signaturesSig[n++] = 'r';
    signaturesSig[n++] = 'e';
    signaturesSig[n++] = ';';
    signaturesSig[n++] = 0;
    return signaturesSig;
}

//"toByteArray", "()[B"
static char toByteArray[20];
static char* toByteArrayFunc() {
    int n = 0;
    toByteArray[n++] = 't';
    toByteArray[n++] = 'o';
    toByteArray[n++] = 'B';
    toByteArray[n++] = 'y';
    toByteArray[n++] = 't';
    toByteArray[n++] = 'e';
    toByteArray[n++] = 'A';
    toByteArray[n++] = 'r';
    toByteArray[n++] = 'r';
    toByteArray[n++] = 'a';
    toByteArray[n++] = 'y';
    toByteArray[n++] = 0;
    return toByteArray;
}

static char toByteArraySig[10];
static char* toByteArraySigFunc() {
    int n = 0;
    toByteArraySig[n++] = '(';
    toByteArraySig[n++] = ')';
    toByteArraySig[n++] = '[';
    toByteArraySig[n++] = 'B';
    toByteArraySig[n++] = 0;
    return toByteArraySig;
}

static char ByteArrayInputStream[50];
static char* ByteArrayInputStreamFunc() {
    //java/io/ByteArrayInputStream
    int n = 0;
    n = appendjava(n, ByteArrayInputStream);
    ByteArrayInputStream[n++] = '/';
    ByteArrayInputStream[n++] = 'i';
    ByteArrayInputStream[n++] = 'o';
    ByteArrayInputStream[n++] = '/';
    ByteArrayInputStream[n++] = 'B';
    ByteArrayInputStream[n++] = 'y';
    ByteArrayInputStream[n++] = 't';
    ByteArrayInputStream[n++] = 'e';
    ByteArrayInputStream[n++] = 'A';
    ByteArrayInputStream[n++] = 'r';
    ByteArrayInputStream[n++] = 'r';
    ByteArrayInputStream[n++] = 'a';
    ByteArrayInputStream[n++] = 'y';
    ByteArrayInputStream[n++] = 'I';
    ByteArrayInputStream[n++] = 'n';
    ByteArrayInputStream[n++] = 'p';
    ByteArrayInputStream[n++] = 'u';
    ByteArrayInputStream[n++] = 't';
    ByteArrayInputStream[n++] = 'S';
    ByteArrayInputStream[n++] = 't';
    ByteArrayInputStream[n++] = 'r';
    ByteArrayInputStream[n++] = 'e';
    ByteArrayInputStream[n++] = 'a';
    ByteArrayInputStream[n++] = 'm';
    ByteArrayInputStream[n++] = 0;
    return ByteArrayInputStream;
}

//"<init>","([B)V"
static char init[10];
static char* initFunc() {
    int n = 0;
    init[n++] = '<';
    init[n++] = 'i';
    init[n++] = 'n';
    init[n++] = 'i';
    init[n++] = 't';
    init[n++] = '>';
    init[n++] = 0;
    return init;
}
static char initSig[10];
static char* initSigFunc() {
    int n = 0;
    initSig[n++] = '(';
    initSig[n++] = '[';
    initSig[n++] = 'B';
    initSig[n++] = ')';
    initSig[n++] = 'V';
    initSig[n++] = 0;
    return initSig;
}

static char CertificateFactory[100];
static char* CertificateFactoryFunc() {
    //java/security/cert/CertificateFactory
    int n = 0;
    n = appendjavascPrefix(n, CertificateFactory);
    n = appendCertificate(n, CertificateFactory);
    n = appendFactory(n, CertificateFactory);
    CertificateFactory[n++] = 0;
    return CertificateFactory;
}

static char getInstance[20];
static char* getInstanceFunc() {
    int n = 0;
    getInstance[n++] = 'g';
    getInstance[n++] = 'e';
    getInstance[n++] = 't';
    getInstance[n++] = 'I';
    getInstance[n++] = 'n';
    getInstance[n++] = 's';
    getInstance[n++] = 't';
    getInstance[n++] = 'a';
    getInstance[n++] = 'n';
    getInstance[n++] = 'c';
    getInstance[n++] = 'e';
    getInstance[n++] = 0;
    return getInstance;
}

static char getInstanceSig[100];
static char* getInstanceSigFunc() {
    //(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;
    int n = 0;
    getInstanceSig[n++] = '(';
    n = appendLString(n, getInstanceSig);
    getInstanceSig[n++] = ')';
    getInstanceSig[n++] = 'L';
    n = appendjavascPrefix(n, getInstanceSig);
    n = appendCertificate(n, getInstanceSig);
    n = appendFactory(n, getInstanceSig);
    getInstanceSig[n++] = ';';
    getInstanceSig[n++] = 0;
    return getInstanceSig;
}

static char getInstanceMDSig[100];
static char* getInstanceMDSigFunc() {
    //(Ljava/lang/String;)Ljava/security/MessageDigest;
    int n = 0;
    getInstanceMDSig[n++] = '(';
    n = appendLString(n, getInstanceMDSig);
    getInstanceMDSig[n++] = ')';
    getInstanceMDSig[n++] = 'L';
    n = appendjavasm(n, getInstanceMDSig);
    getInstanceMDSig[n++] = ';';
    getInstanceMDSig[n++] = 0;
    return getInstanceMDSig;
}

static char X509[10];
static char* X509Func() {
    //X.509
    int n = 0;
    X509[n++] = 'X';
    X509[n++] = '.';
    X509[n++] = '5';
    X509[n++] = '0';
    X509[n++] = '9';
    X509[n++] = 0;
    return X509;
}

static char SHA1[10];
static char* SHA1Func() {
    int n = 0;
    SHA1[n++] = 'S';
    SHA1[n++] = 'H';
    SHA1[n++] = 'A';
    SHA1[n++] = '1';
    SHA1[n++] = 0;
    return SHA1;
}

static char digest[10];
static char* digestFunc() {
    int n = 0;
    digest[n++] = 'd';
    digest[n++] = 'i';
    digest[n++] = 'g';
    digest[n++] = 'e';
    digest[n++] = 's';
    digest[n++] = 't';
    digest[n++] = 0;
    return digest;
}

static char digestSig[10];
static char* digestSigFunc() {
    //([B)[B
    int n = 0;
    digestSig[n++] = '(';
    digestSig[n++] = '[';
    digestSig[n++] = 'B';
    digestSig[n++] = ')';
    digestSig[n++] = '[';
    digestSig[n++] = 'B';
    digestSig[n++] = 0;
    return digestSig;
}

static char generateCertificate[30];
static char* generateCertificateFunc() {
    //generateCertificate
    int n = 0;
    generateCertificate[n++] = 'g';
    generateCertificate[n++] = 'e';
    generateCertificate[n++] = 'n';
    generateCertificate[n++] = 'e';
    generateCertificate[n++] = 'r';
    generateCertificate[n++] = 'a';
    generateCertificate[n++] = 't';
    generateCertificate[n++] = 'e';
    n = appendCertificate(n, generateCertificate);
    generateCertificate[n++] = 0;
    return generateCertificate;
}

static char generateCertificateSig[100];
static char* generateCertificateSigFunc() {
    //(Ljava/io/InputStream;)Ljava/security/cert/Certificate;
    int n = 0;
    generateCertificateSig[n++] = '(';
    generateCertificateSig[n++] = 'L';
    n = appendjava(n, generateCertificateSig);
    generateCertificateSig[n++] = '/';
    generateCertificateSig[n++] = 'i';
    generateCertificateSig[n++] = 'o';
    generateCertificateSig[n++] = '/';
    generateCertificateSig[n++] = 'I';
    generateCertificateSig[n++] = 'n';
    generateCertificateSig[n++] = 'p';
    generateCertificateSig[n++] = 'u';
    generateCertificateSig[n++] = 't';
    generateCertificateSig[n++] = 'S';
    generateCertificateSig[n++] = 't';
    generateCertificateSig[n++] = 'r';
    generateCertificateSig[n++] = 'e';
    generateCertificateSig[n++] = 'a';
    generateCertificateSig[n++] = 'm';
    generateCertificateSig[n++] = ';';
    generateCertificateSig[n++] = ')';
    generateCertificateSig[n++] = 'L';
    n = appendjavascPrefix(n, generateCertificateSig);
    n = appendCertificate(n, generateCertificateSig);
    generateCertificateSig[n++] = ';';
    generateCertificateSig[n++] = 0;
    return generateCertificateSig;
}

static char getEncoded[20];
static char* getEncodedFunc() {
    //getEncoded
    int n = 0;
    getEncoded[n++] = 'g';
    getEncoded[n++] = 'e';
    getEncoded[n++] = 't';
    getEncoded[n++] = 'E';
    getEncoded[n++] = 'n';
    getEncoded[n++] = 'c';
    getEncoded[n++] = 'o';
    getEncoded[n++] = 'd';
    getEncoded[n++] = 'e';
    getEncoded[n++] = 'd';
    getEncoded[n++] = 0;
    return getEncoded;
}

static char getEncodedSig[20];
static char* getEncodedSigFunc() {
    //()[B
    int n = 0;
    getEncodedSig[n++] = '(';
    getEncodedSig[n++] = ')';
    getEncodedSig[n++] = '[';
    getEncodedSig[n++] = 'B';
    getEncodedSig[n++] = 0;
    return getEncodedSig;
}

static char MessageDigest[50];
static char* MessageDigestFunc() {
    //java/security/MessageDigest
    int n = 0;
    n = appendjavasm(n, MessageDigest);
    MessageDigest[n++] = 0;
    return MessageDigest;
}

/**
 * 校验APP 包名和签名是否合法
 *
 * 返回值为1 表示合法
 */
jint check_signature(JNIEnv *env, jobject thiz, jobject context);
jint check_signature_sha1(JNIEnv *env, jobject type, jobject context);

#endif //AESJNIENCRYPT_SIGNACTURECHECK_H
