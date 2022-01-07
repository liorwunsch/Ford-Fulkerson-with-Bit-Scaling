# Ford-Fulkerson with Bit-Scaling
Gabow's Bit-Scaling Method is an algorithm whose form of problem solving it offers is by initially considering only the most significant bit of each input value (such as edge capacity) in its representation as a binary number.
It then expands the initial solution by looking at the two most significant bits.
The algorithm gradually looks at more and more bits according to their importance, perfecting the solution each time, until it checks all the bits and calculates a final solution.

We use this method to design an algorithm based on Ford-Fulkerson to solve the problem of finding the maximum flow value in a flow network.
The algorithm is designed to improve the runtime of the Ford-Fulkerson algorithm for certain inputs.
In addition, we use the method to solve a problem of task assignment in the processor.