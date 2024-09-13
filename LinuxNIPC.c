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

#include "LinuxNIPC.h"

void setErrnum (JNIEnv *, jobject, int);

typedef struct message_buffer { 
    long type;
    char message;
} mb;

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


void setErrnum (JNIEnv *env, jobject obj, int errnum) { 
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "setErrnum", "(I)V");
    if (mid == 0) { 
        printf("Can't find method setErrnum\n");
        return;
    }
    (*env)->ExceptionClear(env);
    (*env)->CallVoidMethod(env, obj, mid, errnum);
    if ((*env)->ExceptionOccurred(env)) { 
        printf("Error occured calling setErrnum\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
}
