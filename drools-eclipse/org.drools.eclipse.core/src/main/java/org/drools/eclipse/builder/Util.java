package org.drools.eclipse.builder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Utility class.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class Util {

    public static final char[] NO_CHAR = new char[0];
    private static final int DEFAULT_READING_SIZE = 8192;
    
    public static char[] getResourceContentsAsCharArray(IFile file) throws CoreException {
        String encoding = null;
        try {
            encoding = file.getCharset();
        }
        catch(CoreException ce) {
            // do not use any encoding
        }
        
        InputStream stream= null;
        stream = new BufferedInputStream(file.getContents(true));
        try {
            return getInputStreamAsCharArray(stream, -1, encoding);
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, DroolsEclipsePlugin.PLUGIN_ID, IStatus.ERROR, "IOException", e));
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static char[] getInputStreamAsCharArray(InputStream stream,
            int length, String encoding) throws IOException {
        InputStreamReader reader = null;
        reader = encoding == null ? new InputStreamReader(stream)
                : new InputStreamReader(stream, encoding);
        char[] contents;
        if (length == -1) {
            contents = NO_CHAR;
            int contentsLength = 0;
            int amountRead = -1;
            do {
                int amountRequested = Math.max(stream.available(),
                        DEFAULT_READING_SIZE);
                if (contentsLength + amountRequested > contents.length) {
                    System.arraycopy(contents, 0,
                            contents = new char[contentsLength
                                    + amountRequested], 0, contentsLength);
                }
                amountRead = reader.read(contents, contentsLength,
                        amountRequested);

                if (amountRead > 0) {
                    contentsLength += amountRead;
                }
            } while (amountRead != -1);

            int start = 0;
            if (contentsLength > 0 && "UTF-8".equals(encoding)) {
                if (contents[0] == 0xFEFF) {
                    contentsLength--;
                    start = 1;
                }
            }
            if (contentsLength < contents.length) {
                System.arraycopy(contents, start,
                        contents = new char[contentsLength], 0, contentsLength);
            }
        } else {
            contents = new char[length];
            int len = 0;
            int readSize = 0;
            while ((readSize != -1) && (len != length)) {
                len += readSize;
                readSize = reader.read(contents, len, length - len);
            }
            int start = 0;
            if (length > 0 && "UTF-8".equals(encoding)) {
                if (contents[0] == 0xFEFF) {
                    len--;
                    start = 1;
                }
            }
            if (len != length)
                System.arraycopy(contents, start, (contents = new char[len]),
                        0, len);
        }

        return contents;
    }
}
