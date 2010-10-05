/*****************************************************************
/
/ File   :   pcscdefines.h
/ Author :   David Corcoran <corcoran@linuxnet.com>
/ Date   :   June 15, 2000
/ Purpose:   This provides PC/SC shared defines.
/            See http://www.linuxnet.com for more information.
/ License:   See file LICENSE
/
******************************************************************/

#ifndef _pcscdefines_h_
#define _pcscdefines_h_

#ifdef __cplusplus
extern "C" {
#endif 

/* Defines a list of pseudo types. */

  typedef unsigned long      DWORD;
  typedef unsigned long*     PDWORD;
  typedef unsigned char      UCHAR;
  typedef unsigned char*     PUCHAR;
  typedef char*              LPSTR;
  typedef long               RESPONSECODE;
  typedef void               VOID;

  #define MAX_RESPONSE_SIZE  264
  #define MAX_ATR_SIZE       33

#ifdef __cplusplus
}
#endif

#endif /* _pcscdefines_h_ */
