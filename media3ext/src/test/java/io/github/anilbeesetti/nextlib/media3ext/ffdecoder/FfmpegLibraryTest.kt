package io.github.anilbeesetti.nextlib.media3ext.ffdecoder

import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import org.junit.Assert.assertEquals
import org.junit.Test

@UnstableApi
class FfmpegLibraryTest {
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
}
