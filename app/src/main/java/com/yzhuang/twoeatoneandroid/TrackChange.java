package com.yzhuang.twoeatoneandroid;

import java.util.ArrayList;
import java.util.HashSet;

import com.yzhuang.twoeatoneandroid.BasicBoard.TEAM;

/**
 * @author Yuan Zhuang
 *
 */
public class TrackChange {
    private int mPieceLabel;
    private TrackPiece mOldPiece;
    private ArrayList<TrackPiece> mKills;

    public class TrackPiece{
        int x;
        int y;
        int mLabel;
        TEAM mTeam;
        TrackPiece(int label, int xx, int yy, TEAM team){
            mLabel = label;
            x = xx;
            y = yy;
            mTeam = team;
        }
    }

    public TrackChange(Piece piece){
        mOldPiece = new TrackPiece(piece.getLabel()
                , piece.getPieceX(), piece.getPieceY(), piece.getTeam());
        mKills = new ArrayList<>();
    }

    public TrackChange(Piece piece, HashSet<Piece> killsList){
        mKills = new ArrayList<>();
        mOldPiece = new TrackPiece(piece.getLabel()
                , piece.getPieceX(), piece.getPieceY(), piece.getTeam());
        for(Piece killedPiece : killsList){
            mKills.add(new TrackPiece(killedPiece.getLabel(), killedPiece.getPieceX(),
                    killedPiece.getPieceY(), killedPiece.getTeam()));
        }

    }

    public void addKills(HashSet<Piece> killsList){
        for(Piece killedPiece : killsList){
            mKills.add(new TrackPiece(killedPiece.getLabel(), killedPiece.getPieceX(),
                    killedPiece.getPieceY(), killedPiece.getTeam()));
        }
    }

    public ArrayList<TrackPiece> getKills(){
        return mKills;
    }

    public TEAM getOldPieceTeam(){
        return mOldPiece.mTeam;
    }

    public int getOldPieceX(){
        return mOldPiece.x;
    }

    public int getOldPieceY(){
        return mOldPiece.y;
    }

    public int getOldPieceLabel(){
        return mOldPiece.mLabel;
    }
}