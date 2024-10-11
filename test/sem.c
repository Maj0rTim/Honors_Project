#include <stdio.h>
#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#include <linux/stat.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/sem.h>
#include <sys/shm.h>

#include "sem.h"

// Semaphore operations
void sem_wait(int semid) {
    struct sembuf sb = {0, -1, 0};
    if (semop(semid, &sb, 1) == -1) {
        perror("sem_wait failed");
        exit(EXIT_FAILURE);
    }
}

void sem_signal(int semid) {
    struct sembuf sb = {0, 1, 0};
    if (semop(semid, &sb, 1) == -1) {
        perror("sem_signal failed");
        exit(EXIT_FAILURE);
    }
}

void setErrnum (JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL Java_sem_initShrSem (JNIEnv *env, jobject obj, jint key, jint size)
  { int shmid;
    int semid;
    int shmaddr;
    shmid = shmget(key, size, IPC_CREAT | 0666);
    shmaddr = shmat(shmid, 0, 0);
    
    semid = semget(key, 1, IPC_CREAT | 0666);
    if (semid == -1) {
        perror("semget failed");
        exit(EXIT_FAILURE);
    }
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "initFields", "(III)V");
    if (mid == 0) { 
        printf("Can't find method initFields\n");
        return;
    }
    (*env)->ExceptionClear(env);
    (*env)->CallVoidMethod(env, obj, mid, shmid, shmaddr, semid);

    if ((*env)->ExceptionOccurred(env)) { 
        printf("Error occured calling initFields\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
    semctl(semid, 0, SETVAL, 1);
}

JNIEXPORT void JNICALL Java_sem_sendMsg (JNIEnv *env, jobject obj, jint shmaddr, jint semid, jobject buf, jint len) {
    int *segment = (int *)shmaddr;
    sem_wait(semid);

    jbyte *data = (jbyte *) (*env)->GetDirectBufferAddress(env, buf);
    memcpy(segment, data, len);
    
    sem_signal(semid);
    shmdt(segment);
}

JNIEXPORT void JNICALL Java_sem_getMsg (JNIEnv *env, jobject obj, jint shmaddr, jint semid, jobject buf, jint len) {
    int *segment = (int *)shmaddr;
    sem_wait(semid);

    jbyte *data = (jbyte *) (*env)->GetDirectBufferAddress(env, buf);
    memcpy(data, segment, len);

    sem_signal(semid);
    shmdt(segment);
}

JNIEXPORT void JNICALL Java_sem_closeShm (JNIEnv *env, jobject obj, jint shmid, jint shmaddr, jint semid, jint removeIds) { 
    shmctl(shmid, IPC_RMID, NULL);
}

JNIEXPORT jstring JNICALL Java_sem_strerror (JNIEnv *env, jobject obj, jint errnum) {
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