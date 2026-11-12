package d021248.mytry.core.lang.fp.functions.silentinvoker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CallUncheckedTest {

  /// Minimal test class that implements the mixin.
  private static class TestService implements CallUnchecked {

    /// Parses URLs, using callUnchecked to suppress checked exceptions.
    List<URL> parseUrls(List<String> urls) {
      return urls.stream().map(u -> callUnchecked(() -> URI.create(u).toURL())).toList();
    }

    /// Forces a checked exception to be thrown silently.
    String failingMethod() {
      return callUnchecked(
          () -> {
            throw new IOException("forced");
          });
    }
  }

  private final TestService service = new TestService();

  @Test
  @DisplayName("callUnchecked returns the result of a successful Callable")
  void callUncheckedReturnsSuccessfulResult() {
    String result = service.callUnchecked(() -> "hello");
    assertThat(result).isEqualTo("hello");
  }

  @Test
  @DisplayName("callUnchecked returns an integer from a Callable<Integer>")
  void callUncheckedReturnsInteger() {
    Integer result = service.callUnchecked(() -> 42);
    assertThat(result).isEqualTo(42);
  }

  @Test
  @DisplayName("callUnchecked throws the original checked exception without wrapping")
  void callUncheckedWrapsCheckedException() {
    // SilentInvoker uses type erasure to throw checked exceptions unchecked (no
    // wrapper)
    assertThatThrownBy(service::failingMethod)
        .isInstanceOf(Exception.class)
        .hasMessageContaining("forced");
  }

  @Test
  @DisplayName("callUnchecked can parse valid URLs without declaring checked exceptions")
  void callUncheckedParsesUrls() {
    List<java.net.URL> urls = service.parseUrls(List.of("https://example.com", "https://test.org"));
    assertThat(urls).hasSize(2);
  }

  @Test
  @DisplayName("callUnchecked propagates RuntimeException from Callable directly")
  void callUncheckedPropagatesRuntimeException() {
    assertThatThrownBy(
            () ->
                service.callUnchecked(
                    () -> {
                      throw new IllegalStateException("rt");
                    }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("rt");
  }
}
