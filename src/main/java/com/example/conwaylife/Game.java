package com.example.conwaylife;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Game extends Application {
    public static int Height = 1000;
    public static int Width = Height;
    public static int WidthTotal = Width*5/4;
    public static int WidthPanel = Width/4;
    public static Tableau tableau;
    public static int nb_image = 0;
    public static int raffraichissement = 16;
    public static boolean pause = true;
    private static int cellX;
    private static int cellY;
    private static int PanelcellX;
    private static int PanelcellY;
    public static int step = 0;
    public static boolean mouseMoved = false;
    public double mouseX;
    public double mouseY;
    public static boolean mouseLeft = true;
    private static Random random = new Random();
    public static ShapePanel shapePanel;
    public static VBox root = new VBox();
    public static Canvas canvas = new Canvas(WidthTotal, Height);

    public static class ShapePanel {
        private int width = 10;
        private int height = 10;
        private double cellSize;
        private boolean[][] shape  = new boolean[width][height]; // Mini-tableau représentant la forme

        public ShapePanel() {
            // Initialiser le mini-tableau avec une forme par défaut
            Arrays.stream(shape).forEach(row -> Arrays.fill(row, false));
        }
        public ShapePanel(int taille){
            width = taille;
            height = taille;
            shape  = new boolean[width][height];
            Arrays.stream(shape).forEach(row -> Arrays.fill(row, false));
            changeCellSize();
        }
        public ShapePanel(int x, int y){
            width = x;
            height = y;
            shape = new boolean[width][height];
            Arrays.stream(shape).forEach(row -> Arrays.fill(row, false));
            changeCellSize();
        }
        public void changeWidth(int delta){
            int newShapeWidth = width + delta;
            if (newShapeWidth > 0) {
                boolean[][] newShape = new boolean[newShapeWidth][height];
                for (int i = 0; i < Math.min(newShapeWidth, width); i++) {
                    System.arraycopy(shape[i], 0, newShape[i], 0, height);
                }
                shape = newShape;
                width = newShapeWidth;
                changeCellSize();
            }
        }
        public void changeHeight(int delta){
            int newShapeHeight = height + delta;
            if (newShapeHeight > 0) {
                boolean[][] newShape = new boolean[width][newShapeHeight];
                for (int i = 0; i < width; i++) {
                    System.arraycopy(shape[i], 0, newShape[i], 0, Math.min(height, newShapeHeight));
                }
                shape = newShape;
                height = newShapeHeight;
                changeCellSize();
            }
        }
        public void changeCellSize() {
            if (3 * width < height) {
                double maxHeight = 3.0 / 4.0 * Game.Height;
                double maxWidth = Game.WidthPanel / (double) width;
                cellSize = Math.min(maxHeight / height, maxWidth);
            } else {
                cellSize = (double) Game.WidthPanel / width;
            }
        }

        public void drawShapePanel(GraphicsContext gc) {
            // Dessiner le panneau de forme
            gc.setFill(Color.GREY);
            gc.fillRect((int) (Game.Width), 0, Game.WidthTotal, Game.Height);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (shape[i][j]) {
                        gc.setFill(Color.BLUE);
                    } else {
                        gc.setFill(Color.WHITE);
                    }
                    gc.fillRect((int)(Game.Width) + i * cellSize, j * cellSize, cellSize, cellSize);
                    gc.setStroke(Color.LIGHTGREY);
                    gc.setLineWidth((1.0 / 10) * Camera.zoom);
                    gc.strokeRect((int)(Game.Width) + i * cellSize, j * cellSize, cellSize, cellSize);
                }
            }
        }

        public void drawHighlight(GraphicsContext gc) {

            double startX = Game.Width + (PanelcellX / (double) width) * width * cellSize;
            double startY = (PanelcellY / (double) height) * height * cellSize;
            double endX = startX + cellSize;
            double endY = startY + cellSize;

            // Dessiner la surbrillance
            if (startX >= Width  && startY < height * cellSize){
                gc.setStroke(Color.RED);
                gc.setLineWidth(cellSize / 10);
                gc.strokeRect(startX, startY, endX - startX, endY - startY);
            }
        }

    }

    @Override
    public void start(Stage primaryStage) {
        try {

            GraphicsContext gc = canvas.getGraphicsContext2D();
            root.getChildren().add(canvas);

            tableau = new Tableau(100, 100, 0);
            shapePanel = new ShapePanel(10);


            // Calcul de la taille totale du tableau
            double tableauWidth = tableau.getTailleX() * tableau.getSize();
            double tableauHeight = tableau.getTailleY() * tableau.getSize();
            // Calcul du facteur de zoom nécessaire pour que le tableau s'adapte entièrement à la fenêtre
            double zoomX = WidthTotal / tableauWidth;
            double zoomY = Height / tableauHeight;
            double zoom = Math.min(zoomX, zoomY);
            Camera.zoom = zoom;
            Camera.zoomMax = zoom;

            new AnimationTimer() {
                long lastTick = 0;

                public void handle(long now) {
                    if (lastTick == 0) {
                        lastTick = now;
                        tick(gc);
                        return;
                    }

                    if (now - lastTick > 1_000_000) {
                        lastTick = now;
                        tick(gc);
                        nb_image++;
                    }
                }
            }.start();

            Scene scene = new Scene(root, WidthTotal, Height);

            // Événements pour déplacer la caméra

            Set<KeyCode> keysPressed = new HashSet<>();

            // Gérer l'événement de pression d'une touche
            scene.setOnKeyPressed(event -> {
                // Ajouter la touche enfoncée à l'ensemble
                keysPressed.add(event.getCode());

                // Vérifier quelles touches sont enfoncées et effectuer les actions appropriées
                if (keysPressed.contains(KeyCode.Z)) {
                    Camera.y -= 10 / Camera.zoom;
                }
                if (keysPressed.contains(KeyCode.S)) {
                    Camera.y += 10 / Camera.zoom;
                }
                if (keysPressed.contains(KeyCode.D)) {
                    Camera.x += 10 / Camera.zoom;
                }
                if (keysPressed.contains(KeyCode.Q)) {
                    Camera.x -= 10 / Camera.zoom;
                }
                if (keysPressed.contains(KeyCode.A)) {
                    Camera.zoom *= 1.1;
                }
                if (keysPressed.contains(KeyCode.E)) {
                    if (Camera.zoom > Camera.zoomMax) {
                        Camera.zoom /= 1.1; // Zoom out
                    }
                }
                if (keysPressed.contains(KeyCode.CONTROL)) {
                    Camera.zoom = Camera.zoomMax;
                    Camera.x = Camera.y = 0;
                }
                if (keysPressed.contains(KeyCode.ADD)) {
                    if (raffraichissement > 1) {
                        raffraichissement = (int) (raffraichissement * 0.5);
                    }
                    System.out.println(raffraichissement);
                }
                if (keysPressed.contains(KeyCode.SUBTRACT)) {
                    if (raffraichissement < 256) {
                        raffraichissement *= 2;
                    }
                    System.out.println(raffraichissement);
                }
                if (keysPressed.contains(KeyCode.SPACE)) {
                    pause = !pause;
                }
                if (keysPressed.contains(KeyCode.UP)) {
                    shapePanel.changeHeight(1);
                }
                if (keysPressed.contains(KeyCode.DOWN) && shapePanel.height > 1) {
                    shapePanel.changeHeight(-1);
                }
                if (keysPressed.contains(KeyCode.RIGHT)) {
                    shapePanel.changeWidth(1);
                }
                if (keysPressed.contains(KeyCode.LEFT) && shapePanel.width > 1) {
                    shapePanel.changeWidth(-1);
                }
                if (keysPressed.contains(KeyCode.R)) {
                    for (int i =0; i<tableau.tailleX; i++){
                        for (int j=0; j<tableau.tailleY; j++){
                            tableau.tableau[i][j] = false;
                            tableau.tableauCouleur[i][j] = 0;
                        }
                    }
                    pause = true;
                    step = 0;
                    Camera.zoom = Camera.zoomMax;
                    Camera.x = Camera.y = 0;

                }
                if (keysPressed.contains(KeyCode.Y)) {
                    for (int i =0; i<tableau.tailleX; i++){
                        for (int j=0; j<tableau.tailleY; j++){
                            tableau.tableau[i][j] = false;
                            tableau.tableauCouleur[i][j] = 0;
                        }
                    }
                    pause = true;
                }
                if (keysPressed.contains(KeyCode.T)) {
                    for (int i =0; i<shapePanel.width; i++){
                        for (int j=0; j<shapePanel.height; j++){
                            shapePanel.shape[i][j] = false;
                        }
                    }
                    shapePanel.width = 10;
                    shapePanel.height = 10;
                }
                if (keysPressed.contains(KeyCode.W)) {
                    boolean[][] newShape = new boolean[shapePanel.height][shapePanel.width];
                    for (int i =0; i<shapePanel.width; i++){
                        for (int j=0; j<shapePanel.height; j++){
                            newShape[j][i] = shapePanel.shape[i][j];
                        }
                    }
                    int i = shapePanel.height;
                    shapePanel.height = shapePanel.width;
                    shapePanel.width = i;
                    shapePanel.shape = newShape;
                    shapePanel.changeCellSize();
                }
            });

            // Gérer l'événement de relâchement d'une touche
            scene.setOnKeyReleased(event -> {
                // Supprimer la touche relâchée de l'ensemble
                keysPressed.remove(event.getCode());
            });

            scene.setOnMouseMoved(event -> {
                // Récupérer les coordonnées de la souris
                mouseX = event.getX();
                mouseY = event.getY();

                // Calculer les coordonnées de la case en fonction du zoom de la caméra
                cellX = (int) ((mouseX / Camera.zoom + Camera.x) / tableau.getSize());
                cellY = (int) ((mouseY / Camera.zoom + Camera.y) / tableau.getSize());

                PanelcellX = (int) ((mouseX - Game.Width) / shapePanel.cellSize);
                PanelcellY = (int) (mouseY / shapePanel.cellSize);

            });

            // Déplacer la caméra en maintenant le clic gauche de la souris enfoncé et en déplaçant la souris
            double[] lastMousePosition = new double[2];
            scene.setOnMousePressed(event -> {
                mouseMoved = false;
                if (event.isPrimaryButtonDown()) {
                    lastMousePosition[0] = event.getX();
                    lastMousePosition[1] = event.getY();
                    mouseLeft = true;
                } else if (event.isSecondaryButtonDown()) {
                    mouseLeft = false;
                }
            });

            scene.setOnMouseDragged(event -> {
                if (event.isPrimaryButtonDown()) {
                    mouseMoved = true;
                    double deltaX = event.getX() - lastMousePosition[0];
                    double deltaY = event.getY() - lastMousePosition[1];
                    Camera.x -= deltaX / Camera.zoom / tableau.getSize();
                    Camera.y -= deltaY / Camera.zoom / tableau.getSize();
                    lastMousePosition[0] = event.getX();
                    lastMousePosition[1] = event.getY();
                }
            });

            scene.setOnMouseReleased(event -> {
                if (pause && !mouseMoved && mouseLeft) {
                    if (cellX >= 0 && cellX < tableau.getTailleX() && cellY >= 0 && cellY < tableau.getTailleY() && mouseX < Width) {
                        // Si vous êtes dans le tableau
                        tableau.tableau[cellX][cellY] = !tableau.tableau[cellX][cellY];
                        if (tableau.tableau[cellX][cellY])
                            tableau.tableauCouleur[cellX][cellY] = 4;
                        else
                            tableau.tableauCouleur[cellX][cellY] = 0;
                    } else if (PanelcellX >= 0 && PanelcellX < shapePanel.width && PanelcellY >= 0 && PanelcellY < shapePanel.height && mouseX >= Width) {
                        // Si vous êtes dans le shapePanel
                        shapePanel.shape[PanelcellX][PanelcellY] = !shapePanel.shape[PanelcellX][PanelcellY];
                    }
                } else if (pause && !mouseMoved && !mouseLeft){
                    if (cellX >= 0 && cellX < tableau.getTailleX() && cellY >= 0 && cellY < tableau.getTailleY() && mouseX < Width) {
                        // Copier la forme du panelShape dans le tableau à partir de cellX, cellY
                        for (int i = 0; i < shapePanel.width; i++) {
                            for (int j = 0; j < shapePanel.height; j++) {
                                int tableauX = cellX + i;
                                int tableauY = cellY + j;
                                if (tableauX >= 0 && tableauX < tableau.getTailleX() && tableauY >= 0 && tableauY < tableau.getTailleY()) {
                                    tableau.tableau[tableauX][tableauY] = shapePanel.shape[i][j];
                                    if (tableau.tableau[tableauX][tableauY])
                                        tableau.tableauCouleur[tableauX][tableauY] = 4;
                                    else
                                        tableau.tableauCouleur[tableauX][tableauY] = 0;
                                }
                            }
                        }
                    }
                }
            });

            scene.setOnScroll(event -> {
                double delta = event.getDeltaY();
                if (delta > 0) {
                    Camera.zoom *= 1.1; // Zoom in
                } else if (delta < 0 && Camera.zoom > Camera.zoomMax){
                    Camera.zoom /= 1.1; // Zoom out
                }
            });

            // Afficher la scène
            primaryStage.setScene(scene);
            primaryStage.setTitle("Jeu de la vie");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tick(GraphicsContext gc) {
        tableau.draw(gc);
        shapePanel.drawShapePanel(gc);
        // Dessiner la surbrillance autour de la case
        tableau.drawHighlight(gc, cellX, cellY);
        shapePanel.drawHighlight(gc);
        if(nb_image >= raffraichissement && !pause) {
            nb_image = 0;
            ++step;
            faireEtape();
        }
        drawStep(gc);
    }

    public static void faireEtape() {
        Tableau nouveauTableau = new Tableau(tableau.getTailleX(),tableau.getTailleY());
        boolean tableauChange = false;
        // Appliquer les règles du jeu
        for (int i = 0; i < tableau.getTailleX(); i++) {
            for (int j = 0; j < tableau.getTailleY(); j++) {
                int voisins = tableau.compterVoisins(i, j);
                if (tableau.tableau[i][j]) {
                    nouveauTableau.tableau[i][j] = (voisins == 2 || voisins == 3);
                } else {
                    nouveauTableau.tableau[i][j] = (voisins == 3);

                }
                if (nouveauTableau.tableau[i][j]){
                    nouveauTableau.tableauCouleur[i][j] = 4;
                } else {
                    if (tableau.tableauCouleur[i][j] >= 1) {
                        nouveauTableau.tableauCouleur[i][j] = tableau.tableauCouleur[i][j] - 1;
                    } else {
                        nouveauTableau.tableauCouleur[i][j] = 0;
                    }
                }
                if (tableau.tableau[i][j] != nouveauTableau.tableau[i][j]) {
                    tableauChange = true;
                }
            }
        }
        pause = !tableauChange;
        tableau = nouveauTableau;
    }

    public static void drawStep(GraphicsContext gc){
        gc.setFill(Color.BLACK); // Définir la couleur du texte
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30)); // Définir la police et la taille du texte

        String texte = "Étape : "+step;
        double x = Game.WidthTotal - gc.getFont().getSize() * texte.length()/2; // Calculer la position x
        double y = Game.Height - gc.getFont().getSize(); // Position y fixe pour le texte en haut
        gc.fillText(texte, x, y); // Afficher le texte
        String texte2 = "En vie : " + tableau.nb_alive();
        x = Game.WidthTotal - gc.getFont().getSize() * texte2.length()/2; // Calculer la position x
        y = Game.Height - gc.getFont().getSize() * 2; // Position y fixe pour le texte en haut
        gc.fillText(texte2, x, y); // Afficher le texte

    }


    public static void main(String[] args) {
        launch(args);
    }
}
