#include <jni.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <linux/ipc.h>
#include <linux/sem.h>
#include <linux/shm.h>

#include "SharedMemoryStreams.h"

#define WRITE_SEM 0
#define READ_SEM 1

void setErrnum (JNIEnv *, jobject, jint);

/*
 * Class:     SharedMemoryStreams
 * Method:    initStream
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_SharedMemoryStreams_initStream (JNIEnv *env, jobject obj, jint key, jint size, jint initSems)
  { int shmid;
    int semid;
    int shmaddr;
    shmid = shmget(key, size+4, IPC_CREAT | 0600);
    if (shmid == -1)
      { setErrnum(env, obj, errno);
        return;
      }
    shmaddr = shmat(shmid, 0, 0);
    if (shmaddr == -1)
      { setErrnum(env, obj, errno);
        return;
      }
    // Now create semaphore set
    semid = semget(key, 2, IPC_CREAT | 0600);
    if (semid == -1)
      { setErrnum(env, obj, errno);
        return;
      }
    // Now initialise fields of Java object
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "initFields", "(III)V");
    if (mid == 0)
      { printf("Can't find method initFields\n");
        return;
      }
    (*env)->ExceptionClear(env);
    (*env)->CallVoidMethod(env, obj, mid, shmid, shmaddr, semid);
    if ((*env)->ExceptionOccurred(env))
      { printf("Error occured calling initFields\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
      }
    // Initialise semaphore values if necessary
    if (initSems)
      { union semun semopts;
        semopts.val = 1;
        if (semctl(semid, WRITE_SEM, SETVAL, semopts) == -1) // Ready to write
          { setErrnum(env, obj, errno);
            return;
          }
        semopts.val = 0;
        if (semctl(semid, READ_SEM, SETVAL, semopts) == -1) // Not ready to read
          setErrnum(env, obj, errno);
      }
  } // initStream

/*
 * Class:     SharedMemoryStreams
 * Method:    sendData
 * Signature: (II[BII)I
 */
JNIEXPORT jint JNICALL Java_SharedMemoryStreams_sendData (JNIEnv *env, jobject obj, jint shmaddr, jint semid, jbyteArray buf, jint offset, jint len)
  { struct sembuf sb;
    sb.sem_num = WRITE_SEM;
    sb.sem_op = -1;
    sb.sem_flg = 0;
    if (semop(semid, &sb, 1) == -1)
      { setErrnum(env, obj, errno);
        return;
      }

    int *p = (int *)shmaddr;
    *p = len;
    p++;
    jbyte* bytes = (*env)->GetByteArrayElements(env, buf, 0);
    memcpy(p, bytes+offset, len);
    (*env)->ReleaseByteArrayElements(env, buf, bytes, 0);
    sb.sem_num = READ_SEM;
    sb.sem_op = 1; 
    if (semop(semid, &sb, 1) == -1)
      setErrnum(env, obj, errno);
  } // sendData

/*
 * Class:     SharedMemoryStreams
 * Method:    fillBuffer
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_SharedMemoryStreams_fillBuffer (JNIEnv *env, jobject obj, jint shmaddr, jint semid, jbyteArray buf)
  { struct sembuf sb;
    sb.sem_num = READ_SEM;
    sb.sem_op = -1;
    sb.sem_flg = 0;
    if (semop(semid, &sb, 1) == -1)
      { setErrnum(env, obj, errno);
        return -1;
      }

    int *p = (int *)shmaddr;
    int len = *p;
    p++;
    (*env)->SetByteArrayRegion(env, buf, 0, len, (jbyte *)p);

    sb.sem_num = WRITE_SEM;
    sb.sem_op = 1;
    if (semop(semid, &sb, 1) == -1)
      { setErrnum(env, obj, errno);
        len = -1;
      }
    return len;
  } // fillBuffer
  
/*
 * Class:     SharedMemoryStreams
 * Method:    close
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_SharedMemoryStreams_close (JNIEnv *env, jobject obj, jint shmid, jint shmaddr, jint semid, jint removeIds)
  { if (shmdt(shmaddr) == -1)
      setErrnum(env, obj, errno);
    if (removeIds)
      { if (shmctl(shmid, IPC_RMID, 0) == -1)
          setErrnum(env, obj, errno);
        if (semctl(semid, 0, IPC_RMID, 0) == -1)
          setErrnum(env, obj, errno);
      }
  } // close

/* Function to set the error code in the Java program.  Calls setErrnum in
 * the LinuxIPC class.
 */
void setErrnum (JNIEnv *env, jobject obj, jint errnum)
  { jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "setErrnum", "(I)V");
    if (mid == 0)
      { printf("Can't find method setErrnum\n");
        return;
      }

    (*env)->ExceptionClear(env);
    (*env)->CallVoidMethod(env, obj, mid, errnum);
    if ((*env)->ExceptionOccurred(env))
      { printf("Error occured calling setErrnum\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
      }
  } // setErrnum

/*
 * Class:     LinuxIPC
 * Method:    strerror
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_LinuxIPC_strerror (JNIEnv *env, jobject obj, jint errnum)
  { const char * err_str = strerror (errnum);
    return ((*env)->NewStringUTF(env, err_str));
  } // strerror
  
/* To compile:
   gcc -o libSharedMemoryStreams.so -shared -I/usr/lib/jvm/java-6-sun/include -I/usr/lib/jvm/java-6-sun/include/linux SharedMemoryStreams.c
*/
