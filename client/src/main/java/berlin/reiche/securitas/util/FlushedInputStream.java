package berlin.reiche.securitas.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.BitmapFactory;

/**
 * There is a bug in the {@link BitmapFactory#decodeStream(InputStream)}
 * implementation. The problem occurs on some JPG files on a slow or hanging
 * connection.
 * 
 * This implementation skips the exact number of bytes provided, unless it
 * reaches EOF.
 * 
 * @see <a href="http://code.google.com/p/android/issues/detail?id=6066">
 *      http://code.google.com/p/android/issues/detail?id=6066</a>
 * 
 * @author Konrad Reiche
 * 
 */
public class FlushedInputStream extends FilterInputStream {

	/**
	 * Default constructor.
	 * 
	 * @param inputStream
	 *            the input stream to be wrapped.
	 */
	public FlushedInputStream(InputStream inputStream) {
		super(inputStream);
	}

	/**
	 * Skips {@code byteCount} bytes in this stream or until EOF is reached.
	 * 
	 * @see java.io.FilterInputStream#skip(long)
	 */
	@Override
	public long skip(long byteCount) throws IOException {
		long totalBytesSkipped = 0L;
		while (totalBytesSkipped < byteCount) {
			long bytesSkipped = in.skip(byteCount - totalBytesSkipped);
			if (bytesSkipped == 0L) {
				int b = read();
				if (b < 0) {
					// reached EOF
					break;
				} else {
					// read one byte
					bytesSkipped = 1;
				}
			}
			totalBytesSkipped += bytesSkipped;
		}
		return totalBytesSkipped;
	}

}
