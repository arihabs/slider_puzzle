import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Queue;
import java.util.Iterator;

public class Board{

    // create a board from an n-by-n array of tiles,
    // where tiles[row][col] = tile at (row, col)
    public Board(int[][] tiles){
        if(tiles==null)
            throw new IllegalArgumentException("tiles are null!");

        this.N = tiles.length;
        this.numel = N*N;
//        this.tiles = new int[N][N];
        this.tiles = new short[N][N];

        assert this.N > 1;

        // String representation
//        final int nDigits = (int) Math.floor(Math.log10((double) this.numel-1)) + 1;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Integer.toString(this.N) + "\n");

        int hammCnt = 0;
        int manhattanCnt = 0;
        int zeroIdxTmp = -1;
        for(int i =0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                this.tiles[i][j] = (short) tiles[i][j];
//                int currentTile = tiles[i][j];
                short currentTile = this.tiles[i][j];

                int linearIdx = i*N + j; // 0 based indexing
                if(currentTile==0)
                    zeroIdxTmp = linearIdx;

                // Compute Hamming distance
                if(currentTile!=0 && currentTile!=(linearIdx+1))//1 based indexing offset
                    hammCnt++;

                // Compute Manhattan distance
                if(currentTile!=0) {
                    int idealRow = (currentTile-1) / N; //zero-based indexing
                    int idealCol = (currentTile-1) % N;
                    int rowError = Math.abs(i - idealRow);
                    int colError = Math.abs(j - idealCol);
                    manhattanCnt = manhattanCnt + rowError + colError;
                }

                // String representation
                stringBuilder.append(String.format("%3d ",this.tiles[i][j]));
//                stringBuilder.append(String.format("%"+nDigits+"d ",this.tiles[i][j]));
//                stringBuilder.append(Integer.toString(this.tiles[i][j]) + " ");
            }
            stringBuilder.append("\n");
        }

        this.hammDist = hammCnt;
        this.manhattanDist = manhattanCnt;
        this.strBoard = stringBuilder.toString();
        this.zeroIdx = zeroIdxTmp;

        if(hammCnt==0)
            this.isSolution = true;
        else
            this.isSolution = false;
    }

    // string representation of this board
    public String toString(){
        return this.strBoard;
    }

    // board dimension n
    public int dimension(){
        return this.N;
    }

    // number of tiles out of place
    public int hamming(){
        return this.hammDist;
    }

    // sum of Manhattan distances between tiles and goal
    public int manhattan(){
        return this.manhattanDist;
    }

    // is this board the goal board?
    public boolean isGoal(){
        return this.isSolution;
    }

    // does this board equal y?
    public boolean equals(Object y){
        if(y == this) return true;
        if(y == null) return false;
        if(y.getClass() != this.getClass()) return false;
        Board that = (Board) y;
        if(this.N != that.dimension() || this.numel != that.numel)
            return false;

        for(int i =0; i < N; i++)
            for (int j = 0; j < N; j++){
                if(this.tiles[i][j] != that.tiles[i][j])
                    return false;
            }

        return true;
    }

    // all neighboring boards
    public Iterable<Board> neighbors(){
        if(pvNeighbors != null)
            return pvNeighbors;

        // Find all neighboring boards relative to zero tile. Create new board, swaps neighboring tile and push onto queue.
        int[] zeroSubIdx = ind2sub(this.zeroIdx);
        int row = zeroSubIdx[0];
        int col = zeroSubIdx[1];
        // Neighbor 1. [row-1,col]
        // Neighbor 2. [row,col-1]
        // Neighbor 3. [row+1,col]
        // Neighbor 3. [row,col+1]
        Queue<Board> queueBoard = new Queue<Board>();
        int[][] neighborSet = {{row-1,col}, {row, col-1},{row+1, col},{row, col+1}};
        for(int i=0; i < neighborSet.length; i++){
                int currRow = neighborSet[i][0];
                int currCol = neighborSet[i][1];
//                int currLinIdx = sub2ind(currRow,currCol);
                if(!isValidSubIdx(currRow,currCol))
                    continue;
                // Copy tiles into new board and swap
            int[][] newTiles = copyArray2D(this.tiles);
            newTiles[row][col] = newTiles[currRow][currCol];
            newTiles[currRow][currCol] = 0;
            queueBoard.enqueue(new Board(newTiles));
        }

        pvNeighbors = queueBoard;

        return pvNeighbors;
    }
    private Iterable<Board> pvNeighbors = null;

    // a board that is obtained by exchanging any pair of tiles
    public Board twin(){
        if(pvTwin != null)
            return pvTwin;

        int newTiles[][] = new int[N][N];
        // Go through each element and find first 2 non-zero elements and swap them
        Queue<Integer> idxQueue = new Queue<Integer>();
//        int idx1 = -1;
//        int idx2 = -1;

        for(int i =0; i < N; i++){
            for (int j = 0; j < N; j++){
                int currentTile = this.tiles[i][j];
                newTiles[i][j] = currentTile;
                if(idxQueue.size() < 2 && currentTile!=0)
                    idxQueue.enqueue(sub2ind(i,j));
//                if(currentTile!=0){
//                    if(idx1 < 0)
//                        idx1 = sub2ind(i,j);
//                    else if(idx2 < 0)
//                        idx2 = sub2ind(i,j);
                }
            }
        assert idxQueue.size() == 2;
        // Swap tiles
//        int[] sub1 = ind2sub(idx1);
        int[] sub1 = ind2sub(idxQueue.dequeue());
        int[] sub2 = ind2sub(idxQueue.dequeue());
        assert idxQueue.isEmpty();
        int tileTmp = newTiles[sub1[0]][sub1[1]];
        newTiles[sub1[0]][sub1[1]] = newTiles[sub2[0]][sub2[1]];
        newTiles[sub2[0]][sub2[1]] = tileTmp;

        Board twinBoard = new Board(newTiles);
        pvTwin = twinBoard;
        return pvTwin;
    }

    //Private properties & Methods
    private Board pvTwin = null;

    private final int N;

    private final int numel;
//    private final int tiles[][];
    private final short tiles[][];
    private final int hammDist;
    private final int manhattanDist;
    private final boolean isSolution;

    private final String strBoard;

    private final int zeroIdx;

    private int[] ind2sub(int idx){
        int[] subIdx = new int[2];
        subIdx[0] = idx/this.N;
        subIdx[1] = idx % this.N;
        return subIdx;
    }

    private int sub2ind(int row, int col){
        return row*this.N + col;
    }

    private boolean isValidSubIdx(int row, int col){
        if(row < 0 || row > N-1) return false;
        if(col < 0 || col > N-1) return false;
        return true;
    }

    private int sub2ind(int[] subIdx){
        return sub2ind(subIdx[0],subIdx[1]);
    }

    private int[][] copyArray2D(int[][] a){
        int[][] aCopy = new int[this.N][this.N];
        for(int i =0; i < N; i++){
            for (int j = 0; j < N; j++) {
                aCopy[i][j] = a[i][j];
               }
            }
        return aCopy;
    }

    private int[][] copyArray2D(short[][] a){
        int[][] aCopy = new int[this.N][this.N];
        for(int i =0; i < N; i++){
            for (int j = 0; j < N; j++) {
                aCopy[i][j] = (int) a[i][j];
            }
        }
        return aCopy;
    }

//    private final Board twinBoard()

    //Unit testing
    public static void main(String[] args) {
        if (args.length == 0)
            return;

        for (String filename : args) {
            In in = new In(filename);
            int n = in.readInt();
            int[][] tiles = new int[n][n];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    tiles[i][j] = in.readInt();

            Board initial = new Board(tiles);

            StdOut.println(initial.toString());
            StdOut.println("N = " + initial.dimension());
            StdOut.println("Hamming Distance = " + initial.hamming());
            StdOut.println("Manhattan Distance = " + initial.manhattan());
            StdOut.println("Is Goal = " + initial.isGoal());
            StdOut.println("Twin Board\n" + initial.twin().toString());

//        Iterator<Board> it = initial.neighbors().iterator();
            Iterable<Board> it = initial.neighbors();

            StdOut.println("Neighbor Boards");
            for (Board b : it) {
                StdOut.println(b.toString());
                StdOut.println("This neighbor equals original?: " + initial.equals(b));
            }
        }
    }
}
