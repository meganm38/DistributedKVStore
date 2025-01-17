package com.g3.CPEN431.A12.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Various static routines for solving endian problems.
 */
public class ByteOrder {
    /**
     * Returns the reverse of x.
     */
    public static byte[] reverse(byte[] x) {
        int n=x.length;
        byte[] ret=new byte[n];
        for (int i=0; i<n; i++) 
            ret[i]=x[n-i-1];
        return ret;
    }

    /** 
     * Little-endian bytes to int
     * 
     * @requires x.length-offset>=4
     * @effects returns the value of x[offset..offset+4] as an int, 
     *   assuming x is interpreted as a signed little endian number (i.e., 
     *   x[offset] is LSB) If you want to interpret it as an unsigned number,
     *   call ubytes2long on the result.
     */
    public static int leb2int(byte[] x, int offset) {
        //Must mask value after left-shifting, since case from byte
        //to int copies most significant bit to the left!
        int x0=x[offset] & 0x000000FF;
        int x1=(x[offset+1]<<8) & 0x0000FF00;
        int x2=(x[offset+2]<<16) & 0x00FF0000;
        int x3=(x[offset+3]<<24);
        return x3|x2|x1|x0;
    }
    
    /**
     * Big-endian bytes to int.
     *
     * @requires x.length-offset>=4
     * @effects returns the value of x[offset..offset+4] as an int, 
     *   assuming x is interpreted as a signed big endian number (i.e., 
     *   x[offset] is MSB) If you want to interpret it as an unsigned number,
     *   call ubytes2long on the result.
     */
    public static int beb2int(byte[] x, int offset) {
        //Must mask value after left-shifting, since case from byte
        //to int copies most significant bit to the left!
        int x0=x[offset+3] & 0x000000FF;
        int x1=(x[offset+2]<<8) & 0x0000FF00;
        int x2=(x[offset+1]<<16) & 0x00FF0000;
        int x3=(x[offset]<<24);
        return x3|x2|x1|x0;
    }     
    
    /** 
     * Little-endian bytes to int - stream version
     * 
     */
    public static int leb2int(InputStream is) throws IOException{ 
        //Must mask value after left-shifting, since case from byte
        //to int copies most significant bit to the left!
        int x0=is.read() & 0x000000FF;
        int x1=(is.read()<<8) & 0x0000FF00;
        int x2=(is.read()<<16) & 0x00FF0000;
        int x3=(is.read()<<24);
        return x3|x2|x1|x0;
    }

    /** 
     * Little-endian bytes to int.  Unlike leb2int(x, offset), this version can
     * read fewer than 4 bytes.  If n<4, the returned value is never negative.
     * 
     * @param x the source of the bytes
     * @param offset the index to start reading bytes
     * @param n the number of bytes to read, which must be between 1 and 4, 
     *  inclusive
     * @return the value of x[offset..offset+N] as an int, assuming x is 
     *  interpreted as an unsigned little-endian number (i.e., x[offset] is LSB). 
     * @exception IllegalArgumentException n is less than 1 or greater than 4
     * @exception IndexOutOfBoundsException offset<0 or offset+n>x.length
     */
    public static int leb2int(byte[] x, int offset, int n) 
            throws IndexOutOfBoundsException, IllegalArgumentException {
        if (n<1 || n>4)
            throw new IllegalArgumentException("No bytes specified");

        //Must mask value after left-shifting, since case from byte
        //to int copies most significant bit to the left!
        int x0=x[offset] & 0x000000FF;
        int x1=0;
        int x2=0;
        int x3=0;
        if (n>1) {
            x1=(x[offset+1]<<8) & 0x0000FF00;
            if (n>2) {
                x2=(x[offset+2]<<16) & 0x00FF0000;
                if (n>3)
                    x3=(x[offset+3]<<24);               
            }
        }
        return x3|x2|x1|x0;
    }


    /**
     * Int to little-endian bytes: writes x to buf[offset..]
     */
    public static void int2leb(int x, byte[] buf, int offset) {
        buf[offset]=(byte)(x & 0x000000FF);
        buf[offset+1]=(byte)((x>>8) & 0x000000FF);
        buf[offset+2]=(byte)((x>>16) & 0x000000FF);
        buf[offset+3]=(byte)((x>>24) & 0x000000FF);
    }

    public static void int2leb2Bytes(int x, byte[] buf, int offset) {
        buf[offset]=(byte)(x & 0x000000FF);
        buf[offset+1]=(byte)((x>>8) & 0x000000FF);
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    /**
     * Int to little-endian bytes: writes x to given stream
     */
    public static void int2leb(int x, OutputStream os) throws IOException {
        os.write((byte)(x & 0x000000FF));
        os.write((byte)((x>>8) & 0x000000FF));
        os.write((byte)((x>>16) & 0x000000FF));
        os.write((byte)((x>>24) & 0x000000FF));
    }


   /**
     * Interprets the value of x as an unsigned byte, and returns 
     * it as integer.  For example, ubyte2int(0xFF)==255, not -1.
     */
    public static int ubyte2int(byte x) {
        return ((int)x) & 0x000000FF;
    }

    public static void short2beb(short x, byte[] buf, int offset) {
        buf[offset+1]=(byte)(x & 0x000000FF);
        buf[offset]=(byte)((x>>8) & 0x000000FF);
    }
    public static void long2beb(long x, byte[] buf, int offset) {
        for (int i = 0; i < 8; i += 1) {
            buf[offset + 7 - i] = (byte)((x>>(8*i)) & 0xFF);
        }
    }
}