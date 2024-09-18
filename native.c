#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ipc.h>
#include <sys/msg.h>

// Define the maximum length of the message
#define MAX_TEXT 512

// Define a structure for the message queue
struct message {
    long msg_type;           // Message type, must be > 0
    char msg_text[MAX_TEXT]; // Message data
};

int main() {
    key_t key;
    int msgid;
    struct message msg;

    // Generate a unique key for the message queue
    key = ftok("progfile", 65); // "progfile" is a filename used to generate a unique key

    // Create a message queue and get the identifier
    msgid = msgget(key, 0666 | IPC_CREAT);
    if (msgid == -1) {
        perror("msgget");
        exit(EXIT_FAILURE);
    }

    // Set up the message to be sent
    msg.msg_type = 1; // Message type (any positive number)
    strcpy(msg.msg_text, "Hello, this is a message from System V IPC!");

    // Send the message to the queue
    if (msgsnd(msgid, &msg, sizeof(msg), 0) == -1) {
        perror("msgsnd");
        exit(EXIT_FAILURE);
    }

    printf("Message sent: %s\n", msg.msg_text);

    return 0;
}