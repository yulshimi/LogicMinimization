# LogicMinimization
doQuineMcCluskey, doTabulationMethod, and helper functions for them in BooleanExpression.java have been implemented by Phillip Jo.
Implemented the Tabulation Method and the Quine McCluskey algorithm to find out unique prime implicants in Java.
Implemented the Branch and Bound algorithm to resolve cyclic core.
For the Tabulation Method, two bits are used to represent the state of each variable in an implicant.
For the Quine McCluskey algorithm, each prime implicant is represented as an array of long integer. Each bit in a long integer represents each minterm. 1 at some index position means that implicant covers a minterm corresponding to that index position and vice versa.
