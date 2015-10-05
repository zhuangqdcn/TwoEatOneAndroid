package com.yzhuang.twoeatoneandroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by yuan on 9/26/15.
 */
public class Board extends BasicBoard{

    private float[][][] mPositions;
    private boolean mIsBlackAI;
    private boolean mIsWhiteAI;
    private int mAILevel;
    private boolean mAIEnhanced;

    private Random mRandom;

    private static String TAG = "Board";
    /**
     * Default constructor of the board.
     * @param positions all the possible positions in the board
     * @param firstTurn the first turn of the team
     */
    public Board(float[][][] positions,  boolean isWhiteAI, boolean isBlackAI,
                 int aiLevel, boolean aiEnhance, TEAM firstTurn, Random random){
        super(firstTurn);

        int i;
        mPositions = positions;
        mAILevel = aiLevel;
        mAIEnhanced = aiEnhance;
        for(i=0;i<SIZE;i++){
            getTeamBlack().add(new Piece(i+1,1,TEAM.BLACK, i+1));
            getTeamWhite().add(new Piece(i+1,SIZE,TEAM.WHITE, i+1));
        }
        mIsWhiteAI = isWhiteAI;
        mIsBlackAI = isBlackAI;
        mRandom = random;
        Log.d(TAG, "New game for" + " BlackAI "+String.valueOf(mIsBlackAI)
                + " WhiteAI "+String.valueOf(mIsWhiteAI)
                + " aiLevel "+String.valueOf(mAILevel)
                + " aiEnhanced "+String.valueOf(mAIEnhanced));
    }

    public Piece aiMoves(){
        Piece returnPiece = null;
        if( !(isBlackVictory() || isWhiteVictory()) ){
            ArrayList<Piece> currTeam = getTeamBlack();
            switch(getNextTurn()){
                case BLACK:
                    currTeam = getTeamBlack();
                    break;
                case WHITE:
                    currTeam = getTeamWhite();
                    break;
            }
            ShadowBoard shadowBoard = new ShadowBoard(this);
            shadowBoard.generateAIMoves();
            int movePieceLabel = shadowBoard.getMovePieceLabel();
            for(Piece piece: currTeam){
                if(piece.getLabel() == movePieceLabel){
                    int xNew = shadowBoard.getMovePieceNewX();
                    int yNew = shadowBoard.getMovePieceNewY();
                    movePiece(piece, xNew, yNew);
                    returnPiece = new Piece(piece);
                    break;
                }
            }

        }
        setNextTurn(oppositeTeam(getNextTurn()));
        return returnPiece;
    }



    /**
     * overloaded method by moving piece object to xNew yNew
     * @param currPiece current Piece object
     * @param xNew new x coordinate
     * @param yNew new y coordinate
     */
    public void movePiece(Piece currPiece, int xNew, int yNew){
        Piece piece = currPiece;
        int idxPiece = 0;
        switch(piece.getTeam()){
            case BLACK:
                for(int i=0;i<getTeamBlack().size();i++){
                    if(getTeamBlack().get(i)==piece){
                        idxPiece = i;
                        break;
                    }
                }
                break;
            case WHITE:
                for(int i=0;i<getTeamWhite().size();i++){
                    if(getTeamWhite().get(i)==piece){
                        idxPiece = i;
                        break;
                    }
                }
                break;
        }
        HashSet<Piece> killedPieces = possibleKills(piece.getTeam(), idxPiece, xNew, yNew);
        killPieces(killedPieces, piece.getTeam());
        piece.setCoord(xNew, yNew);
        switch(getNextTurn()){
            case BLACK:
                setNextTurn(TEAM.WHITE);
                break;
            case WHITE:
                setNextTurn(TEAM.BLACK);
                break;
        }
        if(!(isBlackVictory() || isWhiteVictory())){
            if(mIsWhiteAI&&(getNextTurn()==TEAM.WHITE))
                aiMoves();
            else if(mIsBlackAI&&(getNextTurn()==TEAM.BLACK))
                aiMoves();
        }
    }



    /**
     * remove set pieces from the board
     */


    private boolean isPieceOnPoint(int xx, int yy){
        for ( Piece pieces : getTeamWhite() ){
            if (pieces.getPieceX()==xx&&pieces.getPieceY()==yy){
                return true;
            }
        }
        for ( Piece pieces : getTeamBlack() ){
            if (pieces.getPieceX()==xx&&pieces.getPieceY()==yy){
                return true;
            }
        }
        return false;
    }

    public float[] coord2ScreenCoord(int[] coords){
        float[] screenCoord = new float[2];
        screenCoord[0] = mPositions[coords[0]-1][coords[1]-1][0];
        screenCoord[1] = mPositions[coords[0]-1][coords[1]-1][1];
        return screenCoord;
    }

    public int[] findNearestXYCoord(float xInput, float yInput){
        int[] nearestXYCoord = new int[2];
        float minDistance = Integer.MAX_VALUE;
        for(int i=0;i<SIZE; i++)
            for(int j=0; j<SIZE; j++){
                float newDistance = distance2(xInput, yInput
                        , mPositions[i][j][0], mPositions[i][j][1]);
                if(newDistance<minDistance){
                    minDistance = newDistance;
                    nearestXYCoord[0] = i+1;
                    nearestXYCoord[1] = j+1;
                }
            }
        return nearestXYCoord;
    }

    private float distance2(float x0, float y0, float x1, float y1){
        return (x0-x1)*(x0-x1) +(y0-y1)*(y0-y1);
    }

    public void setIsBlackAI(boolean isBlackAI){
        mIsBlackAI = isBlackAI;
    }

    public void setIsWhiteAI(boolean isWhiteAI){
        mIsWhiteAI = isWhiteAI;
    }

    public boolean getIsBlakcAI(){
        return mIsBlackAI;
    }

    public boolean getIsWhiteAI(){
        return mIsWhiteAI;
    }

    public int getAILevel(){
        return mAILevel;
    }

    public void setAILevel(int aiLevel){
        mAILevel = aiLevel;
    }

    public boolean getAILevelEnhance(){
        return mAIEnhanced;
    }

    public void setAILevelEnhace(boolean aiEnhanced){
        mAIEnhanced = aiEnhanced;
    }

    public Random getRandom(){
        return mRandom;
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
}
