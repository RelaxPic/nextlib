package io.github.anilbeesetti.nextlib.media3ext.ffdecoder;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class FfmpegAudioParametersTest {
  @Test public void decodesLittleEndianAlignmentForHeaderlessCodecs() {
    for (String mime : new String[] {"audio/x-ms-wmav1", "audio/x-ms-wmav2",
        "audio/x-adpcm-ima-wav", "audio/x-pn-realaudio-cook"}) {
      assertEquals(1024, FfmpegAudioParameters.blockAlignment(mime,
          Arrays.asList(new byte[0], new byte[] {0, 4, 0, 0, 4, 0, 0, 0})));
    }
  }

  @Test public void cookRequiresAlignmentButNotImaSampleBits() {
    assertThrows(IllegalArgumentException.class, () -> FfmpegAudioParameters.blockAlignment(
        "audio/x-pn-realaudio-cook", Collections.emptyList()));
    assertEquals(0, FfmpegAudioParameters.codedSampleBits("audio/x-pn-realaudio-cook",
        Arrays.asList(new byte[8], new byte[] {0, 4, 0, 0, 0, 0, 0, 0})));
  }

  @Test public void leavesExistingCodecInitializationUntouched() {
    assertEquals(0, FfmpegAudioParameters.blockAlignment("audio/mp4a-latm",
        Collections.singletonList(new byte[] {1, 2})));
  }

  @Test public void rejectsMissingTruncatedZeroNegativeAndOversizedAlignment() {
    assertInvalid(Collections.emptyList());
    assertInvalid(Arrays.asList(new byte[0], new byte[3]));
    assertInvalid(Arrays.asList(new byte[0], new byte[8]));
    assertInvalid(Arrays.asList(new byte[0], new byte[] {-1, -1, -1, -1, 0, 0, 0, 0}));
    assertInvalid(Arrays.asList(new byte[0], new byte[] {1, 0, 0, 1, 0, 0, 0, 0}));
  }

  @Test public void validatesImaCodedBitsWithoutChangingOtherCodecs() {
    assertEquals(4, FfmpegAudioParameters.codedSampleBits("audio/x-adpcm-ima-wav",
        Arrays.asList(new byte[0], new byte[] {0, 4, 0, 0, 4, 0, 0, 0})));
    assertEquals(0, FfmpegAudioParameters.codedSampleBits("audio/mp4a-latm", Collections.emptyList()));
    assertThrows(IllegalArgumentException.class,
        () -> FfmpegAudioParameters.codedSampleBits("audio/x-adpcm-ima-wav",
            Arrays.asList(new byte[0], new byte[] {0, 4, 0, 0, 0, 0, 0, 0})));
  }

  private static void assertInvalid(java.util.List<byte[]> data) {
    assertThrows(IllegalArgumentException.class,
        () -> FfmpegAudioParameters.blockAlignment("audio/x-ms-wmav2", data));
  }
}
