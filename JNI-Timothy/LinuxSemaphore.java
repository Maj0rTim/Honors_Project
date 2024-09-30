// LinuxSempahore.java

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/** Class to implement semaphores in the same way as java.util.concurrent.Semaphore,
  * but using Linux IPC semaphores.
  * @author George Wells
  * @version 1.0 (2 December 2008)
  */
public class LinuxSemaphore
  { private LinuxIPC ipc = new LinuxIPC(); // Access Linux IPC facilities
    private LinuxIPC.Sembuf[] sembuf = { new LinuxIPC.Sembuf(0, 0, 0) } ; // Used for semop operations
    private int semId; // Semaphore ID
  
    /** Creates a Semaphore with the given number of permits.
      */
    public LinuxSemaphore (int permits)
      { int key = (int)this.hashCode();
        if ((semId = ipc.semget(key, 1, LinuxIPC.IPC_CREAT | LinuxIPC.IPC_EXCL | 0600)) == -1)
          System.err.println("semget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        sembuf[0].sem_op = (short)permits;
        if (ipc.semop(semId, sembuf) == -1)
          System.err.println("Initialisation of semaphore failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // constructor

    /** Creates a Semaphore with the given number of permits - the given fairness setting is ignored.
      */
    public LinuxSemaphore (int permits, boolean fair)
      { this(permits);
      } // constructor

    protected void finalize () 
      { close();
      } // finalize

    /** Acquires a permit from this semaphore, blocking until one is available, or the thread is interrupted.
      */
    public void acquire ()
      { acquire(1);
      } // 
      
    /** Acquires the given number of permits from this semaphore, blocking until all are available, or the thread is interrupted.
      */
    public void acquire (int permits)
      { sembuf[0].sem_op = (short)(-permits);
        sembuf[0].sem_flg = 0;
        if (ipc.semop(semId, sembuf) == -1)
          System.err.println("acquire failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // acquire

    /** Acquires a permit from this semaphore, blocking until one is available.
      */
    public void acquireUninterruptibly ()
      { acquire(1);
      } // acquireUninterruptibly

    /** Acquires the given number of permits from this semaphore, blocking until all are available.
      */
    public void acquireUninterruptibly (int permits)
      { acquire(permits);
      } // acquireUninterruptibly

    /** Returns the current number of permits available in this semaphore.
      */
    public int availablePermits ()
      { return ipc.semGetVal(semId, 0);
      } // availablePermits

    /** Acquires and returns all permits that are immediately available.
      */
    public int drainPermits()
      { int numPermits = availablePermits();
        ipc.semSetVal(semId, 0, 0);
        return numPermits;
      } // drainPermits

    /** Returns a collection containing threads that may be waiting to acquire.
      * @returns null.
      */
    protected Collection<Thread> getQueuedThreads ()
      { return null;
      } // getQueuedThreads

    /** Returns an estimate of the number of threads waiting to acquire.
      */
    public int getQueueLength ()
      { return ipc.semGetNCnt(semId, 0);
      } // getQueueLength

    /** Queries whether any threads are waiting to acquire.
      */
    public boolean hasQueuedThreads ()
      { return getQueueLength() > 0;
      } // hasQueuedThreads

    /** Always returns false in this implementation.
      */
    public boolean isFair ()
      { return false;
      } // isFair

    /** Shrinks the number of available permits by the indicated reduction.
      * Unsupported in this version.
      * @throws UnsupportedOperationException
      */
    protected void reducePermits (int reduction)
      { throw new UnsupportedOperationException("reducePermits unsupported");
      } // reducePermits

    /** Releases a permit, returning it to the semaphore.
      */
    public void release ()
      { release(1);
      } // release

    /** Releases the given number of permits, returning them to the semaphore.
      */
    public void release (int permits)
      { sembuf[0].sem_op = (short)permits;
        sembuf[0].sem_flg = 0;
        if (ipc.semop(semId, sembuf) == -1)
          System.err.println("release failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // release

    /** Returns a string identifying this semaphore, as well as its state.
      */
    public String toString()
      { return "Semaphore " + super.toString() + ":  Permits = " + availablePermits() + " Waiting threads = " + getQueueLength();
      } // toString
      
    /** Acquires a permit from this semaphore, only if one is available at the time of invocation.
      */
    public boolean tryAcquire ()
      { return tryAcquire(1);
      } // tryAcquire

    /** Acquires the given number of permits from this semaphore, only if all are available at the time of invocation.
      */
    public boolean tryAcquire (int permits)
      { sembuf[0].sem_op = (short)(-permits);
        sembuf[0].sem_flg = LinuxIPC.IPC_NOWAIT;
        if (ipc.semop(semId, sembuf) == -1)
          { if (ipc.getErrnum() != LinuxIPC.EAGAIN)
              System.err.println("acquire failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            return false;
          }
        return true;
      } // tryAcquire

    /** Acquires the given number of permits from this semaphore, if all become available within the given waiting time and the current thread has not been interrupted.
      */
    public boolean tryAcquire (int permits, long timeout, TimeUnit unit)
      { sembuf[0].sem_op = (short)(-permits);
        sembuf[0].sem_flg = LinuxIPC.IPC_NOWAIT;
        long seconds = unit.toSeconds(timeout);
        long nanoSeconds = unit.toNanos(timeout);
        
        if (ipc.semtimedop(semId, sembuf, sembuf.length, seconds, nanoSeconds) == -1)
          { if (ipc.getErrnum() != LinuxIPC.EAGAIN)
              System.err.println("tryAcquire failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            return false;
          }
        return true;
      } // tryAcquire

    /** Acquires a permit from this semaphore, if one becomes available within the given waiting time and the current thread has not been interrupted.
      */
    public boolean tryAcquire (long timeout, TimeUnit unit)
      { return tryAcquire(1, timeout, unit);
      } // tryAcquire

    /** Releases all system resources.
      */
    public void close ()
      { if (ipc.semRmid(semId) == -1)
          System.err.println("semRmid failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // close

  } // class LinuxSemaphore
