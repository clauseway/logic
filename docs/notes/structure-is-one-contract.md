# Structure is one contract — constructor, members, rebuild — and a user's compound value can sign it

- **status**: hunch (October 2026, the human's observation on the fourth
  instance landing)
- **evidence held**: derivation — four structure kinds (tuple, `LList`,
  `LTree`, `LOption`) are each wired by hand in the same three places
  (`MiniKanren.decompose`, `MiniKanren.mapStructure`, `Matche`'s per-kind
  arm builders) with the same shape every time, and the unifier's
  `unifyDecomposed` already treats a kind generically (same kind, then
  members pairwise) except for one laziness special case.
- **imports**: Term / Unifiable / Reified, reify (glossary); method.md §8
  (run the loop backwards: the structure closed with the fourth instance,
  the missing operation is "a fifth, not written by us"). ⋯proposed
  term: **structural contract** — the phrase `MiniKanren`'s javadoc
  already uses for how tuples opt in; needs ratifying before any doc
  leans on it.
- **obligations**: (1) name the contract as an interface the engine owns:
  a value's KIND (its constructor's identity; a nullary constructor is an
  equality atom, as the empty list and `none()` are today), its MEMBERS
  in order (fixed arity per constructor, every member a `Term`), and
  REBUILD from mapped members; route `decompose`, `mapStructure` and
  `namesIn` through it; the four kinds become its instances; laws pinned —
  `rebuild(members(x)) ≡ x`, kind mismatch fails, arity mismatch fails,
  `StructuralClassesTest`'s pin that a foreign `Iterable` is an atom
  stays exactly as it is (signing is explicit, never inferred). (2) One
  generic `Matche` arm builder from a constructor — the arm mints fresh
  members, unifies the scrutinee with the rebuilt shape, hands the
  members to the body — with `llist`/`loption` as its named instances.
  (3) A user instance as the receipt: a domain value object (a cargo
  `Leg`, say) signed and unified member-wise from a test, without the
  engine knowing its class. (4) The boundary stated in pldb: a signed
  value is structure in LOGIC, not in STORAGE — a structured cell is
  still the first-normal-form refusal at the wire face, and `LOption` is
  the one kind that ships a codec because a NULL is the one structure SQL
  already has. Status upgrades to argued at (1) with laws green, to
  receipted at (3).
- **links**: absence-is-a-constructor.md (the fourth instance, and the
  one with a storage story), list-constraints.md (what a kind may want
  beyond unification — a store — which this note does not touch),
  `StructuralClassesTest` (the atom pin that must survive).

The engine recognises structure in exactly one place, by design
(`decompose`: "the ONE place that knows what structure is"), but that
place is a list of kinds, and each kind is a branch written three times:
once to take a value apart, once to put it back together under a mapper,
once to give `matche` an arm. The fourth kind cost the same three
branches as the third. What the four have in common is the whole of what
unification needs from a compound value — which constructor it is, what
its members are, and how to rebuild it from members that have been walked
— and nothing else: the unifier already works on that shape alone,
`namesIn` already walks members alone, reification already rebuilds
alone. The abstraction is not a hope; it is the shape the code already
has, minus the copying.

What it buys: a compound value from outside the engine — a domain value
object, a record, a user's own sum type — unifies member-wise by signing
the contract, with `matche` arms for free, and the engine never learns
its class. The next kind costs an instance, not three branches. And the
opt-in stays a signature, so the rule that made tuples structural and
left every `Iterable` an atom is kept, not loosened: structure is
granted, never inferred from shape.

What it does NOT buy: storage. A value that unifies member-wise is not
thereby a cell; pldb's wire face refuses a structural value in a column
as a first-normal-form violation and keeps doing so — the relational
spelling of a compound is a child relation. It does not buy constraint
awareness either: a kind that wants propagation (a list wanting
`lengtho` as a store) is list-constraints.md's problem, above this one.

Cheapest way to kill it: the one special case. `unifyDecomposed` defers
the first member of a list or tree (`mdefer`) so that deep spines stay
iterative; the generic path has no per-kind knowledge to decide what to
defer. If keeping spines iterative turns out to need the contract to
mark a member as the lazy one, the interface grows a notion it would
rather not have, and the saving is three branches against one new
concept. Measure that on a deep `LList` before extracting.
