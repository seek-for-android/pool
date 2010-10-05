/*****************************************************************
/
/ File   :   btpcsp_setup.c
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 5, 2010
/ Purpose:   A configuration tool for BTPCSC
/
******************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../pcsc_bluetooth.h"
#include "../devices.h"
#include "../btpcsc_config.h"

int arg_index, mode = -1;
char *config_file = "/etc/btpcsc.conf", *pcsc_config_file = "/etc/reader.conf.d/libbtpcsc", *addr = NULL;

void check_mode_not_set() {
    if (mode != -1) {
        printf("You may only specify one action. Call btpcsc_setup -h for a list of valid options.\n");
        exit(-1);
    }
}

void process_argument(int argc, char *argv[]) {
    
    if (strcmp(argv[arg_index], "-c") == 0) {
        pcsc_config_file = argv[++arg_index];

    } else if (strcmp(argv[arg_index], "-h") == 0) {
        print_help();
        mode = 0;

    } else if (strcmp(argv[arg_index], "-l") == 0) {
        check_mode_not_set();
        mode = 1;

    } else if (strcmp(argv[arg_index], "-a") == 0) {
        check_mode_not_set();
        mode = 2;

    } else if (strcmp(argv[arg_index], "-r") == 0) {
        check_mode_not_set();
        mode = 3;

    } else if (strcmp(argv[arg_index], "-u") == 0) {
        check_mode_not_set();
        mode = 4;

    } else if (strcmp(argv[arg_index], "-s") == 0) {
        check_mode_not_set();
        mode = 5;

    } else if (strcmp(argv[arg_index], "-addr") == 0) {
        if (addr)
            printf("Only one remote address may be specified.\n");
        addr = argv[++arg_index];

    } else {
        printf("Invalid option '%s' Call btpcsc_setup -h for a list of valid options.\n", argv[arg_index]);
        exit(-1);
    }
    
}


int list_devices() {

    if (first_reader) {

        printf(" Name                                ID      Address             Slot\n");
        virtual_reader *reader;
        for (reader = first_reader; reader; reader = reader->next) {
            printf(" %-36s%-8d%-20s%s\n", reader->name, reader->id, reader->address, reader->slot);
        }

    } else {
        printf("No readers configured.\n");
    }

}

int print_help() {
    // TODO
}

virtual_reader *find_reader(char *address, char *slot) {
    
    virtual_reader *lastreader = NULL, *reader;
    for (reader = first_reader; reader; lastreader = reader, reader = reader->next) {
        if (strcmp(address, reader->address) == 0 && strcmp(slot, reader->slot) == 0)
            return reader;
    }

    reader = malloc(sizeof(virtual_reader));
    if (lastreader)
        lastreader->next = reader;
    else
        first_reader = reader;

    return reader;

}

char check_id(virtual_reader *reader, int id) {
    virtual_reader *_reader;
    for (_reader = first_reader; _reader; _reader = _reader->next) {
        if (_reader != reader && _reader->id == id) return 0;
    }
    return 1;
}

int get_free_id(virtual_reader *reader, int first_id) {
    int id;
    for (id = first_id; !check_id(reader, id); id++);
    return id;
}

int add_device() {

    int slotid = -1;
    if (strlen(addr) > 18 && addr[17] == ':') {
        int result = sscanf(addr+18, "%d", &slotid);
        if (result == 1) {
            addr[17] = 0;
        } else {
            slotid = -1;
        }
    }

    if (!is_valid_address(addr)) {
        printf("%s is not a valid address.\n", addr);
        return -1;
    }

    bt_pcsc_connection connection;
    memcpy(&connection.remote_addr, addr, 18);

    int result = bt_connect(&connection);
    if (result < 0) {
        printf("Could not connect to device %s.\n", addr);
        return result;
    }

    char *slots[255];
    int nslots = bt_get_slots(&connection, slots, 255);
    if (nslots < 0) {
        printf("Could not get slot list from device %s.\n", addr);
        return result;
    }

    if (nslots == 0)
        printf("No available slots in device %s.", addr);

    char name[255];
    get_device_name(addr, 255, name);
    int last_id = 1;

    int i, readers_added = 0;
    for (i = 0; i < nslots; i++) {

        if (slotid != -1 && slotid != i) continue;

        virtual_reader *reader = find_reader(addr, slots[i]);
        snprintf(reader->name, 255, "%s:%s", name, slots[i]);
        memcpy(reader->address, addr, 18);
        int length = strlen(slots[i]);
        if (length > 255) length = 255;
        memcpy(reader->slot, slots[i], length + 1);
        reader->id = get_free_id(reader, last_id);
        last_id = reader->id + 1;

        printf("Added reader %s:%s\n", name, slots[i]);
        readers_added++;
        free(slots[i]);

    }

    if (readers_added == 0 && slotid != -1) {
        printf("The specified device has no slot %d.\n", slotid);
    }

    bt_disconnect(&connection);
}

int remove_device() {

    


}

int update_config() {
    int result = write_config(config_file);
    if (result < 0)
        return result;
    printf("Wrote config file %s\n", config_file);
   return 0;
}

int update_pcsc_config() {
    int result = write_pcsc_config(pcsc_config_file);
    if (result < 0)
        return result;
    printf("Wrote PCSC config file %s\n", pcsc_config_file);
    return 0;
    
}

int scan_device() {

    if (!is_valid_address(addr)) {
        printf("%s is not a valid address.\n", addr);
        return -1;
    }

    bt_pcsc_connection connection;
    memcpy(&connection.remote_addr, addr, 18);

    int result = bt_connect(&connection);
    if (result < 0) {
        printf("Could not connect to device %s.\n", addr);
        return result;
    }

    char *slots[255];
    int nslots = bt_get_slots(&connection, slots, 255);
    if (nslots < 0) {
        printf("Could not get slot list from device %s.\n", addr);
        return result;
    }

    if (nslots == 0)
        printf("No available slots in device %s.", addr);
    else
        printf(" Name                Address             Slot ID   Slot name\n");

    char name[255];
    get_device_name(addr, 255, name);

    int i;
    for (i = 0; i < nslots; i++) {
        printf(" %-19s %-19s %-9d %s\n", name, addr, i, slots[i]);
        free(slots[i]);
    }

    bt_disconnect(&connection);

}

int scan_for_devices() {

    bt_device devices[255];

    printf("Performing 10.24 s scan for BTPCSC devices...\n");
    uint8_t uuid[16] = BT_PCSC_UUID;
    int ndevices = scan_for_bt_devices_with_service(devices, 0, 0, 255, 10.24f, 1, uuid);

    if (ndevices < 0) return ndevices;

    char printedheader = 0;

    if (ndevices > 0) {
        bt_pcsc_connection connection;
        char *slots[255];

        bt_device *device = devices;
        int i;
        for (i = 0; i < ndevices; i++, device++) {
            memcpy(&connection.remote_addr, device->addr, 18);
            int result = bt_connect(&connection);
            if (result < 0) continue;

            int nslots = bt_get_slots(&connection, slots, 255);
            if (nslots < 0) continue;

            int j;
            for (j = 0; j < ndevices; j++) {
                if (!printedheader) {
                    printf(" ID   Name                Address             Slot ID   Slot name\n");
                    printedheader = 1;
                }
                printf(" %-4d %-19s %-19s %-9d %s\n", i, device->name, device->addr, j, slots[j]);
                free(slots[j]);
            }

            bt_disconnect(&connection);
        }
    }

    return 0;

}

int main(int argc, char *argv[]) {
    
    for (arg_index = 1; arg_index < argc; arg_index++) {
        process_argument(argc, argv);
    }

    int return_value = 0;

    if (mode != 0) {
        return_value = parse_config(config_file);
        if (return_value < 0)
            exit(-1);
    }

    switch (mode) {
    case 0:
        break;

    case -1:
    case 1:
        return_value = list_devices();
        break;

    case 2:
        return_value = add_device();
        if (return_value >= 0)
            return_value = update_config();
        break;

    case 3:
        return_value = remove_device();
        if (return_value >= 0)
            return_value = update_config();
        break;

    case 4:
        return_value = update_pcsc_config();
        break;

    case 5:
        if (addr)
            return_value = scan_device();
        else
            return_value = scan_for_devices();
        break;

    }

    free_readers();

    return return_value;

}
