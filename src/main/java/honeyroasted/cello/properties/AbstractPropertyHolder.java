package honeyroasted.cello.properties;

public abstract class AbstractPropertyHolder implements PropertyHolder {
    private Properties properties = new Properties();

    @Override
    public Properties properties() {
        return this.properties;
    }

}
