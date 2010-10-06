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
/ File   :   btpcsc_config.c
/ Author :   Manuel Eberl <manueleberl@gmx.de>
/ Date   :   October 4, 2010
/ Purpose:   Provides functions to access the BTPCSC config files.
/
******************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>

#include "btpcsc_config.h"

virtual_reader *first_reader, *last_reader;

void free_readers() {
    free_reader_list(first_reader);
    first_reader = NULL;
}

void free_reader_list(virtual_reader *first_reader) {
    virtual_reader *reader = first_reader, *next;
    while (reader) {
        next = reader->next;
        free(reader);
        reader = next;
    }
}

char *stripquotes(char *value) {
    int length = strlen(value);
    if (value[0] == '"' && value[length - 1] == '"') {
        value[length - 1] = 0;
        return value + 1;
    }
    return value;
}

int value2int(char *value) {
    int result = 0;
    char *cur = value;

    while (*cur) {
        if (*cur < '0' || *cur > '9') {
            return -1;
        }

        result = result * 10 + (*cur - '0');
        cur++;
    }

    return result;
}

int handle_start_section(char *section, int line_number) {
    virtual_reader *reader = malloc(sizeof(virtual_reader));
    memset(reader, 0, sizeof(virtual_reader));

    if (last_reader) {
        last_reader->next = reader;
        last_reader = reader;
    } else {
        last_reader = first_reader = reader;
    }

    return 0;
}

int handle_end_section(char *section, int line_number) {
    return 0;
}

char is_valid_address(char *value) {

    if (!value) return 0;

    int length = strlen(value), i;
    if (length != 17) return 0;

    for (i = 0; i < 17; i++) {
        if (i % 3 == 2) {
            if (value[i] != ':')
                return 0;
        } else {
            if ((value[i] < '0' || value[i] > '9') && (value[i] < 'A' || value[i] > 'F') && (value[i] < 'a' || value[i] > 'f'))
                return 0;
        }
    }

    return 1;

}

int handle_key_value_pair(char *key, char *value, char *section, int line_number) {

    value = stripquotes(value);

    if (last_reader) {

        if (strcmp(key, "name") == 0) {

            if (strlen(value) == 0) {
                printf("Invalid name '%s' in line %d.\n", value, line_number);
            }
            strncpy(last_reader->name, value, 255);


        } else if (strcmp(key, "address") == 0) {

            if (strlen(value) == 0 || !is_valid_address(value)) {
                printf("Invalid address '%s' in line %d.\n", value, line_number);
            }
            strncpy(last_reader->address, value, 18);


        } else if (strcmp(key, "id") == 0) {

            int id = value2int(value);
            if (id > 0) {
                last_reader->id = id;
            } else {
                printf("Invalid ID '%s' in line %d.\n", value, line_number);
                return -1;
            }


        } else if (strcmp(key, "slot") == 0) {

            strncpy(last_reader->slot, value, 255);
        }

    }

}

void strtolower(char *str) {

    char *c = str;
    while (*c) {
        *c = tolower(*c);
        c++;
    }

}

int parse_config(char *file_name) {
    
    FILE *f = fopen(file_name, "r");

    if (!f) {
        f = fopen(file_name, "w+");
        if (!f) {
            fopen(file_name, "r");
            perror("Could not open config file");
            return -1;
        }
    }

    char buffer[32768], tmp[255], *section_name = NULL;
    char is_in_section = 0, reading_section_name = 0;
    char *key = NULL, *value = NULL;
    int line_number = 0;
    last_reader = NULL;

    while (!feof(f)) {

        line_number++;

        void *result = fgets(buffer, sizeof(buffer), f);
        if (!result) {
            if (feof(f)) break;
            perror("Could not read config file");
            close(f);
            return -1;
        }

        char expecting_line_end = 0;
        if (key) free(key);
        if (value) free(value);
        key = NULL;
        value = NULL;

        char *cur = buffer, *end = buffer + strlen(buffer);
        while (cur < end && *cur != '\n' && *cur != '#') {

            // Skip whitespace
            if (*cur == ' ' || *cur == '\t') {
               cur++;
               continue;
            }

            // If we want the end of the line, but there's something else in the line,
            // the line is invalid.
            if (expecting_line_end) {
                printf("Invalid config file: Expected line end after section definition at line %d.\n", line_number);
                return -1;
            }

            // If we're reading a section name, try to read it.
            if (reading_section_name) {
                int name_length;
                for (name_length = 0; cur + name_length < end; name_length++)
                    if (cur[name_length] == ' ' || cur[name_length] == '\t' || cur[name_length] == '\n')
                        break;

                if (name_length < 1) {
                    printf("Invalid config file: Expected section name at line %d.\n", line_number);
                    return -1;
                }

                section_name = malloc(name_length + 1);
                memcpy(section_name, cur, name_length);
                cur += name_length;
                section_name[name_length] = 0;
                reading_section_name = 0;
                expecting_line_end = 1;
                is_in_section = 1;
                handle_start_section(section_name, line_number);
                if (result < 0)
                    return -1;
                continue;
            }

            // See if there's a keyword "Section", if yes, this is a section definition
            if (end > cur + 7) {

                // Does this say "Section"?
                memcpy(tmp, cur, 7);
                int i;
                tmp[7] = 0;
                strtolower(tmp);

                if (memcmp(tmp, "section", 7) == 0) {

                    if (cur+7 >= end || cur[7] == ' ' || cur[7] == '\t' || cur[7] == '\n' || cur[7] == '#') {
                        // Yes. This is a section definition.
                        if (is_in_section) {
                            printf("Invalid config file: Unexpected 'Section' in line %d.\n", line_number);
                            return -1;
                        }

                        cur += 7;
                        reading_section_name = 1;
                        continue;
                    }

                }

            }


            // See if there's a keyword "EndSection"
            if (end > cur + 10) {

                // Does this say "EndSection"?
                memcpy(tmp, cur, 10);
                tmp[10] = 0;
                strtolower(tmp);

                if (memcmp(tmp, "endsection", 10) == 0) {

                    if (cur+10 >= end || cur[10] == ' ' || cur[10] == '\t' || cur[10] == '\n' || cur[10] == '#') {

                        // Yes. The section ends here.
                        if (!is_in_section) {
                            printf("Invalid config file: Unexpected 'EndSection' in line %d.\n", line_number);
                            return -1;
                        }
                        cur += 10;
                        expecting_line_end = 1;
                        is_in_section = 0;
                        handle_end_section(section_name, line_number);
                        if (result < 0)
                            return -1;
                        free(section_name);
                        section_name = NULL;
                        continue;
                    }

                }

            }

            // No keywords. Parse a key-value pair.
            if (!key) {
                int key_length;
                for (key_length = 0; cur + key_length < end; key_length++)
                if (cur[key_length] == ' ' || cur[key_length] == '\t' || cur[key_length] == '\n' || cur[key_length] == '#')
                        break;
                if (key_length < 1) {
                    printf("Invalid config file: Expected key in line %d.\n", line_number);
                    return -1;
                }
                key = malloc(key_length + 1);
                memcpy(key, cur, key_length);
                key[key_length] = 0;
                strtolower(key);
                cur += key_length;
            } else {
                value = malloc(end - cur + 1);
                memcpy(value, cur, end - cur);
                char *last_value_char;

                // Make sure we don't parse a comment
                for (last_value_char = value; last_value_char < value + (end - cur) - 1; last_value_char++)
                    if (*last_value_char == '#') {
                        last_value_char--;
                        break;
                    }

                // Trim the string of any whitespace
                for (; last_value_char >= value; last_value_char--)
                    if (*last_value_char != ' ' && *last_value_char != '\t' && *last_value_char != '\n')
                        break;
                *(last_value_char + 1) = 0;

                cur = end;
            }

        }

        if (key) {
            int result = handle_key_value_pair(key, value, section_name, line_number);
            if (result < 0)
                return -1;
        }

        if (reading_section_name) {
            return -1;
        }

    }

    if (is_in_section) {
        return -1;
    }
    
    fclose(f);
    return 0;

}

int write_reader_to_config(FILE *f, virtual_reader *reader) {
    return fprintf(f, "\nSection Device\n  Name \"%s\"\n  Address \"%s\"\n  ID \"%d\"\n  Slot \"%s\"\nEndSection\n",
        reader->name, reader->address, reader->id, reader->slot);
}

int write_config(char *config_file) {
    FILE *f = fopen(config_file, "w+");
    if (!f) {
        perror("Could not write config file");
        return -1;
    }

    int result = fprintf(f, "# This file was automatically generated by btpcsc_setup. Please do not edit it by hand.\n");
    if (result < 0) {
        perror("Could not write PCSC config file");
        return result;
    }

    virtual_reader *reader;
    for (reader = first_reader; reader; reader = reader->next) {
        result = write_reader_to_config(f, reader);
        if (result < 0) {
            perror("Could not write config file");
            return result;
        }
    }

    result = fclose(f);
    if (result < 0) {
        perror("Could not write config file");
        return result;
    }

    return result;

}

int write_reader_to_pcsc_config(FILE *f, virtual_reader *reader) {
    return fprintf(f, "\nFRIENDLYNAME\t\"%s\"\nDEVICENAME\t/dev/null\nLIBPATH\t\t/usr/lib/libbtpcsc.so\nCHANNELID\t%x\n",
        reader->name, reader->id);
}

int write_pcsc_config(char *pcsc_config_file) {
    FILE *f = fopen(pcsc_config_file, "w+");
    if (!f) {
        perror("Could not write PCSC config file");
        return -1;
    }

    int result = fprintf(f, "# This file was automatically generated by btpcsc_setup. Please do not edit it by hand.\n");
    if (result < 0) {
        perror("Could not write PCSC config file");
        return result;
    }

    virtual_reader *reader;
    for (reader = first_reader; reader; reader = reader->next) {
        result = write_reader_to_pcsc_config(f, reader);
        if (result < 0) {
            perror("Could not write PCSC config file");
            return result;
        }
    }

    result = fclose(f);
    if (result < 0) {
        perror("Could not write PCSC config file");
        return result;
    }

    return result;

}
