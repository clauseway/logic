package org.clauseway.logic.unification.terms;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import java.util.Optional;

/**
 * @author TGa
 */

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LVal<T> implements Unifiable<T>, Reified<T> {
	T value;

	/** A null is not a value; absence is a term, {@code LOption.none()}. */
	public static <T> Unifiable<T> lval(T v) {
		if (v == null) {
			throw new IllegalArgumentException("lval(null): a null is not a value — absence is LOption.none()");
		}
		return new LVal<>(v);
	}

	@Override
	public String toString() {
		return "{" + value + '}';
	}

	@Override
	public Optional<T> asVal() {
		return Optional.of(value);
	}

	@Override
	public boolean isVal() {
		return true;
	}
}
