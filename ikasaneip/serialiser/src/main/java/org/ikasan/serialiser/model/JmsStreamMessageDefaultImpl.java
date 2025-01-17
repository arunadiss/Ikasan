/*
 * $Id$
 * $URL$
 * 
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 * 
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing 
 * of individual contributors are as shown in the packaged copyright.txt 
 * file. 
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This code has been modified from the original class which was sourced from the
 * Apache ActiveMQ project under the following license.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ikasan.serialiser.model;

import jakarta.jms.JMSException;
import jakarta.jms.MessageEOFException;
import jakarta.jms.MessageFormatException;
import jakarta.jms.MessageNotReadableException;
import jakarta.jms.MessageNotWriteableException;
import jakarta.jms.StreamMessage;

import org.ikasan.apache.activemq.util.MarshallingSupport;

import java.io.*;

/**
 * Light JMS Stream message implementation purely for serialiser usage.
 * 
 * @author Ikasan Development Team
 * 
 */
public class JmsStreamMessageDefaultImpl extends JmsMessageDefaultImpl implements StreamMessage {

    protected transient DataOutputStream dataOut;
    protected transient ByteArrayOutputStream bytesOut;
    protected transient DataInputStream dataIn;
    protected transient int remainingBytes = -1;
    protected transient byte[] content;

    protected void setContent(byte[] content)
    {
        this.content = content;
    }

    protected void storeContent() {
        if (dataOut != null) {
            try {
                dataOut.close();
                setContent(bytesOut.toByteArray());
                bytesOut = null;
                dataOut = null;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public String getJMSXMimeType() {
        return "jms/stream-message";
    }

    /**
     * Clears out the message body. Clearing a message's body does not clear its
     * header values or property entries. <p/>
     * <P>
     * If this message body was read-only, calling this method leaves the
     * message body in the same state as an empty body in a newly created
     * message.
     *
     * @throws JMSException if the JMS provider fails to clear the message body
     *                 due to some internal error.
     */

    @Override
    public void clearBody() throws JMSException {
        super.clearBody();
        this.dataOut = null;
        this.dataIn = null;
        this.bytesOut = null;
        this.remainingBytes = -1;
    }

    /**
     * Reads a <code>boolean</code> from the stream message.
     *
     * @return the <code>boolean</code> value read
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public boolean readBoolean() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(10);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.BOOLEAN_TYPE) {
                return this.dataIn.readBoolean();
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return Boolean.valueOf(this.dataIn.readUTF()).booleanValue();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to boolean.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a boolean type");
            }
        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a <code>byte</code> value from the stream message.
     *
     * @return the next byte from the stream message as a 8-bit
     *         <code>byte</code>
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public byte readByte() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(10);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.BYTE_TYPE) {
                return this.dataIn.readByte();
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return Byte.valueOf(this.dataIn.readUTF()).byteValue();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to byte.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a byte type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a 16-bit integer from the stream message.
     *
     * @return a 16-bit integer from the stream message
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public short readShort() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(17);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.SHORT_TYPE) {
                return this.dataIn.readShort();
            }
            if (type == MarshallingSupport.BYTE_TYPE) {
                return this.dataIn.readByte();
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return Short.valueOf(this.dataIn.readUTF()).shortValue();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to short.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a short type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
    }

    /**
     * Reads a Unicode character value from the stream message.
     *
     * @return a Unicode character from the stream message
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public char readChar() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(17);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.CHAR_TYPE) {
                return this.dataIn.readChar();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to char.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a char type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a 32-bit integer from the stream message.
     *
     * @return a 32-bit integer value from the stream message, interpreted as an
     *         <code>int</code>
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public int readInt() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(33);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.INTEGER_TYPE) {
                return this.dataIn.readInt();
            }
            if (type == MarshallingSupport.SHORT_TYPE) {
                return this.dataIn.readShort();
            }
            if (type == MarshallingSupport.BYTE_TYPE) {
                return this.dataIn.readByte();
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return Integer.valueOf(this.dataIn.readUTF()).intValue();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to int.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not an int type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a 64-bit integer from the stream message.
     *
     * @return a 64-bit integer value from the stream message, interpreted as a
     *         <code>long</code>
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public long readLong() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(65);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.LONG_TYPE) {
                return this.dataIn.readLong();
            }
            if (type == MarshallingSupport.INTEGER_TYPE) {
                return this.dataIn.readInt();
            }
            if (type == MarshallingSupport.SHORT_TYPE) {
                return this.dataIn.readShort();
            }
            if (type == MarshallingSupport.BYTE_TYPE) {
                return this.dataIn.readByte();
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return Long.valueOf(this.dataIn.readUTF()).longValue();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to long.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a long type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a <code>float</code> from the stream message.
     *
     * @return a <code>float</code> value from the stream message
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public float readFloat() throws JMSException {
        initializeReading();
        try {
            this.dataIn.mark(33);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.FLOAT_TYPE) {
                return this.dataIn.readFloat();
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return Float.valueOf(this.dataIn.readUTF()).floatValue();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to float.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a float type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a <code>double</code> from the stream message.
     *
     * @return a <code>double</code> value from the stream message
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public double readDouble() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(65);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.DOUBLE_TYPE) {
                return this.dataIn.readDouble();
            }
            if (type == MarshallingSupport.FLOAT_TYPE) {
                return this.dataIn.readFloat();
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return Double.valueOf(this.dataIn.readUTF()).doubleValue();
            }
            if (type == MarshallingSupport.NULL) {
                this.dataIn.reset();
                throw new NullPointerException("Cannot convert NULL value to double.");
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a double type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a <CODE>String</CODE> from the stream message.
     *
     * @return a Unicode string from the stream message
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     */

    @Override
    public String readString() throws JMSException {
        initializeReading();
        try {

            this.dataIn.mark(65);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.NULL) {
                return null;
            }
            if (type == MarshallingSupport.BIG_STRING_TYPE) {
                return MarshallingSupport.readUTF8(dataIn);
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return this.dataIn.readUTF();
            }
            if (type == MarshallingSupport.LONG_TYPE) {
                return Long.valueOf(this.dataIn.readLong()).toString();
            }
            if (type == MarshallingSupport.INTEGER_TYPE) {
                return Integer.valueOf(this.dataIn.readInt()).toString();
            }
            if (type == MarshallingSupport.SHORT_TYPE) {
                return Short.valueOf(this.dataIn.readShort()).toString();
            }
            if (type == MarshallingSupport.BYTE_TYPE) {
                return Byte.valueOf(this.dataIn.readByte()).toString();
            }
            if (type == MarshallingSupport.FLOAT_TYPE) {
                return Float.valueOf(this.dataIn.readFloat()).toString();
            }
            if (type == MarshallingSupport.DOUBLE_TYPE) {
                return Double.valueOf(this.dataIn.readDouble()).toString();
            }
            if (type == MarshallingSupport.BOOLEAN_TYPE) {
                return (this.dataIn.readBoolean() ? Boolean.TRUE : Boolean.FALSE).toString();
            }
            if (type == MarshallingSupport.CHAR_TYPE) {
                return Character.valueOf(this.dataIn.readChar()).toString();
            } else {
                this.dataIn.reset();
                throw new MessageFormatException(" not a String type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        }
        catch (EOFException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }
        catch (IOException e)
        {
            throw JmsExceptionFactory.getJmsException(e);
        }

    }

    /**
     * Reads a byte array field from the stream message into the specified
     * <CODE>byte[]</CODE> object (the read buffer). <p/>
     * <P>
     * To read the field value, <CODE>readBytes</CODE> should be successively
     * called until it returns a value less than the length of the read buffer.
     * The value of the bytes in the buffer following the last byte read is
     * undefined. <p/>
     * <P>
     * If <CODE>readBytes</CODE> returns a value equal to the length of the
     * buffer, a subsequent <CODE>readBytes</CODE> call must be made. If there
     * are no more bytes to be read, this call returns -1. <p/>
     * <P>
     * If the byte array field value is null, <CODE>readBytes</CODE> returns
     * -1. <p/>
     * <P>
     * If the byte array field value is empty, <CODE>readBytes</CODE> returns
     * 0. <p/>
     * <P>
     * Once the first <CODE>readBytes</CODE> call on a <CODE>byte[]</CODE>
     * field value has been made, the full value of the field must be read
     * before it is valid to read the next field. An attempt to read the next
     * field before that has been done will throw a
     * <CODE>MessageFormatException</CODE>. <p/>
     * <P>
     * To read the byte field value into a new <CODE>byte[]</CODE> object, use
     * the <CODE>readObject</CODE> method.
     *
     * @param value the buffer into which the data is read
     * @return the total number of bytes read into the buffer, or -1 if there is
     *         no more data because the end of the byte field has been reached
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     * @see #readObject()
     */

    @Override
    public int readBytes(byte[] value) throws JMSException {

        initializeReading();
        try {
            if (value == null) {
                throw new NullPointerException();
            }

            if (remainingBytes == -1) {
                this.dataIn.mark(value.length + 1);
                int type = this.dataIn.read();
                if (type == -1) {
                    throw new MessageEOFException("reached end of data");
                }
                if (type != MarshallingSupport.BYTE_ARRAY_TYPE) {
                    throw new MessageFormatException("Not a byte array");
                }
                remainingBytes = this.dataIn.readInt();
            } else if (remainingBytes == 0) {
                remainingBytes = -1;
                return -1;
            }

            if (value.length <= remainingBytes) {
                // small buffer
                remainingBytes -= value.length;
                this.dataIn.readFully(value);
                return value.length;
            } else {
                // big buffer
                int rc = this.dataIn.read(value, 0, remainingBytes);
                remainingBytes = 0;
                return rc != -1 ? rc : 0;
            }

        } catch (EOFException e) {
            JMSException jmsEx = new MessageEOFException(e.getMessage());
            jmsEx.setLinkedException(e);
            throw jmsEx;
        } catch (IOException e) {
            JMSException jmsEx = new MessageFormatException(e.getMessage());
            jmsEx.setLinkedException(e);
            throw jmsEx;
        }
    }

    /**
     * Reads an object from the stream message. <p/>
     * <P>
     * This method can be used to return, in objectified format, an object in
     * the Java programming language ("Java object") that has been written to
     * the stream with the equivalent <CODE>writeObject</CODE> method call, or
     * its equivalent primitive <CODE>write<I>type</I></CODE> method. <p/>
     * <P>
     * Note that byte values are returned as <CODE>byte[]</CODE>, not
     * <CODE>Byte[]</CODE>. <p/>
     * <P>
     * An attempt to call <CODE>readObject</CODE> to read a byte field value
     * into a new <CODE>byte[]</CODE> object before the full value of the byte
     * field has been read will throw a <CODE>MessageFormatException</CODE>.
     *
     * @return a Java object from the stream message, in objectified format (for
     *         example, if the object was written as an <CODE>int</CODE>, an
     *         <CODE>Integer</CODE> is returned)
     * @throws JMSException if the JMS provider fails to read the message due to
     *                 some internal error.
     * @throws MessageEOFException if unexpected end of message stream has been
     *                 reached.
     * @throws MessageFormatException if this type conversion is invalid.
     * @throws MessageNotReadableException if the message is in write-only mode.
     * @see #readBytes(byte[] value)
     */

    @Override
    public Object readObject() throws JMSException {
        initializeReading();
        try {
            this.dataIn.mark(65);
            int type = this.dataIn.read();
            if (type == -1) {
                throw new MessageEOFException("reached end of data");
            }
            if (type == MarshallingSupport.NULL) {
                return null;
            }
            if (type == MarshallingSupport.BIG_STRING_TYPE) {
                return MarshallingSupport.readUTF8(dataIn);
            }
            if (type == MarshallingSupport.STRING_TYPE) {
                return this.dataIn.readUTF();
            }
            if (type == MarshallingSupport.LONG_TYPE) {
                return Long.valueOf(this.dataIn.readLong());
            }
            if (type == MarshallingSupport.INTEGER_TYPE) {
                return Integer.valueOf(this.dataIn.readInt());
            }
            if (type == MarshallingSupport.SHORT_TYPE) {
                return Short.valueOf(this.dataIn.readShort());
            }
            if (type == MarshallingSupport.BYTE_TYPE) {
                return Byte.valueOf(this.dataIn.readByte());
            }
            if (type == MarshallingSupport.FLOAT_TYPE) {
                return Float.valueOf(this.dataIn.readFloat());
            }
            if (type == MarshallingSupport.DOUBLE_TYPE) {
                return Double.valueOf(this.dataIn.readDouble());
            }
            if (type == MarshallingSupport.BOOLEAN_TYPE) {
                return this.dataIn.readBoolean() ? Boolean.TRUE : Boolean.FALSE;
            }
            if (type == MarshallingSupport.CHAR_TYPE) {
                return Character.valueOf(this.dataIn.readChar());
            }
            if (type == MarshallingSupport.BYTE_ARRAY_TYPE) {
                int len = this.dataIn.readInt();
                byte[] value = new byte[len];
                this.dataIn.readFully(value);
                return value;
            } else {
                this.dataIn.reset();
                throw new MessageFormatException("unknown type");
            }
        } catch (NumberFormatException mfe) {
            try {
                this.dataIn.reset();
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
            throw mfe;

        } catch (EOFException e) {
            JMSException jmsEx = new MessageEOFException(e.getMessage());
            jmsEx.setLinkedException(e);
            throw jmsEx;
        } catch (IOException e) {
            JMSException jmsEx = new MessageFormatException(e.getMessage());
            jmsEx.setLinkedException(e);
            throw jmsEx;
        }
    }

    /**
     * Writes a <code>boolean</code> to the stream message. The value
     * <code>true</code> is written as the value <code>(byte)1</code>; the
     * value <code>false</code> is written as the value <code>(byte)0</code>.
     *
     * @param value the <code>boolean</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeBoolean(boolean value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalBoolean(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a <code>byte</code> to the stream message.
     *
     * @param value the <code>byte</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeByte(byte value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalByte(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a <code>short</code> to the stream message.
     *
     * @param value the <code>short</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeShort(short value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalShort(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a <code>char</code> to the stream message.
     *
     * @param value the <code>char</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeChar(char value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalChar(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes an <code>int</code> to the stream message.
     *
     * @param value the <code>int</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeInt(int value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalInt(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a <code>long</code> to the stream message.
     *
     * @param value the <code>long</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeLong(long value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalLong(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a <code>float</code> to the stream message.
     *
     * @param value the <code>float</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeFloat(float value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalFloat(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a <code>double</code> to the stream message.
     *
     * @param value the <code>double</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeDouble(double value) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalDouble(dataOut, value);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a <code>String</code> to the stream message.
     *
     * @param value the <code>String</code> value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeString(String value) throws JMSException {
        initializeWriting();
        try {
            if (value == null) {
                MarshallingSupport.marshalNull(dataOut);
            } else {
                MarshallingSupport.marshalString(dataOut, value);
            }
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes a byte array field to the stream message. <p/>
     * <P>
     * The byte array <code>value</code> is written to the message as a byte
     * array field. Consecutively written byte array fields are treated as two
     * distinct fields when the fields are read.
     *
     * @param value the byte array value to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeBytes(byte[] value) throws JMSException {
        writeBytes(value, 0, value.length);
    }

    /**
     * Writes a portion of a byte array as a byte array field to the stream
     * message. <p/>
     * <P>
     * The a portion of the byte array <code>value</code> is written to the
     * message as a byte array field. Consecutively written byte array fields
     * are treated as two distinct fields when the fields are read.
     *
     * @param value the byte array value to be written
     * @param offset the initial offset within the byte array
     * @param length the number of bytes to use
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        initializeWriting();
        try {
            MarshallingSupport.marshalByteArray(dataOut, value, offset, length);
        } catch (IOException ioe) {
            throw JmsExceptionFactory.getJmsException(ioe);
        }
    }

    /**
     * Writes an object to the stream message. <p/>
     * <P>
     * This method works only for the objectified primitive object types (<code>Integer</code>,
     * <code>Double</code>, <code>Long</code>&nbsp;...),
     * <code>String</code> objects, and byte arrays.
     *
     * @param value the Java object to be written
     * @throws JMSException if the JMS provider fails to write the message due
     *                 to some internal error.
     * @throws MessageFormatException if the object is invalid.
     * @throws MessageNotWriteableException if the message is in read-only mode.
     */

    @Override
    public void writeObject(Object value) throws JMSException {
        initializeWriting();
        if (value == null) {
            try {
                MarshallingSupport.marshalNull(dataOut);
            } catch (IOException ioe) {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
        } else if (value instanceof String) {
            writeString(value.toString());
        } else if (value instanceof Character character) {
            writeChar(character.charValue());
        } else if (value instanceof Boolean boolean1) {
            writeBoolean(boolean1.booleanValue());
        } else if (value instanceof Byte byte1) {
            writeByte(byte1.byteValue());
        } else if (value instanceof Short short1) {
            writeShort(short1.shortValue());
        } else if (value instanceof Integer integer) {
            writeInt(integer.intValue());
        } else if (value instanceof Float float1) {
            writeFloat(float1.floatValue());
        } else if (value instanceof Double double1) {
            writeDouble(double1.doubleValue());
        } else if (value instanceof byte[] bytes) {
            writeBytes(bytes);
        }else if (value instanceof Long long1) {
            writeLong(long1.longValue());
        }else {
            throw new MessageFormatException("Unsupported Object type: " + value.getClass());
        }
    }

    /**
     * Puts the message body in read-only mode and repositions the stream of
     * bytes to the beginning.
     *
     * @throws JMSException if an internal error occurs
     */

    @Override
    public void reset() throws JMSException {
        storeContent();
        this.bytesOut = null;
        this.dataIn = null;
        this.dataOut = null;
        this.remainingBytes = -1;
    }

    private void initializeWriting() throws JMSException {
        if (this.dataOut == null) {
            this.bytesOut = new ByteArrayOutputStream();
            OutputStream os = bytesOut;
            this.dataOut = new DataOutputStream(os);
        }

        if (this.content != null && this.content.length > 0)
        {
            try
            {
                this.dataOut.write(this.content, 0, this.content.length);
                this.content = null;
            }
            catch(IOException ioe)
            {
                throw JmsExceptionFactory.getJmsException(ioe);
            }
        }
    }

    private void initializeReading() throws MessageNotReadableException
    {
        if (this.dataIn == null)
        {
            byte[] data = this.getContent();
            if (data == null)
            {
                data = new byte[0];
            }
            InputStream is = new ByteArrayInputStream(data);
            this.dataIn = new DataInputStream(is);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " JmsStreamMessageDefaultImpl{ " + "bytesOut = " + bytesOut + ", dataOut = " + dataOut + ", dataIn = " + dataIn + " }";
    }

    protected byte[] getContent()
    {
        return this.content;
    }
}
