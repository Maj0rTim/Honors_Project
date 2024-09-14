all: LinuxNIPC.c

LinuxNIPC.so: LinuxNIPC.c
	gcc -o libLinuxNIPC.so -lc -shared -I"/usr/lib/jvm/java-11-openjdk-amd64/include" -I"/usr/lib/jvm/java-11-openjdk-amd64/include/linux" LinuxNIPC.c
	export LD_LIBRARY_PATH=.