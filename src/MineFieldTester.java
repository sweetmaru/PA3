public class MineFieldTester {

   private static boolean[][] field1 =
      {{false, true, false, false},
       {true, true, false, false},
       {true, true, true, false},
       {false, false, true, true}};


   public static void main(String[] args) {
      MineField mine1 = new MineField(field1);

      System.out.println("the number of rows: [exp: 4]: " + mine1.numRows());
      System.out.println("the number of columns: [exp: 4]: " + mine1.numCols());
      System.out.println("the number of mines: [exp: 8]: " + mine1.numMines());
      System.out.println("is it in range? [exp: false]: " + mine1.inRange(2, 4));
      System.out.println("has mine? [exp: true]: " + mine1.hasMine(0,1));
      System.out.println("how many mines around? [exp: 4]: " + mine1.numAdjacentMines(1, 2));

      mine1.resetEmpty();

      mine1 = new MineField(4, 4, 5);
      mine1.populateMineField(1, 2);

   }
}
