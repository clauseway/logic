package org.clauseway.logic.unification;

// ABOUTME: A null is not a value: lval(null) refuses at construction, so no
// ABOUTME: term in the engine ever carries one — absence is LOption.none().

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.clauseway.logic.unification.terms.LVal.lval;

import org.junit.Test;

public class NullPayloadTest {

	@Test
	public void aNullPayloadRefuses() {
		assertThatThrownBy(() -> lval((String) null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("LOption.none()");
	}
}
