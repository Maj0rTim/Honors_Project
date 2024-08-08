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
#include <linux/sem.h>
#include <linux/shm.h>

#include "LinuxIPC.h"

void setErrnum (JNIEnv *, jobject, int);

/* msgbuf type used as a pattern for message queue
 * messages.
 */
typedef struct msg_buffer
  { long mtype; // Type
    char msg; // Usually much longer
  } msgbuf;

/*
 * Class:     LinuxIPC
 * Method:    mkfifo
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_mkfifo (JNIEnv *env, jobject obj, jstring name, jint perms)
  { jboolean iscopy;
    const char* c_name = (*env)->GetStringUTFChars(env, name, &iscopy);
    jint retval = 0;
    
    if (iscopy) // success
      { // printf("Filename: %s\n", c_name);
        retval = mknod(c_name, S_IFIFO|perms, 0);
        if (retval != 0) // Problem!
          setErrnum(env, obj, errno);
      }
    else
      { retval = -1;
        setErrnum(env, obj, ENOENT);
      } // else

    (*env)->ReleaseStringUTFChars(env, name, c_name); // Release the string

    return retval;
  } // mkfifo


/*
 * Class:     LinuxIPC
 * Method:    strerror
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_LinuxIPC_strerror (JNIEnv *env, jobject obj, jint errnum)
  { const char * err_str = strerror (errnum);
    return ((*env)->NewStringUTF(env, err_str));
  } // strerror
  
  
/*
 * Class:     LinuxIPC
 * Method:    ftok
 * Signature: (Ljava/lang/String;C)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_ftok (JNIEnv * env, jobject obj, jstring pathname, jchar proj)
  { jboolean iscopy;
    const char* c_pathname = (*env)->GetStringUTFChars(env, pathname, &iscopy);
    jint retval = 0;
    char c_proj = proj;
    
    if (iscopy) // success
      { retval = ftok(c_pathname, c_proj);
        if (retval == -1) // Problem!
          setErrnum(env, obj, errno);
      }
    else
      { retval = -1;
        setErrnum(env, obj, ENOENT);
      } // else

    (*env)->ReleaseStringUTFChars(env, pathname, c_pathname); // Release the string

    return retval;
  } // ftok


/*
 * Class:     LinuxIPC
 * Method:    msgget
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_msgget (JNIEnv * env, jobject obj, jint key, jint msgflg)
  { jint retval = msgget(key, msgflg);
    if (retval == -1)
      setErrnum(env, obj, errno);
    return retval;
  } // msgget


/*
 * Class:     LinuxIPC
 * Method:    msgsnd
 * Signature: (II[BII)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_msgsnd (JNIEnv * env, jobject obj, jint msgqid, jint msg_type, jbyteArray msg, jint msgsz, jint msgflgs)
  { int sz;
    jint retval;
  
    if (msgsz < 0)
      sz = (*env)->GetArrayLength(env, msg);
    else
      sz = msgsz;
    msgbuf * m = malloc(sz + sizeof(long)); // Include type field
    jbyte* bytes = (*env)->GetByteArrayElements(env, msg, 0);
    m->mtype = msg_type;
    memcpy(&(m->msg), bytes, sz);
    (*env)->ReleaseByteArrayElements(env, msg, bytes, 0);
    retval = msgsnd(msgqid, m, sz, msgflgs);
    if (retval != 0)
      setErrnum(env, obj, errno);
    free(m);
    return retval;
  } // msgsnd


/*
 * Class:     LinuxIPC
 * Method:    msgrcv
 * Signature: (I[BIII)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_msgrcv (JNIEnv * env, jobject obj, jint msgqid, jbyteArray msg, jint msgsz, jint msg_type, jint msgflgs)
  { int sz;
    jint retval;
  
    if (msgsz < 0)
      sz = (*env)->GetArrayLength(env, msg);
    else
      sz = msgsz;
    msgbuf * m = malloc(sz + sizeof(long)); // Include type field
  
    retval = msgrcv(msgqid, m, sz, msg_type, msgflgs);
    if (retval >= 0) // Copy data across
      { (*env)->SetByteArrayRegion(env, msg, 0, retval, &(m->msg));
      }
    else
      setErrnum(env, obj, errno);
    free(m);
    return retval;
  } // msgrcv
  
/*
 * Class:     LinuxIPC
 * Method:    msgctl
 * Signature: (IILLinuxIPC/Msqid_ds;)I

JNIEXPORT jint JNICALL Java_LinuxIPC_msgctl (JNIEnv * env, jobject obj, jint msgqid, jint cmd, jobject buf)
  { // First make the call
    struct msqid_ds qbuf;
    
    printf("about to call msgctl\n");
    if (msgctl(msgqid, cmd, &qbuf) == -1)
      { setErrnum(env, obj, errno);
        return(-1);
      }
    printf("back from call to msgctl\n");

    // Then populate the Java object
    printf("About to call GetObjectClass.  buf = %xd\n", buf);
    // jclass cls = (*env)->GetObjectClass(env, buf);
    jclass cls = (*env)->FindClass(env, "LinuxIPC$Msqid_ds");
    printf("back from call to GetObjectClass\n");
    jfieldID fid = (*env)->GetFieldID(env, cls, "msg_perm",  "LLinuxIPC$Ipc_perm");
    printf("back from call to GetFieldID\n");
    if (fid == 0)
      { printf("Can't find field msg_perm");
        return -1;
      }
    else
      { printf("starting to work on ipc_perm structure\n");
        jobject perm_obj = (*env)->GetObjectField(env, buf, fid);
        cls = (*env)->GetObjectClass(env, perm_obj);
        
        fid = (*env)->GetFieldID(env, cls, "key",  "I");
        if (fid == 0)
          { printf("Can't find field key");
            return -1;
          }
        (*env)->SetIntField(env, perm_obj, fid, qbuf.msg_perm.key);
        
        fid = (*env)->GetFieldID(env, cls, "uid",  "I");
        if (fid == 0)
          { printf("Can't find field uid");
            return -1;
          }
        (*env)->SetIntField(env, perm_obj, fid, qbuf.msg_perm.uid);
        
        fid = (*env)->GetFieldID(env, cls, "gid",  "I");
        if (fid == 0)
          { printf("Can't find field gid");
            return -1;
          }
        (*env)->SetIntField(env, perm_obj, fid, qbuf.msg_perm.gid);
        
        fid = (*env)->GetFieldID(env, cls, "cuid",  "I");
        if (fid == 0)
          { printf("Can't find field cuid");
            return -1;
          }
        (*env)->SetIntField(env, perm_obj, fid, qbuf.msg_perm.cuid);
        
        fid = (*env)->GetFieldID(env, cls, "cgid",  "I");
        if (fid == 0)
          { printf("Can't find field cgid");
            return -1;
          }
        (*env)->SetIntField(env, perm_obj, fid, qbuf.msg_perm.cgid);
        
        fid = (*env)->GetFieldID(env, cls, "mode",  "I");
        if (fid == 0)
          { printf("Can't find field mode");
            return -1;
          }
        (*env)->SetIntField(env, perm_obj, fid, qbuf.msg_perm.mode);
        
        fid = (*env)->GetFieldID(env, cls, "seq",  "I");
        if (fid == 0)
          { printf("Can't find field seq");
            return -1;
          }
        (*env)->SetIntField(env, perm_obj, fid, qbuf.msg_perm.seq);
      } // else - setting msg_perm fields
    // Now fill rest of msqid_ds fields
    cls = (*env)->GetObjectClass(env, buf);
    
    fid = (*env)->GetFieldID(env, cls, "msg_stime",  "I");
    if (fid == 0)
      { printf("Can't find field msg_stime");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_stime);

    fid = (*env)->GetFieldID(env, cls, "msg_rtime",  "I");
    if (fid == 0)
      { printf("Can't find field msg_rtime");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_rtime);

    fid = (*env)->GetFieldID(env, cls, "msg_ctime",  "I");
    if (fid == 0)
      { printf("Can't find field msg_ctime");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_ctime);

    fid = (*env)->GetFieldID(env, cls, "msg_lcbytes",  "I");
    if (fid == 0)
      { printf("Can't find field msg_lcbytes");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_lcbytes);

    fid = (*env)->GetFieldID(env, cls, "msg_lqbytes",  "I");
    if (fid == 0)
      { printf("Can't find field msg_lqbytes");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_lqbytes);

    fid = (*env)->GetFieldID(env, cls, "msg_cbytes",  "I");
    if (fid == 0)
      { printf("Can't find field msg_cbytes");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_cbytes);

    fid = (*env)->GetFieldID(env, cls, "msg_qnum",  "I");
    if (fid == 0)
      { printf("Can't find field msg_qnum");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_qnum);

    fid = (*env)->GetFieldID(env, cls, "msg_qbytes",  "I");
    if (fid == 0)
      { printf("Can't find field msg_qbytes");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_qbytes);
    
    fid = (*env)->GetFieldID(env, cls, "msg_lspid",  "I");
    if (fid == 0)
      { printf("Can't find field msg_lspid");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_lspid);
    
    fid = (*env)->GetFieldID(env, cls, "msg_lrpid",  "I");
    if (fid == 0)
      { printf("Can't find field msg_lrpid");
        return -1;
      }
    (*env)->SetIntField(env, buf, fid, qbuf.msg_lrpid);
    
    return(0);
  } // msgctl
*/


/*
 * Class:     LinuxIPC
 * Method:    msgRmid
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_msgRmid (JNIEnv * env, jobject obj, jint msgqid)
  { if (msgctl(msgqid, IPC_RMID, 0) == -1)
      { setErrnum(env, obj, errno);
        return(-1);
      }
    return 0;
  } // msgRmid
  
/*
 * Class:     LinuxIPC
 * Method:    semget
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_semget (JNIEnv *env, jobject obj, jint key, jint n_sems, jint semflg)
  { jint retval = semget(key, n_sems, semflg);
    if (retval == -1)
      setErrnum(env, obj, errno);
    return retval;
  } // semget

/*
 * Class:     LinuxIPC
 * Method:    semop
 * Signature: (I[LLinuxIPC/Sembuf;I)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_semop (JNIEnv *env, jobject obj, jint semid, jobjectArray sops, jint nsops)
  { struct sembuf * sb = malloc(sizeof(struct sembuf) * nsops);
    jint retval;
    jclass sembuf_cls = (*env)->FindClass(env, "LinuxIPC$Sembuf");
    jfieldID sem_num_fid = (*env)->GetFieldID(env, sembuf_cls, "sem_num",  "S");
    jfieldID sem_op_fid = (*env)->GetFieldID(env, sembuf_cls, "sem_op",  "S");
    jfieldID sem_flg_fid = (*env)->GetFieldID(env, sembuf_cls, "sem_flg",  "S");
    int k;
    
    for (k = 0; k < nsops; k++)
      { jobject op = (*env)->GetObjectArrayElement(env, sops, k);
        sb[k].sem_num = (*env)->GetShortField(env, op, sem_num_fid);
        sb[k].sem_op = (*env)->GetShortField(env, op, sem_op_fid);
        sb[k].sem_flg = (*env)->GetShortField(env, op, sem_flg_fid);
      }

    retval = semop(semid, sb, nsops);
    if (retval != 0)
      setErrnum(env, obj, errno);
    free(sb);
    return retval;
  } // semop

/*
 * Class:     LinuxIPC
 * Method:    semtimedop
 * Signature: (I[LLinuxIPC/Sembuf;IJJ)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_semtimedop (JNIEnv *env, jobject obj, jint semid, jobjectArray sops, jint nsops, jlong timeout_s, jlong timeout_ns)
  { struct sembuf * sb = malloc(sizeof(struct sembuf) * nsops);
    jint retval;
    jclass sembuf_cls = (*env)->FindClass(env, "LinuxIPC$Sembuf");
    jfieldID sem_num_fid = (*env)->GetFieldID(env, sembuf_cls, "sem_num",  "S");
    jfieldID sem_op_fid = (*env)->GetFieldID(env, sembuf_cls, "sem_op",  "S");
    jfieldID sem_flg_fid = (*env)->GetFieldID(env, sembuf_cls, "sem_flg",  "S");
    int k;
    
    for (k = 0; k < nsops; k++)
      { jobject op = (*env)->GetObjectArrayElement(env, sops, k);
        sb[k].sem_num = (*env)->GetShortField(env, op, sem_num_fid);
        sb[k].sem_op = (*env)->GetShortField(env, op, sem_op_fid);
        sb[k].sem_flg = (*env)->GetShortField(env, op, sem_flg_fid);
      }
  
    struct timespec timeout;
    timeout.tv_sec = timeout_s;
    timeout.tv_nsec = timeout_ns;
    retval = semtimedop(semid, sb, nsops, &timeout);
    if (retval != 0)
      setErrnum(env, obj, errno);
    free(sb);
    return retval;
  } // semtimedop

/*
 * Class:     LinuxIPC
 * Method:    semRmid
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_semRmid (JNIEnv * env, jobject obj, jint semid)
  { if (semctl(semid, 0, IPC_RMID, 0) == -1)
      { setErrnum(env, obj, errno);
        return(-1);
      }
    return 0;
  } // semRmid

/*
 * Class:     LinuxIPC
 * Method:    semGetVal
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_semGetVal (JNIEnv *env, jobject obj, jint semid, jint semNum)
  { jint retval = semctl(semid, semNum, GETVAL, 0);
    if (retval == -1)
      { setErrnum(env, obj, errno);
        return(-1);
      }
    return retval;
  } // semGetVal
  
/*
 * Class:     LinuxIPC
 * Method:    semSetVal
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_semSetVal (JNIEnv *env, jobject obj, jint semid, jint semNum, jint val)
  { union semun semopts;    
    semopts.val = val;
    if (semctl(semid, semNum, SETVAL, semopts) == -1)
      { setErrnum(env, obj, errno);
        return(-1);
      }
    return 0;
  } // setSemVal

/*
 * Class:     LinuxIPC
 * Method:    semGetNCnt
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_semGetNCnt (JNIEnv *env, jobject obj, jint semid, jint semNum)
  { jint retval = semctl(semid, semNum, GETNCNT, 0);
    if (retval == -1)
      { setErrnum(env, obj, errno);
        return(-1);
      }
    return retval;
  } // semGetNCnt
  
  
/*
 * Class:     LinuxIPC
 * Method:    shmget
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_shmget (JNIEnv *env, jobject obj, jint key, jint size, jint shmflg)
  { jint retval = shmget(key, size, shmflg);
    if (retval == -1)
      setErrnum(env, obj, errno);
    return retval;
  } // shmget

/*
 * Class:     LinuxIPC
 * Method:    shmat
 * Signature: (III)J
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_shmat (JNIEnv *env, jobject obj, jint shmid, jint shmaddr, jint shmflg)
  { int retval = shmat(shmid, (void *)shmaddr, shmflg);
    if (retval == -1)
      setErrnum(env, obj, errno);
    return retval;
  } // shmat

/*
 * Class:     LinuxIPC
 * Method:    shmdt
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_shmdt (JNIEnv *env, jobject obj, jint shmaddr)
  { int retval = shmdt((void *)shmaddr);
    if (retval == -1)
      setErrnum(env, obj, errno);
    return retval;
  } // shmdt

/*
 * Class:     LinuxIPC
 * Method:    shmWrite
 * Signature: (I[BII)V
 */
JNIEXPORT void JNICALL Java_LinuxIPC_shmWrite (JNIEnv *env, jobject obj, jint shmaddr, jbyteArray data, jint offset, jint nbytes)
  { jbyte* bytes = (*env)->GetByteArrayElements(env, data, 0);
    memcpy((void *)shmaddr, bytes+offset, nbytes);
    (*env)->ReleaseByteArrayElements(env, data, bytes, 0);
  } // shmWrite

/*
 * Class:     LinuxIPC
 * Method:    shmRead
 * Signature: (I[BII)V
 */
JNIEXPORT void JNICALL Java_LinuxIPC_shmRead (JNIEnv *env, jobject obj, jint shmaddr, jbyteArray data, jint offset, jint nbytes)
  { (*env)->SetByteArrayRegion(env, data, offset, nbytes, (void *)shmaddr);
  } // shmRead

/*
 * Class:     LinuxIPC
 * Method:    shmRmid
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_LinuxIPC_shmRmid (JNIEnv *env, jobject obj, jint shmid)
  { if (shmctl(shmid, IPC_RMID, 0) == -1)
      { setErrnum(env, obj, errno);
        return(-1);
      }
    return 0;
  } // shmRmid


/* Function to set the error code in the Java program.  Calls setErrnum in
 * the LinuxIPC class.
 */
void setErrnum (JNIEnv *env, jobject obj, int errnum)
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
  
/* To compile:
   gcc -o libLinuxIPC.so -shared -I/usr/lib/jvm/java-6-sun/include -I/usr/lib/jvm/java-6-sun/include/linux LinuxIPC.c
*/
