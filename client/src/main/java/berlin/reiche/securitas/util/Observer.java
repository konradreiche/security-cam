package berlin.reiche.securitas.util;

/**
 * A type-safe interface for the Observer pattern.
 * 
 * @author Konrad Reiche
 * 
 * @param <T>
 *            Type of the subject being observed.
 */
public interface Observer<T extends Observable<T>> {

	void update(T subject);

}
