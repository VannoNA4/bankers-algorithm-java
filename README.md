
# Banker's Algorithm in Java

A Java implementation of the Banker's Algorithm for deadlock avoidance in operating systems. The program checks whether a system is in a safe state and determines if resource requests can be granted safely.

## Features
- Calculates remaining resource needs and identifies safe execution sequences.
- Validates resource requests against available resources and process needs.
- Simulates allocations and rolls back changes if the resulting state is unsafe.
- Prints step-by-step resource allocation decisions.
- Includes input validation and assertion-based tests.

## Running the Program
Requires Java 17 or newer. No external dependencies.

bash
javac BankersAlgorithm.java Main.java
java Main


## Example Output
The program displays resource requirements, safe execution sequences, and explanations for denied requests.

text
TEST 1: Baseline Safety (S0)
Process  Need(A,B,C)
P1       7,4,3
P2       1,2,2
P3       6,0,0
P4       0,1,1
P5       4,3,1

VERDICT: SAFE, sequence <P2,P4,P1,P3,P5>

TEST 2c: P5 requests <3,3,0>
Need after pretend-allocation: P5 <1,0,1>
No process can proceed at Work <0,0,2>:
  P1 short A,B,C | P2 short A,B | P3 short A | P4 short B | P5 short A
VERDICT: DENIED, resulting state is UNSAFE

Output shortened for readability.

## Testing
The program includes five assertion-based test scenarios:
- Baseline system safety and safe execution sequence verification.
- Requests exceeding a process's remaining need.
- Requests exceeding available resources.
- Requests that would create an unsafe state.
- Valid requests that preserve system safety.

The tests also verify that denied requests leave the original resource allocation state unchanged.

## How It Works
The algorithm tracks allocated resources, maximum demands, remaining needs, and available resources. When a process requests additional resources, the program temporarily applies the allocation and checks whether all processes can still finish. If the resulting state is unsafe, the allocation is reversed.

Built as part of my Operating Systems coursework at the University of Missouri–Kansas City (UMKC).

