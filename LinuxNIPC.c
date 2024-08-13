#include <stdio.h>
#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#include <linux/stat.h>
#include <linux/ipc.h>
#include <linux/msg.h>


#include "LinuxIPC.h"

void setErrnum (JNIEnv *, jobject, int);

typedef struct message_buffer { 
    long type;
    char message;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_mkfifo (JNIEnv *env, jobject obj, jstring name, jint perms) {
    jboolean iscopy;
    const char* c_name = (*env)->GetStringUTFChars(env, name, &iscopy);
    jint retval = 0;
    
    if (iscopy) {
        retval = mknod(c_name, S_IFIFO|perms, 0);
        if (retval != 0) { setErrnum(env, obj, errno); }
    } else {
        retval = -1;
        setErrnum(env, obj, ENOENT);
    } 

    (*env)->ReleaseStringUTFChars(env, name, c_name);
    return retval;
} 

JNIEXPORT jstring JNICALL Java_LinuxNIPC_strerror (JNIEnv *env, jobject obj, jint errnum) {
    const char * err_str = strerror (errnum);
    return ((*env)->NewStringUTF(env, err_str));
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_ftok (JNIEnv * env, jobject obj, jstring pathname, jchar proj) { 
    jboolean iscopy;
    const char* c_pathname = (*env)->GetStringUTFChars(env, pathname, &iscopy);
    jint retval = 0;
    char c_proj = proj;
    
    if (iscopy) { 
        retval = ftok(c_pathname, c_proj);
        if (retval == -1) { setErrnum(env, obj, errno); }
    } else { 
        retval = -1;
        setErrnum(env, obj, ENOENT);
    }
    (*env)->ReleaseStringUTFChars(env, pathname, c_pathname);
    return retval;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_msgget (JNIEnv * env, jobject obj, jint key, jint flag) {
    jint num = msgget(key, flag);
    if (num == -1) { setErrnum(env, obj, errno); }
    return num;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_msgsnd (JNIEnv * env, jobject obj, jint id, jint type, jobject buffer, jint length, jint flags) {
    void *bufferAddress = (*env)->GetDirectBufferAddress(env, buffer);
    if (bufferAddress == NULL) { return -1; }
    struct message_buffer message

    memcpy(message.message, bufferAddress, length);

    jint num = msgsnd(id, &message, length, flags);
    if (num <= 0) { setErrnum(env, obj, errno); }
    
    return num;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_msgrcv (JNIEnv * env, jobject obj, jint id, jobject buffer, jint length, jint type, jint flags) { 
    void *bufferAddress = (*env)->GetDirectBufferAddress(env, buffer);
    if (bufferAddress == NULL) { return -1; }
    struct message_buffer message

    jint num = msgrcv(id, &message, length, type, flags);
    if (num >= 0) { memcpy(bufferAddress, message.message, length); }
    else { setErrnum(env, obj, errno); }
    
    return num;
}