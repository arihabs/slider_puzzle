import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.MinPQ;

// TODO: Make prev Node in Node class final.
// Does inner Node class need to be static?

public class Solver{
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
            this.pvMoves = solutionStage.qb.size() - 1;
            pvSolution = solutionStage.qb;
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
        for(String fName : args){
            In in = new In(fName);
            int n = in.readInt();
            int[][] tiles = new int[n][n];
            for(int i = 0; i < n; i++)
                for(int j = 0; j < n; j++)
                    tiles[i][j] = in.readInt();

            Board board = new Board(tiles);
            Solver solver = new Solver(board);
            StdOut.println(fName + " #Moves: " + solver.moves());

//            if(solver.moves() >=0){
//
//            }
        }

    }

    private class SolverStage{
        MinPQ<Node> pq;
        Queue<Board> qb;
        boolean isGoal;
        public SolverStage(Board board) {
            pq = new MinPQ<Node>();
            qb = new Queue<Board>();
            Node n = new Node(board);
            pq.insert(n);
        }

        public void next(){
            //delete from the priority queue the search node with the minimum priority, and insert onto the priority queue all neighboring search nodes
            Node n = pq.delMin();
            qb.enqueue(n.board);
            isGoal = n.isGoal;
            if(isGoal) return;

            // Insert neighbors into PQ
            Iterable<Board> it = n.board.neighbors();
            for(Board b : it) {
                if(n.prev != null && n.prev.board.equals(b)) continue;
                pq.insert(new Node(b));
            }
        }//next
    }

    private final class Node implements Comparable<Node>{
        public final Board board;
        public final int nMoves;
        public Node prev = null;
        public final int hamm;
        public final int manhat;
        public final boolean isGoal;


        public Node(Board board){
            this.board = board;
            if(prev != null)
                nMoves = prev.nMoves + 1;
            else
                nMoves = 0;

            hamm = board.hamming() + nMoves;
            manhat = board.manhattan() + nMoves;
            isGoal = this.board.isGoal();
        }

        public int compareTo(Node that){
            if(this.hamm < that.hamm) return -1;
            if(this.hamm > that.hamm) return +1;
            if(this.manhat < that.manhat) return -1;
            if(this.manhat > that.manhat) return +1;
            return 0;
        }
    }

    private final boolean pvIsSolvable;

    private final int pvMoves;
    private final Iterable<Board> pvSolution;
}