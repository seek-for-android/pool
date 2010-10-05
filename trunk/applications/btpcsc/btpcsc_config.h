/*****************************************************************
/
/ File   :   bt_pcsc_config.h
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 4, 2010
/ Purpose:   Provides functions to access the bt_pcsc config files.
/ License:   See file LICENSE
/
******************************************************************/

#ifndef _btpcsc_config_h_
#define _btpcsc_config_h_

#ifdef __cplusplus
extern "C" {
#endif 

typedef struct virtual_reader {
    char name[255];
    char address[18];
    int id;
    char slot[255];
    struct virtual_reader *next;
} virtual_reader;

extern virtual_reader *first_reader, *last_reader;

void free_reader_list(virtual_reader *first_reader);
char is_valid_address(char *value);
int parse_config(char *file_name);
int write_config(char *config_file);
int write_pcsc_config(char *pcsc_config_file);

#endif
