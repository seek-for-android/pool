/*
 * Copyright 2010 Manuel Eberl <manueleberl@gmx.de> for Giesecke & Devrient
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

/*****************************************************************
/
/ File   :   bt_pcsc_config.h
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 4, 2010
/ Purpose:   Provides functions to access the BTPCSC config files.
/
******************************************************************/

#ifndef _BTPCSC_CONFIG_H_
#define _BTPCSC_CONFIG_H_

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
