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

#include "LinuxNIPC.h"

void setErrnum (JNIEnv *, jobject, int);

#define WRITE_SEM 0
#define READ_SEM 1

typedef struct message_buffer { 
    long mtype;
    char msg;
} msgbuf;

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

JNIEXPORT jint JNICALL Java_LinuxNIPC_ftok (JNIEnv * env, jobject obj, jstring pathname, jchar proj) { 
    jboolean iscopy;
    const char* c_pathname = (*env)->GetStringUTFChars(env, pathname, &iscopy);
    jint retval = 0;
    char c_proj = proj;

    if (iscopy) { 
        retval = ftok(c_pathname, c_proj);
        if (retval == -1)
            setErrnum(env, obj, errno);
    } else { retval = -1;
        setErrnum(env, obj, ENOENT);
    }
    (*env)->ReleaseStringUTFChars(env, pathname, c_pathname);
    return retval;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_msgget (JNIEnv * env, jobject obj, jint key, jint msgflg) { 
    jint retval = msgget(key, msgflg);
    if (retval == -1) {
        setErrnum(env, obj, errno);
    }
    return retval;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_msgsnd (JNIEnv * env, jobject obj, jint msgqid, jobject buffer, jint msgsz, jint msg_type, jint msgflgs) { 
    int sz;
    jint retval;
  
    if (msgsz < 0) {
        sz = (*env)->GetDirectBufferCapacity(env, buffer);
    } else {
        sz = msgsz;
    }

    void *bufferAddress = (*env)->GetDirectBufferAddress(env, buffer);
    if (bufferAddress == NULL) {
        setErrnum(env, obj, EFAULT); 
        return -1;
    }

    msgbuf *m = (msgbuf *)malloc(sizeof(msgbuf) + sz);
    if (m == NULL) {
        setErrnum(env, obj, ENOMEM);
        return -1;
    }

    m->mtype = msg_type;
    memcpy(&(m->msg), bufferAddress, sz);
    retval = msgsnd(msgqid, m, sz, msgflgs);

    if (retval != 0) {
        setErrnum(env, obj, errno);
    }

    free(m);
    return retval;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_msgrcv(JNIEnv *env, jobject obj, jint msgqid, jobject buffer, jint msgsz, jint msg_type, jint msgflgs) {
    int sz;
    jint retval;

    if (msgsz < 0) {
        sz = (*env)->GetDirectBufferCapacity(env, buffer);
    } else {
        sz = msgsz;
    }

    void *bufferAddress = (*env)->GetDirectBufferAddress(env, buffer);
    if (bufferAddress == NULL) {
        setErrnum(env, obj, EFAULT);
        return -1;
    }

    msgbuf *m = (msgbuf *)malloc(sizeof(msgbuf) + sz);
    if (m == NULL) {
        setErrnum(env, obj, ENOMEM);
        return -1;
    }

    retval = msgrcv(msgqid, m, sz, msg_type, msgflgs);

    if (retval >= 0) {
        memcpy(bufferAddress, &(m->msg), retval);
    } else {
        setErrnum(env, obj, errno);
    }

    free(m);
    return retval;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_msgRmid (JNIEnv *env, jobject obj, jint msgqid) {
    if (msgctl(msgqid, IPC_RMID, 0) == -1) {
        setErrnum(env, obj, errno);
        return -1;
    }
    return 0;
}


JNIEXPORT jint JNICALL Java_LinuxNIPC_initShrSem (JNIEnv *env, jobject obj, jint key, jint size, jint initSems)
  { int shmid;
    int semid;
    int shmaddr;
    shmid = shmget(key, size+4, IPC_CREAT | 0600);

    if (shmid == -1) { 
        setErrnum(env, obj, errno);
        return -1;
    }
    shmaddr = shmat(shmid, 0, 0);

    if (shmaddr == -1) { 
        setErrnum(env, obj, errno);
        return -1;
    }
    semid = semget(key, 2, IPC_CREAT | 0600);

    if (semid == -1) { 
        setErrnum(env, obj, errno);
        return -1;
    }

    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "initFields", "(III)V");
    if (mid == 0) { 
        printf("Can't find method initFields\n");
        return -1;
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
            return -1;
        }
        

        if (semctl(semid, READ_SEM, SETVAL, 0) == -1) {
            setErrnum(env, obj, errno);
        }
    }
    return shmid;
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_sendMsg (JNIEnv *env, jobject obj, jint shmaddr, jint semid, jobject buf, jint offset, jint len) {
    struct sembuf sb;
    sb.sem_num = WRITE_SEM;
    sb.sem_op = -1;
    sb.sem_flg = 0;

    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        return -1;
    }
    int *p = (int *)shmaddr;
    *p = len;
    p++;
    void* bufferAddress = (*env)->GetDirectBufferAddress(env, buf);

    if (bufferAddress == NULL) {
        setErrnum(env, obj, ENOMEM);  
        return -1;
    }
    memcpy(p, (char*)bufferAddress + offset, len);
    sb.sem_num = READ_SEM;
    sb.sem_op = 1;

    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        return -1;
    }
    return 0; 
}

JNIEXPORT jint JNICALL Java_LinuxNIPC_getMsg (JNIEnv *env, jobject obj, jint shmaddr, jint semid, jobject buf) {
    struct sembuf sb;
    sb.sem_num = READ_SEM;
    sb.sem_op = -1;
    sb.sem_flg = 0;

    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        return -1;
    }
    int *p = (int *)shmaddr;
    int len = *p;
    p++; 
    void* bufferAddress = (*env)->GetDirectBufferAddress(env, buf);

    if (bufferAddress == NULL) {
        setErrnum(env, obj, ENOMEM);  
        return -1;
    }
    memcpy(bufferAddress, p, len);
    sb.sem_num = WRITE_SEM;
    sb.sem_op = 1;

    if (semop(semid, &sb, 1) == -1) {
        setErrnum(env, obj, errno);
        len = -1;
    }
    return len;
}

JNIEXPORT void JNICALL Java_LinuxNIPC_closeShm (JNIEnv *env, jobject obj, jint shmid, jint shmaddr, jint semid, jint removeIds) { 
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
