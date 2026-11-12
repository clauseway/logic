// ABOUTME: The structural containers the unifier decomposes: LList (head/tail cons
// ABOUTME: cells) and LTree (value plus an LList of children), with their constructors.

/**
 * The structured values the unifier takes apart member by member.
 * {@link org.clauseway.logic.unification.structures.LList} is a cons cell of a
 * head term and a tail term, so a list may end in a variable (an improper
 * list); it also holds the relational list goals ({@code map},
 * {@code zipReduce}, {@code foldLeft}, {@code foldRight}) and a
 * {@code Collector} into a list.
 * {@link org.clauseway.logic.unification.structures.LTree} is a value term
 * with an {@code LList} of child trees. Both are held inside an
 * {@code org.clauseway.logic.unification.terms.LVal}; their static factories
 * return {@code Unifiable} so they can be unified directly. The empty list and
 * the empty tree are equality atoms, not structure.
 * {@link org.clauseway.logic.unification.structures.Structural} is the
 * contract a user's own compound value signs to be taken apart the same
 * way: its kind is its class, its members are in a fixed order, and only
 * a signed value is structure — a foreign value stays an equality atom.
 * {@code org.clauseway.logic.unification.MiniKanren#members} is where the
 * unifier, walk and reification recognize these two shapes alongside
 * tuples; the pattern-matching goals over lists live in
 * {@code org.clauseway.logic.goals.Matche}.
 */
package org.clauseway.logic.unification.structures;
