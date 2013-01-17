package berlin.reiche.securitas.util;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable<T extends Observable<T>> {

	private final List<Observer<T>> observers;

	public Observable() {
		observers = new ArrayList<Observer<T>>();
	}
	

	public synchronized void addObserver(Observer<T> observer) {
		observers.add(observer);
	}

	public synchronized void removeObserver(Observer<T> observer) {
		observers.remove(observer);
	}

	public synchronized void notifyObservers(T subject) {
		for (Observer<T> observer : observers) {
			observer.update(subject);
		}
	}
}
