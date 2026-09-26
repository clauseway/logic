package org.clauseway.logic.goals;

import org.clauseway.logic.unification.MiniKanren;
import org.clauseway.logic.unification.terms.Name;
import org.clauseway.logic.unification.Substitutions;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.vavr.collection.HashMap;
import org.clauseway.vavr.collection.LinkedHashMap;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
public class Knowledge {

	Substitutions substitutions;

	LinkedHashMap<Class<? extends Packaged>, Packaged> stores;

	public static Knowledge empty() {
		return Knowledge.of(Substitutions.empty(), LinkedHashMap.empty());
	}

	public static Knowledge of(HashMap<Name<?>, Term<?>> substitutions,
			LinkedHashMap<Class<? extends Packaged>, Packaged> stores) {
		return Knowledge.of(Substitutions.of(substitutions.toJavaMap()), stores);
	}

	public Knowledge withSubstitutions(Substitutions s) {
		return Knowledge.of(s, stores);
	}

	/** Renders a value for a trace label — a {@link Term} is deep-walked to its current bindings. */
	public String format(Object o) {
		return MiniKanren.format(substitutions, o);
	}

	public <T> Term<T> walk(Term<T> v) {
		return substitutions.walk(v);
	}

	/** The substitution factor — see {@link Substitutions}. */
	public Substitutions substitution() {
		return substitutions;
	}

	public long size() {
		return substitutions.size();
	}

	public Knowledge withStore(Packaged empty) {
		if (stores.get(empty.getClass()).isDefined()) {
			return this;
		} else {
			return Knowledge.of(substitutions, stores.put(empty.getClass(), empty));
		}
	}

	public Knowledge putStore(Packaged store) {
		return Knowledge.of(substitutions, stores.put(store.getClass(), store));
	}

	/** The keyed put — for entries whose key is not their own class. */
	public Knowledge putStore(Class<? extends Packaged> key, Packaged store) {
		return Knowledge.of(substitutions, stores.put(key, store));
	}

	public Knowledge withoutStore(Class<? extends Packaged> cls) {
		return Knowledge.of(substitutions, stores.remove(cls));
	}

	/** The payload registered under {@code cls}; throws when absent. */
	@SuppressWarnings("unchecked")
	public <T extends Packaged> T getStore(Class<T> cls) {
		return (T) stores.get(cls)
				.getOrElseThrow(() -> new IllegalStateException(
						"No store associated with package"));
	}

	/** Applies {@code f} to the payload registered under {@code cls}; unchanged when absent. */
	@SuppressWarnings("unchecked")
	public <T extends Packaged> Knowledge updateStore(Class<T> cls, UnaryOperator<T> f) {
		return stores.get(cls)
				.map(s -> (Packaged) f.apply((T) s))
				.map(s -> Knowledge.of(substitutions, stores.put(cls, s)))
				.getOrElse(this);
	}
}
