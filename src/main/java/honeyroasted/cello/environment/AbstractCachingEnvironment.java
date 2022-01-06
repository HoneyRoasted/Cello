package honeyroasted.cello.environment;

import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCachingEnvironment implements CachingEnvironment {
    private Map<Namespace, ClassNode> cache = new HashMap<>();
    private Environment parent;

    public AbstractCachingEnvironment(Environment parent) {
        this.parent = parent;
    }

    public AbstractCachingEnvironment() {
    }

    protected abstract Verification<ClassNode> performLookup(Namespace namespace);

    protected abstract Verification<ClassNode> performArrayLookup(ClassNode element, Namespace namespace);

    @Override
    public Verification<ClassNode> lookupArray(ClassNode element) {
        Namespace namespace = element.type().namespace();
        Namespace arr = Namespace.of(namespace.packageName(), namespace.className() + "[]");

        Optional<ClassNode> opt = fromCache(arr);
        if (opt.isPresent()) {
            return Verification.success(opt.get());
        } else {
            Verification<ClassNode> lookup = performArrayLookup(element, namespace);
            if (lookup.isPresent()) {
                cache(lookup.value());
            }
            return lookup;
        }
    }

    @Override
    public Verification<ClassNode> lookup(Namespace namespace) {
        Optional<ClassNode> opt = fromCache(namespace);
        if (opt.isPresent()) {
            return Verification.success(opt.get());
        } else {
            Verification<ClassNode> lookup = performLookup(namespace);
            if (lookup.isPresent()) {
                cache(lookup.value());
            }
            return lookup;
        }
    }

    @Override
    public void cache(Namespace namespace, ClassNode node) {
        if (this.parent instanceof CachingEnvironment ce) {
            ce.cache(namespace, node);
        }  else {
            this.cache.put(namespace, node);
        }
    }

    @Override
    public void remove(ClassNode node) {
        if (this.parent instanceof CachingEnvironment ce) {
            ce.remove(node);
        } else {
            this.cache.remove(node.type().namespace());
        }
    }

    @Override
    public Optional<ClassNode> fromCache(Namespace namespace) {
        if (this.parent instanceof CachingEnvironment ce) {
            return ce.fromCache(namespace);
        } else {
            return Optional.ofNullable(this.cache.get(namespace));
        }
    }

}
