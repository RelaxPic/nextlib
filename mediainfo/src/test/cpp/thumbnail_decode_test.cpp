#include <cassert>
#include <cstdio>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
}

static int readPackets = 0;
static int videoPackets = 0;
static int invalidPackets = 0;
static int firstErrorReadPackets = 0;

static int observed_read_frame(AVFormatContext *context, AVPacket *packet) {
    int result = av_read_frame(context, packet);
    if (result >= 0) ++readPackets;
    return result;
}

static int observed_send_packet(AVCodecContext *context, const AVPacket *packet) {
    if (packet) ++videoPackets;
    int result = avcodec_send_packet(context, packet);
    if (result == AVERROR_INVALIDDATA) {
        if (!invalidPackets) firstErrorReadPackets = readPackets;
        ++invalidPackets;
    }
    return result;
}

// Count real FFmpeg calls without changing production decoding or JNI logging.
#define av_read_frame observed_read_frame
#define avcodec_send_packet observed_send_packet
#include "../../main/cpp/media_thumbnail_retriever.cpp"
#undef av_read_frame
#undef avcodec_send_packet

// Exercise the production demuxer/decoder without JNI bitmap allocation.
int main(int argc, char **argv) {
    ThumbnailDecodeReadBudget budget(2);
    budget.onPacket(100);
    assert(budget.remainingPackets == 2 && budget.canRead());
    budget.onInvalidData();
    const int64_t deadline = budget.deadline;
    budget.onPacket(100);
    budget.onInvalidData();
    assert(budget.remainingPackets == 1 && budget.deadline == deadline && budget.canRead());
    budget.onPacket(100);
    assert(!budget.canRead());
    ThumbnailDecodeReadBudget bytesBudget(8192);
    bytesBudget.onInvalidData();
    bytesBudget.onPacket(32 * 1024 * 1024);
    assert(!bytesBudget.canRead());
    ThumbnailDecodeReadBudget timeBudget(8192);
    timeBudget.deadline = av_gettime_relative() - 1;
    assert(!timeBudget.canRead());
    if (argc < 2) return 2;
    av_log_set_level(AV_LOG_QUIET);
    int failures = 0;
    for (int i = 1; i < argc; ++i) {
        auto *context = context_from_handle(create_context_from_source(argv[i]));
        if (!context || context->videoStreamIndex < 0) return 3;
        AVCodecContext *decoder = create_decoder_context(context);
        AVPacket *packet = av_packet_alloc();
        AVFrame *frame = av_frame_alloc();
        assert(decoder && packet && frame);
        AVStream *stream = context->formatContext->streams[context->videoStreamIndex];
        readPackets = videoPackets = invalidPackets = firstErrorReadPackets = 0;
        bool decoded = seek_and_decode_frame(context, decoder, packet, frame, 0);
        printf("sample=%d start=%lld decoded=%d output=%dx%d pts=%lld packets=%d video=%d invalid=%d firstErrorRead=%d\n",
                i, static_cast<long long>(stream->start_time), decoded,
                frame->width, frame->height, static_cast<long long>(frame->best_effort_timestamp),
                readPackets, videoPackets, invalidPackets, firstErrorReadPackets);
        if (!decoded || frame->width <= 0 || frame->height <= 0) ++failures;
        av_frame_free(&frame);
        av_packet_free(&packet);
        avcodec_free_context(&decoder);
        avformat_close_input(&context->formatContext);
        free(context);
    }
    return failures ? 1 : 0;
}
