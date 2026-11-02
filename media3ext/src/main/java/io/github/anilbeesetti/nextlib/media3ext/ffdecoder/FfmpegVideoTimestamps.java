package io.github.anilbeesetti.nextlib.media3ext.ffdecoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Original DTS relative to PTS, preserving Media3's renderer timestamp offsets. */
public final class FfmpegVideoTimestamps {
  private static final long SIGNATURE = 0x534c50444c543031L; // SLPDLT01
  private FfmpegVideoTimestamps() {}

  public static byte[] encodeDecodeTimeUs(long presentationTimeUs, long decodeTimeUs) {
    return ByteBuffer.allocate(16).putLong(SIGNATURE)
        .putLong(Math.subtractExact(decodeTimeUs, presentationTimeUs)).array();
  }

  public static long decodeTimeUs(ByteBuffer supplementalData, long presentationTimeUs) {
    if (supplementalData == null || supplementalData.remaining() != 16) return Long.MIN_VALUE;
    ByteBuffer data = supplementalData.duplicate().order(ByteOrder.BIG_ENDIAN);
    return data.getLong() == SIGNATURE
        ? Math.addExact(presentationTimeUs, data.getLong()) : Long.MIN_VALUE;
  }
}
