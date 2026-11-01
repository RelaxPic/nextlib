package io.github.anilbeesetti.nextlib.media3ext.ffdecoder

import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

@UnstableApi
class FfmpegVideoInitializationTest {
    @Test
    fun vvcPreservesAnnexBParameterSets() {
        val parameters = byteArrayOf(0, 0, 0, 1, 0, 113, 3, 0, 0, 0, 1, 0, 121, 5)
        assertArrayEquals(parameters,
            FfmpegVideoDecoder.getExtraData(MimeTypes.VIDEO_H266, listOf(parameters)))
    }

    @Test
    fun vvcWithoutOutOfBandParametersIsAllowed() {
        assertNull(FfmpegVideoDecoder.getExtraData(MimeTypes.VIDEO_H266, emptyList()))
    }

    @Test
    fun mpeg4PreservesVisualObjectLayerConfiguration() {
        val configuration = byteArrayOf(0, 0, 1, -80, 1, 0, 0, 1, 32, 0, -124)
        assertArrayEquals(configuration,
            FfmpegVideoDecoder.getExtraData(MimeTypes.VIDEO_MP4V, listOf(configuration)))
    }

    @Test
    fun mjpegDoesNotReceiveUnrelatedInitializationData() {
        assertNull(FfmpegVideoDecoder.getExtraData(MimeTypes.VIDEO_MJPEG,
            listOf(byteArrayOf(1, 2, 3))))
    }
}
