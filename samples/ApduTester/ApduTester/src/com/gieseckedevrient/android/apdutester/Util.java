package com.gieseckedevrient.android.apdutester;

import java.util.Random;

class Util {
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static Random rnd = new Random();

	static String randomString( int len ) 
	{
	  StringBuilder sb = new StringBuilder( len );
	  for( int i = 0; i < len; i++ )
		  sb.append( AB.charAt(rnd.nextInt(AB.length())) );
	  
	  return sb.toString();
	}
	
	
	static boolean isEqual(byte[] cmd, byte[]rsp) {
		if (rsp.length-2 != cmd.length || rsp[rsp.length-2] != (byte)0x90 || rsp[rsp.length-1] != (byte)0x00) {
			return false;
		}
		
		for (int t=1; t<cmd.length; t++) {
			if (cmd[t] != rsp[t]) {
				return false;
			}
		}
		
		return true;
	}
}
