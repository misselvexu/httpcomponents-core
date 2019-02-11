/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.http.io.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

/**
 * A streamed entity that obtains its content from a {@link Serializable}.
 * The content obtained from the {@link Serializable} instance can
 * optionally be buffered in a byte array in order to make the
 * entity self-contained and repeatable.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class SerializableEntity extends AbstractHttpEntity {

    private final Serializable objRef;

    private byte[] objSer;
    /**
     * Creates new instance of this class.
     *
     * @param ser input
     * @param bufferize tells whether the content should be
     *        stored in an internal buffer
     * @throws IOException in case of an I/O error
     */
    public SerializableEntity(
            final Serializable ser, final boolean bufferize, final ContentType contentType,
            final String contentEncoding) throws IOException {
        super(contentType, contentEncoding);
        Args.notNull(ser, "Source object");
        if (bufferize) {
            createBytes(ser);
            this.objRef = null;
        } else {
            this.objRef = ser;
        }
    }

    public SerializableEntity(
            final Serializable serializable, final ContentType contentType, final String contentEncoding) {
        super(contentType, contentEncoding);
        Args.notNull(serializable, "Source object");
        this.objRef = serializable;
    }

    public SerializableEntity(
            final Serializable ser, final boolean bufferize, final ContentType contentType) throws IOException {
        this(ser, bufferize, contentType, null);
    }

    public SerializableEntity(final Serializable serializable, final ContentType contentType) {
        this(serializable, contentType, null);
    }

    private void createBytes(final Serializable ser) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(ser);
        out.flush();
        this.objSer = baos.toByteArray();
    }

    @Override
    public final InputStream getContent() throws IOException, IllegalStateException {
        if (this.objSer == null) {
            createBytes(this.objRef);
        }
        return new ByteArrayInputStream(this.objSer);
    }

    @Override
    public final long getContentLength() {
        if (this.objSer ==  null) {
            return -1;
        }
        return this.objSer.length;
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final boolean isStreaming() {
        return this.objSer == null;
    }

    @Override
    public final void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        if (this.objSer == null) {
            final ObjectOutputStream out = new ObjectOutputStream(outStream);
            out.writeObject(this.objRef);
            out.flush();
        } else {
            outStream.write(this.objSer);
            outStream.flush();
        }
    }

    @Override
    public final void close() throws IOException {
    }

}
