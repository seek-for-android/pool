/*
 * MUSCLE SmartCard Development ( http://www.linuxnet.com )
 *
 * Copyright (C) 2002
 *  David Corcoran <corcoran@linuxnet.com>
 *
 * $Id: powermgt_generic.h 5434 2010-12-08 14:13:21Z rousseau $
 */

/**
 * @file
 * @brief This handles power management routines.
 */

#ifndef __powermgt_generic_h__
#define __powermgt_generic_h__

/**
 * Registers for Power Management callbacks
 */
ULONG PMRegisterForPowerEvents(void);

#endif
