
#define ASSD_IOC_MAGIC	'A'

/*
 * Enable the ASSD capable card
 *   - EBUSY - Function temporary not available
 *   - ENODEV - No ASSD capable card is available
 */
#define ASSD_IOC_ENABLE	_IO(ASSD_IOC_MAGIC, 0)

/*
 * Transceive secure token to secure element
 *   - EBUSY - Function temporary not available
 *   - ENODEV - No ASSD capable card is available
 *   - EINVAL - The secure token is invalid
 *   - ETIMEDOUT - Timeout for communication with secure element reached
 *   - EIO - Error in communication with secure element
 */
#define ASSD_IOC_TRANSCEIVE	_IOWR(ASSD_IOC_MAGIC, 1, char *)

/*
 * Probe if currently an ASSD capable card is available
 *   - ENODEV - No ASSD capable card is available
 */
#define ASSD_IOC_PROBE	_IO(ASSD_IOC_MAGIC, 2)

/*
 * Wait until an ASSD capable card is available
 *   - ENODEV - No ASSD capable card is available
 *   - ETIMEOUT - The timeout was reached
 */
#define ASSD_IOC_WAIT	_IOW(ASSD_IOC_MAGIC, 3, int)

/*
 * Set timeout for communication with secure element
 *   - EINVAL - The value is invalid
 */
#define ASSD_IOC_SET_TIMEOUT	_IOW(ASSD_IOC_MAGIC, 4, int)

/*
 * Get version information
 *   - EFAULT - The output buffer is invalid
 */
#define ASSD_IOC_GET_VERSION	_IOR(ASSD_IOC_MAGIC, 5, char *)

