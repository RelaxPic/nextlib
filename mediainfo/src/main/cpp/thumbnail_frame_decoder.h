#pragma once

// Limits apply only after invalid data. Ordinary decoder buffering remains unchanged.
struct ThumbnailDecodeReadBudget {
    explicit ThumbnailDecodeReadBudget(int packets) : remainingPackets(packets) {}
    int remainingPackets;
    int64_t remainingBytes = 32 * 1024 * 1024;
    int64_t deadline = 0;

    void onInvalidData() {
        if (!deadline) deadline = av_gettime_relative() + 2 * AV_TIME_BASE;
    }

    bool canRead() const {
        return !deadline || (remainingPackets > 0 && remainingBytes > 0 &&
                av_gettime_relative() < deadline);
    }

    void onPacket(int size) {
        if (deadline) {
            --remainingPackets;
            remainingBytes -= size;
        }
    }
};

static bool decode_next_frame(MediaThumbnailRetrieverContext *context,
        AVCodecContext *codecContext, AVPacket *packet, AVFrame *frame,
        int maxRecoveryPackets = 8192) {
    ThumbnailDecodeReadBudget budget(maxRecoveryPackets);
    while (true) {
        int receiveResult = avcodec_receive_frame(codecContext, frame);
        if (receiveResult >= 0) return true;
        if (receiveResult != AVERROR(EAGAIN) && receiveResult != AVERROR_INVALIDDATA) {
            return false;
        }
        if (receiveResult == AVERROR_INVALIDDATA) budget.onInvalidData();
        if (!budget.canRead()) return false;
        if (av_read_frame(context->formatContext, packet) < 0) {
            avcodec_send_packet(codecContext, nullptr);
            return avcodec_receive_frame(codecContext, frame) >= 0;
        }
        budget.onPacket(packet->size);
        if (packet->stream_index != context->videoStreamIndex) {
            av_packet_unref(packet);
            continue;
        }
        int sendResult = avcodec_send_packet(codecContext, packet);
        av_packet_unref(packet);
        if (sendResult == AVERROR_INVALIDDATA) budget.onInvalidData();
        else if (sendResult < 0) return false;
    }
}

static bool seek_and_decode_frame(MediaThumbnailRetrieverContext *context,
        AVCodecContext *codecContext, AVPacket *packet, AVFrame *frame, int64_t timeUs) {
    AVStream *stream = context->formatContext->streams[context->videoStreamIndex];
    const int64_t target = av_rescale_q(timeUs, AV_TIME_BASE_Q, stream->time_base);
    // Preserve the original seek first. At most one recovery seek accounts for a nonzero
    // stream origin; never restart indefinitely on damaged or undecodable input.
    const int64_t origin = stream->start_time == AV_NOPTS_VALUE ? 0 : stream->start_time;
    const int64_t relativeTarget = av_sat_add64(target, origin);
    for (int attempt = 0; attempt < 2; ++attempt) {
        if (attempt == 1 && relativeTarget == target) break;
        int seekResult = av_seek_frame(context->formatContext, context->videoStreamIndex,
                attempt == 0 ? target : relativeTarget, AVSEEK_FLAG_BACKWARD);
        if (seekResult < 0) continue;
        avcodec_flush_buffers(codecContext);
        av_frame_unref(frame);
        if (decode_next_frame(context, codecContext, packet, frame)) return true;
    }
    return false;
}
