/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/manuel/workspace/BTPCSCServer_Service/src/btpcsc/server/IBtpcscServer.aidl
 */
package btpcsc.server;
/**
 * Smartcard service interface.
 */
public interface IBtpcscServer extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements btpcsc.server.IBtpcscServer
{
private static final java.lang.String DESCRIPTOR = "btpcsc.server.IBtpcscServer";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an btpcsc.server.IBtpcscServer interface,
 * generating a proxy if needed.
 */
public static btpcsc.server.IBtpcscServer asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof btpcsc.server.IBtpcscServer))) {
return ((btpcsc.server.IBtpcscServer)iin);
}
return new btpcsc.server.IBtpcscServer.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements btpcsc.server.IBtpcscServer
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
}
}
}
