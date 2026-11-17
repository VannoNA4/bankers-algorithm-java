# Banker's Algorithm in Java

A Java implementation of the Banker's Algorithm for deadlock avoidance in operating systems. The program checks whether a system is in a safe state and determines if resource requests can be granted without putting the system at risk.

## Features
- Calculates remaining resource needs and identifies safe execution sequences.
- Validates resource requests against available resources and process needs.
- Simulates allocations and rolls back changes if the resulting state is unsafe.
- Prints step-by-step resource allocation decisions.
- Includes assertion-based tests for safe and unsafe scenarios.

## Running the Program
Requires Java 17 or newer. No external dependencies.

```bash
javac BankersAlgorithm.java Main.java
java Main
```

## Testing
The program includes five test scenarios covering safe-state detection, requests exceeding resource limits, unsafe allocations, and successful resource requests.

Example output:
```text
TEST 1: Baseline Safety (S0)
VERDICT: SAFE, sequence <P2,P4,P1,P3,P5>

TEST 2a: Request exceeds Need
VERDICT: DENIED

TEST 2b: Request exceeds Available
VERDICT: DENIED

TEST 2c: Unsafe allocation
VERDICT: DENIED

TEST 3: Valid resource request
VERDICT: GRANTED, sequence <P2,P4,P1,P3,P5>
```

## How It Works
The algorithm tracks allocated resources, maximum demands, remaining needs, and available resources. When a process requests additional resources, the program temporarily applies the allocation and checks whether all processes can still finish. If not, the allocation is reversed.

Built with Java as part of my Operating Systems coursework at UMKC.


