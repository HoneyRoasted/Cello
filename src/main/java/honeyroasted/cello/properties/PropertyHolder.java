package honeyroasted.cello.properties;

public interface PropertyHolder {

    Properties properties();

    <T> T withProperties(Properties properties);

}
