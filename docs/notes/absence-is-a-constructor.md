# Absence is a constructor, not a null: `LOption<T>` as the engine's optional

- **status**: argued (October 2026, the human's direction after the cargo
  port's delivery row went in and came out again)
- **evidence held**: negative witness — `apps/cargo` ledger entries P3,
  6: a derived relation whose head carried `lval(null)` composed with
  nothing (conjunction never pruned by it, `exclude` over it always
  false, every rule above it owed a null test), and the engine's own
  `NullPayloadTest` receipts that null IS a value today; derivation —
  the four readings of an absent cell below, three of them unsound,
  non-compositional or unenforceable.
- **imports**: Term / Unifiable / Reified, reify, Row, Call (glossary);
  structural unification over the engine's own types (`LList`, `LTree`,
  tuples — `MiniKanren.mapStructure`); `Projected` and the generated
  projection rule (pldb, `Literal.projecting`). No theory import.
- **obligations**: (1) `LOption<T>` in `unification/structures` —
  `some(Term<T>)`, `none()`, `toOptional()`; `MiniKanren` walks, unifies
  and reifies it as it does `LList`; (2) `Matche.none(Supplier<Goal>)`,
  `Matche.some(Function<Unifiable<A>, Goal>)`; (3) `lval(null)` refuses
  and `LVal.asVal` loses its null case — `NullPayloadTest` becomes the
  refusal's receipt; (4) in pldb: `Property.nullable` and every
  `requireNullable` path deleted, a `Codec.optional(inner)` combinator
  mapping `None`↔NULL and `Some(v)`↔`inner`, `regionSql` emitting
  `IS NULL` / `IS NOT NULL` / `= ?` from the probe's `LOption` shape,
  `ColumnResolver.nullable` renamed to the codec's presence,
  `Literal.Builder` claiming a `Projected` one level inside an `LOption`;
  (5) `apps/cargo` as the receipt — `handlingEvent`'s voyage column
  becomes `Unifiable<LOption<String>>`, `NO_VOYAGE` goes, the three
  voyage denials become one `matche`. Status upgrades to receipted at
  (1)–(3) green, verified at (5).
- **links**: `../../pldb/docs/design/external-relations.md` (the backend
  contract this changes), `../../apps/cargo/NOTES.md` entries P3 and 6
  (the witness), `list-constraints.md` (the other structure note).

A cell that may be absent has four possible readings, and only one of
them both composes and can be enforced.

**Null as a value** (built today): `lval(null)` equals itself and nothing
else. Sound and closed-world, but absence has become data: a rule holding
a nullable column cannot be used as a filter by existence, `exclude` over
it is always false, a comparison on it is undefined, and every rule above
it has to test for the null it cannot see in any signature. Fifty rules
up nobody remembers the column was nullable; the failure is a wrong
answer or an exception far from the declaration. The SQL side has the
same hole in its own idiom (three-valued logic drops the NULL row from
both sides of a comparison — `SqlCompilerLawsTest`'s receipt), so the
compilers carry guards that exist only to agree with a sentinel.

**Null as an unbound variable**: a NULL cell leaves the probe's variable
free. Unsound — a free variable in an answer means "for all", a NULL means
"some particular value I do not know"; the nameless row proves that
someone is called Ada, `exclude` over it fails where it should hold, a
free column's support domain goes infinite, and an FD comparison on it
either suspends or passes. Rejected on the derivation alone.

**Null as no row — Codd's answer, unbound as failure**: a relation with a
nullable column is really two, `person(id)` and `personName(id, name)`,
and the NULL row is in the first only. Spelled on one relation: a NULL
cell fails any probe that names the column — by value or by variable —
and satisfies only a probe that projects it away. Sound and fully
compositional (`exclude` is an honest ¬∃, a comparison never meets an
absent cell, a support domain never holds one), and it closes the SQL
hole the same way the constructor does. Rejected for two reasons. First,
it cannot be enforced: the don't-care `lvar()` that silently filters is
indistinguishable, at construction and at solve, from a variable a
sibling conjunct will bind later — a logic program has no "later" to
appeal to — so the safe spelling needs a family of probe markers
(`present(x)`, `present()`, `projected()`) that the author must remember
at every probe of the column. Second, the knowledge does not travel: a
rule over the column has a plain `Unifiable<T>` head, and nothing fifty
rules up says the value it holds came from a column that might not have
one. Both are the constructor's gains — the type is the marker, and it
propagates.

**Absence as a constructor**: the column's type is
`Unifiable<LOption<T>>`, a two-constructor structure the engine unifies
natively, and a value that may be absent is `some(v)` or `none()`. What
this buys: the declaration is the Java type, checked by the compiler at
every signature it passes through, so propagation is free and honest in
both directions — `Unifiable<T>` means never absent; a comparison on the
column does not compile, `eta.unifies(some(t)).and(lss(t, deadline))`
does and means what SQL's `WHERE eta < ?` means; `none()` is a ground
value, so "the rows with no value" is a lookup and never a negation;
`some(projected())` is the ∃ for present-don't-care under `exclude`
through the existing projection rule; and null leaves the engine
entirely, because `None` is a term. What it does NOT buy: a derived rule
is still better written partial — holding when the thing exists — than
total over an `LOption` head; the structure makes the total form legal
and visible, not preferable. The total form has a name: it is the left
outer join of the key over its partial rules — `rule(k, v) ∧ out =
some(v)`, or `¬∃v. rule(k, v) ∧ out = none()` — which is where NULL came
from in the first place (Codd, 1979: the marker exists to give outer join
an absent cell). The constructor is that cell. An `LOption` in a head
declares the outer join; a `.nullable()` on an arg hid it in the body.

Cheapest way to kill it: a backend that cannot tell an absent cell from a
present one at decode time without a per-column declaration. pldb's
answer is the one it already gives every non-JDBC type — the codec
registry, bound through a type-checked template — so the declaration
costs one line on the SQL source and nothing in memory. If that line
turns out to be forgettable in practice, the note's claim survives but
the design owes a loud refusal at first fetch of a NULL into an
undeclared column, which is the refusal the current code already makes.
