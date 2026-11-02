package io.github.anilbeesetti.nextlib.media3ext.ffdecoder;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.Test;

public class FfmpegVideoTimestampsTest {
  @Test public void preservesNegativeAndLargeDecodeTimes() {
    for (long time : new long[] {Long.MIN_VALUE, -40000, 0, 43640000, Long.MAX_VALUE}) {
      assertEquals(time, FfmpegVideoTimestamps.decodeTimeUs(
          ByteBuffer.wrap(FfmpegVideoTimestamps.encodeDecodeTimeUs(0, time)), 0));
    }
  }

  @Test public void ignoresOtherSupplementalData() {
    assertEquals(Long.MIN_VALUE, FfmpegVideoTimestamps.decodeTimeUs(null, 123));
    for (int size : new int[] {0, 8, 15, 16, 17}) {
      assertEquals(Long.MIN_VALUE, FfmpegVideoTimestamps.decodeTimeUs(ByteBuffer.allocate(size), 123));
    }
  }

  @Test public void readsOnlyRemainingBytesWithoutChangingCallerBuffer() {
    ByteBuffer data = ByteBuffer.allocateDirect(24).order(ByteOrder.LITTLE_ENDIAN);
    data.position(8);
    data.put(FfmpegVideoTimestamps.encodeDecodeTimeUs(100000, 123456));
    data.flip();
    data.position(8);
    assertEquals(123456, FfmpegVideoTimestamps.decodeTimeUs(data, 100000));
    assertEquals(8, data.position());
    assertEquals(24, data.limit());
    assertEquals(ByteOrder.LITTLE_ENDIAN, data.order());
  }

  @Test public void carriesDtsThroughRendererOffsetChangesAndSeeks() {
    byte[] encoded = FfmpegVideoTimestamps.encodeDecodeTimeUs(43680000, 43640000);
    for (long offset : new long[] {0, 1000000000000L, -1000000}) {
      assertEquals(43640000 + offset, FfmpegVideoTimestamps.decodeTimeUs(
          ByteBuffer.wrap(encoded), 43680000 + offset));
    }
  }

  @Test public void rejectsTimestampArithmeticOverflow() {
    assertThrows(ArithmeticException.class,
        () -> FfmpegVideoTimestamps.encodeDecodeTimeUs(Long.MIN_VALUE, Long.MAX_VALUE));
    byte[] encoded = FfmpegVideoTimestamps.encodeDecodeTimeUs(0, Long.MAX_VALUE);
    assertThrows(ArithmeticException.class,
        () -> FfmpegVideoTimestamps.decodeTimeUs(ByteBuffer.wrap(encoded), 1));
  }
}
