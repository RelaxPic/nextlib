package io.github.anilbeesetti.nextlib.media3ext.ffdecoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/** Container parameters needed by codecs whose packets carry no audio header. */
final class FfmpegAudioParameters {
  private FfmpegAudioParameters() {}

  static int blockAlignment(String mimeType, List<byte[]> initializationData) {
    if (!"audio/x-ms-wmav1".equals(mimeType) && !"audio/x-ms-wmav2".equals(mimeType)
        && !"audio/x-adpcm-ima-wav".equals(mimeType)
        && !"audio/x-adpcm-ms".equals(mimeType)
        && !"audio/x-pn-realaudio-cook".equals(mimeType)) return 0;
    if (initializationData.size() != 2 || initializationData.get(1).length != 8) {
      throw new IllegalArgumentException("Missing container audio block alignment");
    }
    int value = ByteBuffer.wrap(initializationData.get(1)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    if (value <= 0 || value > 16 * 1024 * 1024) {
      throw new IllegalArgumentException("Invalid container audio block alignment");
    }
    return value;
  }

  static int codedSampleBits(String mimeType, List<byte[]> initializationData) {
    boolean isMs = "audio/x-adpcm-ms".equals(mimeType);
    if (!isMs && !"audio/x-adpcm-ima-wav".equals(mimeType)) return 0;
    blockAlignment(mimeType, initializationData);
    int bits = ByteBuffer.wrap(initializationData.get(1)).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
    if (isMs ? bits != 4 : bits < 2 || bits > 5) {
      throw new IllegalArgumentException("Invalid ADPCM sample bits");
    }
    return bits;
  }
}
