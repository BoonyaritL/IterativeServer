import java.util.function.*;
import java.util.Optional;

public class Try<T> {
    private final T value;
    private final Exception exception;
    
    private Try(T value, Exception exception) {
        this.value = value;
        this.exception = exception;
    }
    
    public static <T> Try<T> of(Supplier<T> supplier) {
        try {
            return new Try<>(supplier.get(), null);
        } catch (Exception e) {
            return new Try<>(null, e);
        }
    }
    
    public static <T> Try<T> of(CheckedSupplier<T> supplier) {
        try {
            return new Try<>(supplier.get(), null);
        } catch (Exception e) {
            return new Try<>(null, e);
        }
    }
    
    public static <T> Try<T> ofChecked(CheckedSupplier<T> supplier) {
        return of(supplier);
    }
    
    public static Try<Void> ofRunnable(CheckedRunnable runnable) {
        try {
            runnable.run();
            return new Try<>(null, null);
        } catch (Exception e) {
            return new Try<>(null, e);
        }
    }
    
    public Optional<Void> toOptionalVoid() {
        return isSuccess() ? Optional.empty() : Optional.empty();
    }
    
    public boolean isSuccess() {
        return exception == null;
    }
    
    public boolean isFailure() {
        return exception != null;
    }
    
    public Optional<T> toOptional() {
        return Optional.ofNullable(value).filter(v -> isSuccess());
    }
    
    public T getOrElse(T defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }
    
    public T getOrElse(Supplier<T> supplier) {
        return Optional.ofNullable(value).orElseGet(supplier);
    }
    
    public <R> Try<R> map(Function<T, R> mapper) {
        return Optional.ofNullable(value)
            .filter(v -> isSuccess())
            .map(mapper)
            .map(result -> new Try<R>(result, null))
            .orElseGet(() -> new Try<>(null, exception));
    }
    
    public <R> Try<R> flatMap(Function<T, Try<R>> mapper) {
        return Optional.ofNullable(value)
            .filter(v -> isSuccess())
            .map(mapper)
            .orElseGet(() -> new Try<>(null, exception));
    }
    
    public Try<T> filter(Predicate<T> predicate) {
        return Optional.ofNullable(value)
            .filter(v -> isSuccess() && predicate.test(v))
            .map(v -> this)
            .orElseGet(() -> new Try<>(null, new RuntimeException("Filter condition failed")));
    }
    
    public Try<T> recover(Function<Exception, T> recoveryFunction) {
        return Optional.ofNullable(exception)
            .map(recoveryFunction)
            .map(recoveredValue -> new Try<T>(recoveredValue, null))
            .orElseGet(() -> this);
    }
    
    public Try<T> recoverWith(Function<Exception, Try<T>> recoveryFunction) {
        return Optional.ofNullable(exception)
            .map(recoveryFunction)
            .orElseGet(() -> this);
    }
    
    public void onSuccess(Consumer<T> consumer) {
        Optional.ofNullable(value).filter(v -> isSuccess()).ifPresent(consumer);
    }
    
    public void onFailure(Consumer<Exception> consumer) {
        Optional.ofNullable(exception).ifPresent(consumer);
    }
    
    public void forEach(Consumer<T> consumer) {
        onSuccess(consumer);
    }
    
    @Override
    public String toString() {
        return Optional.ofNullable(value)
            .filter(v -> isSuccess())
            .map(v -> "Success(" + v + ")")
            .orElseGet(() -> "Failure(" + exception.getMessage() + ")");
    }
    
    @Override
    public boolean equals(Object obj) {
        return Optional.ofNullable(obj)
            .filter(o -> this == o)
            .map(o -> true)
            .orElseGet(() -> 
                Optional.ofNullable(obj)
                    .filter(o -> o != null && getClass() == o.getClass())
                    .map(o -> (Try<?>) o)
                    .map(tryObj -> {
                        boolean bothSuccess = isSuccess() && tryObj.isSuccess();
                        boolean bothFailure = isFailure() && tryObj.isFailure();
                        
                        return bothSuccess ? 
                            Optional.ofNullable(value)
                                .map(v -> value != null ? value.equals(tryObj.value) : tryObj.value == null)
                                .orElse(false) :
                            bothFailure ?
                                Optional.ofNullable(exception)
                                    .map(e -> exception != null ? exception.equals(tryObj.exception) : tryObj.exception == null)
                                    .orElse(false) :
                                false;
                    })
                    .orElse(false)
            );
    }
    
    @Override
    public int hashCode() {
        return Optional.ofNullable(value)
            .filter(v -> isSuccess())
            .map(v -> v.hashCode())
            .orElseGet(() -> 
                Optional.ofNullable(exception)
                    .map(e -> e.hashCode())
                    .orElse(0)
            );
    }
    
    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
    
    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }
}
