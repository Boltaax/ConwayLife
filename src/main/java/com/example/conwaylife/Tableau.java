package com.example.conwaylife;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Random;

public class Tableau {
    public int tailleX;
    public int tailleY;
    private int size = 1;
    public boolean [][]tableau;
    public int [][]tableauCouleur;
    private Random random = new Random();

    public Tableau (int x, int y, double chance){
        tailleX = x;
        tailleY = y;
        tableau = new boolean[tailleX][tailleY];
        tableauCouleur = new int[tailleX][tailleY];
        Arrays.stream(tableauCouleur).forEach(row -> Arrays.fill(row, 0));
        for (int i = 0; i < tailleX; i++){
            for (int j = 0; j < tailleY; j++){
                tableau[i][j] = (random.nextInt(100) < chance);
                if (tableau[i][j])
                    tableauCouleur[i][j] = 100;
            }
        }
    }
    public Tableau (int x, int y){
        tailleX = x;
        tailleY = y;
        tableau = new boolean[tailleX][tailleY];
        tableauCouleur = new int[tailleX][tailleY];
        Arrays.stream(tableauCouleur).forEach(row -> Arrays.fill(row, 0));
        for (int i = 0; i < tailleX; i++){
            for (int j = 0; j < tailleY; j++){
                tableau[i][j] = false;
            }
        }

    }

    public int getSize() {
        return size;
    }

    public int getTailleX(){
        return tailleX;
    }
    public int getTailleY(){
        return tailleY;
    }

    public int compterVoisins(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int voisinX = x + i;
                int voisinY = y + j;
                // Ignorer les bords du tableau
                if (voisinX >= 0 && voisinX < tailleX && voisinY >= 0 && voisinY < tailleY) {
                    if (i != 0 || j != 0) {
                        if (tableau[voisinX][voisinY]) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }



    public void draw(GraphicsContext gc) {
        gc.setFill(Color.LIGHTGREY);
        gc.fillRect(0, 0, Game.Width, Game.Height);

        // Parcourir toutes les cellules du tableau
        for (int i = 0; i < tailleX; i++) {
            for (int j = 0; j < tailleY; j++) {
                // Calculer les coordonnées réelles de la cellule en prenant en compte la position de la caméra
                double cellX = (i - Camera.x) * Camera.zoom * size;
                double cellY = (j - Camera.y) * Camera.zoom * size;

                // Décaler le début et la fin de la case
                double startX = cellX + (1.0 / 20) * size * Camera.zoom;
                double startY = cellY + (1.0 / 20) * size * Camera.zoom;
                double endX = startX + size * Camera.zoom - (1.0 / 10) * size * Camera.zoom;
                double endY = startY + size * Camera.zoom - (1.0 / 10) * size * Camera.zoom;


                // Dessiner les bords
                gc.setStroke(Color.LIGHTGREY);
                gc.setLineWidth((1.0 / 10) * size * Camera.zoom);
                gc.strokeRect(startX, startY, endX - startX, endY - startY);

                // Dessiner la cellule seulement si elle est visible à l'écran
                if (cellX + size * Camera.zoom >= 0 && cellX <= Game.Width && cellY + size * Camera.zoom >= 0 && cellY <= Game.Height) {
                    switch (tableauCouleur[i][j]) {
                        case 4 -> gc.setFill(Color.rgb(0, 0, 255));
                        case 3 -> gc.setFill(Color.rgb(80, 80, 255));
                        case 2 -> gc.setFill(Color.rgb(160, 160, 255));
                        case 1 -> gc.setFill(Color.rgb(240, 240, 255));
                        default -> gc.setFill(Color.WHITE);
                    }
                    gc.fillRect(startX, startY, endX - startX, endY - startY);

                }
            }
        }
    }

    public int nb_alive(){
        int somme = 0;
        for (int i = 0; i < tailleX; i++){
            for (int j = 0; j < tailleY; j++){
                if (tableau[i][j])
                    somme++;
            }
        }
        return somme;
    }

    public void drawHighlight(GraphicsContext gc, int cellX, int cellY) {
        // Calculer les coordonnées réelles de la case en prenant en compte la position de la caméra et le zoom
        double startX = (cellX - Camera.x) * Camera.zoom * size;
        double startY = (cellY - Camera.y) * Camera.zoom * size;
        double endX = startX + size * Camera.zoom;
        double endY = startY + size * Camera.zoom;

        // Dessiner la surbrillance
        if (startX < (int) (Game.Width)){
            gc.setStroke(Color.RED);
            gc.setLineWidth((1.0 / 10) * size * Camera.zoom);
            gc.strokeRect(startX, startY, endX - startX, endY - startY);
        }

    }




}
