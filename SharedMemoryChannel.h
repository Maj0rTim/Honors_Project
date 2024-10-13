/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class SharedMemoryChannel */

#ifndef _Included_SharedMemoryChannel
#define _Included_SharedMemoryChannel
#ifdef __cplusplus
extern "C" {
#endif
#undef SharedMemoryChannel_MAX_BUF_SIZE
#define SharedMemoryChannel_MAX_BUF_SIZE 40960L
/*
 * Class:     SharedMemoryChannel
 * Method:    initShrSem
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_SharedMemoryChannel_initShrSem
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     SharedMemoryChannel
 * Method:    sendMsg
 * Signature: (IILjava/nio/ByteBuffer;II)I
 */
JNIEXPORT jint JNICALL Java_SharedMemoryChannel_sendMsg
  (JNIEnv *, jobject, jint, jint, jobject, jint, jint);

/*
 * Class:     SharedMemoryChannel
 * Method:    getMsg
 * Signature: (IILjava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_SharedMemoryChannel_getMsg
  (JNIEnv *, jobject, jint, jint, jobject, jint);

/*
 * Class:     SharedMemoryChannel
 * Method:    closeShm
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_SharedMemoryChannel_closeShm
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     SharedMemoryChannel
 * Method:    strerror
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_SharedMemoryChannel_strerror
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
