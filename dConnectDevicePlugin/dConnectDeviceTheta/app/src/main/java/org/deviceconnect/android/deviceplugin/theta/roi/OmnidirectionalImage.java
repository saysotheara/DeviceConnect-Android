/*
 OmnidirectionalImage.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.roi;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.IntBuffer;

/**
 * Representation of an omnidirectional image.
 *
 * @author NTT DOCOMO, INC.
 */
public class OmnidirectionalImage {

    private final String mUri;
    private final Bitmap mBitmap;

    /**
     * Constructor.
     *
     * @param uri URI of an omnidirectional image.
     * @param requestOrigin Origin (RFC6454) as an HTTP client which gets an omnidirectional image.
     * @throws IOException if URL connection is failed.
     */
    public OmnidirectionalImage(final String uri, final String requestOrigin) throws IOException {
        mUri = uri;

        InputStream is = null;
        try {
            URLConnection conn = new URL(uri).openConnection();
            conn.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, requestOrigin);
            is = conn.getInputStream();
            mBitmap = BitmapFactory.decodeStream(is);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            IntBuffer buf = IntBuffer.allocate(width * height);
            IntBuffer tmp = IntBuffer.allocate(width * height);
            mBitmap.copyPixelsToBuffer(buf);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    tmp.put((height - i - 1) * width + j, buf.get(i * width + j));
                }
            }
            mBitmap.copyPixelsFromBuffer(tmp);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Gets the URI of this omnidirectional image.
     * @return the URI of this omnidirectional image
     */
    public String getUri() {
        return mUri;
    }

    /**
     * Gets the bitmap data of this omnidirectional image.
     * @return the bitmap data of this omnidirectional image.
     */
    Bitmap getData() {
        return mBitmap;
    }

}
