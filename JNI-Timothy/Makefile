all: libLinuxIPC.so libSharedMemoryStreams.so

libLinuxIPC.so: LinuxIPC.c
	gcc -o libLinuxIPC.so -shared -I/usr/lib/jvm/java-6-sun/include -I/usr/lib/jvm/java-6-sun/include/linux LinuxIPC.c

libSharedMemoryStreams.so: SharedMemoryStreams.c
	gcc -o libSharedMemoryStreams.so -shared -I/usr/lib/jvm/java-6-sun/include -I/usr/lib/jvm/java-6-sun/include/linux SharedMemoryStreams.c
