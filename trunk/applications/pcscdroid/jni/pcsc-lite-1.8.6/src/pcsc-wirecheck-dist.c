#include <sys/types.h>
#include <time.h>
#include <stddef.h>

#include "PCSC/pcsclite.h"
#include "pcscd.h"
#include "readerfactory.h"
#include "eventhandler.h"
#include "winscard_msg.h"

#include "lassert.h"

int pcsc_assert_wire_constants(void);
int pcsc_assert_wire_constants(void)
{

        /* Constants... */

        LASSERTF(PROTOCOL_VERSION_MAJOR == 4," found %lld\n",
                 (long long)PROTOCOL_VERSION_MAJOR);
        LASSERTF(PROTOCOL_VERSION_MINOR == 2," found %lld\n",
                 (long long)PROTOCOL_VERSION_MINOR);

        LASSERTF(MAX_READERNAME == 100," found %lld\n",
                 (long long)MAX_READERNAME);
        LASSERTF(MAX_ATR_SIZE == 33," found %lld\n",
                 (long long)MAX_ATR_SIZE);
        LASSERTF(MAX_BUFFER_SIZE == 264," found %lld\n",
                 (long long)MAX_BUFFER_SIZE);

        /* enum pcsc_msg_commands */
        LASSERTF(SCARD_ESTABLISH_CONTEXT == 1, " found %lld\n",
                 (long long)SCARD_ESTABLISH_CONTEXT);
        LASSERTF(SCARD_RELEASE_CONTEXT == 2, " found %lld\n",
                 (long long)SCARD_RELEASE_CONTEXT);
        LASSERTF(SCARD_LIST_READERS == 3, " found %lld\n",
                 (long long)SCARD_LIST_READERS);
        LASSERTF(SCARD_CONNECT == 4, " found %lld\n",
                 (long long)SCARD_CONNECT);
        LASSERTF(SCARD_RECONNECT == 5, " found %lld\n",
                 (long long)SCARD_RECONNECT);
        LASSERTF(SCARD_DISCONNECT == 6, " found %lld\n",
                 (long long)SCARD_DISCONNECT);
        LASSERTF(SCARD_BEGIN_TRANSACTION == 7, " found %lld\n",
                 (long long)SCARD_BEGIN_TRANSACTION);
        LASSERTF(SCARD_END_TRANSACTION == 8, " found %lld\n",
                 (long long)SCARD_END_TRANSACTION);
        LASSERTF(SCARD_TRANSMIT == 9, " found %lld\n",
                 (long long)SCARD_TRANSMIT);
        LASSERTF(SCARD_CONTROL == 10, " found %lld\n",
                 (long long)SCARD_CONTROL);
        LASSERTF(SCARD_STATUS == 11, " found %lld\n",
                 (long long)SCARD_STATUS);
        LASSERTF(SCARD_GET_STATUS_CHANGE == 12, " found %lld\n",
                 (long long)SCARD_GET_STATUS_CHANGE);
        LASSERTF(SCARD_CANCEL == 13, " found %lld\n",
                 (long long)SCARD_CANCEL);
        LASSERTF(SCARD_CANCEL_TRANSACTION == 14, " found %lld\n",
                 (long long)SCARD_CANCEL_TRANSACTION);
        LASSERTF(SCARD_GET_ATTRIB == 15, " found %lld\n",
                 (long long)SCARD_GET_ATTRIB);
        LASSERTF(SCARD_SET_ATTRIB == 16, " found %lld\n",
                 (long long)SCARD_SET_ATTRIB);
        LASSERTF(CMD_VERSION == 17, " found %lld\n",
                 (long long)CMD_VERSION);
        LASSERTF(CMD_GET_READERS_STATE == 18, " found %lld\n",
                 (long long)CMD_GET_READERS_STATE);
        LASSERTF(CMD_WAIT_READER_STATE_CHANGE == 19, " found %lld\n",
                 (long long)CMD_WAIT_READER_STATE_CHANGE);
        LASSERTF(CMD_STOP_WAITING_READER_STATE_CHANGE == 20, " found %lld\n",
                 (long long)CMD_STOP_WAITING_READER_STATE_CHANGE);
        /* Types... */

        /* Checks for struct version_struct */
        LASSERTF((int)sizeof(struct version_struct) == 12, " found %lld\n",
                 (long long)(int)sizeof(struct version_struct));
        LASSERTF((int)offsetof(struct version_struct, major) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct version_struct, major));
        LASSERTF((int)sizeof(((struct version_struct *)0)->major) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct version_struct *)0)->major));
        LASSERTF((int)offsetof(struct version_struct, minor) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct version_struct, minor));
        LASSERTF((int)sizeof(((struct version_struct *)0)->minor) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct version_struct *)0)->minor));
        LASSERTF((int)offsetof(struct version_struct, rv) == 8, " found %lld\n",
                 (long long)(int)offsetof(struct version_struct, rv));
        LASSERTF((int)sizeof(((struct version_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct version_struct *)0)->rv));

        /* Checks for struct client_struct */
        LASSERTF((int)sizeof(struct client_struct) == 4, " found %lld\n",
                 (long long)(int)sizeof(struct client_struct));
        LASSERTF((int)offsetof(struct client_struct, hContext) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct client_struct, hContext));
        LASSERTF((int)sizeof(((struct client_struct *)0)->hContext) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct client_struct *)0)->hContext));

        /* Checks for struct establish_struct */
        LASSERTF((int)sizeof(struct establish_struct) == 12, " found %lld\n",
                 (long long)(int)sizeof(struct establish_struct));
        LASSERTF((int)offsetof(struct establish_struct, dwScope) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct establish_struct, dwScope));
        LASSERTF((int)sizeof(((struct establish_struct *)0)->dwScope) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct establish_struct *)0)->dwScope));
        LASSERTF((int)offsetof(struct establish_struct, hContext) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct establish_struct, hContext));
        LASSERTF((int)sizeof(((struct establish_struct *)0)->hContext) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct establish_struct *)0)->hContext));
        LASSERTF((int)offsetof(struct establish_struct, rv) == 8, " found %lld\n",
                 (long long)(int)offsetof(struct establish_struct, rv));
        LASSERTF((int)sizeof(((struct establish_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct establish_struct *)0)->rv));

        /* Checks for struct release_struct */
        LASSERTF((int)sizeof(struct release_struct) == 8, " found %lld\n",
                 (long long)(int)sizeof(struct release_struct));
        LASSERTF((int)offsetof(struct release_struct, hContext) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct release_struct, hContext));
        LASSERTF((int)sizeof(((struct release_struct *)0)->hContext) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct release_struct *)0)->hContext));
        LASSERTF((int)offsetof(struct release_struct, rv) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct release_struct, rv));
        LASSERTF((int)sizeof(((struct release_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct release_struct *)0)->rv));

        /* Checks for struct connect_struct */
        LASSERTF((int)sizeof(struct connect_struct) == 124, " found %lld\n",
                 (long long)(int)sizeof(struct connect_struct));
        LASSERTF((int)offsetof(struct connect_struct, hContext) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct connect_struct, hContext));
        LASSERTF((int)sizeof(((struct connect_struct *)0)->hContext) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct connect_struct *)0)->hContext));
        LASSERTF((int)offsetof(struct connect_struct, szReader) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct connect_struct, szReader));
        LASSERTF((int)sizeof(((struct connect_struct *)0)->szReader) == 100, " found %lld\n",
                 (long long)(int)sizeof(((struct connect_struct *)0)->szReader));
        LASSERTF((int)offsetof(struct connect_struct, dwShareMode) == 104, " found %lld\n",
                 (long long)(int)offsetof(struct connect_struct, dwShareMode));
        LASSERTF((int)sizeof(((struct connect_struct *)0)->dwShareMode) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct connect_struct *)0)->dwShareMode));
        LASSERTF((int)offsetof(struct connect_struct, dwPreferredProtocols) == 108, " found %lld\n",
                 (long long)(int)offsetof(struct connect_struct, dwPreferredProtocols));
        LASSERTF((int)sizeof(((struct connect_struct *)0)->dwPreferredProtocols) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct connect_struct *)0)->dwPreferredProtocols));
        LASSERTF((int)offsetof(struct connect_struct, hCard) == 112, " found %lld\n",
                 (long long)(int)offsetof(struct connect_struct, hCard));
        LASSERTF((int)sizeof(((struct connect_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct connect_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct connect_struct, dwActiveProtocol) == 116, " found %lld\n",
                 (long long)(int)offsetof(struct connect_struct, dwActiveProtocol));
        LASSERTF((int)sizeof(((struct connect_struct *)0)->dwActiveProtocol) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct connect_struct *)0)->dwActiveProtocol));
        LASSERTF((int)offsetof(struct connect_struct, rv) == 120, " found %lld\n",
                 (long long)(int)offsetof(struct connect_struct, rv));
        LASSERTF((int)sizeof(((struct connect_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct connect_struct *)0)->rv));

        /* Checks for struct reconnect_struct */
        LASSERTF((int)sizeof(struct reconnect_struct) == 24, " found %lld\n",
                 (long long)(int)sizeof(struct reconnect_struct));
        LASSERTF((int)offsetof(struct reconnect_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct reconnect_struct, hCard));
        LASSERTF((int)sizeof(((struct reconnect_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct reconnect_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct reconnect_struct, dwShareMode) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct reconnect_struct, dwShareMode));
        LASSERTF((int)sizeof(((struct reconnect_struct *)0)->dwShareMode) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct reconnect_struct *)0)->dwShareMode));
        LASSERTF((int)offsetof(struct reconnect_struct, dwPreferredProtocols) == 8, " found %lld\n",
                 (long long)(int)offsetof(struct reconnect_struct, dwPreferredProtocols));
        LASSERTF((int)sizeof(((struct reconnect_struct *)0)->dwPreferredProtocols) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct reconnect_struct *)0)->dwPreferredProtocols));
        LASSERTF((int)offsetof(struct reconnect_struct, dwInitialization) == 12, " found %lld\n",
                 (long long)(int)offsetof(struct reconnect_struct, dwInitialization));
        LASSERTF((int)sizeof(((struct reconnect_struct *)0)->dwInitialization) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct reconnect_struct *)0)->dwInitialization));
        LASSERTF((int)offsetof(struct reconnect_struct, dwActiveProtocol) == 16, " found %lld\n",
                 (long long)(int)offsetof(struct reconnect_struct, dwActiveProtocol));
        LASSERTF((int)sizeof(((struct reconnect_struct *)0)->dwActiveProtocol) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct reconnect_struct *)0)->dwActiveProtocol));
        LASSERTF((int)offsetof(struct reconnect_struct, rv) == 20, " found %lld\n",
                 (long long)(int)offsetof(struct reconnect_struct, rv));
        LASSERTF((int)sizeof(((struct reconnect_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct reconnect_struct *)0)->rv));

        /* Checks for struct disconnect_struct */
        LASSERTF((int)sizeof(struct disconnect_struct) == 12, " found %lld\n",
                 (long long)(int)sizeof(struct disconnect_struct));
        LASSERTF((int)offsetof(struct disconnect_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct disconnect_struct, hCard));
        LASSERTF((int)sizeof(((struct disconnect_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct disconnect_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct disconnect_struct, dwDisposition) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct disconnect_struct, dwDisposition));
        LASSERTF((int)sizeof(((struct disconnect_struct *)0)->dwDisposition) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct disconnect_struct *)0)->dwDisposition));
        LASSERTF((int)offsetof(struct disconnect_struct, rv) == 8, " found %lld\n",
                 (long long)(int)offsetof(struct disconnect_struct, rv));
        LASSERTF((int)sizeof(((struct disconnect_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct disconnect_struct *)0)->rv));

        /* Checks for struct begin_struct */
        LASSERTF((int)sizeof(struct begin_struct) == 8, " found %lld\n",
                 (long long)(int)sizeof(struct begin_struct));
        LASSERTF((int)offsetof(struct begin_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct begin_struct, hCard));
        LASSERTF((int)sizeof(((struct begin_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct begin_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct begin_struct, rv) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct begin_struct, rv));
        LASSERTF((int)sizeof(((struct begin_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct begin_struct *)0)->rv));

        /* Checks for struct end_struct */
        LASSERTF((int)sizeof(struct end_struct) == 12, " found %lld\n",
                 (long long)(int)sizeof(struct end_struct));
        LASSERTF((int)offsetof(struct end_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct end_struct, hCard));
        LASSERTF((int)sizeof(((struct end_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct end_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct end_struct, dwDisposition) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct end_struct, dwDisposition));
        LASSERTF((int)sizeof(((struct end_struct *)0)->dwDisposition) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct end_struct *)0)->dwDisposition));
        LASSERTF((int)offsetof(struct end_struct, rv) == 8, " found %lld\n",
                 (long long)(int)offsetof(struct end_struct, rv));
        LASSERTF((int)sizeof(((struct end_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct end_struct *)0)->rv));

        /* Checks for struct cancel_struct */
        LASSERTF((int)sizeof(struct cancel_struct) == 8, " found %lld\n",
                 (long long)(int)sizeof(struct cancel_struct));
        LASSERTF((int)offsetof(struct cancel_struct, hContext) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct cancel_struct, hContext));
        LASSERTF((int)sizeof(((struct cancel_struct *)0)->hContext) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct cancel_struct *)0)->hContext));
        LASSERTF((int)offsetof(struct cancel_struct, rv) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct cancel_struct, rv));
        LASSERTF((int)sizeof(((struct cancel_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct cancel_struct *)0)->rv));

        /* Checks for struct status_struct */
        LASSERTF((int)sizeof(struct status_struct) == 8, " found %lld\n",
                 (long long)(int)sizeof(struct status_struct));
        LASSERTF((int)offsetof(struct status_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct status_struct, hCard));
        LASSERTF((int)sizeof(((struct status_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct status_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct status_struct, rv) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct status_struct, rv));
        LASSERTF((int)sizeof(((struct status_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct status_struct *)0)->rv));

        /* Checks for struct transmit_struct */
        LASSERTF((int)sizeof(struct transmit_struct) == 32, " found %lld\n",
                 (long long)(int)sizeof(struct transmit_struct));
        LASSERTF((int)offsetof(struct transmit_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, hCard));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct transmit_struct, ioSendPciProtocol) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, ioSendPciProtocol));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->ioSendPciProtocol) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->ioSendPciProtocol));
        LASSERTF((int)offsetof(struct transmit_struct, ioSendPciLength) == 8, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, ioSendPciLength));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->ioSendPciLength) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->ioSendPciLength));
        LASSERTF((int)offsetof(struct transmit_struct, cbSendLength) == 12, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, cbSendLength));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->cbSendLength) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->cbSendLength));
        LASSERTF((int)offsetof(struct transmit_struct, ioRecvPciProtocol) == 16, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, ioRecvPciProtocol));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->ioRecvPciProtocol) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->ioRecvPciProtocol));
        LASSERTF((int)offsetof(struct transmit_struct, ioRecvPciLength) == 20, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, ioRecvPciLength));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->ioRecvPciLength) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->ioRecvPciLength));
        LASSERTF((int)offsetof(struct transmit_struct, pcbRecvLength) == 24, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, pcbRecvLength));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->pcbRecvLength) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->pcbRecvLength));
        LASSERTF((int)offsetof(struct transmit_struct, rv) == 28, " found %lld\n",
                 (long long)(int)offsetof(struct transmit_struct, rv));
        LASSERTF((int)sizeof(((struct transmit_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct transmit_struct *)0)->rv));

        /* Checks for struct control_struct */
        LASSERTF((int)sizeof(struct control_struct) == 24, " found %lld\n",
                 (long long)(int)sizeof(struct control_struct));
        LASSERTF((int)offsetof(struct control_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct control_struct, hCard));
        LASSERTF((int)sizeof(((struct control_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct control_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct control_struct, dwControlCode) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct control_struct, dwControlCode));
        LASSERTF((int)sizeof(((struct control_struct *)0)->dwControlCode) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct control_struct *)0)->dwControlCode));
        LASSERTF((int)offsetof(struct control_struct, cbSendLength) == 8, " found %lld\n",
                 (long long)(int)offsetof(struct control_struct, cbSendLength));
        LASSERTF((int)sizeof(((struct control_struct *)0)->cbSendLength) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct control_struct *)0)->cbSendLength));
        LASSERTF((int)offsetof(struct control_struct, cbRecvLength) == 12, " found %lld\n",
                 (long long)(int)offsetof(struct control_struct, cbRecvLength));
        LASSERTF((int)sizeof(((struct control_struct *)0)->cbRecvLength) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct control_struct *)0)->cbRecvLength));
        LASSERTF((int)offsetof(struct control_struct, dwBytesReturned) == 16, " found %lld\n",
                 (long long)(int)offsetof(struct control_struct, dwBytesReturned));
        LASSERTF((int)sizeof(((struct control_struct *)0)->dwBytesReturned) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct control_struct *)0)->dwBytesReturned));
        LASSERTF((int)offsetof(struct control_struct, rv) == 20, " found %lld\n",
                 (long long)(int)offsetof(struct control_struct, rv));
        LASSERTF((int)sizeof(((struct control_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct control_struct *)0)->rv));

        /* Checks for struct getset_struct */
        LASSERTF((int)sizeof(struct getset_struct) == 280, " found %lld\n",
                 (long long)(int)sizeof(struct getset_struct));
        LASSERTF((int)offsetof(struct getset_struct, hCard) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct getset_struct, hCard));
        LASSERTF((int)sizeof(((struct getset_struct *)0)->hCard) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct getset_struct *)0)->hCard));
        LASSERTF((int)offsetof(struct getset_struct, dwAttrId) == 4, " found %lld\n",
                 (long long)(int)offsetof(struct getset_struct, dwAttrId));
        LASSERTF((int)sizeof(((struct getset_struct *)0)->dwAttrId) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct getset_struct *)0)->dwAttrId));
        LASSERTF((int)offsetof(struct getset_struct, cbAttrLen) == 272, " found %lld\n",
                 (long long)(int)offsetof(struct getset_struct, cbAttrLen));
        LASSERTF((int)sizeof(((struct getset_struct *)0)->cbAttrLen) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct getset_struct *)0)->cbAttrLen));
        LASSERTF((int)offsetof(struct getset_struct, rv) == 276, " found %lld\n",
                 (long long)(int)offsetof(struct getset_struct, rv));
        LASSERTF((int)sizeof(((struct getset_struct *)0)->rv) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct getset_struct *)0)->rv));

        /* Checks for struct pubReaderStatesList */
        LASSERTF((int)sizeof(struct pubReaderStatesList) == 156, " found %lld\n",
                 (long long)(int)sizeof(struct pubReaderStatesList));
        LASSERTF((int)offsetof(struct pubReaderStatesList, readerName) == 0, " found %lld\n",
                 (long long)(int)offsetof(struct pubReaderStatesList, readerName));
        LASSERTF((int)sizeof(((struct pubReaderStatesList *)0)->readerName) == 100, " found %lld\n",
                 (long long)(int)sizeof(((struct pubReaderStatesList *)0)->readerName));
        LASSERTF((int)offsetof(struct pubReaderStatesList, readerState) == 104, " found %lld\n",
                 (long long)(int)offsetof(struct pubReaderStatesList, readerState));
        LASSERTF((int)sizeof(((struct pubReaderStatesList *)0)->readerState) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct pubReaderStatesList *)0)->readerState));
        LASSERTF((int)offsetof(struct pubReaderStatesList, readerSharing) == 108, " found %lld\n",
                 (long long)(int)offsetof(struct pubReaderStatesList, readerSharing));
        LASSERTF((int)sizeof(((struct pubReaderStatesList *)0)->readerSharing) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct pubReaderStatesList *)0)->readerSharing));
        LASSERTF((int)offsetof(struct pubReaderStatesList, cardAtr) == 112, " found %lld\n",
                 (long long)(int)offsetof(struct pubReaderStatesList, cardAtr));
        LASSERTF((int)sizeof(((struct pubReaderStatesList *)0)->cardAtr) == 33, " found %lld\n",
                 (long long)(int)sizeof(((struct pubReaderStatesList *)0)->cardAtr));
        LASSERTF((int)offsetof(struct pubReaderStatesList, cardAtrLength) == 148, " found %lld\n",
                 (long long)(int)offsetof(struct pubReaderStatesList, cardAtrLength));
        LASSERTF((int)sizeof(((struct pubReaderStatesList *)0)->cardAtrLength) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct pubReaderStatesList *)0)->cardAtrLength));
        LASSERTF((int)offsetof(struct pubReaderStatesList, cardProtocol) == 152, " found %lld\n",
                 (long long)(int)offsetof(struct pubReaderStatesList, cardProtocol));
        LASSERTF((int)sizeof(((struct pubReaderStatesList *)0)->cardProtocol) == 4, " found %lld\n",
                 (long long)(int)sizeof(((struct pubReaderStatesList *)0)->cardProtocol));

return 0;
}
