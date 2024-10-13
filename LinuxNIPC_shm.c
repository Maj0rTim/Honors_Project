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

#include "SharedMemoryChannel.h"

void setErrnum (JNIEnv *, jobject, int);

#define WRITE_SEM 0
#define READ_SEM 1

JNIEXPORT void JNICALL Java_SharedMemoryChannel_initShrSem (JNIEnv *env, jobject obj, jstring key, jint size, jint initSems)
  { int shmid;
    int semid;
    int shmaddr;

    key_t keys = ftok(key, 4242);

    shmid = shmget(key, size+4, IPC_CREAT | 0600);
    if (shmid == -1) { 
        setErrnum(env, obj, errno);
        return;
    }

    shmaddr = shmat(shmid, 0, 0);
    if (shmaddr == -1) { 
        setErrnum(env, obj, errno);
        return;
    }

    semid = semget(key, 2, IPC_CREAT | 0600);
    if (semid == -1) { 
        setErrnum(env, obj, errno);
        return;
    }

    printf("Shared memory ID %p\n", shmid);
	printf("Shared memory address %p\n", shmaddr);
    printf("Semaphore ID %p\n", semid);

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

    if (initSems) { 
        if (semctl(semid, WRITE_SEM, SETVAL, 1) == -1) { 
            setErrnum(env, obj, errno);
            return;
        }

        if (semctl(semid, READ_SEM, SETVAL, 0) == -1) {
            setErrnum(env, obj, errno);
        }
    }
}

JNIEXPORT jint JNICALL Java_SharedMemoryChannel_sendMsg (JNIEnv *env, jobject obj, jint shmid, jint semid, jobject buf, jint offset, jint len) {
    struct sembuf sb;
    sb.sem_num = WRITE_SEM;
    sb.sem_op = -1;
    sb.sem_flg = 0;
    
    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        return -1;
    }
    char *segment = (char*) shmat(shmid, NULL, 0);
    void* bufferAddress = (*env)->GetDirectBufferAddress(env, buf);
   
    if (bufferAddress == NULL) {
        setErrnum(env, obj, ENOMEM);  
        return -1;
    }
    memcpy(segment, (char*)bufferAddress + offset, len);
    sb.sem_num = READ_SEM;
    sb.sem_op = 1;
  
    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        return -1;
    }
    return 1; 
}

JNIEXPORT jint JNICALL Java_SharedMemoryChannel_getMsg (JNIEnv *env, jobject obj, jint shmid, jint semid, jobject buf, jint len) {
    struct sembuf sb;
    sb.sem_num = READ_SEM;
    sb.sem_op = -1;
    sb.sem_flg = 0;

    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        return -1;
    }
    char *segment = (char*) shmat(shmid, NULL, 0);
    void* bufferAddress = (*env)->GetDirectBufferAddress(env, buf);

    if (bufferAddress == NULL) {
        setErrnum(env, obj, ENOMEM);  
        return -1;
    }
    memcpy(bufferAddress, segment, len);
    sb.sem_num = WRITE_SEM;
    sb.sem_op = 1;

    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        len = -1;
    }
    return len;
}

JNIEXPORT void JNICALL Java_SharedMemoryChannel_closeShm (JNIEnv *env, jobject obj, jint shmid, jint shmaddr, jint semid, jint removeIds) { 
    if (shmdt(shmaddr) == -1) {
        setErrnum(env, obj, errno);
    }
    if (removeIds) { 
        if (shmctl(shmid, IPC_RMID, 0) == -1) {
          setErrnum(env, obj, errno);
        }
        if (semctl(semid, 0, IPC_RMID, 0) == -1) {
          setErrnum(env, obj, errno);
        }
    }
}

JNIEXPORT jstring JNICALL Java_SharedMemoryChannel_strerror (JNIEnv *env, jobject obj, jint errnum) {
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
