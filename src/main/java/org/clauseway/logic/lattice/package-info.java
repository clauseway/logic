// ABOUTME: The generic lattice store: a factor over per-variable values of a
// ABOUTME: component lattice, plus the propagator kernel it administers.

/**
 * The store behaviour shared by every lattice-valued constraint family.
 * {@link org.clauseway.logic.lattice.LatticeFactor} is the abstract
 * {@code Factor} whose theory maps variable names to values of a component
 * lattice {@code L}; {@link org.clauseway.logic.lattice.Domain} is the
 * capability record such a value must implement (meet, order, membership,
 * collapse). {@link org.clauseway.logic.lattice.Imposition} is the atom that
 * narrows one target to a value. Propagators are the parked units the factor
 * wakes on bindings: {@link org.clauseway.logic.lattice.Propagator} answers a
 * {@link org.clauseway.logic.lattice.Verdict}, whose update form yields an
 * {@link org.clauseway.logic.lattice.Update};
 * {@link org.clauseway.logic.lattice.ParkingPropagator} is the variant whose
 * examination is itself a fiber. The concrete instance is
 * {@code org.clauseway.logic.finitedomain.FiniteDomainConstraints}; the driver
 * that triggers the factor lives in {@code org.clauseway.logic.constraints}.
 */
package org.clauseway.logic.lattice;
