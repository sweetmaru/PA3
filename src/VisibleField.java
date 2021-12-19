// Name: Xiaofeng Luo
// USC NetID: luoxiaof
// CS 455 PA3
// Fall 2021


/**
  VisibleField class
  This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
  user can see about the minefield). Client can call getStatus(row, col) for any square.
  It actually has data about the whole current state of the game, including  
  the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
  It also has mutators related to actions the player could do (resetGameDisplay(), cycleGuess(), uncover()),
  and changes the game state accordingly.
  
  It, along with the MineField (accessible in mineField instance variable), forms
  the Model for the game application, whereas GameBoardPanel is the View and Controller, in the MVC design pattern.
  It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from 
  outside this class via the getMineField accessor.  
 */
public class VisibleField {

   /**
    * Representation invariant:
    *
    * -The row and column of a certain square should be positive integers and in the range of the field.
    * -The numbers of rows and columns of the visible field should be consistent with the ones of the underlying field.
    */

   // ----------------------------------------------------------   
   // The following public constants (plus numbers mentioned in comments below) are the possible states of one
   // location (a "square") in the visible field (all are values that can be returned by public method 
   // getStatus(row, col)).
   
   // The following are the covered states (all negative values):
   public static final int COVERED = -1;   // initial value of all squares
   public static final int MINE_GUESS = -2;
   public static final int QUESTION = -3;

   // The following are the uncovered states (all non-negative values):
   
   // values in the range [0,8] corresponds to number of mines adjacent to this square
   
   public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
   public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
   public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
   // ----------------------------------------------------------   

   //2D array of the 8 directions to be recursively searched
   private static final int[][] directions = {{-1, -1},{0, -1},{1, -1},{-1, 0},{1, 0},{-1, 1},{0, 1},{1, 1}};

   private MineField underField;
   private int[][] status;
   private int mineGuessed; //the number of guesses made


   /**
      Create a visible field that has the given underlying mineField.
      The initial state will have all the mines covered up, no mines guessed, and the game
      not over.
      @param mineField  the minefield to use for for this VisibleField
    */
   public VisibleField(MineField mineField) {
      underField = mineField;
      mineGuessed = 0;
      status = new int[mineField.numRows()][mineField.numCols()];
      setInitial(status);
   }
   
   
   /**
      Reset the object to its initial state (see constructor comments), using the same underlying
      MineField. 
   */     
   public void resetGameDisplay() {

      setInitial(status);
   }
  
   
   /**
      Returns a reference to the mineField that this VisibleField "covers"
      @return the minefield
    */
   public MineField getMineField() {

      return underField;
   }
   
   
   /**
      Returns the visible status of the square indicated.
      @param row  row of the square
      @param col  col of the square
      @return the status of the square at location (row, col).  See the public constants at the beginning of the class
      for the possible values that may be returned, and their meanings.
      PRE: getMineField().inRange(row, col)
    */
   public int getStatus(int row, int col) {

      return status[row][col];
   }

   
   /**
      Returns the the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
      or not.  Just gives the user an indication of how many more mines the user might want to guess.  This value can
      be negative, if they have guessed more than the number of mines in the minefield.     
      @return the number of mines left to guess.
    */
   public int numMinesLeft() {

      return underField.numMines() - mineGuessed;
   }
 
   
   /**
      Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
      changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
      changes it to COVERED again; call on an uncovered square has no effect.  
      @param row  row of the square
      @param col  col of the square
      PRE: getMineField().inRange(row, col)
    */
   public void cycleGuess(int row, int col) {
      if(status[row][col] == COVERED){
            status[row][col] = MINE_GUESS;
            mineGuessed ++;
      }
      else if(status[row][col] == MINE_GUESS){
            status[row][col] = QUESTION;
            mineGuessed --;
      }
      else if(status[row][col] == QUESTION){
            status[row][col] = COVERED;
      }
   }

   
   /**
      Uncovers this square and returns false iff you uncover a mine here.
      If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in 
      the neighboring area that are also not next to any mines, possibly uncovering a large region.
      Any mine-adjacent squares you reach will also be uncovered, and form 
      (possibly along with parts of the edge of the whole field) the boundary of this region.
      Does not uncover, or keep searching through, squares that have the status MINE_GUESS. 
      Note: this action may cause the game to end: either in a win (opened all the non-mine squares)
      or a loss (opened a mine).
      @param row  of the square
      @param col  of the square
      @return false   iff you uncover a mine at (row, col)
      PRE: getMineField().inRange(row, col)
    */
   public boolean uncover(int row, int col) {
      if(underField.hasMine(row, col)) {
         status[row][col] = EXPLODED_MINE;
         return false;
      }

      //if the square has adjacent mines
      if(underField.numAdjacentMines(row, col) != 0){
         status[row][col] = underField.numAdjacentMines(row, col);
         return true;
      }

      //if the square has no adjacent mines, do the recursive search to open the neighboring area
      else uncoverNeighbor(row, col);
      return true;
   }
 
   
   /**
      Returns whether the game is over.
      (Note: This is not a mutator.)
      @return whether game over
    */
   public boolean isGameOver() {
      int numUncovered = status.length * status[0].length - underField.numMines();
      boolean winOrLose = true; //lose is true, win is false;
      for (int[] ints : status) {
         for (int j = 0; j < status[0].length; j++) {
            //lose if uncover a mine
            if (ints[j] == EXPLODED_MINE) {
               gameOverStatus(winOrLose);
               return true;
            }
            if (ints[j] >= 0 && ints[j] <= 8) {
               numUncovered --;
            }
         }
      }
      //win if uncover all the non-mine squares
      if(numUncovered == 0){
         winOrLose = false;
         gameOverStatus(winOrLose);
         return true;
      }
      return false;
   }
 
   
   /**
      Returns whether this square has been uncovered.  (i.e., is in any one of the uncovered states, 
      vs. any one of the covered states).
      @param row of the square
      @param col of the square
      @return whether the square is uncovered
      PRE: getMineField().inRange(row, col)
    */
   public boolean isUncovered(int row, int col) {
      return status[row][col] >= 0;
   }


   /**
    * Sets the initial state of the given field, which means all the squares have the value 'COVERED'.
    *
    * @param status the 2D array of the status
    */
   private void setInitial(int[][] status){
      for(int i = 0; i < status.length; i++){
         for(int j = 0; j < status[0].length; j++){
            status[i][j] = COVERED;
         }
      }
   }

   /**
    * Updates the status when we win or lose the game
    *
    * @param winOrLose true if we lose, false if we win
    */
   private void gameOverStatus(boolean winOrLose){
      //if wins, all the mine areas should turn yellow
      if(!winOrLose){
         for(int i = 0; i < status.length; i++){
            for(int j = 0; j < status[0].length; j++){
               if(underField.hasMine(i, j) && status[i][j] != MINE_GUESS){
                  status[i][j] = MINE_GUESS;
               }
            }
         }
      }
      //if loses, show incorrect guess and unopened mines,
      //a question mark with an underlying mine should be considered as an unopened mine
      if(winOrLose){
         for(int i = 0; i < status.length; i++){
            for(int j = 0; j < status[0].length; j++){
               //skip the exploded location
               if(status[i][j] == EXPLODED_MINE){
                  continue;
               }
               if(underField.hasMine(i, j) && status[i][j] != MINE_GUESS){
                  status[i][j] = MINE;
               }
               if(!underField.hasMine(i, j) && status[i][j] == MINE_GUESS){
                  status[i][j] = INCORRECT_GUESS;
               }
            }
         }
      }
   }


   /**
    * Recursively uncovers all the area with no adjacent mines
    *
    * @param row the row of the start location
    * @param col the column of the start location
    */
   private void uncoverNeighbor(int row, int col){
      //stop recursion when the location is out of range, or with a covered mine, or guessed, or uncovered
      if(!underField.inRange(row, col) || (underField.hasMine(row, col) && status[row][col] == COVERED)
            || status[row][col] == MINE_GUESS || (status[row][col] >= 0 && status[row][col] <= 8)){
         return;
      }
      //stop recursion when the location has adjacent mines and change its status
      if(underField.numAdjacentMines(row, col) != 0){
         status[row][col] = underField.numAdjacentMines(row, col);
         return;
      }

      if(status[row][col] == COVERED || status[row][col] == QUESTION){
         status[row][col] = underField.numAdjacentMines(row, col);
      }

      for(int i = 0; i < directions.length; i++){
         uncoverNeighbor(row + directions[i][0], col + directions[i][1]);
      }
   }


}
