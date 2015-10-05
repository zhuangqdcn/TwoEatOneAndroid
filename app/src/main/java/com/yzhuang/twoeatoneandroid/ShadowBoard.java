package com.yzhuang.twoeatoneandroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

/**
 * Created by yuan on 9/26/15.
 */
public class ShadowBoard extends BasicBoard {
    private int mMovePieceLabel;
    private int[] mMovePieceCoord;
    private int mAILevelInteger;
    private boolean mAILevelEnhance;
    private Stack<TrackChange> mChange;
    private ArrayList<NextMove> mMoves;
    private Random mRandom;

    private static int ENHANCE_MULTIPLIER = 2;

    private static String TAG = "ShadowBoard:";

    // TODO repeat move checker
    // TODO aggressive AI


    class NextMove{
        int mLabel;
        int mXNew;
        int mYNew;
        int mLotteries;
        int mEnhancedModifier;
        NextMove(int label, int xNew, int yNew, int lotteries){
            mLabel = label;
            mXNew = xNew;
            mYNew = yNew;
            mLotteries = lotteries;
            mEnhancedModifier = 0;
        }

        @Override
        public String toString(){
            return "Label:"+String.valueOf(mLabel)+", xNew:"+String.valueOf(mXNew)
                    + ", yNew:" + String.valueOf(mYNew) + ", Lotteries:" + String.valueOf(mLotteries)
                    + ", modifier:" + String.valueOf(mEnhancedModifier);
        }
    }

    public ShadowBoard(Board board){
        super(board.getTeamBlack(), board.getTeamWhite(), board.getNextTurn());
        mMovePieceCoord = new int[2];
        mAILevelInteger = board.getAILevel();
        mAILevelEnhance = board.getAILevelEnhance();
        mChange  = new Stack<>();
        mMoves = new ArrayList<>();
        mRandom = board.getRandom();
    }

    public ShadowBoard(ShadowBoard board){
        super(board.getTeamBlack(), board.getTeamWhite(), board.getNextTurn());
        mMovePieceCoord = new int[2];
        mAILevelEnhance = board.getAILevelEnhance();
        mChange  = new Stack<>();
        mMoves = new ArrayList<>();
        mRandom = board.getRandom();
    }

    public void generateAIMoves(){
        int[] tickets;
        boolean isAllLotteriesZero = true;
        ArrayList<Piece> currTeam = getTeamBlack();
        switch(getNextTurn()){
            case BLACK:
                currTeam = getTeamBlack();
                break;
            case WHITE:
                currTeam = getTeamWhite();
                break;
        }
        // generate possible moves
        mMoves.clear();
        for(Piece piece: currTeam){
            if(isValidMove(piece.getTeam(), piece, piece.getPieceX()+1, piece.getPieceY()))
                mMoves.add(new NextMove(piece.getLabel()
                        , piece.getPieceX()+1, piece.getPieceY(), 1));
            if(isValidMove(piece.getTeam(), piece, piece.getPieceX()-1, piece.getPieceY()))
                mMoves.add(new NextMove(piece.getLabel()
                        , piece.getPieceX()-1, piece.getPieceY(), 1));
            if(isValidMove(piece.getTeam(), piece, piece.getPieceX(), piece.getPieceY()+1))
                mMoves.add(new NextMove(piece.getLabel()
                        , piece.getPieceX(), piece.getPieceY()+1, 1));
            if(isValidMove(piece.getTeam(), piece, piece.getPieceX(), piece.getPieceY()-1))
                mMoves.add(new NextMove(piece.getLabel()
                        , piece.getPieceX(), piece.getPieceY()-1, 1));
        }
        if(mAILevelInteger == 0){
            isAllLotteriesZero = false;
        }
        else if(mAILevelInteger == 1){
            isAllLotteriesZero = false;
            for(NextMove move : mMoves){
                int idxPiece = 0;
                for(int i=0;i<currTeam.size();i++){
                    if(currTeam.get(i).getLabel()==move.mLabel){
                        idxPiece = i;
                        HashSet<Piece> killedList = possibleKills(currTeam.get(i).getTeam(), i, move.mXNew, move.mYNew);
                        move.mLotteries += 100 * killedList.size(); // award 100 lotteries per kill
                        if(mAILevelEnhance){
                            move.mEnhancedModifier = enhanceWeight(currTeam, idxPiece, move);
                        }
                        break;
                    }
                }
            }
        }
        else if(mAILevelInteger == 2){ // AIlevel 2
            ShadowBoard nextShadowBoard = new ShadowBoard(this);
            nextShadowBoard.setAILevelInteger(mAILevelInteger-1);
            nextShadowBoard.setAILevelEnhance(false);
            //System.out.println(nextShadowBoard.toString());
            for(NextMove move : mMoves){
                nextShadowBoard.movePieceByLabel(getNextTurn(), move.mLabel, move.mXNew, move.mYNew);
                if( (getNextTurn()==TEAM.BLACK && nextShadowBoard.isBlackVictory()) ||
                        (getNextTurn()==TEAM.WHITE && nextShadowBoard.isWhiteVictory())){
                    move.mLotteries = Integer.MAX_VALUE;
                    mMovePieceLabel = move.mLabel;
                    mMovePieceCoord[0] = move.mXNew;
                    mMovePieceCoord[1] = move.mYNew;
                    return;
                }
                else{
                    nextShadowBoard.generateAIMoves();
                    ArrayList<NextMove> possibleOppositeMove = nextShadowBoard.getPossibleMovesWithLotteries();
                    int maxOppoLotteries = 0;
                    for(NextMove oppoMove : possibleOppositeMove){
                        if(oppoMove.mLotteries > maxOppoLotteries)
                            maxOppoLotteries = oppoMove.mLotteries;
                    }
                    maxOppoLotteries--;
                    // System.out.println( TAG + move.toString()
                    // +" Max_oppo " + String.valueOf(maxOppoLotteries));
                    move.mLotteries -= maxOppoLotteries;
                    if(move.mLotteries<-100)	//map kill 2 into -1
                        move.mLotteries = -1;
                    else if(move.mLotteries<=0)
                        move.mLotteries=0;
                    else
                        isAllLotteriesZero = false;
                }
                nextShadowBoard.revertLastMove();
                //System.out.println(nextShadowBoard.toString());
                if(mAILevelEnhance){
                    move.mEnhancedModifier = enhanceWeight(currTeam, label2Index(getNextTurn(), move.mLabel), move);
                }
            }
        }
        enhancedAIModify(mMoves);
        //generate lottery array, should always have possible moves

        tickets = new int[mMoves.size()];
        try{
            tickets[0] = mMoves.get(0).mLotteries;
        } catch(IndexOutOfBoundsException e){
            System.out.println( TAG + "No valid moves" );
        }
        if(isAllLotteriesZero)
            tickets[0]++;
        for(int i=1;i<mMoves.size();i++){
            if(isAllLotteriesZero){
                tickets[i] = tickets[i-1] + mMoves.get(i).mLotteries;
                tickets[i]++;
            }
            else{
                if(mMoves.get(i).mLotteries>0)	// deal with -1 case
                    tickets[i] = tickets[i-1] + mMoves.get(i).mLotteries;
                else
                    tickets[i] = tickets[i-1];
            }
        }

        // choose tickets
        int winner = lottery(tickets, mRandom);
        // convert label to idx.
        int winnerLabel = mMoves.get(winner).mLabel;
        // System.out.println( TAG + String.valueOf(mAILevelInteger)+ Arrays.toString(tickets)
        // +"Winner is "+String.valueOf(winner));
        mMovePieceLabel = winnerLabel;
        mMovePieceCoord[0] = mMoves.get(winner).mXNew;
        mMovePieceCoord[1] = mMoves.get(winner).mYNew;
    }

    private int lottery(int[] tickets, Random rand){
        int idx = Arrays.binarySearch(tickets, rand.nextInt(tickets[tickets.length - 1]) + 1);
        if(idx>0){
            while(idx>0 && tickets[idx-1]==tickets[idx])
                idx--;
        }
        else if(idx<0)
            idx = -(idx+1);

        return idx;
    }

    public ArrayList<NextMove> getPossibleMovesWithLotteries(){
        return mMoves;
    }

    private int enhanceWeight(ArrayList<Piece> currTeam, int moveIdx, NextMove move){
        int moveWeight = 0;
        for(int i=0;i<currTeam.size();i++){
            if(i!=moveIdx){	//this enhanced AI is by minimizing the summation of the distances.
                int dx = currTeam.get(i).getPieceX() - move.mXNew;
                int dy = currTeam.get(i).getPieceY() - move.mYNew;
                if(dx*dx+dy*dy>moveWeight)
                    moveWeight = dx*dx + dy*dy;
            }
        }
        return moveWeight;
    }

    private void enhancedAIModify(ArrayList<NextMove> moves){
        int maxModifier = 0;
        for(NextMove move : moves){
            if(move.mEnhancedModifier>maxModifier)
                maxModifier = move.mEnhancedModifier;
        }
        for(NextMove move : moves){
            if(move.mLotteries!=0)
                move.mLotteries += ENHANCE_MULTIPLIER*(maxModifier-move.mEnhancedModifier);
        }
    }



    private boolean isPieceOnPoint(int xx, int yy){
        for ( Piece pieces : getTeamWhite() ){
            if (pieces.getPieceX()==xx&&pieces.getPieceY()==yy){
                return true;
            }
        }
        for ( Piece pieces : getTeamBlack()){
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
    @Override
    public void movePiece(TEAM team, int idx, int xNew, int yNew){
        Piece piece;
        if(team==TEAM.BLACK)
            piece = getTeamBlack().get(idx);
        else
            piece = getTeamWhite().get(idx);
        HashSet<Piece> killedPieces = possibleKills(piece.getTeam(), idx, xNew, yNew);
        mChange.push(new TrackChange(piece, killedPieces)); //record last move
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
        return;
    }

    @Override
    public void movePieceByLabel(TEAM team, int label, int xNew, int yNew){
        int idx = label2Index(team, label);
        Piece piece = null;
        switch(team){
            case WHITE:
                piece = getTeamWhite().get(idx);
                break;
            case BLACK:
                piece = getTeamBlack().get(idx);
                break;
        }
        HashSet<Piece> killedPieces = possibleKills(piece.getTeam(), idx, xNew, yNew);
        mChange.push(new TrackChange(piece, killedPieces)); //record last move
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
        return;
    }

    public void revertLastMove(){
        TrackChange lastChange = mChange.pop();
        ArrayList<Piece> thisTeam = null;
        //ArrayList<Piece> oppoTeam = null;
        switch(getNextTurn()){
            case BLACK:
                thisTeam = getTeamWhite();
                //oppoTeam = getTeamBlack();
                break;
            case WHITE:
                thisTeam = getTeamBlack();
                //oppoTeam = getTeamWhite();
        }
        int oldLabel = lastChange.getOldPieceLabel();
        int oldIdx = label2Index(lastChange.getOldPieceTeam(), oldLabel);
        // System.out.println("oldIndex "+oldIdx);
        thisTeam.get(oldIdx).setCoord(lastChange.getOldPieceX(), lastChange.getOldPieceY());
        for(int i=0 ;i<lastChange.getKills().size(); i++){
            TrackChange.TrackPiece trackPiece = lastChange.getKills().get(i);
            addNew(trackPiece.mTeam, trackPiece.x, trackPiece.y, trackPiece.mLabel);
        }
        switch(getNextTurn()){
            case BLACK:
                setNextTurn( TEAM.WHITE);
                break;
            case WHITE:
                setNextTurn( TEAM.BLACK);
                break;
        }
    }

    public int getMovePieceLabel(){
        return mMovePieceLabel;
    }

    public int getMovePieceNewX(){
        return mMovePieceCoord[0];
    }

    public int getMovePieceNewY(){
        return mMovePieceCoord[1];
    }

    public void setAILevelInteger(int aiLevel){
        mAILevelInteger = aiLevel;
    }

    public void setAILevelEnhance(boolean aiLevelEnhance){
        mAILevelEnhance = aiLevelEnhance;
    }

    public Random getRandom(){
        return mRandom;
    }

    public boolean getAILevelEnhance(){
        return mAILevelEnhance;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Black");
        stringBuilder.append("\n");
        for(Piece piece: getTeamBlack()){
            stringBuilder.append(piece.toString());
            stringBuilder.append("\n");
        }
        stringBuilder.append("White");
        stringBuilder.append("\n");
        for(Piece piece: getTeamWhite()){
            stringBuilder.append(piece.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
