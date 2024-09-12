package io.livekit.android.example.voiceassistant.ui.noise

import com.paramsen.noise.Noise
import java.nio.ByteBuffer
import java.nio.ByteOrder


class FFTAudioProcessor {

    companion object {
        var EMPTY_BUFFER = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
        var SAMPLE_SIZE = 4096
    }

    private var noise: Noise? = null

    private var isActive: Boolean = false

    private var processBuffer = EMPTY_BUFFER
    private var fftBuffer = EMPTY_BUFFER
    private var outputBuffer = EMPTY_BUFFER

    private var inputEnded: Boolean = false

    private lateinit var srcBuffer: ByteBuffer
    private var srcBufferPosition = 0
    private val tempByteArray = ByteArray(SAMPLE_SIZE * 2)

    private var audioTrackBufferSize = 0

    private val src = FloatArray(SAMPLE_SIZE)
    private val dst = FloatArray(SAMPLE_SIZE + 2)

    private fun processFFT(buffer: ByteBuffer) {
        srcBuffer.put(buffer.array())
        srcBufferPosition += buffer.array().size
        // Since this is PCM 16 bit, each sample will be 2 bytes.
        // So to get the sample size in the end, we need to take twice as many bytes off the buffer
        val bytesToProcess = SAMPLE_SIZE * 2
        var currentByte: Byte? = null
        while (srcBufferPosition > audioTrackBufferSize) {
            srcBuffer.position(0)
            srcBuffer.get(tempByteArray, 0, bytesToProcess)

            tempByteArray.forEachIndexed { index, byte ->
                if (currentByte == null) {
                    currentByte = byte
                } else {
                    src[index / 2] =
                        (currentByte!!.toFloat() * Byte.MAX_VALUE + byte) / (Byte.MAX_VALUE * Byte.MAX_VALUE)
                    dst[index / 2] = 0f
                    currentByte = null
                }

            }
            srcBuffer.position(bytesToProcess)
            srcBuffer.compact()
            srcBufferPosition -= bytesToProcess
            srcBuffer.position(srcBufferPosition)
            val fft = noise?.fft(src, dst)!!
            //listener?.onFFTReady(inputAudioFormat.sampleRate, inputAudioFormat.channelCount, fft)
        }
    }

}