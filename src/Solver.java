import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.MinPQ;

import java.io.File;
import java.util.Iterator;

// TODO: Make prev Node in Node class final.
// Does inner Node class need to be static?

public class Solver{
    private static final boolean DEBUG = false;
    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial){
        if(initial==null)
            throw new IllegalArgumentException();
        // Create twin board
        // switch off between the two boards until goal reached.
        SolverStage initSolver = new SolverStage(initial);
        SolverStage twinSolver = new SolverStage(initial.twin());
        boolean locIsSolvable;
        SolverStage solutionStage;
        while(true){
            if(Solver.DEBUG){
                printPQContents(initSolver);
//                StdOut.printf("Manhattan Priority = %d, Hamming Priority = %d\n",initSolver.pq.min().manhat ,initSolver.pq.min().hamm);
//                StdOut.println(initSolver.pq.min().board.toString());
            }
            initSolver.next();
            if(initSolver.isGoal){
                locIsSolvable = true;
                solutionStage = initSolver;
                break;
            }

            twinSolver.next();
            if(twinSolver.isGoal){
                locIsSolvable = false;
                solutionStage = twinSolver;
                break;
            }
        }

        this.pvIsSolvable = locIsSolvable;
        if(locIsSolvable) {
            // Trace back solution and push onto stack
            Node currentNode = solutionStage.currentNode;
            Stack<Board> stackSol = new Stack<Board>();
            stackSol.push(currentNode.board);
            while(currentNode.prev != null){
                currentNode = currentNode.prev;
                stackSol.push(currentNode.board);
            }
            this.pvMoves = stackSol.size() - 1;
            pvSolution = stackSol;
        }
        else {
            this.pvMoves = -1;
            pvSolution = null;
        }
    }

    // is the initial board solvable?
    public boolean isSolvable(){return this.pvIsSolvable;}

    // min number of moves to solve initial board; -1 if unsolvable
    public int moves(){return pvMoves;}

    // sequence of boards in a shortest solution; null if unsolvable
    public Iterable<Board> solution(){return pvSolution;}

    //test client
    public static void main(String[] args){
//        boolean DEBUG = true;
        for(String fName : args){
            In in = new In(fName);
            int n = in.readInt();
            int[][] tiles = new int[n][n];
            for(int i = 0; i < n; i++)
                for(int j = 0; j < n; j++)
                    tiles[i][j] = in.readInt();

//            StdOut.println(fName);
            Board board = new Board(tiles);
            Solver solver = new Solver(board);
            StdOut.println(fName + "- # Moves: " + solver.moves());

            // Save solution to file or print to screen
            String fNameOut = fName.substring(0, fName.lastIndexOf(".")) + "_solution.txt";

            if(Solver.DEBUG) {
                Out out = new Out();
//            if(DEBUG)
//                out = new Out(fNameOut);
//            else
//            out = new Out();
                out.println("--------Start OF SOLUTION--------");
                if (solver.isSolvable()) {
                    Iterable<Board> it = solver.solution();
                    for (Board solBoard : it) {
                        out.printf("Manhattan = %d, Hamm = %d\n", solBoard.manhattan(), solBoard.hamming());
                        out.println(solBoard.toString());
                    }
                } else {
                    out.println("Not solvable!");
                }
                out.println("--------END OF SOLUTION--------");
            }
        }
    }

    private class SolverStage{
        MinPQ<Node> pq;
        Node currentNode;
//        Queue<Board> qb;

        int nStages = 0;
        boolean isGoal;
        public SolverStage(Board board) {
            pq = new MinPQ<Node>();
//            qb = new Queue<Board>();
            Node n = new Node(board,null);
            pq.insert(n);
        }

        public void next(){
            //delete from the priority queue the search node with the minimum priority, and insert onto the priority queue all neighboring search nodes
            Node n = pq.delMin();
            currentNode = n;
//            if(Solver.DEBUG){
//                StdOut.printf("Manhattan Priority = %d, Hamming Priority = %d\n",n.manhat,n.hamm);
//                StdOut.println(n.board.toString());
//            }
//            qb.enqueue(n.board);
            isGoal = n.isGoal;
            if(isGoal) return;

            // Insert neighbors into PQ
            Iterable<Board> it = n.board.neighbors();
            for(Board b : it) {
                if(n.prev != null && n.prev.board.equals(b))
                    continue;
//                Node newNode = new Node(b);
//                newNode.prev = n;
                pq.insert(new Node(b,n));
            }
            nStages++;
//            n.prev = null;
        }//next
    }

    private final class Node implements Comparable<Node>{
        public final Board board;
        public final int nMoves;
        public Node prev = null;
        public final int hamm;
        public final int manhat;
        public final boolean isGoal;


        public Node(Board board, Node prev){
            this.board = board;
            this.prev = prev;
            if(prev != null)
                nMoves = prev.nMoves + 1;
            else
                nMoves = 0;

            hamm = board.hamming() + nMoves;
            manhat = board.manhattan() + nMoves;
            isGoal = this.board.isGoal();
        }

        public int compareTo(Node that){
            if(this.manhat < that.manhat) return -1;
            if(this.manhat > that.manhat) return +1;
//            if(this.hamm < that.hamm) return -1;
//            if(this.hamm > that.hamm) return +1;
            if(this.board.manhattan() < that.board.manhattan()) return -1;
            if(this.board.manhattan() > that.board.manhattan()) return +1;
            if(this.board.hamming() < that.board.hamming()) return -1;
            if(this.board.hamming() > that.board.hamming()) return +1;
//            if(this.manhat < that.manhat) return -1;
//            if(this.manhat > that.manhat) return +1;
            return 0;
        }
    }

    private final boolean pvIsSolvable;

    private final int pvMoves;
    private final Iterable<Board> pvSolution;

    private void printPQContents(SolverStage stage){
        final int currentStage = stage.nStages;
        StdOut.println("Stage " + currentStage + ": PQ Contents");
        int MAXNODES = 1000;
        int nodeCnt = 0;
        String left = "";
        String br = "\n";

        final int board_n = stage.pq.min().board.dimension();
//        final int nDigits = (int) Math.floor(Math.log10((double) board_n*board_n)) + 1;
        final int nDigits = 3;
        final int width_1 = 20;
        final int width_2 = nDigits;
        String strFmt1 = "%-" + width_1 + "s = %" + width_2 + "d\t\n";
        final int width_3 = width_1 + width_2 + 3;
        String strFmt2 = "%-" + width_3 + "s\t\n";
        for(Node n : stage.pq){
            if(nodeCnt >= MAXNODES){
                break;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(strFmt1,"Manhattan Priority", n.manhat));
            sb.append(String.format(strFmt1,"Hamming Priority", n.hamm));
//            sb.append(String.format("Hamming Priority = %d\n", n.hamm));
//            sb.append(String.format("Moves = %d\n", n.nMoves));
            sb.append(String.format(strFmt1,"Moves", n.nMoves));
            sb.append(String.format(strFmt1,"Manhattan Distance", n.board.manhattan()));
            sb.append(String.format(strFmt1,"Hamming Distance", n.board.hamming()));

            //Adjust field width of board
            String[] lines = n.board.toString().substring(1).split(br);
            for(String l:lines)
                sb.append(String.format(strFmt2,l));

//            sb.append(String.format(strFmt2,n.board.toString().substring(1)));
//            sb.append(n.board.toString().substring(1));
            String right = sb.toString();
            nodeCnt++;

            //Append string to left string
            if(left == ""){
                left = right.toString();
                continue;
            }

            String[] lefts = left.split(br);
            String[] rights = right.split(br);

            assert(lefts.length==rights.length);

            StringBuilder sbFinal = new StringBuilder();
            for (int i = 0; i < lefts.length; i++) {
                sbFinal.append(lefts[i]);
                sbFinal.append(rights[i]);
                sbFinal.append(br);
            }

            left = sbFinal.toString();
        }
        StdOut.println(left);
        StdOut.println("--------------------------------------");

//        private String[] combineLeftRight(String[] left, String[] right){
//            if(left == null) return right;
//            if(right == null); return left;
//        }
    }
}