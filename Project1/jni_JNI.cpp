#include <stdio.h>
#include <jni.h>
#include "jni_JNI.h"

JNIEXPORT jint JNICALL Java_jni_JNI_getNumber
(JNIEnv *env, jobject jobj) {
	return 3;
}

JNIEXPORT void JNICALL Java_jni_JNI_printHelloWorld
(JNIEnv *env, jobject jobj) {
	printf ("Hello World!!");
}