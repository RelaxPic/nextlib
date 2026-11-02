package io.github.anilbeesetti.nextlib.media3ext.ffdecoder

import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import org.junit.Assert.assertEquals
import org.junit.Test

@UnstableApi
class FfmpegLibraryTest {
    @Test
    fun legacyCameraCodecsSelectNativeDecoders() {
        assertEquals("dvvideo", FfmpegLibrary.getCodecName("video/x-dv"))
        assertEquals("adpcm_ms", FfmpegLibrary.getCodecName("audio/x-adpcm-ms"))
        assertEquals("adpcm_ima_qt", FfmpegLibrary.getCodecName("audio/x-adpcm-ima-qt"))
        assertEquals("mjpegb", FfmpegLibrary.getCodecName("video/x-mjpeg-b"))
        assertEquals("mace3", FfmpegLibrary.getCodecName("audio/x-mace3"))
        assertEquals("mace6", FfmpegLibrary.getCodecName("audio/x-mace6"))
    }
    @Test
    fun realMediaCodecsSelectDistinctDecoders() {
        for (version in listOf(10, 20, 30, 40)) {
            assertEquals("rv$version", FfmpegLibrary.getCodecName("video/x-pn-realvideo$version"))
        }
        assertEquals("cook", FfmpegLibrary.getCodecName("audio/x-pn-realaudio-cook"))
    }

    @Test
    fun av1SelectsDav1d() {
        assertEquals("libdav1d", FfmpegLibrary.getCodecName(MimeTypes.VIDEO_AV1))
    }

    @Test
    fun additionalVideoCodecsSelectNativeDecoders() {
        assertEquals("mjpeg", FfmpegLibrary.getCodecName(MimeTypes.VIDEO_MJPEG))
        assertEquals("vvc", FfmpegLibrary.getCodecName(MimeTypes.VIDEO_H266))
        assertEquals("prores", FfmpegLibrary.getCodecName("video/x-prores"))
        assertEquals("mpeg4", FfmpegLibrary.getCodecName(MimeTypes.VIDEO_MP4V))
        assertEquals("h263", FfmpegLibrary.getCodecName(MimeTypes.VIDEO_H263))
        assertEquals("mpeg1video", FfmpegLibrary.getCodecName(MimeTypes.VIDEO_MPEG))
    }

    @Test
    fun windowsMediaCodecsSelectDistinctDecoders() {
        assertEquals("wmv1", FfmpegLibrary.getCodecName("video/x-ms-wmv1"))
        assertEquals("wmv2", FfmpegLibrary.getCodecName("video/x-ms-wmv2"))
        assertEquals("wmv3", FfmpegLibrary.getCodecName("video/x-ms-wmv3"))
        assertEquals("vc1", FfmpegLibrary.getCodecName(MimeTypes.VIDEO_VC1))
        assertEquals("wmav1", FfmpegLibrary.getCodecName("audio/x-ms-wmav1"))
        assertEquals("wmav2", FfmpegLibrary.getCodecName("audio/x-ms-wmav2"))
        assertEquals("adpcm_ima_wav", FfmpegLibrary.getCodecName("audio/x-adpcm-ima-wav"))
    }
}
