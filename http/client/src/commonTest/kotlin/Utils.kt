import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.Url


fun MockEngine.getRequestCount(filterUrl: Url) =
    requestHistory.filter { it.url == filterUrl }.size
