package honeyroasted.cello.properties;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class Properties {
    private Map<String, Object> map = new LinkedHashMap<>();

    public Properties put(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    public boolean has(String key) {
        return this.map.containsKey(key);
    }

    public boolean has(String key, Class<?> type) {
        return type.isInstance(this.map.get(key));
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(this.map.get(key));
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(this.map.get(key)).filter(type::isInstance).map(k -> (T) k);
    }

    public Object require(String key) {
        return get(key).orElseThrow(() -> new NoSuchElementException("Required property '" + key + "' but it wasn't present"));
    }

    public <T> T require(String key, Class<T> type) {
        return get(key, type).orElseThrow(() -> new NoSuchElementException("Required property '" + key + "' of type " + type.getName() + " but it wasn't present"));
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(this.map.keySet());
    }

    public Collection<Object> values() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    public <T> Collection<T> values(Class<T> type) {
        return this.map.values().stream().filter(type::isInstance).map(k -> (T) k).toList();
    }
}
