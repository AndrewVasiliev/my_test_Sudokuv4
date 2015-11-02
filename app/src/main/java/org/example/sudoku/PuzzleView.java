/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband2 for more book information.
***/

package org.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;

import android.os.Bundle;
import android.os.Parcelable;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;


public class PuzzleView extends View {
   
   private static final String TAG = "Sudoku";

   
   private static final String SELX = "selX"; 
   private static final String SELY = "selY";
   private static final String VIEW_STATE = "viewState";
   private static final int ID = 42; 

   
   private float width;    // width of one tile
   private float height;   // height of one tile
   private int selX;       // X index of selection
   private int selY;       // Y index of selection
    //ava beg
    private int offsetX;    //смещение по Х, для того чтобы поле было посередине
    private int offsetY;    //смещение по Y, для того чтобы поле было посередине
    private int fieldWidth; //ширина игрового поля в пикселях
    private int fieldHeight;//высота игрового поля в пикселях
    private int fieldSize;  //размер игрового поля в клетках (поле всегда квадратное)

    private int plr_coor[] = new int [2];   //координаты по Х для отображения имен игроков и заработанных очков
    private int plr_height; //высота текста имен игроков и заработанных очков
    private int plr_width;  //ширина полей для имен и очков
    //ava end
   private final Rect selRect = new Rect();

    private Paint background = new Paint();
    private Paint dark = new Paint();
    private Paint hilite = new Paint();
    private Paint light = new Paint();
    private Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint hint = new Paint();
    private Paint currPlayer = new Paint();
    private Rect loc_r = new Rect();
    private Paint selected = new Paint();

   private final Game game;
   
   public PuzzleView(Context context) {
      
      super(context);

      this.game = (Game) context;
      setFocusable(true);
      setFocusableInTouchMode(true);
      
      // ...
      //setId(ID)
       fieldSize = this.game.getfieldSize();
       Log.d(TAG, "fieldSize=" + fieldSize + "  this.game.locFieldSize=" + this.game.locFieldSize);
   }

   @Override
   protected Parcelable onSaveInstanceState() { 
      Parcelable p = super.onSaveInstanceState();
      Log.d(TAG, "onSaveInstanceState");
      Bundle bundle = new Bundle();
      bundle.putInt(SELX, selX);
      bundle.putInt(SELY, selY);
      bundle.putParcelable(VIEW_STATE, p);
      return bundle;
   }
   @Override
   protected void onRestoreInstanceState(Parcelable state) { 
      Log.d(TAG, "onRestoreInstanceState");
      Bundle bundle = (Bundle) state;
      select(bundle.getInt(SELX), bundle.getInt(SELY));
      super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
      //return;
   }
   

   @Override
   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
       //ava beg
       fieldWidth  = w < h ? w : h;
       fieldHeight = w < h ? w : h;


       offsetX = ((w - fieldWidth) / 2);
       offsetY = ((h - fieldHeight) / 2);

       Log.d(TAG, "onSizeChanged: w " + w + ", h " + h);
       Log.d(TAG, "onSizeChanged: offsetX " + offsetX + ", offsetY " + offsetY);

       width = fieldWidth / /*9f*/ (float) fieldSize;
       height = fieldHeight / /*9f*/ (float) fieldSize;

       //имена игроков и заработанные очки
       plr_height = (int) height /2;
       if (offsetY < plr_height) {
           //альбом
           plr_width = (int)((w - fieldWidth) /2 );
           plr_coor[0] = 0;
           plr_coor[1] = w - plr_width;

       } else {
           //портрет
           plr_width = (int)(w /5 * 2);
           plr_coor[0] = 0;
           plr_coor[1] = w - plr_width;
       }

       //ava end

//      width = w / 9f;
//      height = h / 9f;
       getRect(selX, selY, selRect);
       Log.d(TAG, "onSizeChanged: width " + width + ", height " + height);
      super.onSizeChanged(w, h, oldw, oldh);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      // Draw the background...
      //Paint background = new Paint();
      background.setColor(getResources().getColor(R.color.puzzle_background));
      canvas.drawRect(0, 0, getWidth(), getHeight(), background);

      
      // Draw the board...
      
      // Define colors for the grid lines
      //Paint dark = new Paint();
      dark.setColor(getResources().getColor(R.color.puzzle_dark));

      //Paint hilite = new Paint();
      hilite.setColor(getResources().getColor(R.color.puzzle_hilite));

      //Paint light = new Paint();
      light.setColor(getResources().getColor(R.color.puzzle_light));

      // Draw the minor grid lines
      for (int i = 0; i < (fieldSize + 1); i++) {
          //ava beg
//          if (i % 3 != 0) {
              canvas.drawLine(offsetX, offsetY + i * height, offsetX + fieldWidth, offsetY + i * height, dark);
              canvas.drawLine(offsetX, offsetY + i * height + 1, offsetX + fieldWidth, offsetY + i * height + 1, hilite);

              canvas.drawLine(offsetX + i * width, offsetY, offsetX + i * width, offsetY + fieldHeight, dark);
              canvas.drawLine(offsetX + i * width + 1, offsetY, offsetX + i * width + 1, offsetY + fieldHeight, hilite);
//          }
          //ava end
//          canvas.drawLine(0,               i * height + 1, getWidth(), i * height + 1, hilite);
//          canvas.drawLine(0,               i * height, getWidth(), i * height, /*light*/ dark);
//          canvas.drawLine(i * width,       0, i * width, getHeight(), /*light*/ dark);
//          canvas.drawLine(i * width + 1,   0, i * width + 1, getHeight(), hilite);
      }

      // Draw the major grid lines
//      for (int i = 0; i < 9; i++) {
//         if (i % 3 != 0)
//            continue;
//         canvas.drawLine(0, i * height, getWidth(), i * height, dark);
//         canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
//         canvas.drawLine(i * width, 0, i * width, getHeight(), dark);
//         canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
 //     }

       //выделяем столбец или строку для очередного хода
       for (int i = 0; i< fieldSize; i++) {
           if (this.game.vertical_move) {
               getRect(this.game.current_position, i, loc_r);
           } else {
               getRect(i, this.game.current_position, loc_r);
           }
           hint.setColor(getResources().getColor(R.color.puzzle_currentmove));
           canvas.drawRect(loc_r, hint);
       }



      // Draw the numbers...
      // Define color and style for numbers
      //Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
      foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
      foreground.setStyle(Style.FILL);
      foreground.setTextSize(height * 0.75f);
      foreground.setTextScaleX(width / height);
      foreground.setTextAlign(Paint.Align.CENTER);

      // Draw the number in the center of the tile
      FontMetrics fm = foreground.getFontMetrics();
      // Centering in X: use alignment (and X at midpoint)
      float x = width / 2;
      // Centering in Y: measure ascent/descent first
      float y = height / 2 - (fm.ascent + fm.descent) / 2;
      for (int i = 0; i < fieldSize; i++) {
         for (int j = 0; j < fieldSize; j++) {
            canvas.drawText(this.game.getTileString(i, j), offsetX + i
                  * width + x, offsetY + j * height + y, foreground);
         }
      }

       //выделяем цветом игрока, который должен сделать ход
       loc_r.set(plr_coor[this.game.current_player], 0, plr_coor[this.game.current_player] + plr_width, plr_height);
       currPlayer.setColor(getResources().getColor(R.color.puzzle_currentplayer));
       canvas.drawRect(loc_r, currPlayer);

       //рисуем имена игроков и набранный очки
       foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
       foreground.setStyle(Style.FILL);
       foreground.setTextSize(plr_height * 0.8f);
       //foreground.setTextScaleX(width / height);
       foreground.setTextAlign(Paint.Align.CENTER);

       // Draw the number in the center of the tile
       fm = foreground.getFontMetrics();
       // Centering in X: use alignment (and X at midpoint)
       x = plr_width / 2;
       // Centering in Y: measure ascent/descent first
       y = plr_height / 2 - (fm.ascent + fm.descent) / 2;
       for (int j = 0; j < 2; j++) {
           canvas.drawText(this.game.players[j].name, plr_coor[j] + x, 0 + y, foreground);
           canvas.drawText(String.valueOf(this.game.players[j].points), plr_coor[j] + x, 0 + plr_height + y, foreground);
       }



 /*     if (Prefs.getHints(getContext())) {
         // Draw the hints...
         
         // Pick a hint color based on #moves left
         //Paint hint = new Paint();
         int c[] = { getResources().getColor(R.color.puzzle_hint_0),
               getResources().getColor(R.color.puzzle_hint_1),
               getResources().getColor(R.color.puzzle_hint_2), };
         //Rect loc_r = new Rect();
         for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
               int movesleft = 9 - game.getUsedTiles(i, j).length;
               if (movesleft < c.length) {
                  getRect(i, j, loc_r);
                  hint.setColor(c[movesleft]);
                  canvas.drawRect(loc_r, hint);
               }
            }
         }
         
      }
   */

      // Draw the selection...
      Log.d(TAG, "selRect=" + selRect);
      //Paint selected = new Paint();
      selected.setColor(getResources().getColor(R.color.puzzle_selected));
      canvas.drawRect(selRect, selected);
   }

   @Override
   public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() != MotionEvent.ACTION_DOWN)
          return super.onTouchEvent(event);

       int column = (int)((event.getX() - offsetX) / width);
       int row = (int) ((event.getY() - offsetY) / height);
       if ( (this.game.vertical_move && (column == this.game.current_position)) ||
            (!this.game.vertical_move && (row == this.game.current_position)) ) {
           select (column, row);
           if (game.showKeypadOrError(selX, selY)) {
               invalidate();
           }
       } else {
           return true;
       }

       //select((int) ((event.getX() - offsetX) / width), (int) ((event.getY() - offsetY) / height));
       //game.showKeypadOrError(selX, selY);
       Log.d(TAG, "onTouchEvent: x " + selX + ", y " + selY);
      return true;
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event="
            + event);
      switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_UP:
         select(selX, selY - 1);
         break;
      case KeyEvent.KEYCODE_DPAD_DOWN:
         select(selX, selY + 1);
         break;
      case KeyEvent.KEYCODE_DPAD_LEFT:
         select(selX - 1, selY);
         break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
         select(selX + 1, selY);
         break;
      case KeyEvent.KEYCODE_ENTER:
      case KeyEvent.KEYCODE_DPAD_CENTER:
         game.showKeypadOrError(selX, selY);
         break;
      default:
         return super.onKeyDown(keyCode, event);
      }
      return true;
   }

   public void setSelectedTile(int tile) {
      if (game.setTileIfValid(selX, selY, tile)) {
         invalidate();// may change hints
      } else {
         // Number is not valid for this tile
         Log.d(TAG, "setSelectedTile: invalid: " + tile);
         startAnimation(AnimationUtils.loadAnimation(game, R.anim.shake));
      }
   }

   private void select(int x, int y) {
      invalidate(selRect);
      selX = Math.min(Math.max(x, 0), /*8*/ fieldSize-1);
      selY = Math.min(Math.max(y, 0), /*8*/ fieldSize-1);
      getRect(selX, selY, selRect);
      invalidate(selRect);
   }

   private void getRect(int x, int y, Rect rect) {
      rect.set((int) (offsetX + x * width), (int) (offsetY + y * height),
              (int) (offsetX + x * width + width), (int) (offsetY + y * height + height));
   }

 /*   public void Initialization(int locFieldSize) {
        fieldSize = locFieldSize;
    }*/
   // ...
}

