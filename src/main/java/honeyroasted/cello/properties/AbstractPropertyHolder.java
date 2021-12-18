package honeyroasted.cello.properties;

public abstract class AbstractPropertyHolder implements PropertyHolder {
    private Properties properties;

    public AbstractPropertyHolder(Properties properties) {
        this.properties = properties;
    }

    public AbstractPropertyHolder() {
        this(new Properties());
    }

    @Override
    public Properties properties() {
        return this.properties;
    }

    @Override
    public <T> T withProperties(Properties properties) {
        this.properties = properties;
        return (T) this;
    }

}
