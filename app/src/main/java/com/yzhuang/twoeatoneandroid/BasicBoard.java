package com.yzhuang.twoeatoneandroid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by yuan on 9/26/15.
 */
public abstract class BasicBoard {

    public enum TEAM{
        WHITE, BLACK
    }

    private ArrayList<Piece> mBlack;
    private ArrayList<Piece> mWhite;
    private TEAM mTurn;
    public static final int SIZE = 5;


    private static String TAG = "BasicBoard";

    public BasicBoard(TEAM firstTurn){
        mBlack = new ArrayList<>();
        mWhite = new ArrayList<>();
        mTurn = firstTurn;
    }

    public BasicBoard(ArrayList<Piece> black, ArrayList<Piece> white, TEAM firstTurn){
        mBlack = new ArrayList<>();
        mWhite = new ArrayList<>();
        for(int i=0; i<black.size(); i++)
            mBlack.add(new Piece(black.get(i)));
        for(int i=0; i<white.size(); i++)
            mWhite.add(new Piece(white.get(i)));
        mBlack = black;
        mWhite = white;
        mTurn = firstTurn;
    }

    public boolean addNew(TEAM team, int xx, int yy, int label){
        if(this.isPieceOnPoint(xx, yy)||xx<0||xx>=SIZE||yy<0||yy>=SIZE)
            return false;
        else{
            if(team==TEAM.BLACK)
                if(mBlack.size()>=SIZE)
                    return false;
                else{
                    mBlack.add(new Piece(xx, yy, TEAM.BLACK, label));
                    return true;
                }
            else if(team==TEAM.WHITE)
                if(mWhite.size()>=SIZE)
                    return false;
                else{
                    mWhite.add(new Piece(xx, yy, TEAM.WHITE, label));
                    return true;
                }
        }
        return false;
    }

    private boolean isPieceOnPoint(int xx, int yy){
        for ( Piece pieces : mWhite ){
            if (pieces.getPieceX()==xx&&pieces.getPieceY()==yy){
                return true;
            }
        }
        for ( Piece pieces : mBlack ){
            if (pieces.getPieceX()==xx&&pieces.getPieceY()==yy){
                return true;
            }
        }
        return false;
    }

    /**
     * Move the piece idx of team to xNew yNew
     * @param team team side
     * @param idx index of the piece
     * @param xNew new x coordinate
     * @param yNew new y coordinate
     */
    public void movePiece(TEAM team, int idx, int xNew, int yNew){
        Piece piece;
        if(team==TEAM.BLACK)
            piece = mBlack.get(idx);
        else
            piece = mWhite.get(idx);
        HashSet<Piece> killedPieces = possibleKills(piece.getTeam(), idx, xNew, yNew);
        killPieces(killedPieces, piece.getTeam());
        piece.setCoord(xNew, yNew);
        switch(mTurn){
            case BLACK:
                mTurn = TEAM.WHITE;
                break;
            case WHITE:
                mTurn = TEAM.BLACK;
                break;
        }
        return;
    }

    public void movePieceByLabel(TEAM team, int label, int xNew, int yNew){
        int idx = label2Index(team, label);
        Piece piece = null;
        switch(team){
            case WHITE:
                piece = mWhite.get(idx);
                break;
            case BLACK:
                piece = mBlack.get(idx);
                break;
        }
        HashSet<Piece> killedPieces = possibleKills(piece.getTeam(), idx, xNew, yNew);
        killPieces(killedPieces, piece.getTeam());
        piece.setCoord(xNew, yNew);
        switch(mTurn){
            case BLACK:
                mTurn = TEAM.WHITE;
                break;
            case WHITE:
                mTurn = TEAM.BLACK;
                break;
        }
        return;
    }
    /**
     * get a set of the piece that possibly can be killed
     * @param team team side
     * @param idx piece idx of team
     * @param xNew new x coordinate
     * @param yNew new y coordinate
     * @return a hashset of the piece when move to xNew yNew
     */
    public HashSet<Piece> possibleKills(TEAM team, int idx, int xNew, int yNew){
        ArrayList<Piece> teammateOnX = new ArrayList<>();
        ArrayList<Piece> oppositeOnX = new ArrayList<>();
        ArrayList<Piece> teammateOnY = new ArrayList<>();
        ArrayList<Piece> oppositeOnY = new ArrayList<>();
        HashSet<Piece> possibleKillList = new HashSet<>();
        ArrayList<Piece> teammate;
        ArrayList<Piece> opposite;
        if(team==TEAM.BLACK){
            teammate = mBlack;
            opposite = mWhite;
        }
        else{
            teammate = mWhite;
            opposite = mBlack;
        }
        //first found all the piece on x and y
        for(int i=0; i<teammate.size();i++){
            if(i!=idx){
                if(teammate.get(i).getPieceX()==xNew)
                    teammateOnX.add(teammate.get(i));
                if(teammate.get(i).getPieceY()==yNew)
                    teammateOnY.add(teammate.get(i));
            }
        }
        for(Piece oppositePiece: opposite){
            if(oppositePiece.getPieceX()==xNew)
                oppositeOnX.add(oppositePiece);
            if(oppositePiece.getPieceY()==yNew)
                oppositeOnY.add(oppositePiece);
        }
        //examine kill condition
        if(oppositeOnX.size()==1&&teammateOnX.size()==1){
            if(Math.abs(teammateOnX.get(0).getPieceY()-yNew)==1){
                if(yNew+teammateOnX.get(0).getPieceY()+oppositeOnX.get(0).getPieceY()
                        ==3*mid3(yNew,teammateOnX.get(0).getPieceY(),oppositeOnX.get(0).getPieceY())){
                    possibleKillList.add(oppositeOnX.get(0));
                }
            }
        }
        if(oppositeOnY.size()==1&&teammateOnY.size()==1){
            if(Math.abs(teammateOnY.get(0).getPieceX()-xNew)==1){
                if(xNew+teammateOnY.get(0).getPieceX()+oppositeOnY.get(0).getPieceX()
                        ==3*mid3(xNew,teammateOnY.get(0).getPieceX(),oppositeOnY.get(0).getPieceX())){
                    possibleKillList.add(oppositeOnY.get(0));
                }
            }
        }
        return possibleKillList;
    }

    public int[] getLabelsOfKilledList(Piece piece, int xNew, int yNew){
        HashSet<Piece> killedList = possibleKills(piece.getTeam(),
                label2Index(piece.getTeam(), piece.getLabel()), xNew, yNew);
        int[] killedLabel = new int[killedList.size()];
        int idx = 0;
        for(Piece killPiece: killedList){
            killedLabel[idx] = killPiece.getLabel();
            idx++;
        }
        return killedLabel;
    }

    private int mid3(int a, int b, int c){
        return a>b?  ( c>a? a : (b>c? b:c) )  :  ( c>b? b : (a>c? a:c) ) ;
    }

    /**
     * remove set pieces from the board
     * @param pieces pieces
     * @param team team side
     */
    public void killPieces(HashSet<Piece> pieces, TEAM team){
        if(pieces.size()>0){
            int idx = 0;
            ArrayList<Piece> killTeam = null;
            switch(team){
                case BLACK:
                    killTeam = mWhite;
                    break;
                case WHITE:
                    killTeam = mBlack;
                    break;
            }
            while(idx<killTeam.size()){
                if(pieces.contains(killTeam.get(idx))){
                    killTeam.remove(idx);
                }
                else
                    idx++;
            }
        }
    }

    /**
     * determine whether a move is valid or not
     * @param team team side
     * @param idxPiece piece index of team
     * @param xNew new x coordinate
     * @param yNew new y coordinate
     * @return true: a valid move, false: invalid move.
     */
    public boolean isValidMove(TEAM team, int idxPiece, int xNew, int yNew){
        int xOld = getPieceX(team, idxPiece);
        int yOld = getPieceY(team, idxPiece);
        if(team!=mTurn)
            return false;
        if(xNew<1||xNew>SIZE)
            return false;
        if(yNew<1||yNew>SIZE)
            return false;
        if(Math.abs(xNew-xOld)>1)
            return false;
        if(Math.abs(yNew-yOld)>1)
            return false;
        if(Math.abs(yNew-yOld)==1&&Math.abs(xNew-xOld)==1)
            return false;
        if(Math.abs(yNew-yOld)==0&&Math.abs(xNew-xOld)==0)
            return false;
        if(this.isPieceOnPoint(xNew, yNew))
            return false;
        return true;
    }


    public boolean isValidMove(Piece piece, int xNew, int yNew){
        int idxPiece = label2Index(piece.getTeam(), piece.getLabel());
        TEAM team = piece.getTeam();
        int xOld = getPieceX(team, idxPiece);
        int yOld = getPieceY(team, idxPiece);
        if(team!=mTurn)
            return false;
        if(xNew<1||xNew>SIZE)
            return false;
        if(yNew<1||yNew>SIZE)
            return false;
        if(Math.abs(xNew-xOld)>1)
            return false;
        if(Math.abs(yNew-yOld)>1)
            return false;
        if(Math.abs(yNew-yOld)==1&&Math.abs(xNew-xOld)==1)
            return false;
        if(Math.abs(yNew-yOld)==0&&Math.abs(xNew-xOld)==0)
            return false;
        if(this.isPieceOnPoint(xNew, yNew))
            return false;
        return true;
    }

    /**
     * overloaded method by using currPiece object.
     * @param team team side
     * @param currPiece current piece object
     * @param xNew new x coordinate
     * @param yNew new y coordinate
     * @return
     */
    public boolean isValidMove(TEAM team, Object currPiece, int xNew, int yNew){
        Piece piece = (Piece) currPiece;
        int xOld = piece.getPieceX();
        int yOld = piece.getPieceY();
        if(piece.getTeam()!=mTurn)
            return false;
        if(xNew<1||xNew>SIZE)
            return false;
        if(yNew<1||yNew>SIZE)
            return false;
        if(Math.abs(xNew-xOld)>1)
            return false;
        if(Math.abs(yNew-yOld)>1)
            return false;
        if(Math.abs(yNew-yOld)==1&&Math.abs(xNew-xOld)==1)
            return false;
        if(Math.abs(yNew-yOld)==0&&Math.abs(xNew-xOld)==0)
            return false;
        if(isPieceOnPoint(xNew, yNew))
            return false;
        return true;
    }

    /**
     * determine whether black is victory
     * @return true: black is victory, false: not
     */
    public boolean isBlackVictory(){
        if(mWhite.size()==1 || (mTurn==TEAM.WHITE && !(isWhiteHasValidMoves())))
            return true;
        else
            return false;
    }

    /**
     * determine whether white is victory
     * @return true: white is victory, false: not
     */
    public boolean isWhiteVictory(){
        if(mBlack.size()==1 || (mTurn==TEAM.BLACK && !(isBlackHasValidMoves())))
            return true;
        else
            return false;
    }

    public boolean isNextHasValidMoves(){
        switch(getNextTurn()){
            case WHITE:
                if(isWhiteHasValidMoves())
                    return true;
                else
                    return false;
            case BLACK:
                if(isBlackHasValidMoves())
                    return true;
                else
                    return false;
        }
        return true;
    }

    public boolean isWhiteHasValidMoves(){
        for(Piece piece: mWhite){
            if(isValidMove(TEAM.WHITE, piece, piece.getPieceX()+1, piece.getPieceY()))
                return true;
            if(isValidMove(TEAM.WHITE, piece, piece.getPieceX()-1, piece.getPieceY()))
                return true;
            if(isValidMove(TEAM.WHITE, piece, piece.getPieceX(), piece.getPieceY()+1))
                return true;
            if(isValidMove(TEAM.WHITE, piece, piece.getPieceX(), piece.getPieceY()-1))
                return true;
        }
        return false;
    }

    public boolean isBlackHasValidMoves(){
        for(Piece piece: mBlack){
            if(isValidMove(TEAM.BLACK, piece, piece.getPieceX()+1, piece.getPieceY()))
                return true;
            if(isValidMove(TEAM.BLACK, piece, piece.getPieceX()-1, piece.getPieceY()))
                return true;
            if(isValidMove(TEAM.BLACK, piece, piece.getPieceX(), piece.getPieceY()+1))
                return true;
            if(isValidMove(TEAM.BLACK, piece, piece.getPieceX(), piece.getPieceY()-1))
                return true;
        }
        return false;
    }

    public int label2Index(TEAM team, int label){
        ArrayList<Piece> currTeam;
        int idx = -1;
        if(team==TEAM.BLACK)
            currTeam = mBlack;
        else
            currTeam = mWhite;
        for(int i=0; i<currTeam.size();i++){
            // System.out.println("label to index "+String.valueOf(label));
            // System.out.println(currTeam.get(i).toString());
            if(currTeam.get(i).getLabel()==label){
                idx = i;
                break;
            }
        }
        return idx;
    }

    public int remainingPieces(TEAM team){
        if(team==TEAM.BLACK)
            return mBlack.size();
        else
            return mWhite.size();
    }

    public int getPieceX(TEAM team, int idx){
        if(team==TEAM.BLACK)
            return mBlack.get(idx).getPieceX();
        else
            return mWhite.get(idx).getPieceX();
    }

    public int getPieceY(TEAM team, int idx) {
        if(team==TEAM.BLACK)
            return mBlack.get(idx).getPieceY();
        else
            return mWhite.get(idx).getPieceY();
    }

    public TEAM getNextTurn(){
        return mTurn;
    }

    public void setNextTurn(TEAM team){
        mTurn = team;
    }

    public Piece getPieceByLabel(TEAM team, int label){
        Piece piece = null;
        switch(team){
            case BLACK:
                piece = getBlackPieceByLabel(label);
                break;
            case WHITE:
                piece = getWhitePieceByLabel(label);
                break;
        }
        return piece;
    }

    public Piece getBlackPieceById(int idx){
        return mBlack.get(idx);
    }

    public Piece getBlackPieceByLabel(int label){
        return mBlack.get(label2Index(TEAM.BLACK, label));
    }

    public int getBlackRemaining(){
        return mBlack.size();
    }

    public Piece getWhitePieceById(int idx){
        return mWhite.get(idx);
    }

    public Piece getWhitePieceByLabel(int label){
        return mWhite.get(label2Index(TEAM.WHITE, label));
    }

    public int getWhiteRemaining(){
        return mWhite.size();
    }

    public ArrayList<Piece> getTeamBlack(){
        return mBlack;
    }

    public ArrayList<Piece> getTeamWhite(){
        return mWhite;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Black");
        stringBuilder.append("\n");
        for(Piece piece: mBlack){
            stringBuilder.append(piece.toString());
            stringBuilder.append("\n");
        }
        stringBuilder.append("White");
        stringBuilder.append("\n");
        for(Piece piece: mWhite){
            stringBuilder.append(piece.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
