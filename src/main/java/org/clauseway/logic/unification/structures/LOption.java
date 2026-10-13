package org.clauseway.logic.unification.structures;

// ABOUTME: The engine's optional: a two-constructor structure, some(value) and
// ABOUTME: none(), unified natively — absence is a term, never a null.

import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.clauseway.logic.unification.terms.LVal;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;

@Value
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public class LOption<T> {
	private static final Unifiable<?> NONE = LVal.lval(new LOption<>(null));
	Term<T> value;

	public static <T> Unifiable<LOption<T>> some(Term<T> value) {
		return LVal.lval(new LOption<>(value));
	}

	@SuppressWarnings("unchecked")
	public static <T> Unifiable<LOption<T>> none() {
		return (Unifiable<LOption<T>>) NONE;
	}

	public boolean isNone() {
		return this == NONE.get();
	}

	/** The ground option as Java's own; refuses while the value is still a hole. */
	public Optional<T> toOptional() {
		if (isNone()) {
			return Optional.empty();
		}
		if (!value.isVal()) {
			throw new IllegalStateException("toOptional over " + this + ": the value is unbound");
		}
		return Optional.of(value.get());
	}

	@Override
	public String toString() {
		return isNone() ? "None" : "Some(" + value + ")";
	}
}
