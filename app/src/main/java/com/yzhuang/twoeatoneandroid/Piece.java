package com.yzhuang.twoeatoneandroid;

import com.yzhuang.twoeatoneandroid.BasicBoard.TEAM;
/**
 * Created by yuan on 9/26/15.
 */
public class Piece {
    private int mX;	//the x coordinate of the piece
    private int mY;	//the y coordinate of the piece
    private int mLabel; // unique identifier
    private TEAM mTeam;

    Piece(Piece piece){
        mX = piece.getPieceX();
        mY = piece.getPieceY();
        mTeam = piece.getTeam();
        mLabel = piece.getLabel();
    }


    Piece(int xx, int yy, TEAM team, int label){
        mX = xx;
        mY = yy;
        mTeam = team;
        mLabel = label;
    }

    public void setCoord(int xx, int yy){
        mX = xx;
        mY = yy;
        return;
    }

    public TEAM getTeam(){
        return this.mTeam;
    }

    public int getPieceX(){
        return mX;
    }

    public int getPieceY(){
        return mY;
    }

    public int getLabel(){
        return mLabel;
    }

    public void setLabel(int label){
        mLabel = label;
    }

    public void setTeam(TEAM team){
        mTeam = team;
    }

    public void setBundle(int xx, int yy, TEAM team, int label){
        mX = xx;
        mY = yy;
        mTeam = team;
        mLabel = label;
    }

    public void copyFrom(Piece piece){
        mX = piece.getPieceX();
        mY = piece.getPieceY();
        mLabel = piece.getLabel();
        mTeam = piece.getTeam();
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(mTeam));
        stringBuilder.append(": ");
        stringBuilder.append("label " + String.valueOf(mLabel));
        stringBuilder.append(" x " + String.valueOf(mX));
        stringBuilder.append(" y " + String.valueOf(mY));

        return stringBuilder.toString();
    }
}
