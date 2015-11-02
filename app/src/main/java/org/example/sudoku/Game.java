/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband2 for more book information.
***/
package org.example.sudoku;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

public class Game extends Activity {
   private static final String TAG = "Sudoku";

    public static final String KEY_DIFFICULTY = "org.example.sudoku.difficulty";
    public static final String KEY_FIELDSIZE = "org.example.sudoku.fieldsize";

   private static final String PREF_PUZZLE = "puzzle" ;
   
   public static final int DIFFICULTY_EASY = 0;
   public static final int DIFFICULTY_MEDIUM = 1;
   public static final int DIFFICULTY_HARD = 2;
   
   protected static final int DIFFICULTY_CONTINUE = -1;

    protected int locFieldSize;

   private int puzzle[]; //= new int[ locFieldSize * locFieldSize];
   public boolean vertical_move; //true - игрок должен выбрать ячейку по вертикали; false - по горизонтали
   public int current_position; //текущая координата на поле. если vertical_move = true, то горизонтальная позиция столбца. если vertical_move = false, то вертикальная позиция строки.
   public int current_player; //игрок, который должен сделать ход
   public class StructPlayers {
      String name;   //имя игрока
      int points;    //набранные очки
   }
   public StructPlayers players[] = new StructPlayers[2];
/*   private final String easyPuzzle =
      "360000000004230800000004200" +
      "070460003820000014500013020" +
      "001900000007048300000000045";
   private final String mediumPuzzle =
      "650000070000506000014000005" +
      "007009000002314700000700800" +
      "500000630000201000030000097";
   private final String hardPuzzle =
      "009000000080605020501078000" +
      "000000700706040102004000000" +
      "000720903090301080000000600";
*/
   private PuzzleView puzzleView;

   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      
      super.onCreate(savedInstanceState);
      Log.d(TAG, "onCreate");


       locFieldSize = getIntent().getIntExtra(KEY_FIELDSIZE, 9);
       Log.d(TAG, "Game.fieldSize=" + locFieldSize);

       puzzle = new int[locFieldSize * locFieldSize];
     // players = new StructPlayers[2];
      players[0] = new StructPlayers();
      players[1] = new StructPlayers();
      //int diff = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
      //если активити было пересоздано (поворот экрана), то восстанавливаем состояние поля
      if (savedInstanceState != null) {
         //puzzle = fromPuzzleString(getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE, ""));
         LoadCurrentStatus();
      } else {
         puzzle = getPuzzle(locFieldSize);
         vertical_move = true;
         current_position = (locFieldSize - 1)/2;
         players[0].name = "Player 1";
         players[0].points = 0;
         players[1].name = "Player 2";
         players[1].points = 0;
         current_player = 0;
      }
//      calculateUsedTiles();
      requestWindowFeature(Window.FEATURE_NO_TITLE); //окно без заголовка

      puzzleView = new PuzzleView(this);
      //puzzleView.Initialization (fieldSize);
      setContentView(puzzleView);
      puzzleView.requestFocus();

      
      // ...
      // If the activity is restarted, do a continue next time
      getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
   }
   

   @Override
   protected void onResume() {
      super.onResume();
      Music.play(this, R.raw.game);
   }

   private void LoadCurrentStatus(){
      puzzle = fromPuzzleString(getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE, ""));

      players[0].name = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE + "pl1_Name", "No data :(");
      players[0].points = getPreferences(MODE_PRIVATE).getInt(PREF_PUZZLE + "pl1_Points", -1);
      players[1].name = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE + "pl2_Name", "No data :(");
      players[1].points = getPreferences(MODE_PRIVATE).getInt(PREF_PUZZLE + "pl2_Points", -1);

      vertical_move = getPreferences(MODE_PRIVATE).getBoolean(PREF_PUZZLE + "vertical_move", false);
      current_position = getPreferences(MODE_PRIVATE).getInt(PREF_PUZZLE + "current_position", 0);
      current_player = getPreferences(MODE_PRIVATE).getInt(PREF_PUZZLE + "current_player", 0);
   }

   private void SaveCurrentStatus(){
      getPreferences(MODE_PRIVATE)
              .edit()
              .putString(PREF_PUZZLE, toPuzzleString(puzzle))
              .putString(PREF_PUZZLE + "pl1_Name", players[0].name)
              .putInt(PREF_PUZZLE + "pl1_Points", players[0].points)
              .putString(PREF_PUZZLE + "pl2_Name", players[1].name)
              .putInt(PREF_PUZZLE + "pl2_Points", players[1].points)
              .putBoolean(PREF_PUZZLE + "vertical_move", vertical_move)
              .putInt(PREF_PUZZLE + "current_position", current_position)
              .putInt(PREF_PUZZLE + "current_player", current_player)
              .commit();
      //getPreferences(MODE_PRIVATE).edit().putInt(PREF_PUZZLE + "pl1Points", players[0].points).commit();
   }


   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      //ava
      // Save the current puzzle
      //getPreferences(MODE_PRIVATE).edit().putString(PREF_PUZZLE, toPuzzleString(puzzle)).commit();
      SaveCurrentStatus();
   }


   @Override
   protected void onPause() {
      super.onPause();
      Log.d(TAG, "onPause");
      Music.stop(this);

      // Save the current puzzle
      //getPreferences(MODE_PRIVATE).edit().putString(PREF_PUZZLE, toPuzzleString(puzzle)).commit();
      SaveCurrentStatus();
   }



   /** Given a difficulty level, come up with a new puzzle */
   private int[] getPuzzle(int fs) {
      int[] puz;
      int count,        //количество повторений в поле для каждого значения (1,2,3,4,5,6,7,8,9)
              step,     //случайный шаг для заполнения следующей позиции значением
              currPos,  //текущая позиция
              maxIndex, //максимальная позиция в массиве
              posLeft = 0;  //количество оставшихся незаполненных позиций


      Random locRandom = new Random();

      puz = new int[fs*fs];
      currPos = 0;
      maxIndex = fs*fs - 1;
      Arrays.fill(puz, ' ');
      puz[maxIndex/2] = 0;
      count = (fs*fs) / 9;

      //перебираем все значения для игрового поля (1..9)
      int val = 0;
      while (true) {
          val++;
          if (val == 10) {
              val = 1;
          }

         //распологаем случайным образом значение VAL на игровом поле
            posLeft = 0;
            step = locRandom.nextInt(maxIndex)+1;
            int k = currPos;
            while (true) {
               k++;
               if ( k > maxIndex) {
                  k = 0;
               }
               if ((k == currPos) && (posLeft == 0)) {
                  //мы прошли по кругу и пустых мест уже нет. все заполнено.
                  break;
               }
               if (puz[k] == ' ') {
                  posLeft ++;
                  step --;
                  if (step == 0) {
                     //помещаем на поле значение
                     puz[k] = val ;
                     currPos = k;
                     break;
                  }
               }
            }

         if (posLeft == 0) {
            //все поле заполнено. выходим.
            break;
         }
      }
/*
      switch (fs) {
      case DIFFICULTY_CONTINUE:
         puz = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE, "");
         break;
         // ...
         
      case DIFFICULTY_HARD:
         puz = hardPuzzle;
         break;
      case DIFFICULTY_MEDIUM:
         puz = mediumPuzzle;
         break;
      case DIFFICULTY_EASY:
      default:
         puz = easyPuzzle;
         break;
         
      }
      return fromPuzzleString(puz);
*/
      return puz;
   }
   

   /** Convert an array into a puzzle string */
   static private String toPuzzleString(int[] puz) {
      StringBuilder buf = new StringBuilder();
      for (int element : puz) {
         buf.append(element);
      }
      return buf.toString();
   }

   /** Convert a puzzle string into an array */
   static protected int[] fromPuzzleString(String string) {
      int[] puz = new int[string.length()];
      for (int i = 0; i < puz.length; i++) {
         puz[i] = string.charAt(i) - '0';
      }
      return puz;
   }

   /** Return the tile at the given coordinates */
   private int getTile(int x, int y) {
      return puzzle[y * locFieldSize + x];
   }

   /** Change the tile at the given coordinates */
   private void setTile(int x, int y, int value) {
      puzzle[y * locFieldSize + x] = value;
   }

    protected int getfieldSize() {
        return (locFieldSize);
    }

   /** Return a string for the tile at the given coordinates */
   protected String getTileString(int x, int y) {
      int v = getTile(x, y);
      if (v == 0)
         return "";
      else
         return String.valueOf(v);
   }

   /** Change the tile only if it's a valid move */
   protected boolean setTileIfValid(int x, int y, int value) {
      int tiles[] = getUsedTiles(x, y);
      if (value != 0) {
         for (int tile : tiles) {
            if (tile == value)
               return false;
         }
      }
      setTile(x, y, value);
      calculateUsedTiles();
      return true;
   }

   private boolean CheckEndGame () {
      int count = 0;
      for (int i = 0; i < locFieldSize; i++) {
         if (vertical_move) {
            if (getTile(current_position, i) != 0)
               count++;
         } else {
            if (getTile(i, current_position) != 0)
               count++;
         }
      }
      if (count>0)
         return false;
      return true;
   }

   /** Open the keypad if there are any valid moves */
   protected boolean showKeypadOrError(int x, int y) {
/*      int tiles[] = getUsedTiles(x, y);
      if (tiles.length == 9) {
         Toast toast = Toast.makeText(this, R.string.no_moves_label, Toast.LENGTH_SHORT);
         toast.setGravity(Gravity.CENTER, 0, 0);
         toast.show();
      } else {
         Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
         Dialog v = new Keypad(this, tiles, puzzleView);
         v.show();
      }
      */
      if (getTile(x, y) == 0) {
         //пустая ячейка
         Toast toast = Toast.makeText(this, R.string.no_moves_label, Toast.LENGTH_SHORT);
         toast.setGravity(Gravity.CENTER, 0, 0);
         toast.show();
         return false;
      } else {
         players[current_player].points += getTile(x, y);
         setTile(x,y, 0);
         if (vertical_move) {
            current_position = y;
         } else {
            current_position = x;
         }
         vertical_move = !vertical_move;
         current_player ^= 1;

         //проверяем возможность дальнейшей игры
         if (CheckEndGame()) {
            Toast toast = Toast.makeText(this, R.string.end_game, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
         }

      }
      return true;
   }

   //** Cache of used tiles *
   private final int used[][][] = new int[locFieldSize][locFieldSize][];

   //** Return cached used tiles visible from the given coords *
   protected int[] getUsedTiles(int x, int y) {
      return used[x][y];
   }

   //** Compute the two dimensional array of used tiles *
   private void calculateUsedTiles() {
      for (int x = 0; x < locFieldSize; x++) {
         for (int y = 0; y < locFieldSize; y++) {
            used[x][y] = calculateUsedTiles(x, y);
            // Log.d(TAG, "used[" + x + "][" + y + "] = "
            // + toPuzzleString(used[x][y]));
         }
      }
   }

   /** Compute the used tiles visible from this position */
   private int[] calculateUsedTiles(int x, int y) {
      int c[] = new int[locFieldSize];
      // horizontal
      for (int i = 0; i < locFieldSize; i++) {
         if (i == y)
            continue;
         int t = getTile(x, i);
         if (t != 0)
            c[t - 1] = t;
      }
      // vertical
      for (int i = 0; i < locFieldSize; i++) {
         if (i == x)
            continue;
         int t = getTile(i, y);
         if (t != 0)
            c[t - 1] = t;
      }
      // same cell block
      int startx = (x / 3) * 3;
      int starty = (y / 3) * 3;
      for (int i = startx; i < startx + 3; i++) {
         for (int j = starty; j < starty + 3; j++) {
            if (i == x && j == y)
               continue;
            int t = getTile(i, j);
            if (t != 0)
               c[t - 1] = t;
         }
      }
      // compress
      int nused = 0;
      for (int t : c) {
         if (t != 0)
            nused++;
      }
      int c1[] = new int[nused];
      nused = 0;
      for (int t : c) {
         if (t != 0)
            c1[nused++] = t;
      }
      return c1;
   }
   
}
