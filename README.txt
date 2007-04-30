This is the distribution of the Korat testing tool version 1.0
released in April 2007.

KORAT
=====

Korat is a tool for constraint-based generation of structurally
complex test inputs for Java programs.  Structurally complex means
that the inputs are structural (e.g., represented with linked data
structures) and must satisfy complex constraints that relate parts of
the structure (e.g., invariants for linked data structures).

Korat requires (1) an imperative predicate that specifies the desired
structural constraints and (2) a finitization that bounds the desired
test input size.  Korat generates all predicate inputs (within the
bounds) for which the predicate returns true.  To do so, Korat
performs a systematic search of the predicate's input space.  The
inputs that Korat generates enable bounded-exhaustive testing for
programs ranging from library classes to stand-alone applications.

More info about Korat is available at http://mir.cs.uiuc.edu/korat.

INSTALLATION
============

You can obtain Korat as source code or a prepackaged korat.jar binary.
You can build the korat.jar binary from the source code using the
build.xml Ant script.

EXAMPLES
========

To run some Korat examples, you need to add to the Java classpath
either a korat.jar binary, e.g.,
  export KORAT_CP=$DOWNLOAD_DIRECTORY/korat.jar
or the Korat classes and all the jar files from the library, e.g.,
  export KORAT_CP=$KORAT_HOME/build:$KORAT_HOME/lib/javassist.jar:...

You can then run some examples such as these:

  # Generate binary trees with 3 nodes; should generate 5
  java -classpath $KORAT_CP korat.Korat \
    --class korat.examples.binarytree.BinaryTree --args 3
    
  # Generate binary search trees with 3 nodes; should generate 5
  java -classpath $KORAT_CP korat.Korat \
    --class korat.examples.searchtree.SearchTree --args 3

  # Generate singly linked lists of size 3; should generate 5
  java -classpath $KORAT_CP korat.Korat \
    --class korat.examples.singlylinkedlist.SinglyLinkedList --args 3

  # Generate doubly linked lists of size 3; should generate 10
  java -classpath $KORAT_CP korat.Korat \  
    --class korat.examples.doublylinkedlist.DoublyLinkedList --args 3

  # Generate red-black trees with 7 nodes; should generate 35
  java -classpath $KORAT_CP korat.Korat \
    --class korat.examples.redblacktree.RedBlackTree --args 7

  # Generate heaps (priority queues) represented with arrays
  java -classpath $KORAT_CP korat.Korat \
    --class korat.examples.heaparray.HeapArray --args 7
    
INSTRUCTIONS
============

Until we write a user's manual, the best ways to learn how to use
Korat are to look at the examples in the korat.examples.* packages
or to read some paper on Korat from <http://mir.cs.uiuc.edu/korat>.
You should also feel free to contact the Korat team (see below).

VISUALIZATION
=============

Korat can graphically show the structures it generates.  The
visualization in Korat was inspired by Alloy <http://alloy.mit.edu>,
and our current Korat implementation uses the Alloy Analyzer's
visualization facility, which provides a fully customizable display
that allows users to specify desired views on the underlying
structures.  Korat automatically translates object graphs into the
Alloy representation.

To use visualization in Korat, you will need to manually install the
AT&T GraphViz program <http://www.graphviz.org>.

To instruct Korat to visualize the generated structures, you need to
add switch "--visualize" to the command-line arguments such as these:

  java -classpath $KORAT_CP korat.Korat \
    --visualize --class korat.examples.binarytree.BinaryTree --args 3
  
  java -classpath $KORAT_CP korat.Korat \
    --visualize \
    --class korat.examples.singlylinkedlist.SinglyLinkedList --args 3
  
  java -classpath $KORAT_CP korat.Korat \
    --visualize --class korat.examples.fibheap.FibonacciHeap --args 2

LICENSE
=======

The source code of Korat is distributed under the GNU General Public
License version 2 (see LICENSE.txt).  Korat also uses several
third-party packages whose code may be distributed under different
licenses (see the appropriate LICENSE files in the lib directory for
details).

CONTACT
=======

To contact the Korat team, see http://mir.cs.uiuc.edu/korat.
