package com.yzhuang.twoeatoneandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yzhuang.twoeatoneandroid.BasicBoard.TEAM;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;


public class TwoEatOne extends Activity {

    private FrameLayout mFrame;
    private GestureDetector mGestureDetector;
    private DrawBoard mDrawBoard;

    private static float BOARD_LAYOUT_RATIO = 0.83f;
    private static int BOARD_SIZE = 5;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mBoardWidth;
    private int mBoardHeight;
    private int mBoardBottomMargin;
    private int mBoardTopMargin;
    private int mBoardLeftMargin;
    private int mBoardRightMargin;
    private float[][][] mBoardPositionPoints;
    private int mPieceSize;

    private ArrayList<DrawPiece> mBlack;
    private ArrayList<DrawPiece> mWhite;
    private Board mBoard;
    private boolean mIsBlackAI;
    private boolean mIsWhiteAI;
    private int mAILevel;
    private boolean mAIEnhanced;
    private TEAM mTurn;
    private TEAM mFirstTurn;

    private Piece mSelectedPiece;

    private Boolean mIsPieceSelected;

    private Random mRandom;

    private Spinner mSpinnerVS;
    private Spinner mSpinnerAILevel;

    private Button mButtonNew;
    //private Button mButtonUndo;
    private Button mButtonQuit;


    private static final int SPINNER_PLAYER_VS_PLAYER = 0;
    private static final int SPINNER_PLAYER_VS_AI = SPINNER_PLAYER_VS_PLAYER + 1;
    private static final int SPINNER_AI_VS_PLAYER = SPINNER_PLAYER_VS_AI + 1;

    private static final int SPINNER_AI_LEVEL_0 = 0;
    private static final int SPINNER_AI_LEVEL_1 = SPINNER_AI_LEVEL_0 + 1;
    private static final int SPINNER_AI_LEVEL_2 = SPINNER_AI_LEVEL_1 + 1;
    private static final int SPINNER_AI_LEVEL_3 = SPINNER_AI_LEVEL_2 + 1;

    private static String TAG="TwoEatOne-Main";

    private static final String DONATE_URL = "https://www.paypal.me/Zhuang";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_eat_one);

        mFrame = (FrameLayout) findViewById(R.id.main_frame);
        setupSizeParameters();




        mBlack = new ArrayList<>();
        mWhite = new ArrayList<>();

        mIsBlackAI = true;
        mIsWhiteAI = false;
        mAILevel = 0;
        mAIEnhanced = false;
        mFirstTurn = TEAM.WHITE;

        mRandom = new Random();
        mIsPieceSelected = false;

        setupSpinners();
        setupButtons();
    }

    @Override
    protected void onResume(){
        super.onResume();

        newGame();

        setupGestureDetector();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_two_eat_one, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void removePieces(){
        for(DrawPiece piece: mWhite)
            piece.invalidate();
        for(DrawPiece piece: mBlack)
            piece.invalidate();
    }

    public void removeDrawPieceViewByLabel(TEAM team, int label){
        int idxToBeDeleted = -1;
        for(int i=0; i<mFrame.getChildCount(); i++){
            if(mFrame.getChildAt(i) instanceof DrawPiece){
                DrawPiece drawPiece = (DrawPiece) mFrame.getChildAt(i);
                if(drawPiece.getTeam()==team && drawPiece.getLabel()==label){
                    idxToBeDeleted = i;
                    break;
                }
            }
        }
        mFrame.removeViewAt(idxToBeDeleted);
    }

    public void removeDrawPieceByLabel(TEAM team, int label){
        ArrayList<DrawPiece> currTeam = null;
        switch(team){
            case BLACK:
                currTeam = mBlack;
                break;
            case WHITE:
                currTeam = mWhite;
                break;
        }
        int idxToBeDeleted = -1;
        for(int i=0; i<currTeam.size();i++){
            if(currTeam.get(i).getLabel()==label){
                idxToBeDeleted = i;
                break;
            }
        }
        currTeam.remove(idxToBeDeleted);
    }

    private void setupSizeParameters(){
        // setup frame size parameters
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mBoardWidth = (int) (BOARD_LAYOUT_RATIO * mScreenWidth);
        mBoardHeight = mBoardWidth;
        mBoardLeftMargin = ( mScreenWidth - mBoardWidth ) / 2;
        mBoardRightMargin = mBoardLeftMargin;
        mBoardTopMargin = ( mScreenHeight - mBoardHeight ) / 2;
        mBoardBottomMargin = mBoardTopMargin;

        mBoardPositionPoints = new float[BOARD_SIZE][BOARD_SIZE][2];
        setupBoardsPositionPoints();
        mPieceSize = (int) ((mBoardPositionPoints[0][1][1] - mBoardPositionPoints[0][0][1])
                / 2 * 4 / 5 );
    }

    public void setupSpinners(){
        mSpinnerVS = (Spinner) findViewById(R.id.spinner_choose_player_vs);
        ArrayAdapter<CharSequence> adapterVS = ArrayAdapter.createFromResource(this,
                R.array.spinner_vs, R.layout.my_spinner_item);
        adapterVS.setDropDownViewResource(R.layout.my_spinner_dropdown_item);
        mSpinnerVS.setAdapter(adapterVS);
        mSpinnerVS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case SPINNER_PLAYER_VS_PLAYER:
                        mIsWhiteAI = false;
                        mIsBlackAI = false;
                        mFirstTurn = TEAM.WHITE;
                        break;
                    case SPINNER_PLAYER_VS_AI:
                        mIsWhiteAI = false;
                        mIsBlackAI = true;
                        mFirstTurn = TEAM.WHITE;
                        break;
                    case SPINNER_AI_VS_PLAYER:
                        mIsWhiteAI = false;
                        mIsBlackAI = true;
                        mFirstTurn = TEAM.BLACK;
                        break;
                }
                if(mBoard!=null){
                    mBoard.setIsBlackAI(mIsBlackAI);
                    mBoard.setIsWhiteAI(mIsWhiteAI);
                    aiMove();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerAILevel = (Spinner) findViewById(R.id.spinner_choose_ai_level);
        ArrayAdapter<CharSequence> adapterAI = ArrayAdapter.createFromResource(this,
                R.array.spinner_ai, R.layout.my_spinner_item);
        adapterAI.setDropDownViewResource(R.layout.my_spinner_dropdown_item);
        mSpinnerAILevel.setAdapter(adapterAI);
        mSpinnerAILevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case SPINNER_AI_LEVEL_0:
                        mAILevel = 0;
                        mAIEnhanced = false;
                        break;
                    case SPINNER_AI_LEVEL_1:
                        mAILevel = 1;
                        mAIEnhanced = true;
                        break;
                    case SPINNER_AI_LEVEL_2:
                        mAILevel = 2;
                        mAIEnhanced = false;
                        break;
                    case SPINNER_AI_LEVEL_3:
                        mAILevel = 2;
                        mAIEnhanced = true;
                        break;
                }
                if (mBoard != null) {
                    mBoard.setAILevel(mAILevel);
                    mBoard.setAILevelEnhace(mAIEnhanced);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void setupButtons(){
        mButtonNew = (Button) findViewById(R.id.button_new_game);
        mButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });
        //mButtonUndo = (Button) findViewById(R.id.button_undo_last);
        mButtonQuit = (Button) findViewById(R.id.button_quit);
        mButtonQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
    }

    private void setupGestureDetector(){
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event){
                /*if(TOGGLE_ON_CLICK){
                    mSystemUiHider.toggle();
                    return true;
                }*/
                float xTap = event.getX();
                float yTap = event.getY();
                Log.d(TAG, "Tap event at " + String.valueOf(xTap) + " " + String.valueOf(yTap));
                if(mIsPieceSelected){
                    int[] newPieceCoord = mBoard.findNearestXYCoord(xTap, yTap);
                    if(mBoard.isValidMove(mSelectedPiece, newPieceCoord[0], newPieceCoord[1])){
                        unlockSelectedPiece();
                        movePiece(newPieceCoord);
                        if(checkIfWin()){
                            newGame();
                            return true;
                        }
                        aiMove();
                        removePieces();
                        return true;
                    }
                    else{
                        // unlock selected
                        invalidMoveToast();
                        unlockSelectedPiece();
                        removePieces();
                    }
                }
                trySelectedNewPiece(mTurn, xTap, yTap);
                removePieces();
                return true;
            }

        });
    }

    private int findLockedLabel(){
        ArrayList<DrawPiece> currTeam = null;
        switch(mTurn){
            case BLACK:
                currTeam = mBlack;
                break;
            case WHITE:
                currTeam = mWhite;
                break;
        }
        for(DrawPiece drawPiece: currTeam){
            if(drawPiece.getIsLocked())
                return drawPiece.getLabel();
        }
        return -1;
    }

    private int findDrawIdxByLabel(int label, TEAM team){
        ArrayList<DrawPiece> currTeam = null;
        switch(mTurn){
            case BLACK:
                currTeam = mBlack;
                break;
            case WHITE:
                currTeam = mWhite;
                break;
        }
        for(int i=0; i<currTeam.size(); i++){
            if(currTeam.get(i).getLabel() == label)
                return i;
        }
        return 0;
    }

    private void setupBoardsPositionPoints(){
        int baseTop = mBoardTopMargin;
        int baseBottom = mScreenHeight - mBoardBottomMargin;
        int baseLeft = mBoardLeftMargin;
        int baseRight = mScreenWidth - mBoardRightMargin;
        int dVertical = (baseBottom - baseTop) / 4;
        int dHorizontal = (baseRight - baseLeft) / 4;
        for(int i=0; i< BOARD_SIZE;i++){
            for(int j=0; j<BOARD_SIZE;j++){
                mBoardPositionPoints[i][j][0] = (float) (baseLeft + i * dHorizontal);
                mBoardPositionPoints[i][j][1] = (float) (baseTop + j * dVertical);
            }
        }
    }

    private TEAM oppositeTeam(TEAM team){
        switch(team){
            case BLACK:
                return TEAM.WHITE;
            case WHITE:
                return TEAM.BLACK;
        }
        return TEAM.BLACK;
    }

    private void unlockSelectedPiece(){
        switch(mSelectedPiece.getTeam()){
            case BLACK:
                mBlack.get(findDrawIdxByLabel(mSelectedPiece.getLabel(),
                        mSelectedPiece.getTeam())).unlock();
                break;
            case WHITE:
                mWhite.get(findDrawIdxByLabel(mSelectedPiece.getLabel(),
                        mSelectedPiece.getTeam())).unlock();
                break;
        }
        mIsPieceSelected = false;
    }

    private void unlockTeam(TEAM team){
        ArrayList<DrawPiece> currTeam = null;
        switch (team){
            case BLACK:
                currTeam = mBlack;
                break;
            case WHITE:
                currTeam = mWhite;
                break;
        }
        for(DrawPiece drawPiece : currTeam)
            drawPiece.unlock();
    }

    private void movePiece(int[] newPieceCoord){
        int[] labelKilledList = mBoard.getLabelsOfKilledList(mBoard.getPieceByLabel
                        (mSelectedPiece.getTeam(), mSelectedPiece.getLabel()),
                newPieceCoord[0], newPieceCoord[1]);
        // remove view
        for(int label: labelKilledList)
            removeDrawPieceViewByLabel(oppositeTeam(mTurn), label);
        // remove from draw list
        for(int label: labelKilledList)
            removeDrawPieceByLabel(oppositeTeam(mTurn), label);
        // move piece
        mBoard.movePieceByLabel(mSelectedPiece.getTeam(),
                mSelectedPiece.getLabel(), newPieceCoord[0], newPieceCoord[1]);
        float[] newPosition = mBoard.coord2ScreenCoord(newPieceCoord);
        switch(mSelectedPiece.getTeam()){
            case BLACK:
                mBlack.get(findDrawIdxByLabel(mSelectedPiece.getLabel(),
                        mSelectedPiece.getTeam())).setPieceX(newPosition[0]);
                mBlack.get(findDrawIdxByLabel(mSelectedPiece.getLabel(),
                        mSelectedPiece.getTeam())).setPieceY(newPosition[1]);
                break;
            case WHITE:
                mWhite.get(findDrawIdxByLabel(mSelectedPiece.getLabel(),
                        mSelectedPiece.getTeam())).setPieceX(newPosition[0]);
                mWhite.get(findDrawIdxByLabel(mSelectedPiece.getLabel(),
                        mSelectedPiece.getTeam())).setPieceY(newPosition[1]);
                break;
        }
        mTurn = oppositeTeam(mTurn);
        mIsPieceSelected = false;
    }

    public void newGame(){
        removeAllPieceVew();
        mDrawBoard = new DrawBoard(getApplicationContext(), mBoardPositionPoints, BOARD_SIZE);
        mFrame.addView(mDrawBoard);
        //setup piece
        mBlack.clear();
        mWhite.clear();
        for(int i = 0; i<BOARD_SIZE ; i++){
            DrawPiece newBlack = new DrawPiece(getApplicationContext(),
                    mBoardPositionPoints[i][0][0],
                    mBoardPositionPoints[i][0][1], mPieceSize, TEAM.BLACK, i+1, false);
            mBlack.add(newBlack);
            mFrame.addView(newBlack);
            DrawPiece newWhite = new DrawPiece(getApplicationContext(),
                    mBoardPositionPoints[i][BOARD_SIZE-1][0],
                    mBoardPositionPoints[i][BOARD_SIZE-1][1], mPieceSize, TEAM.WHITE, i+1, false);
            mWhite.add(newWhite);
            mFrame.addView(newWhite);
        }
        //setup board
        mTurn = mFirstTurn;
        mBoard = new Board(mBoardPositionPoints, mIsWhiteAI, mIsBlackAI, mAILevel, mAIEnhanced,
                mTurn, mRandom);
        mIsPieceSelected = false;
        mSelectedPiece = new Piece(0, 0, TEAM.BLACK, 1);
        aiMove();
        newGameToast();
    }

    public void trySelectedNewPiece(TEAM team, float xTap, float yTap){
        ArrayList<DrawPiece> currTeam = null;
        switch (team){
            case BLACK:
                currTeam = mBlack;
                break;
            case WHITE:
                currTeam = mWhite;
                break;
        }
        for (DrawPiece piece : currTeam) {
            if (piece.intersects(xTap, yTap)) {
                Log.d(TAG, "intersects piece " + String.valueOf(piece.getLabel()));
                piece.lock();
                mIsPieceSelected = true;
                if(piece.mTeam == TEAM.BLACK)
                    mSelectedPiece.copyFrom(mBoard.getBlackPieceByLabel(piece.getLabel()));
                else
                    mSelectedPiece.copyFrom(mBoard.getWhitePieceByLabel(piece.getLabel()));
                break;
            }
        }
    }

    public void aiMove(){
        if( (mTurn == TEAM.BLACK && mIsBlackAI) ||
                (mTurn == TEAM.WHITE && mIsWhiteAI)){
            Piece movedPiece = mBoard.aiMoves();
            int[] newCoord = new int[2];
            newCoord[0] = movedPiece.getPieceX();
            newCoord[1] = movedPiece.getPieceY();
            mSelectedPiece.copyFrom(movedPiece);
            movePiece(newCoord);
            syncTeammate(mTurn);
            if(checkIfWin()){
                newGame();
            }
        }
    }

    public boolean checkIfWin(){
        boolean isWin = false;
        if(mBoard.isBlackVictory() ||
                (mBoard.getNextTurn()==TEAM.WHITE && !(mBoard.isNextHasValidMoves()))){
            isWin = true;
            showAboutDialog(TEAM.BLACK);
        }
        if(mBoard.isWhiteVictory() ||
                (mBoard.getNextTurn()==TEAM.BLACK&& (!mBoard.isNextHasValidMoves()))) {
            isWin = true;
            showAboutDialog(TEAM.WHITE);
        }
            return isWin;
    }

    private void removeAllPieceVew(){
        ArrayList<Integer> idxsToBeDeleted = new ArrayList<>();
        for(int i=0; i<mFrame.getChildCount(); i++){
            if(mFrame.getChildAt(i) instanceof DrawPiece){
                idxsToBeDeleted.add(i);
            }
        }
        for(int i=idxsToBeDeleted.size()-1;i>=0;i--)
            mFrame.removeViewAt(idxsToBeDeleted.get(i));

    }

    private void syncTeammate(TEAM team){
        HashSet<Integer> labelHash = new HashSet<>();
        ArrayList<Piece> teammate = null;
        ArrayList<DrawPiece> teammateDraw = null;
        switch (team){
            case BLACK:
                teammate = mBoard.getTeamBlack();
                teammateDraw = mBlack;
                break;
            case WHITE:
                teammate = mBoard.getTeamWhite();
                teammateDraw = mWhite;
                break;
        }
        for(Piece piece: teammate)
            labelHash.add(piece.getLabel());
        ArrayList<Integer> removeList = new ArrayList<>();
        for(DrawPiece drawPiece: teammateDraw)
            if(!labelHash.contains(drawPiece.getLabel()))
                removeList.add(drawPiece.getLabel());
        for(int label: removeList)
            removeDrawPieceViewByLabel(mTurn, label);
        for(int label: removeList)
            removeDrawPieceByLabel(mTurn, label);
    }

    public void showAboutDialog(TEAM winTeam){

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.win_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        TextView textView = (TextView) dialogView.findViewById(R.id.dialog_win_title);
        switch(winTeam){
            case BLACK:
                textView.setText(R.string.dialog_black_is_win);;
                break;
            case WHITE:
                textView.setText(R.string.dialog_white_is_win);;
                break;
        }

        dialogBuilder.setPositiveButton(R.string.about_dialog_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(R.string.about_dialog_donate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gotoDonateWebsite();
                dialog.dismiss();
            }
        });
        dialogBuilder.show();

    }

    private void gotoDonateWebsite(){
        Intent visitDonateWebsite = new Intent(Intent.ACTION_VIEW);
        visitDonateWebsite.setData(Uri.parse(DONATE_URL));
        startActivity(visitDonateWebsite);
    }


    public void invalidMoveToast(){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, R.string.toast_invalid_move, duration);
        toast.show();
    }

    public void newGameToast(){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, R.string.toast_new_game, duration);
        toast.show();
    }

    // new class for draw piece
    public class DrawPiece extends View{

        private float mX;
        private float mY;
        private int mRadius;
        private int mLabel;
        private TEAM mTeam;
        private final Paint mPainter = new Paint();
        private boolean mIsLocked;
        private ScheduledFuture<?> mMoverFuture;

        DrawPiece(Context context, float x, float y, int radius, TEAM team, int label, boolean isLocked){
            super(context);
            mX = x;
            mY = y;
            mRadius = radius;
            mLabel = label;
            mTeam = team;
            mIsLocked = isLocked;
            Log.v(TAG, "construct new piece with label " + String.valueOf(mTeam) + " " +
                    String.valueOf(mLabel) );
        }

        public void lock(){
            mIsLocked = true;
        }

        public void unlock(){
            mIsLocked = false;
        }

        public boolean getIsLocked(){
            return mIsLocked;
        }

        public void setPieceX(float x){
            mX = x;
        }

        public int getLabel(){
            return mLabel;
        }

        public TEAM getTeam(){
            return mTeam;
        }

        public void setPieceY(float y){
            mY = y;
        }

        public boolean intersects(float x, float y){
            float dx = (mX - x);
            float dy = (mY - y);
            if( dx*dx + dy*dy < mRadius*mRadius)
                return true;
            else
                return false;
        }

        @Override
        protected synchronized void onDraw(Canvas canvas){
            canvas.save();
            mPainter.setStrokeWidth(2);
            mPainter.setColor(Color.BLACK);
            mPainter.setStyle(Paint.Style.FILL);
            switch(mTeam){
                case BLACK:
                    mPainter.setColor(Color.BLACK);
                    break;
                case WHITE:
                    mPainter.setColor(Color.WHITE);
                    break;
            }
            canvas.drawCircle(mX, mY, mRadius, mPainter);
            mPainter.setStrokeWidth(2);
            mPainter.setColor(Color.BLACK);
            mPainter.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mX, mY, mRadius, mPainter);
            if(mIsLocked){
                Log.d(TAG, "locked piece " + String.valueOf(getLabel()));
                mPainter.setColor(Color.GRAY);
                mPainter.setAlpha(100);
                mPainter.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mX, mY, mRadius, mPainter);
            }
            canvas.restore();
        }

    }

    // new class that draws board
    public class DrawBoard extends View {

        private int mSize;
        private float[][][] mBoardPositionPoints;
        private final Paint mPainter = new Paint();

        DrawBoard(Context context, float[][][] boardPositionPoints, int size){
            super(context);
            Log.v(TAG, "construct draw board object");
            mBoardPositionPoints = boardPositionPoints;
            mPainter.setAntiAlias(true);
            mSize = size;

        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            mPainter.setStrokeWidth(5);
            mPainter.setColor(Color.BLACK);
            for(int i = 0; i<mSize ; i++){
                canvas.drawLine(mBoardPositionPoints[i][0][0],mBoardPositionPoints[i][0][1]
                        ,mBoardPositionPoints[i][mSize-1][0],
                        mBoardPositionPoints[i][mSize-1][1], mPainter );
                canvas.drawLine(mBoardPositionPoints[0][i][0],mBoardPositionPoints[0][i][1]
                        ,mBoardPositionPoints[mSize-1][i][0],
                        mBoardPositionPoints[mSize-1][i][1], mPainter);
            }
        }


    }
}
