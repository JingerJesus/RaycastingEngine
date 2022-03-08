package com.example.betterraycastingthethird;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.security.Key;
import java.util.EventListener;

public class Raycasting extends Application {

    private Camera camera;
    private Room testRoom;
    public Group mainGroup = new Group();
    public Line[] screen;
    private Stage stageM;
    private Scene mainScene;
    private EventHandler keyboard;
    private String[] keyDown = {"NONE", "NONE"}; //index 0 is major axis, index 1 is rotation.


    @Override
    public void start(Stage stage) throws IOException, InterruptedException {

        stageM = stage;

        mainScene = new Scene(mainGroup,900,500,CustomColors.darkGrey);
        stageM.setScene(mainScene); //cole says this is "so true"
        create();



    }

    KeyFrame animate = new KeyFrame(

            //Common FPS Values:
            //120 FPS: 0.0083 sec
            //60 FPS: 0.0167 sec
            //35 FPS: 0.029 sec
            //1 FPS: 1.0 sec
            Duration.seconds(0.029),
            actionEvent -> {
                Raycast();
                stageM.show();
            }
            );

    Timeline loop = new Timeline(animate);



    private void handleEvent(KeyEvent keyEvent) {
        if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
            switch (keyEvent.getCode()) {
                case W:
                    keyDown[0] = "UP";
                    break;
                case S:
                    keyDown[0] = "DOWN";
                    break;
                case A:
                    keyDown[1] = "LEFT";
                    break;
                case D:
                    keyDown[1] = "RIGHT";
                    break;
                default:
                    keyDown[0] = "NONE";
                    keyDown[1] = "NONE";
                    break;
            }

        } else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
            keyDown[0] = "NONE";
            keyDown[1] = "NONE";
        }
        System.out.println(keyEvent.toString());
        System.out.println(keyDown);

    }


    //run this once at the very start of each room
    private void create() {
        camera = new Camera(22,12,-1,0,0,0.66);
        testRoom = new Room(-1);
        screen = new Line[900];
        
        mainScene.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            public void handle(final KeyEvent keyEvent)
            {
                handleEvent(keyEvent);
            }

        });

        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
        for (int i = 0; i < screen.length; i ++) {
            screen[i] = new Line();
            mainGroup.getChildren().add(screen[i]);
        }

    }

    //do all the gross raycasting
        //get to know this better
    public void Raycast() {

        //making the variables outside the loop that runs way too many times.
        double cameraX, rayDirX, rayDirY; //sets the X of the camera plane as well as the direction to capture in screen
        int mapX, mapY, stepX, stepY; //map tile and step length for x and y
        double sideDistX, sideDistY, perpWallDist; //distance to the side of the current map space
        double deltaDistX, deltaDistY;
        int hit; //this helps determine whether to color this side light or dark
        int side, lineHeight = 0;
        double frameTime = 0.029;
        double oldPlaneX;
        double oldDirX;

        double moveSpeed = frameTime * 5.0; //the constant value is in squares/second
        double rotSpeed = frameTime * 3.0; //the constant value is in radians/second

        //stuff
        for (int i = 0; i < 900; i ++) { //for every vertical line in the screen (screen width)
            cameraX = 2 * i / 900.0 - 1; //x coord in camera space

            rayDirX = camera.dirX + camera.planeX * cameraX;
            rayDirY = camera.dirY + camera.planeY * cameraX;

            mapY = (int)camera.posY; //see above
            mapX = (int)camera.posX; //camera's map location (consists of 1.0 -> 1.999...)


            deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);


            hit = 0; //was there a wall hit?
            side = 0; //was it NS or EW side of wall?



            //calculate step and initial sideDist
            if (rayDirX < 0)
            {
                stepX = -1;
                sideDistX = (camera.posX - mapX) * deltaDistX;
            }
            else
            {
                stepX = 1;
                sideDistX = (mapX + 1.0 - camera.posX) * deltaDistX;
            }
            if (rayDirY < 0)
            {
                stepY = -1;
                sideDistY = (camera.posY - mapY) * deltaDistY;
            }
            else
            {
                stepY = 1;
                sideDistY = (mapY + 1.0 - camera.posY) * deltaDistY;
            }

            //perform DDA
            while (hit == 0)
            {
                //jump to next map square, either in x-direction, or in y-direction
                if (sideDistX < sideDistY)
                {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                }
                else
                {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                //Check if ray has hit a wall
                if (testRoom.map[mapX][mapY] > 0) hit = 1;
            }

            //Calculate distance projected on camera direction
            if(side == 0) perpWallDist = (sideDistX - deltaDistX);
            else          perpWallDist = (sideDistY - deltaDistY);

            //Calculate height of line to draw on screen
            lineHeight = (int)(400 / perpWallDist);

            //calculate lowest and highest pixel to fill in current stripe - screen height is 425
            int drawStart = -lineHeight / 2 + 425 / 2;
            if(drawStart < 0)drawStart = 0;
            int drawEnd = lineHeight / 2 + 425 / 2;
            if(drawEnd >= 425)drawEnd = 425 - 1;


            drawRay(i, drawStart, drawEnd, side);



        }
        //player listener


        mainScene.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            public void handle(final KeyEvent keyEvent)
            {
                handleEvent(keyEvent);
            }

        });
        mainScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                handleEvent(keyEvent);
            }
        });

        //move forward if no wall in front of you
        if (keyDown[0].equals("UP"))
        {
            if(testRoom.map[(int)(camera.posX + camera.dirX * moveSpeed)][(int)(camera.posY)] == 0) camera.posX += camera.dirX * moveSpeed;
            if(testRoom.map[(int)(camera.posX)][(int)(camera.posY + camera.dirY * moveSpeed)] == 0) camera.posY += camera.dirY * moveSpeed;
        }
        //move backwards if no wall behind you
        if (keyDown[0].equals("DOWN"))
        {
            if(testRoom.map[(int)(camera.posX - camera.dirX * moveSpeed)][(int)(camera.posY)] == 0) camera.posX -= camera.dirX * moveSpeed;
            if(testRoom.map[(int)(camera.posX)][(int)(camera.posY - camera.dirY * moveSpeed)] == 0) camera.posY -= camera.dirY * moveSpeed;
        }
        //rotate to the right
        if (keyDown[1].equals("RIGHT"))
        {
            //both camera direction and camera plane must be rotated
            oldDirX = camera.dirX;
            camera.dirX = camera.dirX * Math.cos(-rotSpeed) - camera.dirY * Math.sin(-rotSpeed);
            camera.dirY = oldDirX * Math.sin(-rotSpeed) + camera.dirY * Math.cos(-rotSpeed);
            oldPlaneX = camera.planeX;
            camera.planeX = camera.planeX * Math.cos(-rotSpeed) - camera.planeY * Math.sin(-rotSpeed);
            camera.planeY = oldPlaneX * Math.sin(-rotSpeed) + camera.planeY * Math.cos(-rotSpeed);
        }
        //rotate to the left
        if (keyDown[1].equals("LEFT"))
        {
            //both camera direction and camera plane must be rotated
            oldDirX = camera.dirX;
            camera.dirX = camera.dirX * Math.cos(rotSpeed) - camera.dirY * Math.sin(rotSpeed);
            camera.dirY = oldDirX * Math.sin(rotSpeed) + camera.dirY * Math.cos(rotSpeed);
            oldPlaneX = camera.planeX;
            camera.planeX = camera.planeX * Math.cos(rotSpeed) - camera.planeY * Math.sin(rotSpeed);
            camera.planeY = oldPlaneX * Math.sin(rotSpeed) + camera.planeY * Math.cos(rotSpeed);
        }


    }

    public void drawRay(int x, int drawStart, int drawEnd, int side) {

        Color lightSide = CustomColors.lightGrey; //light and dark sides for the ray
        Color darkSide = CustomColors.midGrey;

        //used for debugging
        Color rLightSide = Color.rgb((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255));

        screen[x].setStartX(x); screen[x].setStartY(drawStart);
        screen[x].setEndX(x); screen[x].setEndY(drawEnd);

        if (side == 1) {
            screen[x].setStroke(darkSide);
        } else {
            screen[x].setStroke(lightSide);
        }

    }

    public static void main(String[] args) {
        launch();
    }

}