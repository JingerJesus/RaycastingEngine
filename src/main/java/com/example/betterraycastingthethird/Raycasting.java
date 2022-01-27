package com.example.betterraycastingthethird;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;

public class Raycasting extends Application {

    private Camera camera;
    private Room testRoom;
    public Group mainGroup = new Group();

    @Override
    public void start(Stage stage) throws IOException {

        create();
        Raycast();

        //put things in the group here
        Scene mainScene = new Scene(mainGroup,900,500,CustomColors.darkGrey);
        stage.setScene(mainScene); //cole says this is "so true"
        stage.show();
    }

    //run this once at the very start of each room
    private void create() {
        camera = new Camera(22,12,-1,0,0,0.66);
        testRoom = new Room(-1);
    }

    //game loop method
    private void Raycast() {

        //stuff
        for (int i = 0; i < 900; i ++) {
            double cameraX = 2 * i / 900.0 - 1; //x coord in camera space

            int mapX = (int)camera.posX;
            int mapY = (int)camera.posY;

            double rayDirX = camera.dirX + camera.planeX * cameraX;
            double rayDirY = camera.dirY + camera.planeY * cameraX;

            double sideDistX;
            double sideDistY;

            double perpWallDist;

            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);

            int stepX;
            int stepY;

            int hit = 0; //was there a wall hit?
            int side = 0; //was it NS or EW side of wall?

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
            int lineHeight = (int)(400 / perpWallDist);

            //calculate lowest and highest pixel to fill in current stripe - screen height is 425
            int drawStart = -lineHeight / 2 + 425 / 2;
            if(drawStart < 0)drawStart = 0;
            int drawEnd = lineHeight / 2 + 425 / 2;
            if(drawEnd >= 425)drawEnd = 425 - 1;

            mainGroup.getChildren().add(drawRay(i, drawStart, drawEnd, side));

        }
    }

    public Line drawRay(int x, int drawStart, int drawEnd, int side) {

        Color lightSide = CustomColors.lightGrey; //light and dark sides for the ray
        Color darkSide = CustomColors.midGrey;

        Line viewLine = new Line(x,drawStart,x,drawEnd);
        if (side == 1) {
            viewLine.setStroke(darkSide);
        } else {
            viewLine.setStroke(lightSide);
        }

        return viewLine;

    }

    public static void main(String[] args) {
        launch();
    }
}