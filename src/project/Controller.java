package project;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controller extends KeyAdapter {
    private Model model;
    private View view;
    private static final int WINNING_TILE = 2048;

    public Controller(Model model) {
        this.model = model;
        this.view = new View(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            resetGame();
        if (!model.canMove()) {
            view.isGameLost = true;
        }
        if (!view.isGameLost && !view.isGameWon) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                model.left();
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                model.right();
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                model.up();
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                model.down();
            }
            if (e.getKeyCode() == KeyEvent.VK_Z) {
                model.rollback();
            }
            if (e.getKeyCode() == KeyEvent.VK_R) {
                model.randomMove();
            }
            if (e.getKeyCode() == KeyEvent.VK_A) {
                model.autoMove();
            }


            if (model.maxTile==WINNING_TILE){
                view.isGameWon=true;
            }
        }
        view.repaint();
    }

    public void resetGame() {
        model.score=0;
        view.isGameLost = false;
        view.isGameWon = false;
        model.resetGameTiles();
    }

    public View getView() {
        return view;
    }

    Tile[][] getGameTiles() {
        return model.getGameTiles();
    }

    int getScore() {
        return model.score;
    }
}
