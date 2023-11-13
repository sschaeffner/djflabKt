package xyz.schaeffner.djflab

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import xyz.schaeffner.djflab.snapcast.Response
import xyz.schaeffner.djflab.web.Notification
import xyz.schaeffner.djflab.snapcast.Status

class MapperTest {
    private val json = Json {
        prettyPrint = true
        isLenient = false
        encodeDefaults = true
    }

    @Test
    fun `when mapping to Notification then all rooms are present`() {
        // given
        val statusJson = "{\"id\":882713855,\"jsonrpc\":\"2.0\",\"result\":{\"server\":{\"groups\":[{\"clients\":[{\"config\":{\"instance\":1,\"latency\":0,\"name\":\"\",\"volume\":{\"muted\":false,\"percent\":87}},\"connected\":true,\"host\":{\"arch\":\"aarch64\",\"ip\":\"10.89.0.3\",\"mac\":\"9a:dc:ed:1f:ad:08\",\"name\":\"6d005283db1d\",\"os\":\"Debian GNU/Linux 12 (bookworm)\"},\"id\":\"9a:dc:ed:1f:ad:08\",\"lastSeen\":{\"sec\":1699907484,\"usec\":237795},\"snapclient\":{\"name\":\"Snapclient\",\"protocolVersion\":2,\"version\":\"0.27.0\"}}],\"id\":\"bea21449-7e98-bcd5-34c0-147bdfd56e3d\",\"muted\":false,\"name\":\"\",\"stream_id\":\"src1\"},{\"clients\":[{\"config\":{\"instance\":1,\"latency\":0,\"name\":\"\",\"volume\":{\"muted\":false,\"percent\":100}},\"connected\":true,\"host\":{\"arch\":\"aarch64\",\"ip\":\"10.89.0.4\",\"mac\":\"e2:6f:8b:05:0b:3f\",\"name\":\"4507ed51d032\",\"os\":\"Debian GNU/Linux 12 (bookworm)\"},\"id\":\"e2:6f:8b:05:0b:3f\",\"lastSeen\":{\"sec\":1699907484,\"usec\":237865},\"snapclient\":{\"name\":\"Snapclient\",\"protocolVersion\":2,\"version\":\"0.27.0\"}}],\"id\":\"839d9632-3e59-e643-98ef-9c6321cf5600\",\"muted\":false,\"name\":\"\",\"stream_id\":\"src1\"}],\"server\":{\"host\":{\"arch\":\"aarch64\",\"ip\":\"\",\"mac\":\"\",\"name\":\"a1e70a58f127\",\"os\":\"Debian GNU/Linux 12 (bookworm)\"},\"snapserver\":{\"controlProtocolVersion\":1,\"name\":\"Snapserver\",\"protocolVersion\":1,\"version\":\"0.27.0\"}},\"streams\":[{\"id\":\"src1\",\"properties\":{\"canControl\":false,\"canGoNext\":false,\"canGoPrevious\":false,\"canPause\":false,\"canPlay\":false,\"canSeek\":false},\"status\":\"idle\",\"uri\":{\"fragment\":\"\",\"host\":\"\",\"path\":\"/tmp/snapfifo1\",\"query\":{\"chunk_ms\":\"20\",\"codec\":\"flac\",\"name\":\"src1\",\"sampleformat\":\"48000:16:2\"},\"raw\":\"pipe:////tmp/snapfifo1?chunk_ms=20&codec=flac&name=src1&sampleformat=48000:16:2\",\"scheme\":\"pipe\"}},{\"id\":\"src2\",\"properties\":{\"canControl\":false,\"canGoNext\":false,\"canGoPrevious\":false,\"canPause\":false,\"canPlay\":false,\"canSeek\":false},\"status\":\"idle\",\"uri\":{\"fragment\":\"\",\"host\":\"\",\"path\":\"/tmp/snapfifo2\",\"query\":{\"chunk_ms\":\"20\",\"codec\":\"flac\",\"name\":\"src2\",\"sampleformat\":\"48000:16:2\"},\"raw\":\"pipe:////tmp/snapfifo2?chunk_ms=20&codec=flac&name=src2&sampleformat=48000:16:2\",\"scheme\":\"pipe\"}},{\"id\":\"src3\",\"properties\":{\"canControl\":false,\"canGoNext\":false,\"canGoPrevious\":false,\"canPause\":false,\"canPlay\":false,\"canSeek\":false},\"status\":\"idle\",\"uri\":{\"fragment\":\"\",\"host\":\"\",\"path\":\"/tmp/snapfifo3\",\"query\":{\"chunk_ms\":\"20\",\"codec\":\"flac\",\"name\":\"src3\",\"sampleformat\":\"48000:16:2\"},\"raw\":\"pipe:////tmp/snapfifo3?chunk_ms=20&codec=flac&name=src3&sampleformat=48000:16:2\",\"scheme\":\"pipe\"}}]}}}"
        val status = json.decodeFromString<Response<Status>>(statusJson).result
        val server = status!!.server

        // when
        val notification = Notification.from(server)

        // then
        assertEquals(notification.rooms.size, 2)
    }
}