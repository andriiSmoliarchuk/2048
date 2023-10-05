package project;

import java.io.*;
import java.security.PrivateKey;
import java.util.*;
import java.util.stream.Collectors;

public class Model {
    private static final int FIELD_WIDTH=4;
    private Tile[][] gameTiles;
    int score;
    int  maxTile;
    private Stack<Tile[][]> previousStates=new Stack<>();
    private Stack<Integer> previousScores=new Stack<>();
    private boolean  isSaveNeeded = true;
    public Model() {
  resetGameTiles();
    }
    private void saveState (Tile[][]array){

        previousStates.push(tilesDeepClone(array));
        previousScores.push(score);
        isSaveNeeded = false;
    }
    public void rollback(){
       if(!previousStates.isEmpty())
          gameTiles=previousStates.pop();
       if(!previousScores.isEmpty())
        score=previousScores.pop();

    }
    public void randomMove(){
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n){
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }
    private void addTile(){
        List<Tile> emptyTiles=getEmptyTiles();
        if(!emptyTiles.isEmpty()) {
            Tile newTile = emptyTiles.get((int) (Math.random() * emptyTiles.size()));
            newTile.value=Math.random() < 0.9 ? 2 : 4;
        }
    }
    private List<Tile> getEmptyTiles(){
     return Arrays.stream(gameTiles).
             flatMap(Arrays::stream).filter(Tile::isEmpty).
             collect(Collectors.toList());
    }
    public void resetGameTiles(){
        this.score=0;
        this.maxTile=0;

        gameTiles=new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i=0;i<gameTiles.length;i++){
            for (int j=0;j<gameTiles[i].length;j++){
                gameTiles[i][j]=new Tile();
            }
        }
        addTile();
        addTile();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private boolean compressTiles(Tile[] tiles){
        Tile[]arrayBeforeChanged=tiles.clone();

        int insertIndex = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].value != 0) {
                Tile temp=tiles[insertIndex];
                tiles[insertIndex] = tiles[i];
                tiles[i]=temp;
                insertIndex++;
            }
        }
return !Arrays.equals(arrayBeforeChanged,tiles);

    }
    private boolean mergeTiles(Tile[] tiles){
        boolean isChanged=false;
        for(int i=1;i<tiles.length;i++){
            if (tiles[i-1].value!=0&&tiles[i-1].value==tiles[i].value){
                int currentScore=tiles[i-1].value+=tiles[i].value;
                score+=currentScore;
                if (currentScore>maxTile)
                    maxTile=currentScore;
                tiles[i].value=0;
                isChanged=true;
            }
        }
        compressTiles(tiles);
        return isChanged;
    }
    public void left(){
        if(isSaveNeeded) {
            saveState(gameTiles);
        }
        boolean isChanged=false;
        for (int i=0;i<gameTiles.length;i++) {
         if(compressTiles(gameTiles[i])|mergeTiles(gameTiles[i])) {
                   isChanged=true;
                }
            }
        if (isChanged)
            addTile();
        isSaveNeeded=true;
        }
    public void right(){
        saveState(gameTiles);
        gameTiles=twist(gameTiles,2);
        left();
        gameTiles=twist(gameTiles,2);
    }
    public void up(){
        saveState(gameTiles);
        gameTiles=twist(gameTiles,1);
        left();
        gameTiles=twist(gameTiles,3);
    }
    public void down(){
        saveState(gameTiles);
        gameTiles=twist(gameTiles,3);
        left();
        gameTiles=twist(gameTiles,1);
    }
    private  Tile[][]twist(Tile[][]array,int n){
        if(n<=0){
            return array;
        }else{
            Tile [][] copyArray=new Tile[array.length][array.length];
            for(int i=0,i1=array.length-1;i<array.length;i++,i1--){
                for(int j=0;j<array[i].length;j++){
                    copyArray[i1][j]=array[j][i];
                }
            }
            return twist(copyArray,n-1);
        }
    }
    public boolean canMove(){
      if(!getEmptyTiles().isEmpty()){
          return true;
      }
      for (int i=0;i<gameTiles.length;i++){
          for (int j=1;j<gameTiles[i].length;j++){
              if(gameTiles[i][j-1].value==gameTiles[i][j].value){
                  return true;
              }
          }
      }
        for (int i=1;i<gameTiles.length;i++){
            for (int j=0;j<gameTiles[i].length;j++){
                if(gameTiles[i-1][j].value==gameTiles[i][j].value){
                    return true;
                }
            }
        }
        return false;
    }
    private Tile[][]tilesDeepClone(Tile[][]array){
        Tile[][] deepClone=new Tile[array.length][array.length];
    for (int i=0;i<array.length;i++){
        for (int j=0;j<array[i].length;j++){
            deepClone[i][j]=new Tile(array[i][j].value);
        }
    }
        return deepClone;
    }
   private boolean hasBoardChanged(){
       for (int i = 0; i < FIELD_WIDTH; i++) {
           for (int j = 0; j < FIELD_WIDTH; j++) {
               if (gameTiles[i][j].value != previousStates.peek()[i][j].value) {
                   return true;
               }
           }
       }
       return false;
   }
    private MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;
    }
    public void autoMove(){
        PriorityQueue<MoveEfficiency>priorityQueue=new PriorityQueue<>(4,Collections.reverseOrder());
        priorityQueue.add(getMoveEfficiency(this::left));
        priorityQueue.add(getMoveEfficiency(this::right));
        priorityQueue.add(getMoveEfficiency(this::up));
        priorityQueue.add(getMoveEfficiency(this::down));
        priorityQueue.poll().getMove().move();
    }
}

